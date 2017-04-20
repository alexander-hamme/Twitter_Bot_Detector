import twitter4j.Status;
import java.util.Date;

/**
 * @author Alexander Hamme
 * @author Sasha Fedchin
 * 
 * This class contains all the information about the user. It also contains the BST of tweets by this user.
 */

public class User {
    private double isBot; //how possible it is that the user is a bot. If isBot<0 - the user is possibly not a bot,
    //otherwise - it is most likely a bot
    public RedBlackBST<Date, Status> tweets; //all the tweets by this user

    /**
     * sets isBot to defaultValue and initializes the red-black tree
     */
    public User() {
        isBot=1; //basic probability
        tweets=new RedBlackBST<>();
    }

    /**
     * multiplies the isBot variable by multiplier after calculating the probability of an independent reason for user
     * to be a bot
     * @param multiplier value to multiply by
     */
    public void updateIsBot(double multiplier) {
        isBot*=multiplier;
    }

    /**
     * 
     * @return isBot
     */
    public double getIsBot() {
        return isBot;
    }

    /**
     * puts the tweets to the RedBlackTree
     * @param key the key of the value to put into the BST
     * @param val the value itself
     * @return 1 if the tweet is new, 0 if it is already in the tree
     */
    public int put(Date key, Status val) {
        int oldSize=tweets.size();
        tweets.put(key,val);
        return tweets.size()-oldSize;
    }

    /**
     * @return the RedBlackTree
     */
    public RedBlackBST get() {
        return tweets;
    }

    public String userName() {
        return (String)(tweets.iterator().next().getUser().getScreenName());
    }
}
