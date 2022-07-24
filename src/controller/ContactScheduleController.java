package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Appointment;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * The controller for contactschedule.fxml.
 */
public class ContactScheduleController {
    private Stage myStage;
    private ArrayList<Map<String, String>> cachedAppointments = null;
    private ArrayList<Map<String, String>> cachedContacts = null;

    @FXML
    private TableView<Appointment> tableViewMaster;
    @FXML
    private TableColumn<Appointment, String> tableColumnType;
    @FXML
    private TableColumn<Appointment, Integer> tableColumnId;
    @FXML
    private TableColumn<Appointment, String> tableColumnTitle;
    @FXML
    private TableColumn<Appointment, String> tableColumnDescription;
    @FXML
    private TableColumn<Appointment, String> tableColumnStart;
    @FXML
    private TableColumn<Appointment, String> tableColumnEnd;
    @FXML
    private TableColumn<Appointment, Integer> tableColumnCustomerId;
    @FXML
    private Label labelContactId;
    @FXML
    private ComboBox<String> comboContactId;
    @FXML
    private Button buttonExit;

    /**
     * Initializes the controller.
     * @param myStage The reference to its own stage component.
     * @param appointments The appointments that have been pulled from the database.
     * @param contacts The contacts that have been pulled from the database.
     */
    public void initialize(Stage myStage, ArrayList<Map<String, String>> appointments, ArrayList<Map<String, String>> contacts) {
        this.myStage = myStage;
        this.cachedAppointments = appointments;
        this.cachedContacts = contacts;

        // Adjust the control text
        setControlText();

        // Get our combo box setup
        setupContactComboBox(contacts);

        // Set our button action
        buttonExit.setOnAction(event -> this.myStage.close());

        // Set our comboButton action event
        comboContactId.setOnAction(event -> displayAppointmentsByContactId(getContactIdFromCacheByName(comboContactId.getValue())));

        // Trigger our first sort
        displayAppointmentsByContactId(getContactIdFromCacheByName(comboContactId.getValue()));
    }

    /**
     * Sets the appropriate language for the controls on the form to be readable in either English or French depending on detected region.
     */
    private void setControlText() {
        ResourceBundle languageBundle = ResourceBundle.getBundle("main/lang", Locale.getDefault());

        this.myStage.setTitle(languageBundle.getString("ReportTitleContactSchedule"));

        // Table
        tableColumnType.setText(languageBundle.getString("AppointmentType"));
        tableColumnId.setText(languageBundle.getString("AppointmentID"));
        tableColumnTitle.setText(languageBundle.getString("AppointmentTitle"));
        tableColumnDescription.setText(languageBundle.getString("AppointmentDesc"));
        tableColumnStart.setText(languageBundle.getString("AppointmentStart"));
        tableColumnEnd.setText(languageBundle.getString("AppointmentEnd"));
        tableColumnCustomerId.setText(languageBundle.getString("AppointmentCustomerID"));

        // Labels
        labelContactId.setText(languageBundle.getString("AppointmentCustomerID"));

        // Our buttons
        buttonExit.setText(languageBundle.getString("MenuExit"));
    }

    /**
     * Adds the contacts to a combo box for easy selection.
     * @param contacts An array of maps related to the contacts to add to the combo box.
     */
    private void setupContactComboBox(ArrayList<Map<String, String>> contacts) {
        // This is less code and more readable than the traditional foreach loop found in Java, hence the stream/lambda was chosen.
        contacts.stream()
                .forEach(contact -> comboContactId.getItems().add(contact.get("Contact_Name")));

        // Set our value to the first item in our list
        comboContactId.setValue(comboContactId.getItems().get(0));
    }

    /**
     * Displays all of the appointments for a particular contact id.
     * @param contactId The contact id for the contact we want to display all of the appointments for.
     */
    public void displayAppointmentsByContactId(int contactId) {
        ObservableList<Appointment> list = FXCollections.observableArrayList();

        // Create a date formatter for our date/times below
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

        cachedAppointments.stream()
                .filter(appointment -> Integer.parseInt(appointment.get("Contact_ID")) == contactId)
                .forEach(result -> {
                    // Create the new appointment
                    Appointment newAppointment = new Appointment(Integer.parseInt(result.get("Appointment_ID")), result.get("Title"),
                            result.get("Description"), result.get("Location"), result.get("Type"), ZonedDateTime.parse(result.get("Start") + " Etc/UTC", formatter),
                            ZonedDateTime.parse(result.get("End") + " Etc/UTC", formatter), Integer.parseInt(result.get("Customer_ID")),
                            Integer.parseInt(result.get("User_ID")), Integer.parseInt(result.get("Contact_ID")));

                    // Add it to the list
                    list.add(newAppointment);
                });

        tableViewMaster.setItems(list);
        tableColumnId.setCellValueFactory(new PropertyValueFactory<>("id"));
        tableColumnTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        tableColumnDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        tableColumnType.setCellValueFactory(new PropertyValueFactory<>("type"));
        tableColumnStart.setCellValueFactory(new PropertyValueFactory<>("formattedLocalTimeStart"));
        tableColumnEnd.setCellValueFactory(new PropertyValueFactory<>("formattedLocalTimeEnd"));
        tableColumnCustomerId.setCellValueFactory(new PropertyValueFactory<>("customerId"));
    }

    /**
     * Retrieves a contact id from a given contact name.
     * @param contactName The name of the contact we want to look up the id for.
     * @return The id of the contact we are querying.
     */
    public int getContactIdFromCacheByName(String contactName) {
        for (Map<String, String> contact: cachedContacts) {
            if (contact.get("Contact_Name").equals(contactName)) {
                return Integer.parseInt(contact.get("Contact_ID"));
            }
        }

        return -1;
    }
}
