import twitter4j.Status;
import java.io.*;
import java.util.Date;

/**
 * @author Alexander Hamme
 * @author Sasha Fedchin
 */
public class AnalizeTweets {

    private static SeparateChainingHashST<HashThis,User> tweets=new SeparateChainingHashST<>(100000);
                                                    // tweets arranged in a hash table.
    private static final int AVERAGE_AWAKE=17; //average time (in hours) americans are awake (Wikipedia)
    private static final int MIN_SLEEP=24-AVERAGE_AWAKE; //if the difference between the time two tweets were posted is less than this,
                            //the user is considered to be awake all the time between the two tweets
    /**
     * Load tweets from a saved file of Status objects.
     * Appends to current this.tweets all tweets not already stored.
     * @param filename Name of input file.
     */

    /** loads tweets
    *@param filename a file to read from
    */
    private static void load(String filename) throws IOException, FileNotFoundException {

        ObjectInputStream objInput = null;
        FileInputStream fileInput = null;
        
        try {
            
            fileInput = new FileInputStream(filename);
            objInput = new ObjectInputStream(fileInput);
            
            while(true) {
                Status curr= (Status)objInput.readObject();
                int hashedKey=tweets.hash(new HashThis(curr.getUser().getScreenName()));
                User tree=tweets.getByHashedKey(hashedKey, new HashThis(curr.getUser().getScreenName()));
                if (tree==null) {
                    tree=new User();
                    tree.put(curr.getCreatedAt(),curr);
                    tweets.putByHashedKey(hashedKey,new HashThis(curr.getUser().getScreenName()),tree);
                  

                } else tree.put(curr.getCreatedAt(), curr);
            }
        } catch (EOFException ignored) {
            // Exception breaks the while loop, as expected

        } catch (ClassNotFoundException e) {
            // Unexpected exception
           e.printStackTrace();

        } finally {
            if(objInput != null){
                try {
                    objInput.close();
                } catch (Exception ex){
                    ex.printStackTrace();
                } 
            }
            if (fileInput != null){
                try {
                    fileInput.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Closed files");
        }
    }

    /**
     * Print one particular Status.
     */
    private static void printStatus(Status tweet) {
        if (tweet== null) return;
        // Print out info about the Status
        System.out.println("\n\n@" + tweet.getUser().getScreenName() + " (hashcode:"+tweets.hash( new HashThis(tweet.getUser().getScreenName()))+
                ") - " + tweet.getText());
        System.out.println("date: " + tweet.getCreatedAt());
        System.out.println("Reply to: " + tweet.getInReplyToScreenName());
    }

    public static void main(String args[]) throws Exception {
        load("alltweets.dat");
        for (SequentialSearchST<HashThis,User> i:tweets)
            if (i!=null)
                for (User j:i) {
                    if ((j!=null)&&(j.get().size()>2)) {
                        sleepingTime(j);
                    }
                }
    }

    // Check if 'bot' is in the username

     String checkName(User user) {
        RedBlackBST<Date,Status> tree = user.get();
        String username = tree.iterator().next().getUser().getScreenName();
        if (username.toLowerCase().contains("bot")) 
            return true;
        return false;
    }
    
    /**
     * This method tries to identify users that seem like they don't sleep
     * @param user
     */
    private static void sleepingTime(User user) {
        int maxAwake=MIN_SLEEP;
        int currentAwake=MIN_SLEEP;
        RedBlackBST<Date,Status> tree=user.get();
        Status prev=null;
        String userName="";
        for (Status status:tree) {
            if (prev!=null) {
                long diffInMiliSec=status.getCreatedAt().getTime()-prev.getCreatedAt().getTime();
                if ((double)(diffInMiliSec/1000/60)/60 <= MIN_SLEEP) {
                    currentAwake += (double)(diffInMiliSec/1000/60)/60;
                    if (currentAwake>maxAwake)
                        maxAwake = currentAwake;
                } else
                    currentAwake = MIN_SLEEP;
            } else {
                userName=status.getUser().getScreenName();
            }
            prev=status;
        }
        if (maxAwake>=AVERAGE_AWAKE) {
            user.updateIsBot((double) maxAwake / AVERAGE_AWAKE);
        }
    }
}
