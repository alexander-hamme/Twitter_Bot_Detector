/**
 * @author Alexander Hamme
 * @author Sasha Fedchin
 * 
 * Class to support search of Twitter's tweets using twitter4j API.
 * Concatenates tweets to a file.
 */

import twitter4j.*;

import java.io.*;
import java.util.*;

/**
 * Constructing a GatherTweets object will perform a single search
 * and store them in a Hash Table by userID.
 *
 */
public class GatherTweets {
	private static final int MAXTWEETS = 500; // max tweets at once
	private static final int GATHER_DELAY = 60; //delay between two calls to collectTweets (in seconds)
    private static final int EXCEPTION_DELAY = 60*30; //15 mins, the delay to wait if twitter shuts down the flow of tweets
	private static final int RADIUS= 20*(int)(6*(1.60934)/Math.sqrt(Math.PI));//default radius (in km) of locations searched.
	// Currently the search area will be 720 miles squared, the average area of a town in the USA (multiplied by 10 to get more data) according to Wikipedia.
    private static int SAVE_COUNTER = 10;
    private static String FILE_NAME = "alltweets.dat";
    private static String BACKUP_FILE_NAME = "alltweets2.dat";
    private static int FAIL_COUNTER = 2;
    private static int toCollect; // number of tweets to be collected
	private static Location[] locs; //Locations to collect tweets from. The number of tweets collected from each city will be proportional to its population.
	private static int collected; //number of tweets collected
    private static int currLoc; //the current location from where the tweets are currently being collected, represented by the index in the Location list.
    private static SeparateChainingHashST<HashThis,User> tweets; // tweets arranged in a hash table.
    private static Query query; // query that generated the tweets
	private Timer timer; // timer that allows the app to gather tweets every n minutes
    private static int counter; // Increments each round of collectTweets, after every 10 collections, save to file - ensures data is saved in case process is interuppted
    private static int failureCounter; // Increment each time Twitter gives 0 responses - at a certain number, wait for a delay before requesting again

    /**
     * Retrieve tweets based on typical Twitter search string.
     * @param query valid Query object
     * @param toCollectParam number of tweets to get (max 100)
     * @param newLocs  locations to collect tweets from. Location array should not be changed after being used in
     *              this constructor. PRECOND: locs!=null
     */
    public GatherTweets(Query query,int toCollectParam, Location[] newLocs) {
        counter=0;
        this.query=query;
		toCollect=Math.abs(toCollectParam); //in case it is negative
        locs = newLocs;
        currLoc=0;
        query.setGeoCode(locs[currLoc].getLocation(),RADIUS,Query.KILOMETERS);
        query.setResultType(Query.ResultType.recent);
        timer=new Timer();
        timer.schedule(new CollectTweetsFurther(),(long)1000*GATHER_DELAY);
	}

    /**
     * Class containing a method that decides how to collect the next portion of tweets
     * Handles potential shutdowns from Twitter and decides how long to wait before requesting more tweets
     */
	public class CollectTweetsFurther extends TimerTask {
        @Override
        public void run() {
            if (collected<toCollect) {     
                int tmp=collectTweets(MAXTWEETS);
                collected+=tmp;
                if (tmp!=0) {
                    failureCounter = 0;
                    timer.schedule(new CollectTweetsFurther(),(long)1000*GATHER_DELAY);
                } else {
                    failureCounter++;
                    if (failureCounter>FAIL_COUNTER)
                        timer.schedule(new CollectTweetsFurther(),(long)1000*EXCEPTION_DELAY);
                    else
                        timer.schedule(new CollectTweetsFurther(),(long)1000*GATHER_DELAY);
                }
            } else { //no tweets needed from this location
                save(FILE_NAME);
            }
        }
    }

    /**
     * Collect tweets from one particular location
     * @param numtweets number of tweets to gather
     * @return the number of new tweets collected
     */
    private int collectTweets(int numtweets) {
        query.setCount(numtweets);
        query.setGeoCode(locs[currLoc].getLocation(),RADIUS,Query.KILOMETERS);
        currLoc=(++currLoc)%locs.length;
        Twitter twitter = new TwitterFactory().getInstance();
        QueryResult result = null;
        int newTweets=0; //how many NEW tweets the twitter gave
        try {
            result = twitter.search(query);
            while(!result.getTweets().isEmpty()) { // store retrieved tweets
                Status curr=result.getTweets().remove(0);
                int hashedKey=tweets.hash(new HashThis(curr.getUser().getScreenName()));
                // Check if there are already saved tweets for this user
                User tree=tweets.getByHashedKey(hashedKey,new HashThis(curr.getUser().getScreenName()));
                if (tree==null) { // If not, create a new User
                    tree=new User();
                    tree.put(curr.getCreatedAt(),curr);
                    tweets.putByHashedKey(hashedKey,new HashThis(curr.getUser().getScreenName()),tree);
                    newTweets++;
                } else
                    newTweets+=tree.put(curr.getCreatedAt(), curr);
            }
        }
        catch (TwitterException te) { // If Twitter raises an error, e.g. because program requested too many tweets at once
            te.printStackTrace();
            System.out.println("Failed to search tweets: " + te.getMessage());
            //timer.schedule(new CollectTweetsFurther(),(long)1000*EXCEPTION_DELAY);
        }
        System.out.println("\n In total we have "+collected+" Just now we collected "+newTweets+" tweets for location= "+query.getGeocode()+" request= "+query.getQuery()+"\n");
        

        counter++;
        if (counter==SAVE_COUNTER) {
            counter = 0;
            save(FILE_NAME);
        }
        return newTweets;
    }

    /**
     * Save all tweets to a file.  Saves all Status data
     * as serialized objects.
     * @param filename Name of output file.  Will append
     * all unique tweets to any file of same name.
     */
    public static void save(String filename) {
        try {
            FileOutputStream fos= new FileOutputStream(filename);
            ObjectOutputStream str = new ObjectOutputStream(fos);

            RedBlackBST<Date,Status> tree;
            for (SequentialSearchST<HashThis,User> i:tweets)
                if (i!=null)
                    for (User j:i) {
                        if (j!=null) {
                            tree = j.get();
                            for (Status k : tree)
                                if (k!=null)
                                    str.writeObject(k);
                        }
                    }
            System.exit(0);
        } catch (FileNotFoundException e) {
            System.out.println("file not found");
        }  catch (IOException e) {
            System.out.println("another exception");
        }

    }

    /**
     * Load location data from a file.
     * @param fname File from which to load data
     * @return Arraylist of all cities in the file.
     */
    public static ArrayList<Location> loadLocations(String fname) {
		ArrayList<Location> locations = new ArrayList<Location>();
		File infile = new File(fname);
		Scanner scanner = null;
		try {
			scanner = new Scanner(infile);
		} catch (FileNotFoundException ex) {
			System.err.println("Error: locations file not found.");
			return null;
		}
		// Loop over lines and split into fields we keep.
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			StringTokenizer tokenizer = new StringTokenizer(line);
			String[] fields = line.split(",");
			long pop = Long.parseLong(fields[4]);
			if (pop > 100000) { // Ignore small towns.
				// See Location.java to figure out these fields.
				Location loc = new Location(fields[0], fields[1],
						Double.parseDouble(fields[2]), Double.parseDouble(fields[3]), pop);
				locations.add(loc);
				//System.out.println("Added " + loc.toString());
			}

		}
		scanner.close();
		return locations;
	}

    /** loads tweets
     *@param filename a file to read from
     */
    private static void load(String filename) throws IOException, FileNotFoundException {

        ObjectInputStream objInput = null;
        FileInputStream fileInput = null;

        try {

            fileInput = new FileInputStream(filename);
            objInput = new ObjectInputStream(fileInput);

            while(true) { // Will loop until it reaches the end of the file, then will break with EOFException
                Status curr= (Status)objInput.readObject();
                int hashedKey=tweets.hash(new HashThis(curr.getUser().getScreenName()));
                // Check if there are already saved tweets for this user
                User tree=tweets.getByHashedKey(hashedKey, new HashThis(curr.getUser().getScreenName()));
                if (tree==null) { // If not, create new User
                    tree=new User();
                    tree.put(curr.getCreatedAt(),curr);
                    tweets.putByHashedKey(hashedKey,new HashThis(curr.getUser().getScreenName()),tree);
                    GatherTweets.collected++;

                } else GatherTweets.collected+=tree.put(curr.getCreatedAt(), curr); // Otherwise, add to this User's tree
            }
        } catch (EOFException ignored) {
        // Expected EOFException breaks the while loop

        } catch (Exception e) {
            // Unexpected exception, e.g. ClassNotFoundException
            e.printStackTrace();

        } finally { // Close the file stream objects
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
     * Usage: java twitter4j.examples.search.GatherTweets [query]
     *
     * @param args
     */
    public static void main(String[] args) throws Exception{
        if (args.length < 2) {
            System.out.println("Usage: java GatherTweets minPopulation totalTweets queryString");
            System.exit(-1);
        }


        ArrayList<Location> locations = loadLocations("us-cities.txt");
        long minpop = Long.parseLong(args[0]); // min city size to consider
        int count = 0;
        for (Location i : locations)
            if (i.getPopulation() > minpop)
                count++;
        Location[] eligibleLocations = new Location[count];
        for (Location i : locations) {
            if (i.getPopulation() > minpop) {
                eligibleLocations[--count] = i;
            }
        }
        HashThis.setM(Integer.parseInt(args[1]));
        GatherTweets.tweets=new SeparateChainingHashST(Integer.parseInt(args[1])); //although toCollect is the number of tweets and not the users
        // it makes sense to make the table greater than needed
        load("alltweets.dat");
        String q="";
        for (int i=2;i<args.length;i++)
            q+=args[i]+" ";
        GatherTweets gt=new GatherTweets(new Query(q),Integer.parseInt(args[1]),eligibleLocations);
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {public void run() {
            gt.save("alltweets2.dat");
        }}));
	}

}


