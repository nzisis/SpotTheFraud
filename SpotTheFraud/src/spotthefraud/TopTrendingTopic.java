package spotthefraud;

/**
 * This class is used for describing a trending topic. It stores all its
 * characteristics that are used further on, during the program.
 *
 * @authors Nikos Zissis, Sokratis Papadopoulos, George Mihailidis, Anastasios Kostas
 */
public class TopTrendingTopic {

    private String name;
    private int arrival_time; //time that the trend topic arrives from Twitter API (measured in # of 5mins window)
    private int finish_time; //time that the trend stops appearing in top 10 trends in Twitter API

    /**
     * Constructor of a trending topic. It initialises basic variables of
     * a trending topic.
     *
     * @param name  
     * @param arrival_time 
     */
    public TopTrendingTopic(String name, int arrival_time) {
        this.name = name;
        this.arrival_time = arrival_time;
        this.finish_time = arrival_time + 1;
    }

    /**
     * Sets the finish time of the trend. It's called when the topic is no
     * longer in the top 10 trending list of Twitter API.
     *
     * @param finish
     */
    public void setFinishTime(int finish) {
        this.finish_time = finish;
    }

    /**
     * increases the finish time up by 1 (5min window) time is represented in #
     * of 5-minutes-window passed.
     *
     * @param newValue
     */
    public void increaseFinishTime(int newValue) {
        this.finish_time = newValue + 1;
        //this.FinishTime+=1;  
    }
    
    //==== GETTERS =====
    
    public String getName() {
        return name;
    }

    public int getArrivalTime() {
        return arrival_time;
    }

    public int getFinishTime() {
        return finish_time;
    }
}
