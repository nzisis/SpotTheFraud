/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spotthefraud;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import twitter4j.JSONArray;
import twitter4j.JSONException;
import twitter4j.JSONObject;
import twitter4j.conf.ConfigurationBuilder;

/**
 *
 * @author geomih
 */
public class ExportData {
    
    
    private MongoClient client;
    private DB FollowedUsers;
    private DBCollection followedColl;
    private ConfigurationBuilder cb;
    private ArrayList<FollowedUserDetails> followedUsers;
    
    
    
    
    public ExportData(){
        initBasicVariables();
        export();
    }
    
    
    
    private void initBasicVariables(){
          cb=new ConfigurationBuilder();
        cb.setOAuthConsumerKey("0cc8fkRgUfzX5fYK14m211vhE");
        cb.setOAuthConsumerSecret("45d3sLIiEG0suWxEGBECTWP0tXJL6hJQwqqNCvo04eeGKjL8Al");
        cb.setOAuthAccessToken("43403340-aUeWfSgfYpYSDmoeVzaPXF1aaiBAo3IL7zgIXwahU");
        cb.setOAuthAccessTokenSecret("Tc40irSU8G15IvvEu6EuVjsaM1xQAVCDzJoaSTnxYVFOI");
        cb.setJSONStoreEnabled(true); //We use this as we pull json files from Twitter Streaming API
        
          try {
             client=new MongoClient("localhost",27017);
           
             FollowedUsers=client.getDB("Followed");
             followedColl=FollowedUsers.createCollection("followedColl", null);
             
            }catch (UnknownHostException ex) {
             Logger.getLogger(TweetsControl.class.getName()).log(Level.SEVERE, null, ex);
            }
          
          followedUsers=new ArrayList<>();
          
    }
    
    
    public void export(){
        
        
        DBCursor cursor = followedColl.find(); //get a cursor that will run throughout the collection.
           
	while (cursor.hasNext()) { //for each tweet in the collection
            
            DBObject obj=cursor.next();
            
             String userID=obj.get("id_str").toString();
             FollowedUserDetails tweet_user;
             
             int pos=0;
             boolean flag=false;
             int tweets=0,user_mentions=0,user_hashtag=0,user_url=0,retweeted=0,reply=0,retweet_count=0;
          
             String reply_str=obj.get("in_reply_to_status_id_str").toString();
             String retweet_count_str=obj.get("retweet_count").toString();
             retweet_count=Integer.parseInt(retweet_count_str);
             
            
            try {
                 JSONObject jobj=new JSONObject(obj.toString());
                 String tweets_str = jobj.getJSONObject("user").getString("statuses_count");
                 tweets=Integer.parseInt(tweets_str);
                
                JSONObject entities=jobj.getJSONObject("entities");
                JSONArray mentionArray=entities.getJSONArray("user_mentions");
                JSONArray hashtagArray=entities.getJSONArray("hashtags");
                JSONArray urlArray=entities.getJSONArray("urls");
                
                 user_mentions=mentionArray.length();
                 user_hashtag=hashtagArray.length();
                 user_url=urlArray.length();
                
                
                if(jobj.has("retweeted_status")){
                    retweeted+=1;
                }
                
                if(reply_str!=null){
                  reply+=1;
                }
                
                //int not=obj.getJson
            } catch (JSONException ex) {
                Logger.getLogger(ExportData.class.getName()).log(Level.SEVERE, null, ex);
            }
            
              for(FollowedUserDetails user :followedUsers){
                 String id=user.getUserID();
                 if(id.equals(userID)){
                    flag=true;
                     break;
                 }
                 pos++;
             }
             
             //Elegxoume to id ama uparxei idi sta arrraylist ama dn uparxei ftiaxnoume kainourio xrhsth
             if(!flag){
                 tweet_user=new FollowedUserDetails(userID);
                 tweet_user.setNOTweets(tweets);
                if(retweeted!=0){
                 tweet_user.increaseNORetweets();
                }
                if(reply!=0){
                    tweet_user.increaseNOReplies();
                }
                tweet_user.increaseNOMentions(user_mentions);
                tweet_user.increaseNOHashtags(user_hashtag);
                tweet_user.increaseNOUrls(user_url);
                tweet_user.increaseNORReceived(retweet_count);
                
                
             }else{
                 
                 followedUsers.get(pos).setNOTweets(tweets);
                 if(retweeted!=0){
                 followedUsers.get(pos).increaseNORetweets();
                }
                if(reply!=0){
                    followedUsers.get(pos).increaseNOReplies();
                }
                followedUsers.get(pos).increaseNOMentions(user_mentions);
                followedUsers.get(pos).increaseNOHashtags(user_hashtag);
                followedUsers.get(pos).increaseNOUrls(user_url);
                followedUsers.get(pos).increaseNORReceived(retweet_count);
                
                 
                 
             }
                
             
             
            
            
	}
        
           for(FollowedUserDetails user :followedUsers){
              
               user.calculateAvegHashtagsPerTweet();
               user.calculateAvegRetweetPerTweet();
               user.calculatePercentageOfTweetsWithHashtag();
               user.calculatePercentageOfTweetsWithURL();
               
             }
        
        
        
    }
    
    
}
