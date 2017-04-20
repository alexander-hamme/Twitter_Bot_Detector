import twitter4j.Status;
import java.util.Date;

/**
 * @author Alexander Hamme
 * @author Sasha Fedchin
 * 
 * This class contains information about the user, including a BST of this user's tweets.
 */

public class User {
    private double isBot; // probability of the user being a bot. 
    public RedBlackBST<Date, Status> tweets; //all the tweets by this user

    /**
     * sets isBot to defaultValue and initializes the red-black tree
     */
    public User() {
        isBot=1; //basic probability
        tweets=new RedBlackBST<>();
    }

    /**
     * @param multiplier value to multiply by
     */
    public void updateIsBot(double multiplier) {
        isBot *= multiplier;
    }

    /**
     * @return isBot
     */
    public double getIsBot() {
        return isBot;
    }

    /**
     * put tweets in the RedBlackTree
     * @param key: key of the value to put into BST
     * @param val: value itself
     * @return 1 if the tweet is new, 0 if it is already contained in the tree
     */
    public int put(Date key, Status val) {
        int oldSize=tweets.size();
        tweets.put(key,val);
        return tweets.size()-oldSize;
    }

    /**
     * @return RedBlackTree
     */
    public RedBlackBST get() {
        return tweets;
    }

    public String userName() {
        return (String)(tweets.iterator().next().getUser().getScreenName());
    }
}
