/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spotthefraud;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;
import java.io.IOException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import twitter4j.HttpResponse;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.Trends;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.json.DataObjectFactory;

/**
 *
 * @author Vromia
 */
public class TweetsControl {
    
     private ScheduledExecutorService scheduler;
     private  Twitter twitter;
    // private TwitterStream twitterStream;
    // private StatusListener listener;
     private MongoClient client;
     private DB db,TweetDb;
     private DBCollection coll,tweetColl;
     private ArrayList<TopTrendingTopic> allTopics;
     private DateFormat dateformat;
     private boolean start,exists;
     private int time;
     private Thread topTopicThread,trendingTweetsThread;
     private Runnable topTopicRunnable,trendintTweetRunnabe;
     private DefaultHttpClient Httpclient;
     private HttpGet getJson;
     private int statusCode;
     
     private volatile boolean stopRequested=false;
     private CountDownLatch cdl;
     
     public TweetsControl(){
        scheduler =Executors.newScheduledThreadPool(1);
       // this.twitter=twitter;
        
        allTopics=new ArrayList<>();
        dateformat=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        start=true;
        time=0;
        
          ConfigurationBuilder cb=new ConfigurationBuilder();
            cb.setOAuthConsumerKey("0cc8fkRgUfzX5fYK14m211vhE");
            cb.setOAuthConsumerSecret("45d3sLIiEG0suWxEGBECTWP0tXJL6hJQwqqNCvo04eeGKjL8Al");
            cb.setOAuthAccessToken("43403340-aUeWfSgfYpYSDmoeVzaPXF1aaiBAo3IL7zgIXwahU");
            cb.setOAuthAccessTokenSecret("Tc40irSU8G15IvvEu6EuVjsaM1xQAVCDzJoaSTnxYVFOI");
            cb.setJSONStoreEnabled(true);
            
          twitter=new TwitterFactory(cb.build()).getInstance();
          
          //twitterStream=new TwitterStreamFactory(cb.build()).getInstance();
        
         try {
             client=new MongoClient("localhost",27017);
             db=client.getDB("TopTopics");
             TweetDb=client.getDB("TopTopics");
             
             coll=db.createCollection("topicsColl",null);
             tweetColl=TweetDb.createCollection("tweetsColl", null);
            // BasicDBObject doc=new BasicDBObject();
            // doc.put("name", "Test");
             //coll.insert(doc);
         } catch (UnknownHostException ex) {
             Logger.getLogger(TweetsControl.class.getName()).log(Level.SEVERE, null, ex);
         }
        
        // initializeClient();
         initializeRunnables();
         
         
         topTopicThread=new Thread(topTopicRunnable);
         trendingTweetsThread=new Thread(trendintTweetRunnabe);
         
         cdl=new CountDownLatch(1);
         
         
         topTopicThread.start();
         trendingTweetsThread.start();
         
         
         
         
     /*
       final ScheduledFuture<?> gatheringHandler=scheduler.scheduleAtFixedRate(new Runnable() {


        }, 0, 5, TimeUnit.MINUTES);
         scheduler.schedule(new Runnable() {

            @Override
            public void run() {
              gatheringHandler.cancel(true);
              scheduler.shutdown();
            }
        }, 10, TimeUnit.MINUTES);
         */
         
         
        
     }
     
     
private void initializeRunnables(){
    topTopicRunnable=new Runnable() {

        @Override
        public void run() {
            
            //finishProcedure();
            
            
            while(!stopRequested){
             try {
                     
                     Trends trends=twitter.getPlaceTrends(1);
                      System.out.println("hello");
 
                     for(int i=0; i<trends.getTrends().length; i++){
                         if(start){
                         TopTrendingTopic topic=new TopTrendingTopic(trends.getTrends()[i].getName(),time);
                         allTopics.add(topic);
                         BasicDBObject doc=new BasicDBObject();
                         doc.put("Name",topic.getName());
                         doc.put("Arrival Time", topic.getArrivalTime());
                         doc.put("Finish Time",topic.getFinishTime());
                         coll.insert(doc);
                         }else{
                             String nameF=trends.getTrends()[i].getName();
                             System.out.println(nameF);
                             exists=false;
                             int pos=0;
                             for(TopTrendingTopic top:allTopics){
                                 
                                 //Edw elegxoume gia t kainouria trends ama uparxoun idi mesa stn lista mas
                                 //Ama uparxoun au3anoume to finish time tous
                                 if(top.getActivity() && trends.getTrends()[i].getName().equals(top.getName()) ){
                                     
                                     BasicDBObject query=new BasicDBObject().append("Name", top.getName());
                                     allTopics.get(pos).increaseFinishTime(time);
                                     exists=true;
                                     
                                    BasicDBObject newdocument=new BasicDBObject().append("$set",new BasicDBObject().append("Finish Time", time+1));
                                    coll.update(query, newdocument);
                     
                                     break;
                                 }
                              pos++;   
                             }
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
                      System.out.println("----------");
                     for(int i=0; i<allTopics.size(); i++){
                        
                         System.out.println("Name="+allTopics.get(i).getName()+" Arrival Time="+allTopics.get(i).getArrivalTime()
                         +" Finish Time="+allTopics.get(i).getFinishTime());
                     }
                        System.out.println("################");
                     
                     time+=1;//Edv metrame 5leta
                     start=false;
                     
                 } catch (TwitterException ex) {
                   
                     Logger.getLogger(TweetsControl.class.getName()).log(Level.SEVERE, null, ex);
                 }
             cdl.countDown();
             
      
                         
            try {
                Thread.sleep(300000);
            } catch (InterruptedException ex) {
               // Logger.getLogger(TweetsControl.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Process Finished");
            }
             
                 
                 
                 
         
        }
            
            
        }
    };
    
    
    trendintTweetRunnabe=new Runnable() {

        @Override
        public void run() {
            
            finishProcedure();
            while(!stopRequested){
            
                try {
                    cdl.await();
                } catch (InterruptedException ex) {
                    Logger.getLogger(TweetsControl.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            if(allTopics==null){
                System.out.println("No topics are initialized");
            }
           int pos=0;
            for(TopTrendingTopic top:allTopics){
              
                System.out.println(pos);
          
              
               
                
                //For each topic we check the finish time if the topic has total time > from the current time it means that is active
                //else time has passed 2 hours so the topic is not active
                if(top.getFinishTime()+24>=time){
                    //TODO implement the JSON Parser need to check also the threads.
                    Query query=new Query(top.getName());
                    try {
                        QueryResult result=twitter.search(query);
                         for(Status tweet:result.getTweets()){
                            

                              String json=DataObjectFactory.getRawJSON(tweet);
                              DBObject jsonObj=(DBObject) JSON.parse(json);
                              tweetColl.insert(jsonObj);
                                      
                             System.out.println(json+"**************");
                         }
                    } catch (TwitterException ex) {
                        Logger.getLogger(TweetsControl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                   
                    
                }else{
                    allTopics.remove(pos);
                }
                
                pos++;
            }
            
               if(time==2){
                   stopRequested=true;
                   topTopicThread.interrupt();
               }else{
            
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

private void finishProcedure(){
    
    if(time==3){
    stopRequested=true;
    }
    }
    
}


     

