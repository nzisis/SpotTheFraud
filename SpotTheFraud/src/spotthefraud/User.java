package spotthefraud;

import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * It's a basic representation of a Twitter user. It stores the basic of a user
 * and class FollowedUserDetails is its extention.
 * It actually store data asked in 4th-A part of project.
 *
 * @authors Nikos Zissis, Sokratis Papadopoulos, George Mihailidis, Anastasios Kostas
 */
public class User {

    private String userID;
    private int followers;
    private int followees;
    private double ratio; //followers/followees
    private String accountAge;

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
        //setAccountAge(accountAge);
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
        ratio = (double) followers / followees;
        return ratio;
    }

    public String getAccountAge() {
        return accountAge;
    }

    public void setAccountAge(String createdAge) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        setAccountAge(createdAge, dateFormat.format(date).toString());
    }

    public void setAccountAge(String createdAge, String currentDate) {
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

        Date d1, d2;
        try {
            d1 = format.parse(createdAge);
            d2 = format.parse(currentDate);

            long diff = d2.getTime() - d1.getTime();

            long diffSeconds = diff / 1000 % 60;
            long diffMinutes = diff / (60 * 1000) % 60;
            long diffHours = diff / (60 * 60 * 1000) % 24;
            long diffDays = diff / (24 * 60 * 60 * 1000);

            StringBuilder sb = new StringBuilder();
            sb.append(diffDays).append("days, ").append(diffHours).append("hours, ").append(diffMinutes).append("minutes, ").append(diffSeconds).append("seconds.");

            this.accountAge = sb.toString();
        } catch (ParseException ex) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void print() {
        NumberFormat nf = NumberFormat.getPercentInstance();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        nf.setRoundingMode(RoundingMode.HALF_UP);

        System.out.println("-------------");
        System.out.println("User ID: " + this.userID);
        System.out.println("Followers: " + this.followers);
        System.out.println("Followees: " + this.followees);
        System.out.println("Ratio: " + nf.format(this.ratio));
        System.out.println("Acount Age: " + this.accountAge);
    }
}
