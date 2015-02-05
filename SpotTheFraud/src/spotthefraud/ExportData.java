package spotthefraud;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import twitter4j.JSONArray;
import twitter4j.JSONException;
import twitter4j.JSONObject;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Runs through every tweet that was collected from the tracking of selected
 * 40 users and extracts wanted data. It assigns an object of FollowedUserDetails
 * to each of the 40 users and stores their data in there.
 * Finally, it prints all final data for each user.
 * 
 * @authors Nikos Zissis, Sokratis Papadopoulos, George Mihailidis, Anastasios Kostas
 */
public class ExportData {

    //basic variables for Mongo & Twitter API

    private MongoClient client;
    private DB FollowedUsers;
    private DBCollection followedColl;
    private ConfigurationBuilder cb;

    private ArrayList<FollowedUserDetails> followedUsers; //creates the arraylist of final data of followed users

    public ExportData() throws JSONException, URISyntaxException {
        initBasicVariables();
        export();
    }

    private void initBasicVariables() {
        cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey("0cc8fkRgUfzX5fYK14m211vhE");
        cb.setOAuthConsumerSecret("45d3sLIiEG0suWxEGBECTWP0tXJL6hJQwqqNCvo04eeGKjL8Al");
        cb.setOAuthAccessToken("43403340-aUeWfSgfYpYSDmoeVzaPXF1aaiBAo3IL7zgIXwahU");
        cb.setOAuthAccessTokenSecret("Tc40irSU8G15IvvEu6EuVjsaM1xQAVCDzJoaSTnxYVFOI");
        cb.setJSONStoreEnabled(true); //We use this as we pull json files from Twitter Streaming API

        try {
            client = new MongoClient("localhost", 27017);
            FollowedUsers = client.getDB("Followed");
            followedColl = FollowedUsers.createCollection("followedColl", null);
        } catch (UnknownHostException ex) {
            Logger.getLogger(TweetsControl.class.getName()).log(Level.SEVERE, null, ex);
        }

        followedUsers = new ArrayList<>();
    }

    public void export() throws JSONException, URISyntaxException {

        DBCursor cursor = followedColl.find(); //get a cursor that will run throughout the collection.
        while (cursor.hasNext()) { //for each tweet in the collection
            DBObject obj = cursor.next(); //stores the tweet data into obj
            FollowedUserDetails tweet_user; //going to store the details of each tweet and then insert into arraylist followedUsers
            
            //init variables
            boolean flag = false; //used to identify if a user is already in the list or not
            int tweets = 0, user_mentions = 0, user_hashtag = 0, user_url = 0, retweeted = 0, reply = 0, retweet_count = 0;
            String expanded_url[] = new String[1];
            String tweet = "", reply_to_userID = "";

            //======= GETTING USER ID =======
            JSONObject jobj = new JSONObject(obj.toString());
            String userID = jobj.getJSONObject("user").getString("id_str"); //gets the ID of user
            //System.out.println("1) userID: "+userID);
            int followers = jobj.getJSONObject("user").getInt("followers_count");
            //System.out.println("Followers: " + followers);
            int followees = jobj.getJSONObject("user").getInt("friends_count");
            //System.out.println("Followees: " + followees);
            String account_age = jobj.getJSONObject("user").getString("created_at");
            //System.out.println("Account age: "+ account_age);

            //======= GETTING REPLY_TO_STATUS (true/false)=======
            if (obj.get("in_reply_to_status_id_str") != null) {
                reply += 1;
                reply_to_userID = obj.get("in_reply_to_status_id_str").toString();
            }
            //System.out.println("2) reply?: "+reply_to_userID);

            //======= GETTING RETWEET_COUNT =======
            String retweet_count_str = obj.get("retweet_count").toString();
            retweet_count = Integer.parseInt(retweet_count_str);
            //System.out.println("3) retweet_count: "+retweet_count);

            //======= GETTING SOURCE =======
            String source = obj.get("source").toString();
            //System.out.println("4) source: "+source);

            //======= GETTING TWEETS (STATUSES_COUNT) =======
            //String tweets_str = jobj.getJSONObject("user").getString("statuses_count");
            //tweets = Integer.parseInt(tweets_str);
            //System.out.println("5) statuses_count: "+tweets);

            JSONObject entities = jobj.getJSONObject("entities"); //getting inside "entities" in json

            //======= GETTING MENTIONS =======
            JSONArray mentionArray = entities.getJSONArray("user_mentions");
            user_mentions = mentionArray.length();
            //System.out.println("6) mentions: "+user_mentions);

            //======= GETTING HASHTAGS =======
            JSONArray hashtagArray = entities.getJSONArray("hashtags");
            user_hashtag = hashtagArray.length();
            //System.out.println("7) hashtags: "+user_hashtag);

            //======= GETTING URLs =======
            JSONArray urlArray = entities.getJSONArray("urls");
            user_url = urlArray.length();
            //System.out.println("8) urls: "+user_url);

            //======= GETTING EXPANDED URLs =======
            if (user_url != 0) { //if URLs exist, it takes the expanded version of them
                expanded_url = new String[user_url];
                for (int i = 0; i < user_url; i++) {
                    expanded_url[i] = urlArray.getJSONObject(i).getString("expanded_url");
                    //System.out.println("9) Expanded URL: "+expanded_url[i]);
                }
            }
            
            //------- CHECK IF RETWEETED -------
            if (jobj.has("retweeted_status")) {
                retweeted += 1;
            } else {
                tweet = obj.get("text").toString();
                //System.out.println("Tweet text is: "+tweet);
            }

            int pos = 0;
            for (FollowedUserDetails user : followedUsers) {
                String id = user.getUserID();
                if (id.equals(userID)) {
                    flag = true;
                    break;
                }
                pos++;
            }

            //We check if userID exists already in our arrayList. If not we create a new user.
            if (!flag) {
                //System.out.println("Kainourios xrhsths");
                tweet_user = new FollowedUserDetails(userID, followers, followees, account_age);
                tweet_user.setNOTweets(1);
                if (retweeted != 0) {
                    tweet_user.increaseNORetweets();
                } else {
                    tweet_user.addAndProcessTweet(tweet);
                }
                if (reply != 0) {
                    tweet_user.increaseNOReplies();
                }
                tweet_user.increaseNOMentions(user_mentions);
                tweet_user.increaseNOHashtags(user_hashtag);
                tweet_user.increaseNOUrls(user_url);
                tweet_user.increaseNORReceived(retweet_count);
                if (user_url != 0) {
                    for (int i = 0; i < user_url; i++) {
                        tweet_user.addUniqueURL(expanded_url[i]);
                    }
                }
                tweet_user.increaseSourceCount(source);
                followedUsers.add(tweet_user);

            } else {
                //System.out.println("Yparxei o xrhsths");
                followedUsers.get(pos).increaseNOTweets(1);
                if (retweeted != 0) {
                    followedUsers.get(pos).increaseNORetweets();
                } else {
                    followedUsers.get(pos).addAndProcessTweet(tweet);
                }
                if (reply != 0) {
                    followedUsers.get(pos).increaseNOReplies();
                }
                followedUsers.get(pos).increaseNOMentions(user_mentions);
                followedUsers.get(pos).increaseNOHashtags(user_hashtag);
                followedUsers.get(pos).increaseNOUrls(user_url);
                followedUsers.get(pos).increaseNORReceived(retweet_count);
                if (user_url != 0) {
                    for (int i = 0; i < user_url; i++) {
                        followedUsers.get(pos).addUniqueURL(expanded_url[i]);
                    }
                }
                followedUsers.get(pos).increaseSourceCount(source);
            }
            //System.out.println("---------------");
        }

        int i=1;
        System.out.println("*** Sinolikoi xristes: " + followedUsers.size() + " ***");
        for (FollowedUserDetails user : followedUsers) {
            user.calculateAvegHashtagsPerTweet();
            user.calculateAvegRetweetPerTweet();
            user.calculatePercentageOfTweetsWithHashtag();
            user.calculatePercentageOfTweetsWithURL();
            user.calculateURLsRatio();
            user.calculateMostFrequentSource();
            user.calculateUniqueDomains();
            user.calculateCopies();
            i++;
        }

        //PRINTING FINAL RESULTS
        System.out.println("!!!!!!!!!!!! FINAL RESULTS !!!!!!!!!!!!");
        for (FollowedUserDetails user : followedUsers) {
            user.printAll();
        }
    }
}
