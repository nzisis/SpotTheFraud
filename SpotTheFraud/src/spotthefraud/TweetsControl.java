package spotthefraud;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;
import java.lang.Thread.State;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import twitter4j.ConnectionLifeCycleListener;
import twitter4j.FilterQuery;
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
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.json.DataObjectFactory;

/**
 * 
 * 
 * @authors Nikos Zissis, Sokratis Papadopoulos, George Mihailidis, Anastasios Kostas
 */
public class TweetsControl {

    private Twitter twitter;
    // variables for handling nosql database Mongo
    private MongoClient client;
    private DB db, TweetDb;
    private DBCollection coll, tweetColl;

    //variables for  parallel running Threads
    private Thread topTopicThread;
    private Runnable topTopicRunnable;
    private volatile boolean stopRequested = false;//volatile variables are global for all Threads

    private ArrayList<TopTrendingTopic> allTopics;
    private boolean start; //distinguises the very first iteration of topics to allTopics arraylist. That happens because at the next iterations we use another method.
    private boolean exists; //identifies if a trend already exists in allTopics
    private int time; //this is our variable to measure time. It is measured in # of 5-minutes-window. 

    private TwitterStream stream;
    private StatusListener listener;
    private final Object lock;
    private final Configuration config;
    private FilterQuery fq;

    public TweetsControl() {

        // The configuration details of our application as developer mode of Twitter API
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey("0cc8fkRgUfzX5fYK14m211vhE");
        cb.setOAuthConsumerSecret("45d3sLIiEG0suWxEGBECTWP0tXJL6hJQwqqNCvo04eeGKjL8Al");
        cb.setOAuthAccessToken("43403340-aUeWfSgfYpYSDmoeVzaPXF1aaiBAo3IL7zgIXwahU");
        cb.setOAuthAccessTokenSecret("Tc40irSU8G15IvvEu6EuVjsaM1xQAVCDzJoaSTnxYVFOI");
        cb.setJSONStoreEnabled(true); //We use this as we pull json files from Twitter Streaming API

        config = cb.build();
        lock = new Object();

        //We use Twitter4J library to connect to Twitter API
        twitter = new TwitterFactory(config).getInstance();

        listener = new StatusListener() {

            @Override
            public void onStatus(Status status) {

                String json = DataObjectFactory.getRawJSON(status);
                DBObject jsonObj = (DBObject) JSON.parse(json);
                tweetColl.insert(jsonObj);
                if (topTopicThread.getState() != State.TIMED_WAITING) { //ginete alithes otan ksekinaei i ananewsi threads //trexoun akoma i ananewsi twn trends
                    System.out.println("Stop gathering status");
                    synchronized (lock) {
                        try {
                            lock.wait();
                        } catch (InterruptedException ex) {
                            Logger.getLogger(TweetsControl.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                    if (stream != null) {
                        stopStreaming();
                    }

                    System.out.println("Start again the filtering process");
                    if (!stopRequested) {
                        startFiltering(listener, fq);
                    }
                }
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

            }
        };

        //the arraylist of the TopTrendingTopics objects. 
        allTopics = new ArrayList<>();
        start = true;
        time = 0;

        initializeMongo();
        initializeRunnables();
        initializeThreadsAndStartProcedure();
    }

    /**
     * This is the method that initialize our 2 Runnables that we run
     * concurrently
     */
    private void initializeRunnables() {
        //the execution regarding gathering of top trending topics from REST API
        topTopicRunnable = new Runnable() {

            @Override
            public void run() {

                //while volatile variable is false we continue to execute this runnable
                while (!stopRequested) {
                    System.out.println("Find the top trends");

                    try {
                        //we get the top10 topic trends using getPlaceTrends(int scope) and we use value '1' for global scope
                        Trends trends = twitter.getPlaceTrends(1);

                        //for each one top trending topic...
                        for (int i = 0; i < trends.getTrends().length; i++) {
                            /**
                             * if true, means that we gather for the first time
                             * the trending topics. So, we create a
                             * TopTrendingTopic object and we add it to
                             * arraylist: allTopics Then, we create a
                             * BasicDBObject with columns: name, arrival_time,
                             * finish_time. We add the values of
                             * TopTrendingTopic object. Finally, we insert this
                             * object to mongoDB collection: coll.
                             */
                            if (start) {
                                TopTrendingTopic topic = new TopTrendingTopic(trends.getTrends()[i].getName(), time);
                                allTopics.add(topic);
                                BasicDBObject doc = new BasicDBObject();
                                doc.put("Name", topic.getName());
                                doc.put("Arrival Time", topic.getArrivalTime());
                                doc.put("Finish Time", topic.getFinishTime());
                                coll.insert(doc);
                            } //if start=false, means that we are not in the first iteration, so we need to check if
                            //the current trends exists already in arraylist:allTopics
                            else {

                                exists = false;//before we iterate the list the exists variable is false
                                int pos = 0;//position of current Topic in arraylist:allTopics
                                for (TopTrendingTopic top : allTopics) {

                                    //Check if the trends exists in allTopics and if it does we update the value of finishTime
                                    if (trends.getTrends()[i].getName().equals(top.getName())) {
                                    //Also we update the finish time of the topic in the mongoDb using method Collection.update(query,new document)
                                        //Where query is the DBObject that we want to update and new document is the value of the column that we want
                                        //to update
                                        BasicDBObject query = new BasicDBObject().append("Name", top.getName());
                                        allTopics.get(pos).increaseFinishTime(time);//update the value of finishTime
                                        exists = true;//change the boolean variable to true because it exists in allTopics

                                        BasicDBObject newdocument = new BasicDBObject().append("$set", new BasicDBObject().append("Finish Time", time + 1));
                                        coll.update(query, newdocument);

                                        break;
                                    }
                                    pos++;//In each iteration we increase the position
                                }
                                //When the current trend Topic have been checked with all existing trend Topics in arraylist:allTopics
                                //If it doesn;t exists we create a TopTrendingTopic object and add it to allTopics.
                                //Also we insert it to mongoDB Collections: coll
                                if (!exists) {
                                    TopTrendingTopic topic = new TopTrendingTopic(trends.getTrends()[i].getName(), time);
                                    allTopics.add(topic);
                                    BasicDBObject doc = new BasicDBObject();
                                    doc.put("Name", topic.getName());
                                    doc.put("Arrival Time", topic.getArrivalTime());
                                    doc.put("Finish Time", topic.getFinishTime());
                                    coll.insert(doc);
                                }
                            }
                        }

                        for (int i = 0; i < allTopics.size(); i++) {

                            System.out.println("Name=" + allTopics.get(i).getName() + " Arrival Time=" + allTopics.get(i).getArrivalTime()
                                    + " Finish Time=" + allTopics.get(i).getFinishTime());
                        }

                        time += 1;//Every time that we gather trends Topics we increate the time by 1 that is translated in 5 minutes real time
                        start = false;

                    } catch (TwitterException ex) {
                        Logger.getLogger(TweetsControl.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    ArrayList<String> currentTopTrends = new ArrayList<>();
                    for (TopTrendingTopic topic : allTopics) {
                        if (topic.getFinishTime() + 24 >= time) {
                            currentTopTrends.add(topic.getName());
                        }
                    }

                    fq = new FilterQuery();
                    String keywords[] = new String[currentTopTrends.size()];
                    currentTopTrends.toArray(keywords);
                    fq.track(keywords);

                    synchronized (lock) {
                        lock.notify();
                    }

                    //We suspend the execution of topTopicThread for 5 min because we can obtain new data from REST API only after 5 min have passed.
                    // 567 ==> two days(48h) | 865 ==> three days (72h)
                    if (time == 865) {
                        //We change the value of volatile boolean variable stopRequested to true. Then the trendingTweetsRunnable proceeds to the
                        //next iteration but stopRequested is now true so it cannot enter while loop.As a result the Thread is being terminated.
                        //Also we have woken up the topTopicThread and it cannot enter while loop so it terminates as well.
                        stopRequested = true;
                    } else {
                        try {
                            Thread.sleep(300000);
                        } catch (InterruptedException ex) {
                            System.out.println("Process Finished");
                        }
                    }
                }
            }
        };
    }

    /**
     * We initialize the attributes of MongoDb.First we create a MongoClient
     * object. Then we take topTopics Database and we create two collections for
     * Top Trending Topics and for Tweets from Top Trending Topics
     */
    private void initializeMongo() {

        try {
            client = new MongoClient("localhost", 27017);
            db = client.getDB("TopTopics");
            TweetDb = client.getDB("Tweets");

            coll = db.createCollection("topicsColl", null);
            tweetColl = TweetDb.createCollection("tweetsColl", null);

        } catch (UnknownHostException ex) {
            Logger.getLogger(TweetsControl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * We initialize our Threads and also the CountDownLatch object and after
     * that we start the procedure.
     */
    public void initializeThreadsAndStartProcedure() {

        topTopicThread = new Thread(topTopicRunnable);
        //ksekinaei kai lockarete kateutheian
        topTopicThread.start();
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                System.out.println("Interrupted");
            }
        }
        startFiltering(listener, fq);
        System.out.println("Start Filtering");
    }
    
    /**
     * Sets the filter for the crawling from Streaming API.
     * @param listener
     * @param query 
     */
    private void startFiltering(StatusListener listener, FilterQuery query) {
        stream = new TwitterStreamFactory(config).getInstance();
        stream.addListener(listener);
        stream.filter(query);
    }
    
    /**
     * Shut downs the crawling.
     */
    private void stopStreaming() {

        if (stream == null) {
            return;
        }
        stream.addConnectionLifeCycleListener(new ConnectionLifeCycleListener() {

            @Override
            public void onConnect() {

            }

            @Override
            public void onDisconnect() {

            }

            @Override
            public void onCleanUp() {
                stream = null;
            }
        });

        stream.shutdown();

        while (stream != null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
        if (stream == null) {
            System.out.println("Stream Stopped");
        }
    }
}
