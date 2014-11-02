/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spotthefraud;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import twitter4j.Query;
import twitter4j.Trends;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 *
 * @author Vromia
 */
public class SpotTheFraud {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
          //The first thing we’ll do in our new file is to store the credentials
            ConfigurationBuilder cb=new ConfigurationBuilder();
            cb.setOAuthConsumerKey("0cc8fkRgUfzX5fYK14m211vhE");
            cb.setOAuthConsumerSecret("45d3sLIiEG0suWxEGBECTWP0tXJL6hJQwqqNCvo04eeGKjL8Al");
            cb.setOAuthAccessToken("43403340-aUeWfSgfYpYSDmoeVzaPXF1aaiBAo3IL7zgIXwahU");
            cb.setOAuthAccessTokenSecret("Tc40irSU8G15IvvEu6EuVjsaM1xQAVCDzJoaSTnxYVFOI");
            
            Twitter twitter=new TwitterFactory(cb.build()).getInstance();
            
           TweetsControl a=new TweetsControl(twitter);
           a.GatherTopTweets();
            
            
            
         
        
    }
    
}
