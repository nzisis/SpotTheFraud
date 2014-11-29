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
    private int numberOfTweets,numberOfRetweets,numberOfReplies,numberOfUsersMentioned,numberOfRetweetsReceived;
    private double averageRetweetsPerTweet;//numberOfRetweetsReceived/numberOfTweets
    private int numberOfHashtags;//in the users tweets
    private double averageHashtagsPerTweet;//numberOfhashtags/numberOfTweets
    private int tweetsWithHashtags;//tweetsWithoutHashtags=numberOfTweets-tweetsWithhashtags
    private double percentageOfTweetsWithHashtag;
    private int tweetsWithURL;
    private double percentageOfTweetsWithURL;
    
    //meros 2
    private int copiedTweets;//Sum of exactly the same tweet without retweets, including the original first tweet. Calculation with Levenstein Distance
    
    //meros 3
    private HashMap<String,Integer> tweetsPerSource;
    
    //meros4
    private int uniqueURLs;//facebook.com/link1, facebook.com/link2 ....
    private int uniqueDomains;//facebook.com, twitter.com, youtube.com....
}
