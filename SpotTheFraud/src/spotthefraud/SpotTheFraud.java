package spotthefraud;

import java.net.URISyntaxException;
import twitter4j.JSONException;

/**
 * This project is about spotting users in Twitter that are using the top
 * trending topics in order to spam or get more famous.
 *
 * Here, in the main class, we run the project. Firstly we create an object that
 * # starts the crawling of wanted data from Twitter's REST & Streaming API.
 * Secondly, we analyse the data that we collected and we select 40 users that
 * we then start to follow their activity for 7 days. Finally, we export the
 * data that are coming from the 40 followed users, in order to identify the
 * behaviour of the possible bots and lead to some conclusions regarding it.
 *
 * @authors Nikos Zissis, Sokratis Papadopoulos, George Mihailidis, Anastasios Kostas
 */
public class SpotTheFraud {

    /**
     * Here is where all starts. In main, we call the classes we made for the
     * project.
     *
     * @param args the command line arguments
     * @throws twitter4j.JSONException
     * @throws java.net.URISyntaxException
     */
    public static void main(String[] args) throws JSONException, URISyntaxException {
        
        //TweetsControl crawl=new TweetsControl();
        
        //TweetsAnalyzer analyzeData = new TweetsAnalyzer();
        
        //ExportData exportStatistics = new ExportData();
    }
}
