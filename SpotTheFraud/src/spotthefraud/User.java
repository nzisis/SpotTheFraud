package spotthefraud;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import static java.lang.String.format;
import static java.lang.String.format;
import static java.lang.String.format;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * It's a basic representation of a Twitter user. It stores the basic of a user
 * and class FollowedUserDetails is its extention. It actually store data asked
 * in 4th-A part of project.
 *
 * @authors Nikos Zissis, Sokratis Papadopoulos, George Mihailidis, Anastasios Kostas
 */
public class User {

    private String userID;
    private int followers;
    private int followees;
    private double ratio; //followers/followees
    private String accountAge;
    private Long age;

    public User() {
        userID = "";
        followers = 0;
        followees = 0;
    }

    public User(String userID) {
        this.userID = userID;
        followers = 0;
        followees = 0;
    }

    public User(String userID, int followers, int followees, String accountAge) {
        this.userID = userID;
        this.followers = followers;
        this.followees = followees;
        this.accountAge = accountAge;
        this.age = setAccountAge(accountAge);
        ratio = (double) followers / followees;
    }

    public int getFollowers() {
        return followers;
    }

    public void setFollowers(int followers) {
        this.followers = followers;
    }

    public int getFollowees() {
        return followees;
    }

    public void setFollowees(int followees) {
        this.followees = followees;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public double getRatio() {
        if (followees != 0) {
            ratio = (double) followers / followees;
        } else {
            ratio = 0;
        }
        return ratio;

    }

    public Long getAccountAge() {
        return age;
    }

    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    public Long setAccountAge(String createdAge) {

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzzz yyyy", Locale.ENGLISH);
            Date accountCreationTime = dateFormat.parse(createdAge);
            Date currentTime = new Date();
            dateFormat.format(currentTime);
            
            return getDateDiff(accountCreationTime, currentTime, TimeUnit.DAYS);

        } catch (ParseException ex) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public void print() {

            NumberFormat nf = NumberFormat.getPercentInstance();
            nf.setMaximumFractionDigits(2);
            nf.setMinimumFractionDigits(2);
            nf.setRoundingMode(RoundingMode.HALF_UP);
            
            
            System.out.println(this.userID + "\t" + this.followers + "\t" + this.followees + "\t" + nf.format(this.ratio) + "\t" + this.age );
            /*
            
            System.out.println("-------------");
            System.out.println("User ID: " + this.userID);
            System.out.println("Followers: " + this.followers);
            System.out.println("Followees: " + this.followees);
            System.out.println("Ratio: " + this.ratio);
            //System.out.println("Ratio: " + nf.format(this.ratio));
            System.out.println("Acount Age: " + this.age + " days"); */

    }
    
    /**
     * used for write the data for 4th partA for all users
     */
    public void writeToFile() {
        
        try {
            Scanner s = new Scanner(new File("allNumbers.txt"));
            int[] array = new int[2054836];
            int temp=1;
            int count=1;
            for (int i = 0; i < array.length; i++){
                array[i] = s.nextInt();
                if(array[i]==temp){
                    count++;
                } else{
                    System.out.println(temp + " " + count);
                    temp = array[i];
                    count=1;
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SpotTheFraud.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
}
