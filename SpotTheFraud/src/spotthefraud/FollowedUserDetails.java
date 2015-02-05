/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spotthefraud;

import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
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
    private HashSet<String> uniqueDomains;
    private int numberOfUniqueDomains;//facebook.com, twitter.com, youtube.com....
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
        uniqueDomains = new HashSet<>();
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
        if (noTweets != 0) {
            this.averageRetweetsPerTweet = (double) noRetweets / noTweets;
        } else {
            this.averageRetweetsPerTweet = 0;
        }
    }

    public void calculateAvegHashtagsPerTweet() {
        if (noTweets != 0) {
            this.averageHashtagsPerTweet = (double) noHashtags / noTweets;
        } else {
            this.averageHashtagsPerTweet = 0;
        }
    }

    public void calculatePercentageOfTweetsWithHashtag() {
        if (noTweets != 0) {
            double noHash = (double) noHashtags;
            this.percentageOfTweetsWithHashtag = noHash / noTweets;
            this.percentageOfTweetsWithHashtag *= 100;
        } else {
            this.percentageOfTweetsWithHashtag = 0;
        }
    }

    public void calculatePercentageOfTweetsWithURL() {
        if (noTweets != 0) {
            double URLs = (double) noURL;
            this.percentageOfTweetsWithURL = (URLs / noTweets);
            this.percentageOfTweetsWithURL *= 100;
        } else {
            this.percentageOfTweetsWithURL = 0;
        }
    }

    public void increaseSourceCount(String newSource) {

        org.jsoup.nodes.Document doc = Jsoup.parse(newSource);
        Element link = doc.select("a").first();
        String sourceName = link.text(); // "example""            

        if (tweetsPerSource.containsKey(sourceName)) {
            tweetsPerSource.put(sourceName, tweetsPerSource.get(sourceName) + 1);
        } else {
            tweetsPerSource.put(sourceName, 1);
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
     *
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
                    cleanedTweet += newElements[j] + " ";
                }
            }
            cleanedTweet = cleanedTweet.replaceAll("https?://\\S+\\s?", "");
            tweets.add(cleanedTweet);
        } else {
            tweets.add(newTweet);
        }
    }

    public void calculateUniqueDomains() throws URISyntaxException {
        for (String url : uniqueURLs) {
            uniqueDomains.add(getDomainName(url));
        }
        numberOfUniqueDomains = uniqueDomains.size();
    }

    public static String getDomainName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }

    /**
     * Using the levenshtein distance it calculates how many tweets are actually
     * duplicates.
     */
    public void calculateCopies() {

        if (!tweets.isEmpty()) {
            HashSet<String> uniqueTexts = new HashSet<>(); //used to store the unique texts of tweets
            boolean flag = false;

            uniqueTexts.add(tweets.get(0));

            for (int i = 1; i < tweets.size(); i++) {

                for (String text : uniqueTexts) {

                    int distance = LevenshteinDistance.computeDistance(tweets.get(i), text);
                    double normalized_distance = (double) distance / (tweets.get(i).length() + text.length());

                    if (normalized_distance < 0.1) {
                        copiedTweets++;
                        flag = true;
                        break;
                    }
                }

                if (!flag) {
                    uniqueTexts.add(tweets.get(i));
                    flag = false;
                }
            }
        } else {
            copiedTweets = 0;
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
        return numberOfUniqueDomains;
    }

    public double getUrlRatio() {
        return urlRatio;
    }

    public void printAll() {
        //NumberFormat nf = NumberFormat.getPercentInstance();
        //nf.setMaximumFractionDigits(2);
        //nf.setMinimumFractionDigits(2);
        //nf.setRoundingMode(RoundingMode.HALF_UP);

        this.print();
        System.out.println("====== 1o MEROS ======");
        System.out.println("Number of Tweets: " + this.noTweets);
        System.out.println("Number of Retweets: " + this.noRetweets);
        System.out.println("Number of Replier: " + this.noReplies);
        System.out.println("Number of Mentions: " + this.noMentions);
        System.out.println("Number of RT received:" + this.norReceived);
        System.out.println("Average Retweets Per Tweet: " + this.averageRetweetsPerTweet);
        System.out.println("Number of Hashtags: " + this.noHashtags);
        System.out.println("Average Hashtags Per Tweet: " + this.averageHashtagsPerTweet);
        System.out.println("Percentage of tweets with hashtags: " + this.percentageOfTweetsWithHashtag);
        System.out.println("Number of URLs: " + this.noURL);
        System.out.println("Percentage of tweets with URL: " + this.percentageOfTweetsWithURL);

        System.out.println("====== 2o MEROS ======");
        System.out.println("Number of copied tweets: " + this.copiedTweets);
        //System.out.println("ArrayList of tweets: ");
        //for (int i = 0; i < tweets.size(); i++) {
        // System.out.println("Tweet[" + i + "]: " + tweets.get(i));
        //}

        System.out.println("====== 3o MEROS ======");
        System.out.println("Most Frequent Source: " + this.mostFrequentSource);

        System.out.println("====== 4o MEROS ======");
        System.out.println("Unique URLs: " + this.uniqueURLs.size());
        System.out.println("Number of URLs: " + this.noURL);
        System.out.println("URL Ratio: " + this.urlRatio);
        System.out.println("Unique Domains: " + this.numberOfUniqueDomains);
    }
}
