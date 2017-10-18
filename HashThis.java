/**
 * @author Alexander Hamme
 * @author Sasha Fedchin
 */
public class HashThis {
    private String str;
    public static final int R=31;  // hash multiplier
    public static int m=1000;      // initial table size

    /**
     * Create new HashThis instance
     * @param str
     */
    public HashThis(String str) { 
        this.str = str;
    }
    /**
     * Public hashcode method for use outside this class
     * Takes string input and returns hash 
     * @return int
     */
    @Override
    public int hashCode() {
        int hash = 0;
        for (int i = 0; i < str.length(); i += 1){ // Start at index 1 to exclude the '@' symbol
            if ( (int)(str.charAt(i)) > 127) { continue; } // Exclude non-basic ASCII characters
            hash = ((hash * R) + str.charAt(i)) % m;
        }
        return hash;
    }
    
    /**
    * Only to be used if the table is not currently being hashed into
    * For use within gatherTweets, to reset table size from m to newM, as needed
    *@param newM value for hash table size
    */
    public static void setM(int newM) {  
        m=newM;
    }

    public boolean equals(Object obj) {
        if (obj==null) return false;
        return this.str.equals(((HashThis)obj).str);
    }
}
