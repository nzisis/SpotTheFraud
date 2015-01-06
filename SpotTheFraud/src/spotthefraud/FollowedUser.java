package spotthefraud;

import java.util.Comparator;
import java.util.HashSet;

/**
 *
 * @authors Nikos Zissis, Sokratis Papadopoulos, George Mihailidis, Anastasios Kostas
 */
public class FollowedUser {
    //private int userID; //the unique ID number of each user in Twitter
    //Hashmap<Trend, Integer> //posa tweets exei kanei se kathe trend -an xreiastei
    private int totalNumberOfTweets; //each user's total number of Tweets in our database
    //private double id;
    private String userID;
    private HashSet<Integer> trendsOccured;
    
    
    public FollowedUser(){
        userID="";   
        totalNumberOfTweets = 0;
        trendsOccured = new HashSet<>();
    }
 
    public FollowedUser(String userID, int totalNumberOfTweets){
        this.userID  = userID;
        this.totalNumberOfTweets = totalNumberOfTweets;
        this.trendsOccured = new HashSet<>();
    }
    
    public void setUserID(String userID){
        this.userID = userID;
    }
    
    public String getUserID(){
        return userID;
    }
    
    public void increaseNumberOfTweets(){
        this.totalNumberOfTweets +=1;
    }
    
    public void setTotalNumberOfTweets(int totalNumberOfTweets){
        this.totalNumberOfTweets = totalNumberOfTweets;
    }
    
    public int getTotalNumberOfTweets(){
        return totalNumberOfTweets;
    }
    
    public HashSet<Integer> getTrendsOccured(){
        return trendsOccured;
    }
    
    public void addTrend(int numberOfTrend){
        trendsOccured.add(numberOfTrend);
    }
    
    public void printTrendsOccured() {
        for(Integer occ : trendsOccured){
            System.out.println("Trend: " + occ + "\n");
        }
    }
   
}
