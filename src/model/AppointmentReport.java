package model;

/**
 * A class used to track appointment data for easy display in a table view.
 */
public class AppointmentReport {
    String type = null;
    String month = null;
    int count = 0;

    /**
     * Creates and initializes an object.
     * @param type The meeting type that should be associated with the object.
     * @param month The month that the meeting is in.
     * @param count The initial count value.
     */
    public AppointmentReport(String type, String month, int count) {
        this.type = type;
        this.month = month;
        this.count = count;
    }

    /**
     * Sets the meeting type for the report object.
     * @param type The meeting type being tracked.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the meeting type of this object.
     * @return The type of meeting being tracked by this object.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the appointment count for the meeting type and month being tracked.
     * @param count The value that should be set for the report object.
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * Gets the appointment count for the meeting type and month being tracked.
     * @return The number of appointments for the type and month being tracked.
     */
    public int getCount() {
        return count;
    }

    /**
     * Sets the month that should be used for tracking.
     * @param month The month name used for the appointment tracking.
     */
    public void setMonth(String month) {
        this.month = month;
    }

    /**
     * Gets the month name used for appointment tracking.
     * @return The month name that's being used for appointment tracking.
     */
    public String getMonth() {
        return month;
    }

    /**
     * Increments the appointment count by one.
     */
    public void incrementCount() {
        this.count++;
    }
}