package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.AppointmentReport;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * The controller for appointmentstypemonth.fxml
 */
public class AppointmentsTypeMonthController {
    private Stage myStage;

    @FXML
    private TableView<AppointmentReport> tableViewMaster;
    @FXML
    private TableColumn<AppointmentReport, String> tableColumnType;
    @FXML
    private TableColumn<AppointmentReport, String> tableColumnMonth;
    @FXML
    private TableColumn<AppointmentReport, Integer> tableColumnCount;
    @FXML
    private Button buttonExit;

    /**
     * Initializes the controller.
     * @param myStage The reference to its own stage component.
     * @param appointments The appointments that have been pulled from the database.
     */
    public void initialize(Stage myStage, ArrayList<Map<String, String>> appointments) {
        this.myStage = myStage;

        // Adjust the control text
        setControlText();

        // Run our report
        countAppointmentsByTypeAndMonth(appointments);

        // Set our button action
        buttonExit.setOnAction(event -> this.myStage.close());
    }

    /**
     * Sets the appropriate language for the controls on the form to be readable in either English or French depending on detected region.
     */
    private void setControlText() {
        ResourceBundle languageBundle = ResourceBundle.getBundle("main/lang", Locale.getDefault());

        this.myStage.setTitle(languageBundle.getString("ReportTitleTypeMonthCount"));

        tableColumnType.setText(languageBundle.getString("AppointmentType"));
        tableColumnMonth.setText(languageBundle.getString("ReportMonth"));
        tableColumnCount.setText(languageBundle.getString("ReportCount"));

        // Our buttons
        buttonExit.setText(languageBundle.getString("MenuExit"));
    }

    /**
     * Counts the number of appointments by type and the month that they appear in and then displays them in a table view.
     * @param appointments The appointments to be checked against.
     */
    private void countAppointmentsByTypeAndMonth(ArrayList<Map<String, String>> appointments) {
        ObservableList<AppointmentReport> list = FXCollections.observableArrayList();

        for (Map<String, String> appointment : appointments) {
            ZonedDateTime appointmentDateAndTimeInUTC = ZonedDateTime.parse(appointment.get("Start") + " Etc/UTC", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));

            // Loop through our list and see if we already have an object that's tracking that specific month/type combo
            long numberOfMatches = list.stream()
                    .filter(result -> result.getMonth().equals(appointmentDateAndTimeInUTC.getMonth().name()) && result.getType().equals(appointment.get("Type")))
                    .count();

            // If we have a number greater than zero then let's loop through again and increment our match
            if (numberOfMatches > 0) {
                list.stream()
                        .filter(result -> result.getMonth().equals(appointmentDateAndTimeInUTC.getMonth().name()) && result.getType().equals(appointment.get("Type")))
                        .forEach(result -> result.incrementCount());
            } else {
                // No matches found so go ahead and create and object, and add it to the list
                AppointmentReport newAppointmentReport = new AppointmentReport(appointment.get("Type"), appointmentDateAndTimeInUTC.getMonth().name(), 1);
                list.add(newAppointmentReport);
            }

            // Once this far should be good to go ahead and set up our table view
            tableViewMaster.setItems(list);
            tableColumnType.setCellValueFactory(new PropertyValueFactory<>("type"));
            tableColumnMonth.setCellValueFactory(new PropertyValueFactory<>("month"));
            tableColumnCount.setCellValueFactory(new PropertyValueFactory<>("count"));
        }
    }
}

