package model;

/**
 * Customer class that facilitates the display of information in a table view.
 */
public class Customer {
    private int id;
    private String name;
    private String address;
    private String postalCode;
    private String phone;
    private int divisionId;
    private String firstLevelDivision;
    private String country;

    /**
     * Gets the customer id.
     * @return An integer value corresponding to the customer's ID.
     */
    public int getId() {return this.id;}

    /**
     * Sets the customer id.
     * @param id The id value that corresponds to the customer.
     */
    public void setId (int id) {this.id = id;}

    /**
     * Gets the customer name.
     * @return The name value that corresponds to the customer.
     */
    public String getName() {return this.name;}

    /**
     * Sets the customer name.
     * @param name The name value that corresponds to the customer.
     */
    public void setName(String name) {this.name = name;}

    /**
     * Gets the street address of the customer.
     * @return The street address value that corresponds to the customer.
     */
    public String getAddress() {return this.address;}

    /**
     * Sets the street address of the customer.
     * @param address The street address value that corresponds to the customer.
     */
    public void setAddress(String address) {this.address = address;}

    /**
     * Gets the postal code of the customer.
     * @return The postal code value that corresponds to the customer.
     */
    public String getPostalCode() {return this.postalCode;}

    /**
     * Sets the postal code of the customer.
     * @param postalCode The postal code value that corresponds to the customer.
     */
    public void setPostalCode(String postalCode) {this.postalCode = postalCode;}

    /**
     * Gets the division id of the customer.
     * @return The division id value that corresponds to the customer.
     */
    public int getDivisionId() {return this.divisionId;}

    /**
     * Sets the division id of the customer.
     * @param divisionId The division id value that corresponds to the customer.
     */
    public void setDivisionId(int divisionId) {this.divisionId = divisionId;}

    /**
     * Gets the phone number of the customer.
     * @return The phone number value that corresponds to the customer.
     */
    public String getPhone() {return this.phone;}

    /**
     * Sets the phone number of the customer.
     * @param phone The phone number value that corresponds to the customer.
     */
    public void setPhone(String phone) {this.phone = phone;}

    /**
     * Gets the first level division string of the customer.
     * @return A human descriptive string of the customer's first level division.
     */
    public String getFirstLevelDivision() {return this.firstLevelDivision;}

    /**
     * Sets the first level division string of the customer
     * @param firstLevelDivision A human descriptive string of the customer's first level division.
     */
    public void setFirstLevelDivision(String firstLevelDivision) {this.firstLevelDivision = firstLevelDivision;}

    /**
     * Gets the country of the customer.
     * @return A human descriptive string of the customer's country.
     */
    public String getCountry() {return this.country;}

    /**
     * Sets the country of the customer.
     * @param country A human descriptive string of the customer's country.
     */
    public void setCountry(String country) {this.country = country;}

    /**
     * Creates a customer object with initial values.
     * @param id The id of the customer.
     * @param name The name of the customer.
     * @param address The street address of the customer.
     * @param postalCode The postal code of the customer.
     * @param phone The phone number of the customer.
     * @param divisionId The division ID of the customer.
     * @param firstLevelDivision The first level division of the customer.
     * @param country The country of the customer.
     */
    public Customer(int id, String name, String address, String postalCode, String phone, int divisionId, String firstLevelDivision, String country) {
        setId(id);
        setName(name);
        setAddress(address);
        setPostalCode(postalCode);
        setPhone(phone);
        setDivisionId(divisionId);
        setFirstLevelDivision(firstLevelDivision);
        setCountry(country);
    }
}
