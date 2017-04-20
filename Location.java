import twitter4j.GeoLocation;

/**
 * Data about a location (latitude and longitude).
 **/

public class Location {

	private String city;
	private String state;
	private double latitude;
	private double longitude;
	private long population;
	
	public Location(String city, String state, double latitude, double longitude, long pop) {
		this.city = city;
		this.state = state;
		this.latitude = latitude;
		this.longitude = longitude;
		this.population = pop;
	}

	/**
	 * @return the city
	 */
	public String getCity() {
		return city;
	}

	/**
	 * @param city the city to set
	 */
	public void setCity(String city) {
		this.city = city;
	}

	/**
	 * @return the state
	 */
	public String getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(String state) {
		this.state = state;
	}

	/**
	 * @return the latitude
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * @param latitude the latitude to set
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	/**
	 * @return the longitude
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * @param longitude the longitude to set
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	/**
	 * @return the population
	 */
	public long getPopulation() {
		return population;
	}

	/**
	 * @param population the population to set
	 */
	public void setPopulation(long population) {
		this.population = population;
	}
	
	public GeoLocation getLocation() {
		return new GeoLocation(latitude,longitude);
	}
	
	public String toString() {
		return city + ", " + state + ", " + population;
	}
}
