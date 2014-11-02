/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spotthefraud;

/**
 *
 * @author Vromia
 */
public class TopTrendingTopic {
    
    private String name;
    private String date,Arrivaltime,FinishTime;
    private int arrival_time;
    private int  finish_time;
    private boolean active;
    
    public TopTrendingTopic(String name,int arrival_time){
        
        this.name=name;
        active=true;
        this.arrival_time=arrival_time;
        this.finish_time=arrival_time+1;
       
    }
    
    
    public TopTrendingTopic(String name,String date,String Arrivaltime){
        this.name=name;
        active=true;
        this.date=date;
        this.Arrivaltime=FinishTime=Arrivaltime;
        
    }
    
    public void setFinishTime(int finish){
        this.finish_time=finish;
                
    }
    
    public void changeActivity(){
        active=false;
    }
    
    public String getName(){
        return name;
    }
    
    public void increaseFinishTime(){
      
      this.FinishTime+=1;  
    }
    
    public boolean getActivity(){
        return active;
    }
    public int getArrivalTime(){
        return arrival_time;
    }
    public int getFinishTime(){
        return finish_time;
    }
    
}
