package spotthefraud;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.Logger;
import twitter4j.FilterQuery;
import twitter4j.JSONArray;
import twitter4j.JSONException;
import twitter4j.JSONObject;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.json.DataObjectFactory;


/**
 *TODO debugging and implement the crawler of tweets for 40 users
 * @authors Nikos Zissis, Sokratis Papadopoulos, George Mihailidis, Anastasios Kostas
 */
public class TweetsAnalyzer {
    private ArrayList<FollowedUser> statistics; //contains (userID, totalTweets) for all our database
    //The 4 differnt groups that our users will be classified in.
    private ArrayList<FollowedUser> group1;
    private ArrayList<FollowedUser> group2;
    private ArrayList<FollowedUser> group3;
    private ArrayList<FollowedUser> group4;
    private ArrayList<FollowedUser> usersCollection; //final collection of users that will be followed
    
    private HashMap<String, Integer> trends; // to set a number for each trend
    private HashMap<String,Integer> uniqueIds;
    
    private MongoClient client;
    private DB TrendsDb, TweetDb,FollowedUsers;
    private DBCollection topicsColl,tweetColl,followedColl;

    private TwitterStream stream;
    private StatusListener listener;
    private FilterQuery fq;
    private ConfigurationBuilder cb;
    
    String key;
    /**
     * Constructor.
     * Initializes Mongo, calculates the frequency of tweets for each user
     * and then classifies them into 4 groups. Finally we select randomly 10
     * users from each group and we store them in usersCollection.
     * @throws JSONException 
     */
    public TweetsAnalyzer() throws JSONException  {
        statistics = new ArrayList<>();
        group1 = new ArrayList<>();
        group2 = new ArrayList<>();
        group3 = new ArrayList<>();
        group4 = new ArrayList<>();
        trends = new HashMap<>();
        uniqueIds=new HashMap<>();
        usersCollection=new ArrayList<>();
        key = null;
        initializeBasicVariables();
        calculateFrequency();
        //classificationOfUsers();  
        //startTrackingUsersTweets();
    }
    
    /**
    * We initialize the attributes of MongoDb.First we create a MongoClient object. Then we take topTopics Database and we create two
    *collections for Top Trending Topics and for Tweets from Top Trending Topics
    */
    private void initializeBasicVariables(){
        System.out.println("Initializing basic values...");
        // The configuration details of our application as developer mode of Twitter API
        cb=new ConfigurationBuilder();
        cb.setOAuthConsumerKey("0cc8fkRgUfzX5fYK14m211vhE");
        cb.setOAuthConsumerSecret("45d3sLIiEG0suWxEGBECTWP0tXJL6hJQwqqNCvo04eeGKjL8Al");
        cb.setOAuthAccessToken("43403340-aUeWfSgfYpYSDmoeVzaPXF1aaiBAo3IL7zgIXwahU");
        cb.setOAuthAccessTokenSecret("Tc40irSU8G15IvvEu6EuVjsaM1xQAVCDzJoaSTnxYVFOI");
        cb.setJSONStoreEnabled(true); //We use this as we pull json files from Twitter Streaming API
        
        try {
            client=new MongoClient("localhost",27017);
            
            //Gets the trends collection from mongodb
            TrendsDb = client.getDB("TopTopics");
            topicsColl = TrendsDb.createCollection("topicsColl", null);
            
            //Gets the tweets collection from mongodb
            TweetDb=client.getDB("Tweets");
            tweetColl=TweetDb.createCollection("tweetsColl", null);

            //gets the followed users collection from mongodb
            FollowedUsers=client.getDB("Followed");
            followedColl=FollowedUsers.createCollection("followedColl", null);
             
        }catch (UnknownHostException ex) {
            Logger.getLogger(TweetsControl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void setTrends(){
        //System.out.println("*****Seting Trends HashMap*****");
        DBCursor cursor = topicsColl.find(); //get a cursor that will run throughout the collection of trends.
        int pos = 0;
        
        while (cursor.hasNext()){
            DBObject obj=cursor.next();
            String name = obj.get("Name").toString(); //gets the name of trend.
            if(trends.containsKey(name)){
            }else{
                trends.put(name, pos);
                //System.out.println("Trend: " + name + "is number: " + pos);
                pos++;
            }
        }
    }
    
    /**
     * Identifies if the text given agrees with a trend that we have in database.
     * If so it returns the number of trend.
     * If not, it returns -1
     * @param text
     * @return number of trend if found, -1 else
     */
    private int identifyTrend(String text){
        //TODO
        Iterator it = trends.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry trend = (Map.Entry)it.next();
            //System.out.println(trend.getKey() + " = " + trend.getValue());
            
            String trendName = (String) trend.getKey();
            int trendNumber = (int) trend.getValue();
            if (text.contains(trendName)){ //if the text contains the trend in it
                key=trendName;
                return trendNumber;
            }
        }
        return -1;
    }
    
    //use index createIndex()
    //hashset gia trends pou emfanizete o kathe xristis !!!!!!
    
    /**
     * Calculates the frequency of tweets appeared on our dataset for each user
     * @throws JSONException 
     */
    private void calculateFrequency() throws JSONException {
        System.out.println("start calculating frequency...");
        setTrends();
        double counter=0; //how many tweets we want to examine
        DBCursor cursor = tweetColl.find(); //get a cursor that will run throughout the collection.
        int pos =0;
        int finish=0;
	while (cursor.hasNext() && counter <10000000) { //for each tweet in the collection
            //System.out.println("---------------------------------------------------");
            //we have to calculate the number of tweets at each trending topic...
            
            //CHEEEEEEEEEEEEEEEEEEEEEEEEECK!!
            DBObject obj=cursor.next();
            
            String text = obj.get("text").toString(); //gets the text of the tweet. It can be used in order to specify what trend tweet is refering to.
            
            // TODO identify what trend it is refering to
            int number = identifyTrend(text);
            
            //Gets the id of user
            JSONObject jobj=new JSONObject(obj.toString());
            String userID= jobj.getJSONObject("user").getString("id_str");
            System.out.println("User ID: "+ userID + "\n");
            System.out.println("tweet = "+text);
            System.out.println("Key is "+key);
            System.out.println("Trend No is: "+ trends.get(key));
            
            System.out.println("---------");
                        
            if(uniqueIds.containsKey(userID)){
                statistics.get(uniqueIds.get(userID)).increaseNumberOfTweets();
                //System.out.println(statistics.get(uniqueIds.get(userID)).getTotalNumberOfTweets());
            }else{
                uniqueIds.put(userID, pos);
                pos++;
                statistics.add(new FollowedUser(userID,1));
                //System.out.println("+++ New user added! " + userID);
            }
            //System.out.println("---------------------------------------------------");
            statistics.get(uniqueIds.get(userID)).addTrend(number);

            counter++;
            
            finish++;
            if(finish==10){
                break;
            }
	}
    }
    
    /**
     * It classifies all users into 4 Categories, depending on the number of tweets they did
     */
    @SuppressWarnings("empty-statement")
    private void classificationOfUsers(){
        System.out.println("-------Starting classification of users-------");
        
        int[] numbers; //stores all tweets frequencies from the users.
        int[] firstHalf; //the first half of the numbers array
        int[] secondHalf; //the second half of the numbers array
        System.out.println("Statistics array size: " + statistics.size());
        int counter, q1, q2, q3, size = statistics.size(), halfsize;
        
        numbers = new int[size];

        System.out.println("<><><> Spliting sizes in half <><><>");
        if(size % 2 == 0){//----------odd number
            halfsize = size/2;
        }else{//--------------------------------even number
            halfsize = size/2 +1;
        }
        System.out.println("<><><> halfsize: "+ halfsize + " <><><>");

        // pass all the users' tweets frequencies to the numbers array
        counter=0;
        for(FollowedUser temp: statistics){
            numbers[counter++] = temp.getTotalNumberOfTweets();          
        }
        System.out.println("|||| frequencies table size: " + numbers.length + " ||||");
        System.out.println("|||| Sorting... ||||");
        Arrays.sort(numbers); // sorts the array of Numbers that contains the frequencies
        System.out.println("|||| Sorting finished! ||||");
        
        q2 = findMedian(numbers);
        System.out.println("**********Middle median (q2): " + q2);

        firstHalf = Arrays.copyOfRange(numbers, 0, halfsize);
        secondHalf = Arrays.copyOfRange(numbers, halfsize, size);
        
        q1 = findMedian(firstHalf);
        q3 = findMedian(secondHalf);
        System.out.println("**********First median (q1): " + q1);
        System.out.println("**********Third median (q3): " + q3);
        
        if (q1 == q2 || q2==q3){ // split in 2 groups because the split in 4 groups failed
            //sort statistics by number of tweets
            Collections.sort(statistics, new Comparator<FollowedUser>() {
                @Override
                public int compare(FollowedUser one, FollowedUser other) {
                    if (one.getTotalNumberOfTweets() < (other.getTotalNumberOfTweets())){
                        return -1;
                    } else if (one.getTotalNumberOfTweets() > other.getTotalNumberOfTweets()){
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });
                
            //printing out all tweets in database (to check their frequencies)
            for(int i=0; i<statistics.size(); i++){
                System.out.println(i + ". " + statistics.get(i).getUserID() + " " + statistics.get(i).getTotalNumberOfTweets());
            }
                
            int critical = 0; //will help split the database in 2 parts
            FollowedUser user = new FollowedUser();
            
            for(int i=0; i<statistics.size(); i++){
                if(i>= statistics.size()){ break; }
                user = statistics.get(i); //gets the next in line user
                group1.add(user); //adds current user to group1
                if (statistics.get(i+1).getTotalNumberOfTweets() != user.getTotalNumberOfTweets()){
                    System.out.println("Different freqs! " + statistics.get(i+1).getTotalNumberOfTweets() + " " + user.getTotalNumberOfTweets());
                    critical=i+1;
                    i=statistics.size()+10;
                }
            }
            
            int critical2 = 0; //will help split the database in 3 parts

            for(int i=critical; i<statistics.size(); i++){
                if(i>= statistics.size()){ break; }
                user = statistics.get(i); //gets the next in line user
                group2.add(user);
                if(statistics.get(i+1).getTotalNumberOfTweets() != user.getTotalNumberOfTweets()){
                    System.out.println("Different freqs! " + statistics.get(i+1).getTotalNumberOfTweets() + " " + user.getTotalNumberOfTweets());
                    critical2=i+1;
                    i= statistics.size()+10;
                }
            }
            
            int critical3 = 0;//will help split the database in 4 parts

            for(int i=critical2; i<statistics.size(); i++){
                if(i>= statistics.size()){ break; }
                user = statistics.get(i); //gets the next in line user
                group3.add(user);
                if(statistics.get(i+1).getTotalNumberOfTweets() != user.getTotalNumberOfTweets()){
                    System.out.println("Different freqs! " + statistics.get(i+1).getTotalNumberOfTweets() + " " + user.getTotalNumberOfTweets());
                    critical3=i+1;
                    i= statistics.size()+10;
                }
            }
            
            //add rest of items in 4th group
            for(int i=critical3; i<statistics.size(); i++){
                if(i>= statistics.size()){ break; }
                user = statistics.get(i);
                group4.add(user);
            }
            
            System.out.println("&^&^&^&^= Random Selection from groups =&^&^&^&^&^");
            //starting the random selection from the created groups
            
            if (!group4.isEmpty()){ //tweets are split in 4 groups. Take random tweets from 4 groups.
                System.out.println("&^&^= Group1 size: " + group1.size() + " =&^&^");
                getMaxTenRandomUsersFromGroup(group1); //brings 10 from group 1 to final collection: usersCollection
                System.out.println("&^&^= Group2 size: " + group2.size() + " =&^&^");
                getMaxTenRandomUsersFromGroup(group2); //brings 10 from group 2 to final collection: usersCollection
                System.out.println("&^&^= Group3 size: " + group3.size() + " =&^&^");
                getMaxTenRandomUsersFromGroup(group3); //brings 10 from group 3 to final collection: usersCollection
                System.out.println("&^&^= Group4 size: " + group4.size() + " =&^&^");
                getMaxTenRandomUsersFromGroup(group4); //brings 10 from group 4 to final collection: usersCollection
                
            } else if(!group3.isEmpty()){ //tweets are split in 3 groups. Take random tweets from 3 groups.
                System.out.println("&^&^= Group1 size: " + group1.size() + " =&^&^");
                getMaxTenRandomUsersFromGroup(group1); //brings 10 from group 1 to final collection: usersCollection
                System.out.println("&^&^= Group2 size: " + group2.size() + " =&^&^");
                getMaxTenRandomUsersFromGroup(group2); //brings 10 from group 2 to final collection: usersCollection
                System.out.println("&^&^= Group3 size: " + group3.size() + " =&^&^");
                getMaxTenRandomUsersFromGroup(group3); //brings 10 from group 3 to final collection: usersCollection
                
            } else if (!group2.isEmpty()){ //tweets are split in 2 groups. Take random tweets from 2 groups.
                System.out.println("&^&^= Group1 size: " + group1.size() + " =&^&^");
                getMaxTenRandomUsersFromGroup(group1); //brings 10 from group 1 to final collection: usersCollection
                getMaxTenRandomUsersFromGroup(group1); //brings 10 from group 1 to final collection: usersCollection

                System.out.println("&^&^= Group2 size: " + group2.size() + " =&^&^");
                getMaxTenRandomUsersFromGroup(group2);  //brings 10 from group 2 to final collection: usersCollection
                getMaxTenRandomUsersFromGroup(group2);  //brings 10 from group 2 to final collection: usersCollection
                
            } else { //all tweets are in group 1, so take random from the whole database
                System.out.println("&^&^= Group1 size: " + group1.size() + " =&^&^");
                getMaxTenRandomUsersFromGroup(group1); //brings 10 from group 1 to final collection: usersCollection
                getMaxTenRandomUsersFromGroup(group1); //brings 10 from group 1 to final collection: usersCollection
                getMaxTenRandomUsersFromGroup(group1); //brings 10 from group 1 to final collection: usersCollection
                getMaxTenRandomUsersFromGroup(group1); //brings 10 from group 1 to final collection: usersCollection
            }
        } else { //the database is for sure split in 4 groups with different medians
            if(usersCollection.isEmpty()){
                for(FollowedUser temp: statistics){
                    if (temp.getTotalNumberOfTweets() <q1){
                        group1.add(temp);
                        System.out.println("!!!!!!!" + temp.getUserID() + "added to GROUP 1");
                    }else if (temp.getTotalNumberOfTweets() < q2){
                        group2.add(temp);
                        System.out.println("!!!!!!!" + temp.getUserID() + "added to GROUP 2");
                    }else if (temp.getTotalNumberOfTweets() < q3){
                        group3.add(temp);
                        System.out.println("!!!!!!!" + temp.getUserID() + "added to GROUP 3");
                    }else{
                        group4.add(temp);
                        System.out.println("!!!!!!!" + temp.getUserID() + "added to GROUP 4");
                    }
                }
                //random selection from each frequency-group
                System.out.println("&^&^&^&^= Random Selection from groups =&^&^&^&^&^");

                System.out.println("&^&^= Group1 size: " + group1.size() + " =&^&^");
                getMaxTenRandomUsersFromGroup(group1);

                System.out.println("&^&^= Group2 size: " + group2.size() + " =&^&^");
                getMaxTenRandomUsersFromGroup(group2);

                System.out.println("&^&^= Group3 size: " + group3.size() + " =&^&^");
                getMaxTenRandomUsersFromGroup(group3);

                System.out.println("&^&^= Group4 size: " + group4.size() + " =&^&^");
                getMaxTenRandomUsersFromGroup(group4);
            }  
        }
        
        //if there are not 40 users completed in the final dataset, complete them with random ones from whole dataset
        while(40 - usersCollection.size() > 0){ 
            System.out.println("Get one random user from whole database");
            getOneRandomUserFromGroup(statistics);   
        }
        
        System.out.println("$$$$$= Starting to print the final users! =$$$$$$$$");
        for(int i=0; i<usersCollection.size(); i++){
            System.out.println(i + ". " + usersCollection.get(i).getUserID() + " " + usersCollection.get(i).getTotalNumberOfTweets());
        }
    }
    
    /**
     * gets maximum of 10 random users from a group
     * @param group 
     */
    private void getMaxTenRandomUsersFromGroup(ArrayList<FollowedUser> group){
        Random random = new Random();
        int counter=0; //will count the loop number up to 10
        while(counter <10 && !group.isEmpty()) {

            System.out.println("(Get random from group) Loop: "+ counter);
            int randomPosition=Math.abs(random.nextInt()%group.size()) ;
            FollowedUser user=group.remove(randomPosition); //returns the user and deletes him from group

            usersCollection.add(user); //adds user to the final collection
            counter++;
        }
    }
    
    /**
     * gets only one random user from group
     * @param group 
     */
    private void getOneRandomUserFromGroup(ArrayList<FollowedUser> group){
        Random random = new Random();
        
        if(!group.isEmpty()) {
            System.out.println("Get one random from database");
            int randomPosition=random.nextInt()%group.size(); 
            FollowedUser user=group.remove(Math.abs(randomPosition)); //returns the user and deletes him from group
            
            boolean flag=false; //used to check if user exists already in final collection
            do{ //loop until it finds one that doesnt exist in final collection
                for(FollowedUser fuser:usersCollection){
                    if(fuser.getUserID().equals(user.getUserID())){
                        flag=true;
                    }
                }
                if(!flag){
                    usersCollection.add(user);
                 }
            } while (flag==true);
        } else {
            System.out.println("Group is empty");
        }
    }
    
    private int findMedian(int[] numbers){
        System.out.println("()()() FindMedian ()()()");
        System.out.println("group length: "+ numbers.length);
        if(numbers.length % 2 == 0){//odd number
            return (numbers[numbers.length/2] + numbers[numbers.length/2 +1])/2;
        }else{ //even number
            return numbers[ numbers.length/2 +1];
        }
    }
    
    /**
     * Follows the users that exists in userCollection (40 people)
     * It runs for 7 days
     */
    public void startTrackingUsersTweets(){
        
        listener=new StatusListener() {

            @Override
            public void onStatus(Status status) {
                 User user=status.getUser();
                 Long id=user.getId();
                 boolean flag=false;
                 for(FollowedUser fuser:usersCollection){
                     if(id==Long.parseLong(fuser.getUserID())){
                         System.out.println("Coble");
                         flag=true;
                         break;
                         }
                 }
                 if(flag){
                    String json=DataObjectFactory.getRawJSON(status);
                    System.out.println("json");
                    DBObject jsonObj=(DBObject) JSON.parse(json);
                    followedColl.insert(jsonObj);
                 }
                 
                 //TODO if seven days passed stop the process
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice sdn) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void onTrackLimitationNotice(int i) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void onScrubGeo(long l, long l1) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void onStallWarning(StallWarning sw) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void onException(Exception excptn) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
        
        fq=new FilterQuery();
        long userIDs[]=new long[usersCollection.size()];
        for(int i=0; i<usersCollection.size(); i++){
            userIDs[i]=Long.parseLong(usersCollection.get(i).getUserID());
        }
        fq.follow(userIDs);
        
        stream=new TwitterStreamFactory(cb.build()).getInstance();
        stream.addListener(listener);
        stream.filter(fq);
    }
}


//differnt way to use statistics
/*
for (Map.Entry entry : statistics.entrySet()) {
            numbers[counter++] = (int) entry.getValue();
        }
*/

/*
if(statistics.containsKey(id)){
                statistics.put(id, statistics.get(id)+1);
            }else{
                statistics.put(id, 1);
            }
*/

/*
for (Map.Entry entry : statistics.entrySet()) {
            if ((int) entry.getValue() < q1){
                group1.put((Integer) entry.getKey(), (Integer) entry.getValue());
            }else if ((int) entry.getValue() < q2){
                group2.put((Integer) entry.getKey(), (Integer) entry.getValue());
            }else if ((int) entry.getValue() < q3){
                group3.put((Integer) entry.getKey(), (Integer) entry.getValue());
            }else{
                group4.put((Integer) entry.getKey(), (Integer) entry.getValue());
            }
        }
*/

/*
Random random= new Random();
        Object[] values = group.values().toArray();
        Object randomValue;
for(int i=0; i<10;i++) {
            randomValue = values[random.nextInt(values.length)];
            //put in userCollections the random values!!!!!!
        }
*/