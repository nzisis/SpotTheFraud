package spotthefraud;

/**
 *
 * @authors Nikos Zissis, Sokratis Papadopoulos, George Mihailidis, Anastasios Kostas
 */
public class FollowedUser {
    private int userID; //the unique ID number of each user in Twitter
    //Hashmap<Trend, Integer> //posa tweets exei kanei se kathe trend -an xreiastei
    private int totalNumberOfTweets; //each user's total number of Tweets in our database
    
    public FollowedUser(){
        userID =0;
        totalNumberOfTweets = 0;
    }
    
    public FollowedUser(int userID, int totalNumberOfTweets){
        this.userID  = userID;
        this.totalNumberOfTweets = totalNumberOfTweets;
    }
    
    public void setUserID(int userID){
        this.userID = userID;
    }
    
    public int getUserID(){
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
}
