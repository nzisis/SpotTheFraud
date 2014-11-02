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
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import twitter4j.Trends;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 *
 * @author Vromia
 */
public class TweetsControl {
    
     private ScheduledExecutorService scheduler;
     private Twitter twitter;
     private MongoClient client;
     private DB db;
     private DBCollection coll;
     private ArrayList<TopTrendingTopic> allTopics;
     private DateFormat dateformat;
     private boolean start,exists;
     private int time;
     
     public TweetsControl(Twitter twitter){
        scheduler =Executors.newScheduledThreadPool(1);
        this.twitter=twitter;
        allTopics=new ArrayList<>();
        dateformat=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        start=true;
        time=0;
        
         try {
             client=new MongoClient("localhost",27017);
             db=client.getDB("TopTopics");
             coll=db.createCollection("topicsColl",null);
             BasicDBObject doc=new BasicDBObject();
             doc.put("name", "Test");
             coll.insert(doc);
         } catch (UnknownHostException ex) {
             Logger.getLogger(TweetsControl.class.getName()).log(Level.SEVERE, null, ex);
         }
        
     
     }
    
     public void GatherTopTweets(){
        final Runnable gathering=new Runnable() {

             @Override
             public void run() {
                 
                 try {
                     Trends trends=twitter.getPlaceTrends(1);
                     /*
                     Date date=new Date();
                     Calendar calendar=Calendar.getInstance();
                     
                     String currentDate=dateformat.format(date);
                     String currentTime=dateformat.format(calendar.getTime());
                             */
                     
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
                             exists=false;
                             for(TopTrendingTopic top:allTopics){
                                 //Edw elegxoume gia t kainouria trends ama uparxoun idi mesa stn lista mas
                                 //Ama uparxoun au3anoume to finish time tous
                                 if(top.getActivity() && trends.getTrends()[i].getName().equals(top.getName()) ){
                                     top.increaseFinishTime();
                                     exists=true;
                                     DBCursor cursor=coll.find();
                                     while(cursor.hasNext()){
                                               
                                       DBObject updObject=cursor.next();
                                       String name=updObject.get("Name").toString();
                                       if(top.getName().equals(name)){
                                           updObject.put("Finish Time",top.getFinishTime());
                                           
                                       }
                                         
                                         
                                     }
                                     
                                     
                                     break;
                                 }
                                 
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
                     time+=1;//Edv metrame 5leta
                     start=false;
                 } catch (TwitterException ex) {
                     Logger.getLogger(TweetsControl.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         };
         final ScheduledFuture<?> gatheringHandler=scheduler.scheduleAtFixedRate(gathering, 0, 5, TimeUnit.MINUTES);
         scheduler.schedule(new Runnable() {

            @Override
            public void run() {
              gatheringHandler.cancel(true);
            }
        }, 3, TimeUnit.DAYS);
     }
     
     
}
