/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package spotthefraud;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sokratis
 */
public class User {
    
    private int followers,followees,userID;
    private double ratio;//followers/followees
    private String accountAge;
    
    public User(){
        userID = 0;
        followers = 0;
        followees = 0;
    }
    public User (int userID){
        this.userID = userID;
        followers = 0;
        followees = 0;
    }
    
    public User (int userID,int followers,int followees,String accountAge){
        this.userID = userID;
        this.followers = followers;
        this.followees = followees;
        this.accountAge = accountAge;
        ratio = (double) followers/followees;
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

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public double getRatio() {
        ratio = (double) followers/followees;
        return ratio;
    }

    public String getAccountAge() {
        return accountAge;
    }

    public void setAccountAge(String createdAge) {
        
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        
        Date date =new Date();
        
        setAccountAge(createdAge,dateFormat.format(date).toString());
        
    }

   public void setAccountAge(String createdAge,String currentDate){
       SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
       
       Date d1;
       Date d2;
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
    
    
    
     
    
}
