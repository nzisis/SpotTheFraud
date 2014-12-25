/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package spotthefraud;

import java.util.HashMap;

/**
 *
 * @author Sokratis
 */
public class FollowedUserDetails extends User{
    
    //meros 1
    private int noTweets,noRetweets,noReplies,noMentions,norReceived;
    private double averageRetweetsPerTweet;//numberOfRetweetsReceived/numberOfTweets
    private int noHashtags;//in the users tweets
    private double averageHashtagsPerTweet;//numberOfhashtags/numberOfTweets
    private double percentageOfTweetsWithHashtag;
    private int noURL;
    private double percentageOfTweetsWithURL;
    
    //meros 2
    private int copiedTweets;//Sum of exactly the same tweet without retweets, including the original first tweet. Calculation with Levenstein Distance
    
    //meros 3
    private HashMap<String,Integer> tweetsPerSource;
    
    //meros4
    private int uniqueURLs;//facebook.com/link1, facebook.com/link2 ....
    private int uniqueDomains;//facebook.com, twitter.com, youtube.com....
    
    
    
    public FollowedUserDetails(String id){
        super(id);
        initVariables();
    }
    
    
    
    
    private void initVariables(){
        noTweets=0;
        noRetweets=0;
        noReplies=0;
        noMentions=0;
        norReceived=0;
        noHashtags=0;
        noURL=0;
        averageHashtagsPerTweet=0;
        averageRetweetsPerTweet=0;
        percentageOfTweetsWithHashtag=0;
        percentageOfTweetsWithURL=0;
    }
    
   
    public void setNOTweets(int noTweets){
        
        this.noTweets=noTweets;
    }
    
    public void increaseNORetweets(){
        this.noRetweets+=1;
    }
    
    public void increaseNOReplies(){
        
        this.noReplies+=1;
    }
    
    public void increaseNOMentions(int increament){
     this.noMentions+=increament;   
    }
    
    public void increaseNORReceived(int increament){
        this.norReceived+=increament;
    }
    
    public void increaseNOHashtags(int increament){
     this.noHashtags+=increament;   
    }
    
    public void increaseNOUrls(int increament){
        this.noURL+=increament;
    }
    
    
    public void calculateAvegRetweetPerTweet(){
        this.averageRetweetsPerTweet=norReceived/noTweets;
    }
    
    public void calculateAvegHashtagsPerTweet(){
        
        this.averageHashtagsPerTweet=noHashtags/noTweets;
    }
    
    public void calculatePercentageOfTweetsWithHashtag(){
        if(noHashtags!=0){
        this.percentageOfTweetsWithHashtag=(noTweets/noHashtags)*100;
        }
    }
    
    public void calculatePercentageOfTweetsWithURL(){
        if(noURL!=0){
        this.percentageOfTweetsWithURL=(noTweets/noURL)*100;
        }
    }
    
}
