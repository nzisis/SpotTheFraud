/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spotthefraud;

import org.shortdistance.LevenshteinDistance;



/**
 *
 * @author Vromia
 */
public class SpotTheFraud {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
       
       //TweetsControl a=new TweetsControl();
       //TweetsAnalyzer analyzer=new TweetsAnalyzer();
       //analyzer.startTrackingUsersTweets();
        String s1="Hi Nick",s2="Hi Mick";
       int dis =LevenshteinDistance.computeDistance(s1,s2);
      
        //System.out.println((double)dis/(s1.length()+s2.length()));
        
    }
    
}
