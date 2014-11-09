package spotthefraud;

/**
 * This class is used for describing a trending topic. 
 * It stores all its characteristics that are used further on, during the program.
 * @authors Nikos Zissis, Sokratis Papadopoulos, George Mihailidis, Anastasios Kostas
 */
public class TopTrendingTopic {
    private String name;    
    private String date,Arrivaltime,FinishTime; //svisimo sti mapa
    private int arrival_time; //time that the trend topic arrives from Twitter API (measured in # of 5mins window)
    private int  finish_time; //time that the trend stops appearing in top 10 trends in Twitter API
    private boolean active; //shows if the trend is active: in the list of top10 trends || finish time < 24= (2 hours)
    
    /**
     * Constructor. 
     * It initializes a trend topic.
     * @param name
     * @param arrival_time 
     */
    public TopTrendingTopic(String name,int arrival_time){
        this.name=name;
        active=true;
        this.arrival_time=arrival_time;
        this.finish_time=arrival_time+1;
    }
    
    /**
     * Constructor. 
     * It initializes a trend topic.
     * @param name
     * @param date
     * @param Arrivaltime 
     */
    public TopTrendingTopic(String name,String date,String Arrivaltime){
        this.name=name;
        active=true;
        this.date=date;
        this.Arrivaltime=FinishTime=Arrivaltime;
    }
    
    /**
     * Sets the finish time of the trend.
     * It's called when the topic is no longer in the top 10 trending list of Twitter API.
     * @param finish 
     */
    public void setFinishTime(int finish){
        this.finish_time=finish;      
    }
    
    /**
     * changes the activity to non-active 
     * means that the finish time of the trend 
     * is more than 2 hours older than current time
     */
    public void changeActivity(){
        active=false;
    }
    
    /**
     * @return the name of the trending topic
     */
    public String getName(){
        return name;
    }
    
    /**
     * increases the finish time up by 1 (5min window)
     * time is represented in # of 5-minutes-window passed.
     * @param newValue 
     */
    public void increaseFinishTime(int newValue){
      this.finish_time=newValue+1;
      //this.FinishTime+=1;  
    }
    
    /**
     * Gets whether the trend is active or not
     * @return 'true' if trend is active or 'false' if not
     */
    public boolean getActivity(){
        return active;
    }
    
    /**
     * @return the arrival time of the topic
     */
    public int getArrivalTime(){
        return arrival_time;
    }
  
    /**
     * @return the finish time of the topic
     */
    public int getFinishTime(){
        return finish_time;
    }
}
