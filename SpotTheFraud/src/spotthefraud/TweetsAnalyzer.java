package spotthefraud;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import twitter4j.JSONArray;
import twitter4j.JSONException;
import twitter4j.JSONObject;


/**
 *
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
    private MongoClient client;
    private DB TweetDb;
    private DBCollection tweetColl;

    /**
     * Constructor.
     * Initializes Mongo, calculates the frequency of tweets for each user
     * and then classifies them into 4 groups. Finally we select randomly 10
     * users from each group and we store them in usersCollection.
     * @throws JSONException 
     */
    public TweetsAnalyzer() throws JSONException {
        statistics = new ArrayList<>();
        group1 = new ArrayList<>();
        group2 = new ArrayList<>();
        group3 = new ArrayList<>();
        group4 = new ArrayList<>();
        initializeMongo();
        calculateFrequency();
        classificationOfUsers();        
    }
    
    /**
    * We initialize the attributes of MongoDb.First we create a MongoClient object. Then we take topTopics Database and we create two
    *collections for Top Trending Topics and for Tweets from Top Trending Topics
    */
    private void initializeMongo(){
        
          try {
             client=new MongoClient("localhost",27017);
             TweetDb=client.getDB("Tweets");
             tweetColl=TweetDb.createCollection("tweetsColl", null);
             
            }catch (UnknownHostException ex) {
             Logger.getLogger(TweetsControl.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    
    private void calculateFrequency() throws JSONException{
        
        DBCursor cursor = tweetColl.find(); //get a cursor that will run throughout the collection.
        
	while (cursor.hasNext()) { //for each tweet in the collection
            //we have to calculate the number of tweets at each trending topic...
            int id = (int) cursor.next().get("id");
                       
            boolean found = false; //used to determine if the author of the tweet exists already or not

            int pos =0;
            for(FollowedUser temp:statistics){

                if(id == temp.getUserID()){
                    statistics.get(pos).increaseNumberOfTweets();
                    found = true;
                }
                pos++;
            }

            if (found == false){
                statistics.add(new FollowedUser(id,1));
            } 
	}
    }
    
    /**
     * It classifies all users into 4 Categories, depending on the number of tweets they did
     */
    @SuppressWarnings("empty-statement")
    private void classificationOfUsers(){
        int[] numbers; //stores all tweets frequencies from the users.
        int[] firstHalf; //the first half of the numbers array
        int[] secondHalf; //the second half of the numbers array
        int counter, q1, q2, q3, size = statistics.size(), halfsize;
        
        numbers = new int[size];
        
        if(size % 2 == 0){//----------odd number
            halfsize = size/2;
        }else{//--------------------------------even number
            halfsize = size/2 +1;
        }
                
        // pass all the users' tweets frequencies to the numbers array
        counter=0;
        for(FollowedUser temp: statistics){
            numbers[counter++] = temp.getTotalNumberOfTweets();          
        }
       
        Arrays.sort(numbers); // sorts the array of Numbers that contains the frequencies
        
        q2 = findMedian(numbers);
        
        firstHalf = Arrays.copyOfRange(numbers, 0, halfsize);
        secondHalf = Arrays.copyOfRange(numbers, halfsize, size);
        
        q1 = findMedian(firstHalf);
        q3 = findMedian(secondHalf);
        
        for(FollowedUser temp: statistics){
            if (temp.getTotalNumberOfTweets() <q1){
                group1.add(temp);
            }else if (temp.getTotalNumberOfTweets() < q2){
                group2.add(temp);
            }else if (temp.getTotalNumberOfTweets() < q3){
                group3.add(temp);
            }else{
                group4.add(temp);
            }
        }
       
        //random selection from each category
        getRandomUsersFromGroup(group1);
        getRandomUsersFromGroup(group2);
        getRandomUsersFromGroup(group3);
        getRandomUsersFromGroup(group4);
    }
    
    private void getRandomUsersFromGroup(ArrayList<FollowedUser> group){
        Random random = new Random();
        for(int i=0; i<10; i++){
            usersCollection.add(group.remove(random.nextInt(group.size())));
        }
    }
    
    private int findMedian(int[] numbers){
        if(numbers.length % 2 == 0){//odd number
            return (numbers[numbers.length/2] + numbers[numbers.length/2 +1])/2;
        }else{ //even number
            return numbers[(int) statistics.size()/2 +1];
        }
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