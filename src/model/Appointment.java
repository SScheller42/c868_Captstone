package model;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * An object that holds appointment information, used for displaying in a tableview.
 */
public class Appointment {
    private int id;
    private String title;
    private String description;
    private String location;
    private String type;
    private ZonedDateTime startUtc;
    private ZonedDateTime endUtc;
    private ZonedDateTime startLocal;
    private ZonedDateTime endLocal;
    private String formattedLocalTimeStart;
    private String formattedLocalTimeEnd;
    private int customerId;
    private int userId;
    private int contactId;

    public Appointment (int id, String title, String description, String location, String type, ZonedDateTime startUtc,
                        ZonedDateTime endUtc, int customerId, int userId, int contactId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.type = type;
        this.startUtc = startUtc;
        this.endUtc = endUtc;
        this.customerId = customerId;
        this.userId = userId;
        this.contactId = contactId;

        setStartLocal();
        setEndLocal();
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setStartUtc(ZonedDateTime startUtc) {
        this.startUtc = startUtc;
        setStartLocal();
    }

    public ZonedDateTime getStartUtc() {
        return startUtc;
    }

    private void setStartLocal() {
        this.startLocal = startUtc.withZoneSameInstant(ZoneId.of(ZoneId.systemDefault().getId()));
        this.formattedLocalTimeStart = this.startLocal.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public ZonedDateTime getStartLocal() {
        return startLocal;
    }


    public String getFormattedLocalTimeStart() {
        return formattedLocalTimeStart;
    }

    public String getFormattedLocalTimeEnd() {
        return formattedLocalTimeEnd;
    }

    public void setEndUtc(ZonedDateTime endUtc) {
        this.endUtc = endUtc;
        setEndLocal();
    }

    public ZonedDateTime getEndUtc() {
        return endUtc;
    }

    private void setEndLocal() {
        this.endLocal = endUtc.withZoneSameInstant(ZoneId.of(ZoneId.systemDefault().getId()));
        this.formattedLocalTimeEnd = this.endLocal.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public String getEndLocalFormattedString() {
        return formattedLocalTimeEnd;
    }

    public int getContactId() {
        return contactId;
    }

    public void setContactId(int contactId) {
        this.contactId = contactId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
