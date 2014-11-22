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
    private ArrayList<FollowedUser> statistics;
    private ArrayList<FollowedUser> group1;
    private ArrayList<FollowedUser> group2;
    private ArrayList<FollowedUser> group3;
    private ArrayList<FollowedUser> group4;
    private ArrayList<FollowedUser> usersCollection; //final collection of users that will be followed
    private MongoClient client;
    private DB TweetDb;
    private DBCollection tweetColl;

    public TweetsAnalyzer() throws JSONException {
        statistics = null;
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
            
            JSONObject root = new JSONObject(cursor.next());
            JSONArray tweetArray = root.getJSONArray("tweet");
            // now get the first element:
            JSONObject firstTweet = tweetArray.getJSONObject(0);
            // and so on
            int id = firstTweet.getInt("id");
            
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
                statistics.add(new FollowedUser(id,0));
            }
	}
    }
    
    /**
     * It classifies all users into 4 Categories, depending on the number of tweets they did
     */
    @SuppressWarnings("empty-statement")
    private void classificationOfUsers(){
        int[] numbers;
        int[] firstHalf;
        int[] secondHalf;
        int counter, q1, q2, q3, size = statistics.size(), halfsize;
        
        numbers = new int[size];
        
        if(size % 2 == 0){//----------odd number
            halfsize = size/2;
        }else{//--------------------------------even number
            halfsize = size/2 +1;
        }
                
        counter=0;
        for(FollowedUser temp: statistics){
            numbers[counter++] = temp.totalNumberOfTweets;          
        }
        
        Arrays.sort(numbers);
        
        q2 = findMedian(numbers);
        
        firstHalf = Arrays.copyOfRange(numbers, 0, halfsize);
        secondHalf = Arrays.copyOfRange(numbers, halfsize, size);
        
        q1 = findMedian(firstHalf);
        q3 = findMedian(secondHalf);             
       
        for(FollowedUser temp: statistics){
            if (temp.totalNumberOfTweets <q1){
                group1.add(temp);
            }else if (temp.totalNumberOfTweets < q2){
                group2.add(temp);
            }else if (temp.totalNumberOfTweets < q3){
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
        Random random= new Random();
        for(int i=0; i<10;i++) {
            usersCollection.add(group.remove(random.nextInt(group.size()-1)));
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
