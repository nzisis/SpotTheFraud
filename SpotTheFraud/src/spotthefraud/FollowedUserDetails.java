/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spotthefraud;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import org.shortdistance.LevenshteinDistance;

/**
 * A representation of a Twitter user (extends User class). It stores many data
 * relevant to a user account. It's used for storing the data that came from 
 * the7 days tracking of 40 selected users.
 * 
 * @authors Nikos Zissis, Sokratis Papadopoulos, George Mihailidis, Anastasios Kostas
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
    private String mostFrequentSource;

    //meros4
    private double urlRatio;
    private int uniqueDomains;//facebook.com, twitter.com, youtube.com....
    private HashSet<String> uniqueURLs;

    public FollowedUserDetails(String id) {
        super(id);
        initVariables();
    }

    public FollowedUserDetails(String userID, int followers, int followees, String accountAge) {
        super(userID, followers, followees, accountAge);
        initVariables();
    }

    /**
     * Initializing basic variables for Users that will later be changed
     */
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
        mostFrequentSource = "No one";
        urlRatio = 0;
        tweets = new ArrayList<>();
        copiedTweets = 0;
    }

    public void setNOTweets(int noTweets) {
        this.noTweets = noTweets;
    }

    public void increaseNOTweets(int noTweets) {
        this.noTweets += noTweets;
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
        this.averageRetweetsPerTweet = (double) norReceived / noTweets;
    }

    public void calculateAvegHashtagsPerTweet() {
        this.averageHashtagsPerTweet = (double) noHashtags / noTweets;
    }

    public void calculatePercentageOfTweetsWithHashtag() {
        if (noHashtags != 0) {
            this.percentageOfTweetsWithHashtag = (double) (noHashtags / noTweets) * 100;
        }
    }

    public void calculatePercentageOfTweetsWithURL() {
        if (noURL != 0) {
            this.percentageOfTweetsWithURL = (double) (noURL / noTweets) * 100;
        }
    }

    public void increaseSourceCount(String newSource) {
        if (tweetsPerSource.containsKey(newSource)) {
            tweetsPerSource.put(newSource, tweetsPerSource.get(newSource) + 1);
        } else {
            tweetsPerSource.put(newSource, 1);
        }
    }

    /**
     * calculates the most frequent source for the user after examining all the
     * sources that he used.
     */
    public void calculateMostFrequentSource() {
        Integer maxFrequency = -1;
        Iterator it = tweetsPerSource.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry user = (Map.Entry) it.next();

            if (maxFrequency < (Integer) user.getValue()) {
                maxFrequency = (Integer) user.getValue();
                mostFrequentSource = (String) user.getKey();
            }
        }
    }

    public void addUniqueURL(String url) {
        uniqueURLs.add(url);
    }

    public void calculateURLsRatio() {
        if (noURL != 0) {
            urlRatio = (double) uniqueURLs.size() / noURL;
        }
    }

    /**
     * Cleans tweets text from URLs and RT@ and keeps it pure text.
     * @param newTweet 
     */
    public void addAndProcessTweet(String newTweet) {

        String[] elements = newTweet.split("@");
        //@gregclermont no, there is not. ^TS
        if (elements.length != 0) {
            String cleanedTweet = "";
            for (int i = 0; i < elements.length; i++) {
                String[] newElements = elements[i].split(" ");
                for (int j = 1; j < newElements.length; j++) {
<<<<<<< HEAD
                    cleanedTweet += newElements[j];
                    cleanedTweet += " ";
=======
                    cleanedTweet += newElements[j] + " ";
>>>>>>> origin/master
                }
            }
            cleanedTweet = cleanedTweet.replaceAll("https?://\\S+\\s?", "");
            tweets.add(cleanedTweet);
        } else {
            tweets.add(newTweet);
        }
    }

    /**
     * Using the levenshtein distance it calculates how many tweets are actually
     * duplicates. 
     */
    public void calculateCopies() {
        //System.out.println("Ksekinw calculate copies");
        int size = tweets.size();
        //System.out.println("To plithos tweets: " + size);
        for (int i = 0; i < size; i++) {
            //System.out.println("------------------------------- arxi eksw loop: " + i);
            for (int j = i + 1; j < size; j++) {
                //System.out.println("========= arxi mesa loop: " + j);
                int distance = LevenshteinDistance.computeDistance(tweets.get(i), tweets.get(j));
                double normalized_distance = (double) distance / (tweets.get(i).length() + tweets.get(j).length());
                if (normalized_distance < 0.1) {
                    copiedTweets++;
                }
                //System.out.println("telos mesa loop: " + j);
            }
            //System.out.println("telos eksw loop: " + i);
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
        return mostFrequentSource;
    }

    public int getUniqueDomains() {
        return uniqueDomains;
    }

    public double getUrlRatio() {
        return urlRatio;
    }

    public void printAll() {
        NumberFormat nf = NumberFormat.getPercentInstance();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        nf.setRoundingMode(RoundingMode.HALF_UP);

        this.print();
        System.out.println("====== 1o MEROS ======");
        System.out.println("Number of Tweets: " + this.noTweets);
        System.out.println("Number of Retweets: " + this.noRetweets);
        System.out.println("Number of Replier: " + this.noReplies);
        System.out.println("Number of Mentions: " + this.noMentions);
        System.out.println("Number of RT received:" + this.norReceived);
        System.out.println("Average Retweets Per Tweet: " + nf.format(this.averageRetweetsPerTweet));
        System.out.println("Number of Hashtags: " + this.noHashtags);
        System.out.println("Average Hashtags Per Tweet: " + nf.format(this.averageHashtagsPerTweet));
        System.out.println("Percentage of tweets with hashtags: " + nf.format(this.percentageOfTweetsWithHashtag));
        System.out.println("Number of URLs: " + this.noURL);
        System.out.println("Percentage of tweets with URL: " + nf.format(this.percentageOfTweetsWithURL));

        System.out.println("====== 2o MEROS ======");
        System.out.println("Number of copied tweets: " + this.copiedTweets);
        System.out.println("ArrayList of tweets: ");
        for (int i = 0; i < tweets.size(); i++) {
            System.out.println("Tweet[" + i + "]: " + tweets.get(i));
        }

        System.out.println("====== 3o MEROS ======");
        System.out.println("Most Frequent Source: " + this.mostFrequentSource);
//        System.out.println("HashMap of tweetsPerSource: ");
//        for(int i=0; i<tweetsPerSource.size(); i++){
//            System.out.println("Source[" + i + "]: " + tweetsPerSource.get(i).toString());
//        }

        System.out.println("====== 4o MEROS ======");
        System.out.println("URL Ratio: " + this.urlRatio);
        System.out.println("Unique Domains: " + this.uniqueDomains);
        //have to print also the unique URLs
    }
}
