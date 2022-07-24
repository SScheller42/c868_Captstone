package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.ContactReport;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * The controller for contactapointmentcountbymonth.fxml
 */
public class ContactAppointmentCountController {
    private Stage myStage;

    @FXML
    private TableView<ContactReport> tableViewMaster;
    @FXML
    private TableColumn<ContactReport, String> tableColumnContactName;
    @FXML
    private TableColumn<ContactReport, String> tableColumnMonth;
    @FXML
    private TableColumn<ContactReport, Integer> tableColumnCount;
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

        // Adjust the control text
        setControlText();

        // Run our report
        countAppointmentsByTypeAndMonth(appointments, contacts);

        // Set our button action
        buttonExit.setOnAction(event -> this.myStage.close());
    }

    /**
     * Sets the appropriate language for the controls on the form to be readable in either English or French depending on detected region.
     */
    private void setControlText() {
        ResourceBundle languageBundle = ResourceBundle.getBundle("main/lang", Locale.getDefault());

        this.myStage.setTitle(languageBundle.getString("ReportTitleContactAppointmentCount"));

        tableColumnContactName.setText(languageBundle.getString("ReportContactName"));
        tableColumnMonth.setText(languageBundle.getString("ReportMonth"));
        tableColumnCount.setText(languageBundle.getString("ReportCount"));

        // Our buttons
        buttonExit.setText(languageBundle.getString("MenuExit"));
    }

    /**
     * Counts all the appointments for a given contact in a month and displays it into a tableview.
     * @param appointments A map array of appointments to check.
     * @param contacts A map array of contacts to check against.
     */
    private void countAppointmentsByTypeAndMonth(ArrayList<Map<String, String>> appointments, ArrayList<Map<String, String>> contacts) {
        ObservableList<ContactReport> list = FXCollections.observableArrayList();

        for (Map<String, String> appointment : appointments) {
            ZonedDateTime appointmentDateAndTimeInUTC = ZonedDateTime.parse(appointment.get("Start") + " Etc/UTC", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));

            // Loop through our list and see if we already have an object that's tracking that specific month/type combo
            long numberOfMatches = list.stream()
                    .filter(result -> result.getMonth().equals(appointmentDateAndTimeInUTC.getMonth().name()) && result.getContactId() == Integer.parseInt(appointment.get("Contact_ID")))
                    .count();

            // If we have a number greater than zero then let's loop through again and increment our match
            if (numberOfMatches > 0) {
                list.stream()
                        .filter(result -> result.getMonth().equals(appointmentDateAndTimeInUTC.getMonth().name()) && result.getContactId() == Integer.parseInt(appointment.get("Contact_ID")))
                        .forEach(result -> result.incrementCount());
            } else {
                // No matches found. We need to loop through our contacts until we find the appropriate ID and create a new object
                contacts.stream()
                        .filter(contact -> contact.get("Contact_ID").equals(appointment.get("Contact_ID")))
                        .forEach(result -> {
                            ContactReport newContactReport = new ContactReport(result.get("Contact_Name"), appointmentDateAndTimeInUTC.getMonth().name(), 1, Integer.parseInt(result.get("Contact_ID")));
                            list.add(newContactReport);
                        });
            }

            // Once this far should be good to go ahead and set up our table view
            tableViewMaster.setItems(list);
            tableColumnContactName.setCellValueFactory(new PropertyValueFactory<>("name"));
            tableColumnMonth.setCellValueFactory(new PropertyValueFactory<>("month"));
            tableColumnCount.setCellValueFactory(new PropertyValueFactory<>("count"));
        }
    }
}

