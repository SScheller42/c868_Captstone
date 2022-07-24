package model;

/**
 * A class that holds contact and appointment information, used by a tableview to display data.
 */
public class ContactReport {
    String name = null;
    String month = null;
    int count = 0;
    int contactId = 0;

    /**
     * Creates an object and initializes it.
     * @param name The name of the contact.
     * @param month The month we want to track appointments for.
     * @param count The initial count value to start.
     * @param contactId The id of the contact we want to track appointments for.
     */
    public ContactReport(String name, String month, int count, int contactId) {
        this.name = name;
        this.month = month;
        this.count = count;
        this.contactId = contactId;
    }

    /**
     * Sets the name of the contact.
     * @param name The name that should be set for the contact.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the name of the contact.
     * @return The name value of the contact.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the appointment count for the contact.
     * @param count The value that the count should be set to.
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * Gets the appointment count for the contact.
     * @return The appointment count for the contact.
     */
    public int getCount() {
        return count;
    }

    /**
     * Sets the month that appointments should be tracked for the contact.
     * @param month The month name that should be tracked.
     */
    public void setMonth(String month) {
        this.month = month;
    }

    /**
     * Gets the month that appointments are being tracked for the contact.
     * @return The month name that is being tracked.
     */
    public String getMonth() {
        return month;
    }

    /**
     * Sets the contact id for the contact that is being tracked.
     * @param contactId The contact id value that is associated with the contact.
     */
    public void setContactId(int contactId) {
        this.contactId = contactId;
    }

    /**
     * Gets the contact id of the contact that is being tracked.
     * @return The contact id of the contact being tracked.
     */
    public int getContactId() {
        return contactId;
    }

    /**
     * Increments the appointment count of the contact.
     */
    public void incrementCount() {
        this.count++;
    }
}

