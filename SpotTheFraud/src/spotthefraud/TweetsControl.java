package spotthefraud;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Trends;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.json.DataObjectFactory;

/**
 *
 * @author Vromia
 */
public class TweetsControl {
    
     private  Twitter twitter;
    // variables for handling nosql database Mongo
     private MongoClient client;
     private DB db,TweetDb;
     private DBCollection coll,tweetColl;
     
    //variables for  parallel running Threads
     private Thread topTopicThread,trendingTweetsThread;
     private Runnable topTopicRunnable,trendintTweetRunnabe;
     private volatile boolean stopRequested=false;//volatile variables are global for all Threads
     private CountDownLatch cdl;//We use CountDownLatch to handle the running of Threads
     
     private ArrayList<TopTrendingTopic> allTopics;
     private boolean start; //distinguises the very first iteration of topics to allTopics arraylist. That happens because at the next iterations we use another method.
     private boolean exists; //identifies if a trend already exists in allTopics
     private int time; //this is our variable to measure time. It is measured in # of 5-minutes-window. 
     
     public TweetsControl(){
        
        // The configuration details of our application as developer mode of Twitter API
        ConfigurationBuilder cb=new ConfigurationBuilder();
        cb.setOAuthConsumerKey("0cc8fkRgUfzX5fYK14m211vhE");
        cb.setOAuthConsumerSecret("45d3sLIiEG0suWxEGBECTWP0tXJL6hJQwqqNCvo04eeGKjL8Al");
        cb.setOAuthAccessToken("43403340-aUeWfSgfYpYSDmoeVzaPXF1aaiBAo3IL7zgIXwahU");
        cb.setOAuthAccessTokenSecret("Tc40irSU8G15IvvEu6EuVjsaM1xQAVCDzJoaSTnxYVFOI");
        cb.setJSONStoreEnabled(true); //We use this as we pull json files from Twitter Streaming API
            
        //We use Twitter4J library to connect to Twitter API
        twitter=new TwitterFactory(cb.build()).getInstance();
        //the arraylist of the TopTrendingTopics objects. 
        allTopics=new ArrayList<>();
        start=true; 
        time=0; 
        
        initializeMongo();
        initializeRunnables();
        initializeThreadsAndStartProcedure();
     }
     
    /**
     * This is the method that initialize our 2 Runnables that we run concurrently
     */
    private void initializeRunnables(){
        //the execution regarding gathering of top trending topics from REST API
        topTopicRunnable=new Runnable() {

            @Override
            public void run() {

                //while volatile variable is false we continue to execute this runnable
                while(!stopRequested){
                 try {
                         //we get the top10 topic trends using getPlaceTrends(int scope) and we use value '1' for global scope
                         Trends trends=twitter.getPlaceTrends(1);

                         //for each one top trending topic...
                         for(int i=0; i<trends.getTrends().length; i++){
                             /**
                              * if true, means that we gather for the first time the trending topics. 
                              * So, we create a TopTrendingTopic object and we add it to arraylist: allTopics
                              * Then, we create a BasicDBObject with columns: name, arrival_time, finish_time.
                              * We add the values of TopTrendingTopic object. Finally, we insert this object to mongoDB collection: coll.
                              */
                             if(start){ 
                                TopTrendingTopic topic=new TopTrendingTopic(trends.getTrends()[i].getName(),time);
                                allTopics.add(topic);
                                BasicDBObject doc=new BasicDBObject();
                                doc.put("Name",topic.getName());
                                doc.put("Arrival Time", topic.getArrivalTime());
                                doc.put("Finish Time",topic.getFinishTime());
                                coll.insert(doc);
                             }
                             //if start=false, means that we are not in the first iteration, so we need to check if
                             //the current trends exists already in arraylist:allTopics
                             else{ 

                                 exists=false;//before we iterate the list the exists variable is false
                                 int pos=0;//position of current Topic in arraylist:allTopics
                                 for(TopTrendingTopic top:allTopics){

                                     //Check if the trends exists in allTopics and if it does we update the value of finishTime
                                     if(trends.getTrends()[i].getName().equals(top.getName()) ){
                                         //Also we update the finish time of the topic in the mongoDb using method Collection.update(query,new document)
                                         //Where query is the DBObject that we want to update and new document is the value of the column that we want
                                         //to update
                                         BasicDBObject query=new BasicDBObject().append("Name", top.getName());
                                         allTopics.get(pos).increaseFinishTime(time);//update the value of finishTime
                                         exists=true;//change the boolean variable to true because it exists in allTopics

                                        BasicDBObject newdocument=new BasicDBObject().append("$set",new BasicDBObject().append("Finish Time", time+1));
                                        coll.update(query, newdocument);

                                         break;
                                     }
                                  pos++;//In each iteration we increase the position
                                 }
                                 //When the current trend Topic have been checked with all existing trend Topics in arraylist:allTopics
                                 //If it doesn;t exists we create a TopTrendingTopic object and add it to allTopics.
                                 //Also we insert it to mongoDB Collections: coll
                                 if(!exists){
                                    TopTrendingTopic topic=new TopTrendingTopic(trends.getTrends()[i].getName(),time);
                                    allTopics.add(topic);
                                    BasicDBObject doc=new BasicDBObject();
                                    doc.put("Name",topic.getName());
                                    doc.put("Arrival Time", topic.getArrivalTime());
                                    doc.put("Finish Time",topic.getFinishTime());
                                    coll.insert(doc);
                                 }

                             }

                         }
                         
                         for(int i=0; i<allTopics.size(); i++){

                             System.out.println("Name="+allTopics.get(i).getName()+" Arrival Time="+allTopics.get(i).getArrivalTime()
                             +" Finish Time="+allTopics.get(i).getFinishTime());
                         }


                         time+=1;//Every time that we gather trends Topics we increate the time by 1 that is translated in 5 minutes real time
                         start=false;

                     } catch (TwitterException ex) {

                         Logger.getLogger(TweetsControl.class.getName()).log(Level.SEVERE, null, ex);
                     }

              cdl.countDown();//We want to run 2 Threads concurrently with a standard order of execution of Threads.
              //We use the object CountDownLatch to prevent the trendingTweetsThread from running before the topTopicThread which gathers the
              //trend Topics.So we use the method countDown to tell that we finished the execution of topTopicThread. After this 2 senarios can
              //happen:a)first start trendingTweetsThread and then interrupt topTopicThread b)first interrupt topTopicThread and then start trendingTweetsThread

              //We suspend the execution of topTopicThread for 5 min because we can obtain new data from REST API only after 5 min have passed.
                try {
                    Thread.sleep(300000);
                } catch (InterruptedException ex) {
                    System.out.println("Process Finished");
                }
            } 
        }
    };
    
    //the execution of Runnable that collects tweets from the current Top Topics: allTopics
    trendintTweetRunnabe=new Runnable() {

        @Override
        public void run() {
            
           //while volatile variable is false we continue to execute this runnable
            while(!stopRequested){
            
                try {
                    //When this Runnable starts to run we need to check if the CountDownLatch object is countDowned from the  Thread
                    //topTopicThread so the execution can start.This is achieved by method CountDownLatch.await().
                    
                    cdl.await();
                } catch (InterruptedException ex) {
                    Logger.getLogger(TweetsControl.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            //Check if trends top topics exist in the list :allTopics    
            if(allTopics==null){
                System.out.println("No topics are initialized");
            }
            
           ArrayList<Integer> positions=new ArrayList<>(); //ArrayList to keep track of the positions of topics that are not active anymore. 
           int pos=0;//position of current Topic in arraylist:allTopics
           
            for(TopTrendingTopic top:allTopics){
              
               
                //For each topic we check its finish time, if it is > from the current time it means that is active.
                if(top.getFinishTime()+24>=time){
                    
                    //We create a query to filter the tweets from Streaming Api based on our top trends Topics. So we take for each active
                    //Top Trending Topic its name and we pass it to constructor of Query Object.
                    Query query=new Query(top.getName());
                    //Then we search the Streaming Api with this query and we save the results in QueryResult object result.
                    try {
                        QueryResult result=twitter.search(query);
                        //For every tweet that we gathered we take its json form and we save it in String variable json. To do that we use
                        //the DataObjectFactory.getRawJSON(tweet) method that returns the json of tweet in String format.Then we parse the json
                        //to a DbObject and we insert it to MongoDB Collection: tweetColl
              
                         for(Status tweet:result.getTweets()){
                              String json=DataObjectFactory.getRawJSON(tweet);
                              DBObject jsonObj=(DBObject) JSON.parse(json);
                              tweetColl.insert(jsonObj);
                                      
                             System.out.println(json+"**************");
                         }
                    } catch (TwitterException ex) {
                        Logger.getLogger(TweetsControl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                //Else 2 hours(24 5-minutes-windows) have passed from the finish time of the trend Topic so its not active anymore.
                //So we add to ArrayList: positions the position of this trend Topic.
                else{
                
                    positions.add(pos);
                }
                pos++;
            }
            
            //After we have collected all the tweets from active trend Topics we remove from ArrayList:
            //allTopics all the trend Topics that are not active anymore.
            for(int i=0; i<positions.size(); i++){
                int correctRemove=positions.get(i)-i;
                String name=allTopics.get(correctRemove).getName();
                allTopics.remove(correctRemove);
                System.out.println(name);
            }
            //If time is 864 it means that 3 days have passed so we need to stop the procedure.
               if(time==3){
                   //We change the value of volatile boolean variable stopRequested to true. Then the trendingTweetsRunnable proceeds to the
                   //next iteration but stopRequested is now true so it cannot enter while loop.As a result the Thread is being terminated.
                   //Also we have woken up the topTopicThread and it cannot enter while loop so it terminates as well.
                   stopRequested=true;
                   topTopicThread.interrupt();
               }
               //Else we stop the this Thread for 5 minutes.
               else{
            
                try {
                    Thread.sleep(300000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(TweetsControl.class.getName()).log(Level.SEVERE, null, ex);
                }
               }           
        }
        }
    };
    
}
/**
 * We initialize the attributes of MongoDb.First we create a MongoClient object. Then we take topTopics Database and we create two
 *collections for Top Trending Topics and for Tweets from Top Trending Topics
 */
    private void initializeMongo(){
        
          try {
             client=new MongoClient("localhost",27017);
             db=client.getDB("TopTopics");
             TweetDb=client.getDB("Tweets");

             coll=db.createCollection("topicsColl",null);
             tweetColl=TweetDb.createCollection("tweetsColl", null);

            } catch (UnknownHostException ex) {
             Logger.getLogger(TweetsControl.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
/**
 * We initialize our Threads and also the CountDownLatch object and after that we start the procedure.
 */
    private void initializeThreadsAndStartProcedure(){

         topTopicThread=new Thread(topTopicRunnable);
         trendingTweetsThread=new Thread(trendintTweetRunnabe);

         cdl=new CountDownLatch(1);

         topTopicThread.start();
         trendingTweetsThread.start();
    }

  }


     

