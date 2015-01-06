/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spotthefraud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import org.shortdistance.LevenshteinDistance;

/**
 *
 * @author Sokratis
 */
public class FollowedUserDetails extends User {

    //meros 1
    private int noTweets, noRetweets, noReplies, noMentions, norReceived;
    private double averageRetweetsPerTweet;//numberOfRetweetsReceived/numberOfTweets
    private int noHashtags;//in the users tweets
    private double averageHashtagsPerTweet;//numberOfhashtags/numberOfTweets
    private double percentageOfTweetsWithHashtag;
    private int noURL;
    private double percentageOfTweetsWithURL;

    //meros 2
    private int copiedTweets;//Sum of exactly the same tweet without retweets, including the original first tweet. Calculation with Levenstein Distance
    private ArrayList<String> tweets;

    //meros 3
    private HashMap<String, Integer> tweetsPerSource;
    private String mostFrequenltySource;

    //meros4
    // private int uniqueURLs;//facebook.com/link1, facebook.com/link2 ....
    private int uniqueDomains;//facebook.com, twitter.com, youtube.com....
    private HashSet<String> uniqueURLs;
    private double urlRatio;

    public FollowedUserDetails(String id) {
        super(id);
        initVariables();
    }

    private void initVariables() {
        noTweets = 0;
        noRetweets = 0;
        noReplies = 0;
        noMentions = 0;
        norReceived = 0;
        noHashtags = 0;
        noURL = 0;
        averageHashtagsPerTweet = 0;
        averageRetweetsPerTweet = 0;
        percentageOfTweetsWithHashtag = 0;
        percentageOfTweetsWithURL = 0;
        tweetsPerSource = new HashMap<>();
        uniqueURLs = new HashSet<>();
        mostFrequenltySource = "No one";
        urlRatio = 0;
        tweets = new ArrayList<>();
        copiedTweets = 0;
    }

    public void setNOTweets(int noTweets) {
        this.noTweets = noTweets;
    }

    public void increaseNORetweets() {
        this.noRetweets += 1;
    }

    public void increaseNOReplies() {

        this.noReplies += 1;
    }

    public void increaseNOMentions(int increament) {
        this.noMentions += increament;
    }

    public void increaseNORReceived(int increament) {
        this.norReceived += increament;
    }

    public void increaseNOHashtags(int increament) {
        this.noHashtags += increament;
    }

    public void increaseNOUrls(int increament) {
        this.noURL += increament;
    }

    public void calculateAvegRetweetPerTweet() {
        this.averageRetweetsPerTweet = norReceived / noTweets;
    }

    public void calculateAvegHashtagsPerTweet() {
        this.averageHashtagsPerTweet = noHashtags / noTweets;
    }

    public void calculatePercentageOfTweetsWithHashtag() {
        if (noHashtags != 0) {
            this.percentageOfTweetsWithHashtag = (noTweets / noHashtags) * 100;
        }
    }

    public void calculatePercentageOfTweetsWithURL() {
        if (noURL != 0) {
            this.percentageOfTweetsWithURL = (noTweets / noURL) * 100;
        }
    }

    public void increaseSourceCount(String newSource) {
        if (tweetsPerSource.containsKey(newSource)) {
            tweetsPerSource.put(newSource, tweetsPerSource.get(newSource) + 1);
        } else {
            tweetsPerSource.put(newSource, 1);
        }
    }

    public void calculateMostFrequentSource() {
        Integer maxFrequency = -1;
        Iterator it = tweetsPerSource.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry user = (Map.Entry) it.next();

            if (maxFrequency < (Integer) user.getValue()) {
                maxFrequency = (Integer) user.getValue();
                mostFrequenltySource = (String) user.getKey();
            }
        }

    }

    public void addUniqueURL(String url) {

        uniqueURLs.add(url);
    }

    public void calculateURLsRatio() {
        if (noURL != 0) {
            urlRatio = uniqueURLs.size() / noURL;
        }
    }

    public void addAndProcessTweet(String newTweet) {

        String elements[] = newTweet.split("@");
        //@gregclermont no, there is not. ^TS
        if (elements.length != 0) {
            String cleanedTweet = "";
            for (int i = 0; i < elements.length; i++) {
                String newElements[] = elements[i].split(" ");
                for (int j = 1; j < newElements.length; j++) {
                    cleanedTweet += newElements[1];
                }
            }
            tweets.add(cleanedTweet);

        } else {
            tweets.add(newTweet);
        }

    }

    public void calculateCopies() {
        int size = tweets.size();
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                int distance = LevenshteinDistance.computeDistance(tweets.get(i), tweets.get(j));
                double normalized_distance = (double) distance / (tweets.get(i).length() + tweets.get(j).length());
                if (normalized_distance < 0.1) {
                    copiedTweets++;
                }
            }
        }

    }

    public int getNoTweets() {
        return noTweets;
    }

    public int getNoRetweets() {
        return noRetweets;
    }

    public int getNoReplies() {
        return noReplies;
    }

    public int getNoMentions() {
        return noMentions;
    }

    public int getNorReceived() {
        return norReceived;
    }

    public double getAverageRetweetsPerTweet() {
        return averageRetweetsPerTweet;
    }

    public int getNoHashtags() {
        return noHashtags;
    }

    public double getAverageHashtagsPerTweet() {
        return averageHashtagsPerTweet;
    }

    public double getPercentageOfTweetsWithHashtag() {
        return percentageOfTweetsWithHashtag;
    }

    public int getNoURL() {
        return noURL;
    }

    public double getPercentageOfTweetsWithURL() {
        return percentageOfTweetsWithURL;
    }

    public int getCopiedTweets() {
        return copiedTweets;
    }

    public String getMostFrequenltySource() {
        return mostFrequenltySource;
    }

    public int getUniqueDomains() {
        return uniqueDomains;
    }

    public double getUrlRatio() {
        return urlRatio;
    }
    
    
    
}
