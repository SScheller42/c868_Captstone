package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.Main;
import model.Appointment;
import model.MySQLWrapper;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * The controller for appointment.fxml
 */
public class AppointmentController {
    Stage myStage = null;
    String userName = null;
    int userId = -1;
    private ArrayList<Map<String, String>> cachedAppointments = null;
    private ArrayList<Map<String, String>> cachedContacts = null;
    private ArrayList<Map<String, String>> cachedUsers = null;
    private ArrayList<Map<String, String>> cachedCustomers = null;

    @FXML
    private MenuBar menuBar;
    @FXML
    private TableView<Appointment> tableViewAppointments;
    @FXML
    private TableColumn<Appointment, Integer> tableColumnId;
    @FXML
    private TableColumn<Appointment, String> tableColumnTitle;
    @FXML
    private TableColumn<Appointment, String> tableColumnDescription;
    @FXML
    private TableColumn<Appointment, String> tableColumnLocation;
    @FXML
    private TableColumn<Appointment, String> tableColumnType;
    @FXML
    private TableColumn<Appointment, String> tableColumnStart;
    @FXML
    private TableColumn<Appointment, String> tableColumnEnd;
    @FXML
    private TableColumn<Appointment, Integer> tableColumnCustomerId;
    @FXML
    private TableColumn<Appointment, Integer> tableColumnUserId;
    @FXML
    private TableColumn<Appointment, Integer> tableColumnContactId;
    @FXML
    private Label labelEdit;
    @FXML
    private Label labelAdd;
    @FXML
    private Label labelInfo;
    @FXML
    private ComboBox<String> comboAddContact;
    @FXML
    private ComboBox<String> comboEditContact;

    @FXML
    private Label labelEditId;
    @FXML
    private Label labelEditTitle;
    @FXML
    private Label labelEditDescription;
    @FXML
    private Label labelEditLocation;
    @FXML
    private Label labelEditContact;
    @FXML
    private Label labelEditType;
    @FXML
    private Label labelEditStartDate;
    @FXML
    private Label labelEditStartTime;
    @FXML
    private Label labelEditEndDate;
    @FXML
    private Label labelEditEndTime;
    @FXML
    private Label labelEditCustomerId;
    @FXML
    private Label labelEditUserId;
    @FXML
    private Label labelAddTitle;
    @FXML
    private Label labelAddDescription;
    @FXML
    private Label labelAddLocation;
    @FXML
    private Label labelAddContact;
    @FXML
    private Label labelAddType;
    @FXML
    private Label labelAddStartDate;
    @FXML
    private Label labelAddStartTime;
    @FXML
    private Label labelAddEndDate;
    @FXML
    private Label labelAddEndTime;
    @FXML
    private Label labelAddCustomerId;
    @FXML
    private Label labelAddUserId;

    @FXML
    private TextField textFieldEditId;
    @FXML
    private TextField textFieldEditTitle;
    @FXML
    private TextField textFieldEditDescription;
    @FXML
    private TextField textFieldEditLocation;
    @FXML
    private TextField textFieldEditType;
    @FXML
    private TextField textFieldEditStartDate;
    @FXML
    private TextField textFieldEditStartTime;
    @FXML
    private TextField textFieldEditEndDate;
    @FXML
    private TextField textFieldEditEndTime;
    @FXML
    private ComboBox<String> comboEditCustomerId;
    @FXML
    private ComboBox<String> comboEditUserId;

    @FXML
    private TextField textFieldAddTitle;
    @FXML
    private TextField textFieldAddDescription;
    @FXML
    private TextField textFieldAddLocation;
    @FXML
    private TextField textFieldAddType;
    @FXML
    private TextField textFieldAddStartDate;
    @FXML
    private TextField textFieldAddStartTime;
    @FXML
    private TextField textFieldAddEndDate;
    @FXML
    private TextField textFieldAddEndTime;
    @FXML
    private ComboBox<String> comboAddCustomerId;
    @FXML
    private ComboBox<String> comboAddUserId;

    @FXML
    private Button buttonEditSubmit;
    @FXML
    private Button buttonEditCancel;
    @FXML
    private Button buttonEditDelete;
    @FXML
    private Button buttonAddSubmit;
    @FXML
    private Button buttonAddCancel;

    @FXML
    private RadioButton radioMonth;
    @FXML
    private RadioButton radioWeek;

    @FXML
    private RadioButton radioAll;

    @FXML
    private TextField searchTextField;



    /**
     * Initializes the controller.
     * Lambda Justification: setOn...events are more comprehensive than trying to cover every possibility in Scene Builder's FXML files. Lambdas provide a very clean and readable way of accessing the event actions
     * instead of overriding them.
     * @param myStage A reference to the controller's stage.
     * @param myUser The username logged into the application.
     * @param myUserId The user id logged into the application.
     */
    public void initialize(Stage myStage, String myUser, int myUserId) {
        this.myStage = myStage;
        this.userName = myUser;
        this.userId = myUserId;

        // Create our language bundle in case of errors
        ResourceBundle languageBundle = ResourceBundle.getBundle("main/lang", Locale.getDefault());

        // Download all of our appointment data
        try {
            if (!getAppointments() || !getContactsAndUsersAndCustomers()) {
                labelInfo.setText(languageBundle.getString("LoginDBConnectFailure"));
            }
        } catch (IOException ex) {
            // Alert that we had an issue with the properties file
            labelInfo.setText(languageBundle.getString("LoginConfigFileMissing"));
        }

        // Set up our menu objects
        setupMenus();

        // Set our languages
        setControlText();

        searchText();


        // Create our GUI events
        tableViewAppointments.setOnMouseClicked(event -> onTableViewClicked());
        buttonAddCancel.setOnAction(event -> clearAddControls());
        buttonEditCancel.setOnAction(event -> clearEditControls());
        buttonAddSubmit.setOnAction(event -> {
            try {
                submitNewAppointment();
            } catch (IOException ex) {
                // Alert the user that the props file is busted
                labelInfo.setText(languageBundle.getString("LoginConfigFileMissing"));
            }
        });

        buttonEditSubmit.setOnAction(event -> {
            try {
                submitUpdateToAppointment();
            } catch (IOException ex) {
                labelInfo.setText(languageBundle.getString("LoginConfigFileMissing"));
            }
        });

        buttonEditDelete.setOnAction(event -> {
            try {
                deleteExistingAppointment();
            } catch (IOException ex) {
                labelInfo.setText(languageBundle.getString("LoginConfigFileMissing"));
            }
        });
            searchTextField.setOnAction(event -> tableViewAppointments.setItems(getAllAppointments()));
            radioAll.setOnAction(event -> tableViewAppointments.setItems(getAllAppointments()));
            radioMonth.setOnAction(event -> tableViewAppointments.setItems(getFilteredAppointments(false)));
            radioWeek.setOnAction(event -> tableViewAppointments.setItems(getFilteredAppointments(true)));

        }


    /**
     * Sets up all menu items and their action events.
     */
    private void setupMenus() {
        // Going to need our language bundle
        ResourceBundle languageBundle = ResourceBundle.getBundle("main/lang", Locale.getDefault());

        // Setup up our file menu and items
        Menu fileMenu = new Menu(languageBundle.getString("MenuFile"));
        MenuItem exitMenuItem = new MenuItem(languageBundle.getString("MenuExit"));
        exitMenuItem.setOnAction(event -> closeAppointmentWindow());
        fileMenu.getItems().add(exitMenuItem);

        // Setup our customer file menu and items
        Menu customerMenu = new Menu(languageBundle.getString("MenuCustomer"));

        // Set up the add customer menu item
        MenuItem addCustomerMenuItem = new MenuItem(languageBundle.getString("MenuCustomerAdd"));
        addCustomerMenuItem.setOnAction(event -> openAddCustomer());

        // Set up the edit customer menu item
        MenuItem editCustomerMenuItem = new MenuItem(languageBundle.getString("MenuCustomerEdit"));
        editCustomerMenuItem.setOnAction(event -> openEditCustomer());

        // Add in all of our customer menu items
        customerMenu.getItems().addAll(addCustomerMenuItem, editCustomerMenuItem);

        // Set up the report file menu and items
        Menu reportMenu = new Menu(languageBundle.getString("MenuReports"));

        // Set up the report sub menu items
        MenuItem appointmentsByTypeAndMonthMenuItem = new MenuItem(languageBundle.getString("MenuReportsTypeMonth"));
        appointmentsByTypeAndMonthMenuItem.setOnAction(event -> openReportAppointmentByMonthAndType());

        MenuItem contactsScheduleReportMenuItem = new MenuItem(languageBundle.getString("MenuReportsTypeSchedule"));
        contactsScheduleReportMenuItem.setOnAction(event -> openReportContactSchedule());

        MenuItem contactAppointmentCountMenuItem = new MenuItem(languageBundle.getString("MenuReportsContactAppointmentCount"));
        contactAppointmentCountMenuItem.setOnAction(event -> openReportContactCount());

        // Add all of our report menu items
        reportMenu.getItems().addAll(appointmentsByTypeAndMonthMenuItem, contactsScheduleReportMenuItem, contactAppointmentCountMenuItem);

        // Add them into the menu bar
        menuBar.getMenus().addAll(fileMenu, customerMenu, reportMenu);
    }

    /**
     * Connects to the database and extracts all appointment information.
     * @return True if the operation is successful, otherwise false.
     * @throws IOException
     */
    private boolean getAppointments() throws IOException {
        // Now create our wrapper object
        MySQLWrapper dbConnection = new MySQLWrapper();

        try (FileReader reader = new FileReader("src/main/MyAppLanguages.properties")) {
            // Tap into our properties file and grab our mysql connection string
            Properties ourProps = new Properties();
            ourProps.load(reader);

            // Attempt to connect
            boolean successful = dbConnection.createConnection(ourProps.getProperty("mysql-connection-string"), ourProps.getProperty("mysql-username"), ourProps.getProperty("mysql-password"));

            // If we have a successful connection, go ahead and do our queries
            if (successful) {
                ArrayList<Map<String, String>> queryResults = dbConnection.selectFromTable(MySQLWrapper.TableNames.appointments,
                        "Appointment_ID, Title, Description, Location, Type, Start, End, Customer_ID, User_ID, Contact_ID", "", "", "Appointment_ID", 0);

                if (queryResults == null || queryResults.stream().count() == 0) {
                    // Something went wrong, advise and abort
                    return false;
                }

                // Close our connection once done
                dbConnection.closeConnection();

                // Cache our appointments
                cachedAppointments = queryResults;

                // Set up our tableview
                tableViewAppointments.setItems(getFilteredAppointments(radioWeek.selectedProperty().get()));
                tableColumnId.setCellValueFactory(new PropertyValueFactory<>("id"));
                tableColumnTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
                tableColumnDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
                tableColumnLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
                tableColumnType.setCellValueFactory(new PropertyValueFactory<>("type"));
                tableColumnStart.setCellValueFactory(new PropertyValueFactory<>("formattedLocalTimeStart"));
                tableColumnEnd.setCellValueFactory(new PropertyValueFactory<>("formattedLocalTimeEnd"));
                tableColumnCustomerId.setCellValueFactory(new PropertyValueFactory<>("customerId"));
                tableColumnUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
                tableColumnContactId.setCellValueFactory(new PropertyValueFactory<>("contactId"));

                // And let's do a check to see if we have any upcoming appointments
                printOutCloseAppointments();
            }
        } catch (FileNotFoundException ex) {
            // If we failed for whatever reason return false
            return false;
        }

        // If we make it all the way to the end, return true
        return true;
    }

    /**
     * Connects to the database and extracts contact, user, and customer information.
     * @return True if the operation is successful, otherwise false.
     * @throws IOException
     */
    private boolean getContactsAndUsersAndCustomers() throws IOException {
        // Now create our wrapper object
        MySQLWrapper dbConnection = new MySQLWrapper();

        try (FileReader reader = new FileReader("src/main/MyAppLanguages.properties")) {
            // Tap into our properties file and grab our mysql connection string
            Properties ourProps = new Properties();
            ourProps.load(reader);

            // Attempt to connect
            boolean successful = dbConnection.createConnection(ourProps.getProperty("mysql-connection-string"), ourProps.getProperty("mysql-username"), ourProps.getProperty("mysql-password"));

            // If we have a successful connection, go ahead and do our queries
            if (successful) {
                ArrayList<Map<String, String>> queryResults = dbConnection.selectFromTable(MySQLWrapper.TableNames.contacts,
                        "Contact_ID, Contact_Name, Email", "", "", "Contact_ID", 0);

                if (queryResults == null || queryResults.stream().count() == 0) {
                    // Something went wrong, advise and abort
                    return false;
                }

                // Cache our results
                cachedContacts = queryResults;

                // Now let's do our users
                queryResults = dbConnection.selectFromTable(MySQLWrapper.TableNames.users, "User_ID, User_Name", "", "", "User_ID", 0);

                if (queryResults == null || queryResults.stream().count() == 0) {
                    // Something went wrong, pull the cord
                    return false;
                }

                // Cache our results
                cachedUsers = queryResults;

                // Finally grab our customers
                queryResults = dbConnection.selectFromTable(MySQLWrapper.TableNames.customers, "Customer_ID, Customer_Name", "", "", "Customer_ID", 0);

                // Close our connection once done
                dbConnection.closeConnection();

                // Verify that we're solid
                if (queryResults == null || queryResults.stream().count() == 0) {
                    // Something went wrong, abort
                    return false;
                }

                // Cache the results
                cachedCustomers = queryResults;

                // Loop through and adjust our contact combo boxes
                cachedContacts.stream()
                        .forEach(contact -> comboAddContact.getItems().add(contact.get("Contact_Name")));

                // Copy the list from one to the other
                comboEditContact.setItems(comboAddContact.getItems());

                // For cleanliness set them both to the first item
                comboAddContact.setValue(comboAddContact.getItems().get(0));
                comboEditContact.setValue(comboEditContact.getItems().get(0));

                // Set our add combo box
                cachedUsers.stream()
                        .forEach(user -> comboAddUserId.getItems().add(user.get("User_Name")));

                // Copy over to edit
                comboEditUserId.setItems(comboAddUserId.getItems());

                // Set both to the first value
                comboAddUserId.setValue(comboAddUserId.getItems().get(0));
                comboEditUserId.setValue(comboEditUserId.getItems().get(0));

                // Set our add combo box
                cachedCustomers.stream()
                        .forEach(customer -> comboAddCustomerId.getItems().add(customer.get("Customer_Name")));

                // Copy over to edit
                comboEditCustomerId.setItems(comboAddCustomerId.getItems());

                // Set both to the first value
                comboAddCustomerId.setValue(comboAddCustomerId.getItems().get(0));
                comboEditCustomerId.setValue(comboEditCustomerId.getItems().get(0));
            }
        } catch (FileNotFoundException ex) {
            // If we failed for whatever reason return false
            return false;
        }

        // If we make it all the way to the end, return true
        return true;
    }

    /**
     * Sets the appropriate language for the controls on the form to be readable in either English or French depending on detected region.
     */
    private void setControlText() {
        // Create our language bundle
        ResourceBundle languageBundle = ResourceBundle.getBundle("main/lang", Locale.getDefault());

        // Set our title
        this.myStage.setTitle(languageBundle.getString("AppointmentWindowTitle"));

        // Setup our table columns
        tableColumnId.setText(languageBundle.getString("AppointmentID"));
        tableColumnTitle.setText(languageBundle.getString("AppointmentTitle"));
        tableColumnDescription.setText(languageBundle.getString("AppointmentDesc"));
        tableColumnLocation.setText(languageBundle.getString("AppointmentLocation"));
        tableColumnType.setText(languageBundle.getString("AppointmentType"));
        tableColumnStart.setText(languageBundle.getString("AppointmentStart"));
        tableColumnEnd.setText(languageBundle.getString("AppointmentEnd"));
        tableColumnCustomerId.setText(languageBundle.getString("AppointmentCustomerID"));
        tableColumnUserId.setText(languageBundle.getString("AppointmentUserID"));
        tableColumnContactId.setText(languageBundle.getString("AppointmentContactID"));

        //  sets labels
        labelEdit.setText(languageBundle.getString("AppointmentLabelEdit"));
        labelAdd.setText(languageBundle.getString("AppointmentLabelAdd"));
        labelEditId.setText(languageBundle.getString("AppointmentID"));
        labelEditTitle.setText(languageBundle.getString("AppointmentTitle"));
        labelAddTitle.setText(languageBundle.getString("AppointmentTitle"));
        labelEditDescription.setText(languageBundle.getString("AppointmentDesc"));
        labelAddDescription.setText(languageBundle.getString("AppointmentDesc"));
        labelEditLocation.setText(languageBundle.getString("AppointmentLocation"));
        labelAddLocation.setText(languageBundle.getString("AppointmentLocation"));
        labelEditContact.setText(languageBundle.getString("AppointmentContactID"));
        labelAddContact.setText(languageBundle.getString("AppointmentContactID"));
        labelEditType.setText(languageBundle.getString("AppointmentType"));
        labelAddType.setText(languageBundle.getString("AppointmentType"));
        labelEditUserId.setText(languageBundle.getString("AppointmentUserID"));
        labelAddUserId.setText(languageBundle.getString("AppointmentUserID"));
        labelEditCustomerId.setText(languageBundle.getString("AppointmentCustomerID"));
        labelAddCustomerId.setText(languageBundle.getString("AppointmentCustomerID"));
        labelEditStartDate.setText(languageBundle.getString("AppointmentStartDate"));
        labelAddStartDate.setText(languageBundle.getString("AppointmentStartDate"));
        labelEditStartTime.setText(languageBundle.getString("AppointmentStartTime"));
        labelAddStartTime.setText(languageBundle.getString("AppointmentStartTime"));
        labelEditEndDate.setText(languageBundle.getString("AppointmentEndDate"));
        labelAddEndDate.setText(languageBundle.getString("AppointmentEndDate"));
        labelEditEndTime.setText(languageBundle.getString("AppointmentEndTime"));
        labelAddEndTime.setText(languageBundle.getString("AppointmentEndTime"));

        // Our buttons
        buttonEditSubmit.setText(languageBundle.getString("CustomerUpdate"));
        buttonAddSubmit.setText(languageBundle.getString("LoginSubmit"));
        buttonEditCancel.setText(languageBundle.getString("ButtonCancel"));
        buttonAddCancel.setText(languageBundle.getString("ButtonCancel"));
        buttonEditDelete.setText(languageBundle.getString("CustomerDelete"));

        // Our radio buttons
        radioMonth.setText(languageBundle.getString("RadioMonth"));
        radioWeek.setText(languageBundle.getString("RadioWeek"));
        radioAll.setText(languageBundle.getString("RadioAll"));
    }

    /**
     * Closes the stage for this controller.
     */
    private void closeAppointmentWindow() {
        this.myStage.close();
    }

    /**
     * Opens the add customer form and closes this form.
     */
    private void openAddCustomer() {
        try {
            Stage customerStage = new Stage();
            FXMLLoader customerLoader = new FXMLLoader(Main.class.getResource("/view/addcustomer.fxml"));
            Scene customerScene = new Scene(customerLoader.load());
            AddCustomerController controller = customerLoader.getController();
            customerStage.setScene(customerScene);
            controller.initialize(customerStage, this.userName, this.userId);
            customerStage.show();

            // Finally, close this window
            closeAppointmentWindow();
        } catch (IOException ex) {
            // Nothing really to do here, the program is likely irreparably broken
        }
    }

    /**
     * Opens the edit customer form and closes this form.
     */
    private void openEditCustomer() {
        try {
            Stage customerStage = new Stage();
            FXMLLoader customerLoader = new FXMLLoader(Main.class.getResource("/view/customer.fxml"));
            Scene customerScene = new Scene(customerLoader.load());
            EditCustomerController controller = customerLoader.getController();
            customerStage.setScene(customerScene);
            controller.initialize(customerStage, this.userName, this.userId);
            customerStage.show();

            // Finally, close this window
            closeAppointmentWindow();
        } catch (IOException ex) {
            // Nothing really to do here, the program is likely irreparably broken
        }
    }

    /**
     * Opens the appointment type/month report.
     */
    private void openReportAppointmentByMonthAndType() {
        try {
            Stage reportStage = new Stage();
            FXMLLoader reportLoader = new FXMLLoader(Main.class.getResource("/view/appointmentstypemonth.fxml"));
            Scene reportScene = new Scene(reportLoader.load());
            AppointmentsTypeMonthController controller = reportLoader.getController();
            reportStage.setScene(reportScene);
            controller.initialize(reportStage, this.cachedAppointments);
            reportStage.initOwner(this.myStage);
            reportStage.initModality(Modality.APPLICATION_MODAL);
            reportStage.showAndWait();


        } catch (IOException ex) {
            // Nothing really to do here, the program is likely irreparably broken
        }
    }

    /**
     * Opens the report for contacts and their schedule.
     */
    private void openReportContactSchedule() {
        try {
            Stage reportStage = new Stage();
            FXMLLoader reportLoader = new FXMLLoader(Main.class.getResource("/view/contactschedule.fxml"));
            Scene reportScene = new Scene(reportLoader.load());
            ContactScheduleController controller = reportLoader.getController();
            reportStage.setScene(reportScene);
            controller.initialize(reportStage, this.cachedAppointments, this.cachedContacts);
            reportStage.initOwner(this.myStage);
            reportStage.initModality(Modality.APPLICATION_MODAL);
            reportStage.showAndWait();


        } catch (IOException ex) {
            // Nothing really to do here, the program is likely irreparably broken
        }
    }

    /**
     * Opens the report for counting the number of appoints each contact has each month.
     */
    private void openReportContactCount() {
        try {
            Stage reportStage = new Stage();
            FXMLLoader reportLoader = new FXMLLoader(Main.class.getResource("/view/contactappointmentcountbymonth.fxml"));
            Scene reportScene = new Scene(reportLoader.load());
            ContactAppointmentCountController controller = reportLoader.getController();
            reportStage.setScene(reportScene);
            controller.initialize(reportStage, this.cachedAppointments, this.cachedContacts);
            reportStage.initOwner(this.myStage);
            reportStage.initModality(Modality.APPLICATION_MODAL);
            reportStage.showAndWait();


        } catch (IOException ex) {
            // Nothing really to do here, the program is likely irreparably broken
        }
    }

    /**
     * Clears the add controls on the stage.
     */
    private void clearAddControls() {
        textFieldAddTitle.setText("");
        textFieldAddDescription.setText("");
        textFieldAddLocation.setText("");
        textFieldAddType.setText("");
        textFieldAddStartDate.setText("");
        textFieldAddStartTime.setText("");
        textFieldAddEndDate.setText("");
        textFieldAddEndTime.setText("");
    }

    /**
     * Clears the edit controls on the stage.
     */
    private void clearEditControls() {
        textFieldEditId.setText("");
        textFieldEditTitle.setText("");
        textFieldEditDescription.setText("");
        textFieldEditLocation.setText("");
        textFieldEditType.setText("");
        textFieldEditStartDate.setText("");
        textFieldEditStartTime.setText("");
        textFieldEditEndDate.setText("");
        textFieldEditEndTime.setText("");
    }

    /**
     * Called when the tableview is clicked on, pulls the information into the edit controls for modification.
     */
    private void onTableViewClicked() {
        // Try to grab an appointment
        Appointment editAppointment = tableViewAppointments.getSelectionModel().getSelectedItem();

        // Make sure that we're not null
        if (editAppointment != null) {
            textFieldEditId.setText(String.valueOf(editAppointment.getId()));
            textFieldEditTitle.setText(editAppointment.getTitle());
            textFieldEditDescription.setText(editAppointment.getDescription());
            textFieldEditLocation.setText(editAppointment.getLocation());
            textFieldEditType.setText(editAppointment.getType());

            // Split our string and then store it in the separate text boxes
            String[] cutDateTime = editAppointment.getFormattedLocalTimeStart().split(" ");

            textFieldEditStartDate.setText(cutDateTime[0]);
            textFieldEditStartTime.setText(cutDateTime[1]);

            cutDateTime = editAppointment.getFormattedLocalTimeEnd().split(" ");

            textFieldEditEndDate.setText(cutDateTime[0]);
            textFieldEditEndTime.setText(cutDateTime[1]);

            comboEditCustomerId.setValue(getCustomerNameFromCacheById(editAppointment.getCustomerId()));
            comboEditUserId.setValue(getUserNameFromCacheById(editAppointment.getUserId()));
            comboEditContact.setValue(getContactNameFromCacheById(editAppointment.getContactId()));
        }
    }

    /**
     * Gets the contacts name from the stored cache by their ID.
     * @param contactId The id we want to query for name information.
     * @return The name of the contact that we provided the ID for. "" if no match is found.
     */
    private String getContactNameFromCacheById(int contactId) {
        for (Map<String, String> contact: cachedContacts) {
            if (Integer.parseInt(contact.get("Contact_ID")) == contactId) {
                return contact.get("Contact_Name");
            }
        }

        return "";
    }

    /**
     * Gets a contacts ID from the stored cache by their name.
     * @param contactName The name we want to query for id information.
     * @return The id of the contact that we provided the name for. -1 if no match is found.
     */
    private int getContactIdFromCacheByName(String contactName) {
        for (Map<String, String> contact: cachedContacts) {
            if (contact.get("Contact_Name").equals(contactName)) {
                return Integer.parseInt(contact.get("Contact_ID"));
            }
        }

        return -1;
    }

    /**
     * Gets the customers name from the stored cache by their id.
     * @param customerId The id we want to query for name information.
     * @return The name of the contact that we provided the id for. "" if no match is found.
     */
    private String getCustomerNameFromCacheById(int customerId) {
        for (Map<String, String> customer: cachedCustomers) {
            if (Integer.parseInt(customer.get("Customer_ID")) == customerId) {
                return customer.get("Customer_Name");
            }
        }

        return "";
    }

    /**
     * Gets the customer id from the stored cache by their name.
     * @param customerName The name we want to query for id information.
     * @return The id of the contact that we provided the name for. -1 if no match is found.
     */
    private int getCustomerIdFromCacheByName(String customerName) {
        for (Map<String, String> customer: cachedCustomers) {
            if (customer.get("Customer_Name").equals(customerName)) {
                return Integer.parseInt(customer.get("Customer_ID"));
            }
        }

        return -1;
    }

    /**
     *Gets the user name from the stored cache by their id.
     * @param userId The id we want to query for name information.
     * @return The name of the user that we provided the id for. "" if no match is found.
     */
    private String getUserNameFromCacheById(int userId) {
        for (Map<String, String> user: cachedUsers) {
            if (Integer.parseInt(user.get("User_ID")) == userId) {
                return user.get("User_Name");
            }
        }

        return "";
    }

    /**
     * Gets the user id from the stored cache by their name.
     * @param userName The username we want to query for id information.
     * @return The id of the user that we provided the name for. -1 if no match is found.
     */
    private int getUserIdFromCacheByName(String userName) {
        for (Map<String, String> user: cachedUsers) {
            if (user.get("User_Name").equals(userName)) {
                return Integer.parseInt(user.get("User_ID"));
            }
        }

        return -1;
    }

    /**
     * Submits a new appointment to the database.
     * @return True if the operation is successful, false if not.
     * @throws IOException
     */
    private boolean submitNewAppointment() throws IOException {
        // First check if we pass all the rules for the appointment times and all our text boxes have values
        if (appointmentTimePassesAllRules(textFieldAddStartDate.getText(), textFieldAddStartTime.getText(), textFieldAddEndDate.getText(), textFieldAddEndTime.getText(),
                getCustomerIdFromCacheByName(comboAddCustomerId.getValue()), -1) && addControlsAllHaveValues()) {
            // Now create our wrapper object
            MySQLWrapper dbConnection = new MySQLWrapper();

            try (FileReader reader = new FileReader("src/main/MyAppLanguages.properties")) {
                // Tap into our properties file and grab our mysql connection string
                Properties ourProps = new Properties();
                ourProps.load(reader);

                // Attempt to connect
                boolean successful = dbConnection.createConnection(ourProps.getProperty("mysql-connection-string"), ourProps.getProperty("mysql-username"), ourProps.getProperty("mysql-password"));

                // If we have a successful connection, go ahead and do our queries
                if (successful) {
                    // Get the time stamp for the creation of the appointment in UTC
                    ZonedDateTime creationTimeStampInUTC = ZonedDateTime.now().withZoneSameInstant(ZoneId.of("Etc/UTC"));
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    String creationTimeStampFormatted = creationTimeStampInUTC.format(formatter);

                    // Grab our start ane end times
                    ZonedDateTime startTimeInUTC = ZonedDateTime.parse(textFieldAddStartDate.getText() + " " + textFieldAddStartTime.getText() + " " + ZoneId.systemDefault().getId(),
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")).withZoneSameInstant(ZoneId.of("Etc/UTC"));

                    ZonedDateTime endTimeInUTC = ZonedDateTime.parse(textFieldAddEndDate.getText() + " " + textFieldAddEndTime.getText() + " " + ZoneId.systemDefault().getId(),
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")).withZoneSameInstant(ZoneId.of("Etc/UTC"));

                    // Convert to formatted strings for the database insertion
                    String startTimeStampFormatted = startTimeInUTC.format(formatter);
                    String endTimeStampFormatted = endTimeInUTC.format(formatter);

                    // If we've made it this far go ahead and create our language resource bundle
                    ResourceBundle languageBundle = ResourceBundle.getBundle("main/lang", Locale.getDefault());

                    // Actually send the insertion request
                    if (dbConnection.insertEntry(MySQLWrapper.TableNames.appointments, new String[]{"Title", "Description", "Location", "Type", "Start", "End", "Create_Date",
                                    "Created_By", "Last_Update", "Last_Updated_By", "Customer_ID", "User_ID", "Contact_ID"},
                            new String[]{textFieldAddTitle.getText(), textFieldAddDescription.getText(), textFieldAddLocation.getText(), textFieldAddType.getText(), startTimeStampFormatted,
                                    endTimeStampFormatted, creationTimeStampFormatted, this.userName, creationTimeStampFormatted, this.userName, String.valueOf(getCustomerIdFromCacheByName(comboAddCustomerId.getValue())),
                                    String.valueOf(getUserIdFromCacheByName(comboAddUserId.getValue())), String.valueOf(getContactIdFromCacheByName(comboAddContact.getValue()))})) {

                        // And advise of our success
                        labelInfo.setText(languageBundle.getString("AppointmentAddSuccess"));

                        // And let's go ahead and refresh
                        getAppointments();

                        // And also clear the add controls
                        clearAddControls();
                    } else {
                        labelInfo.setText(languageBundle.getString("AppointmentAddFailure"));
                    }

                    // Close our connection once done
                    dbConnection.closeConnection();
                }
            } catch (FileNotFoundException ex) {
                // If we failed for whatever reason return false
                return false;
            }
        }

        // If we made it all the way to the end, return true
        return true;
    }

    /**
     * Submits edits to an existing appointment.
     * @return True if the operation is successful, otherwise false.
     * @throws IOException
     */
    private boolean submitUpdateToAppointment() throws IOException {
        // First check if we pass all the rules for the appointment times and all our text boxes have values
        if (appointmentTimePassesAllRules(textFieldEditStartDate.getText(), textFieldEditStartTime.getText(), textFieldEditEndDate.getText(), textFieldEditEndTime.getText(),
                getCustomerIdFromCacheByName(comboEditCustomerId.getValue()), Integer.parseInt(textFieldEditId.getText())) && editControlsAllHaveValues()) {
            // Now create our wrapper object
            MySQLWrapper dbConnection = new MySQLWrapper();

            try (FileReader reader = new FileReader("src/main/MyAppLanguages.properties")) {
                // Tap into our properties file and grab our mysql connection string
                Properties ourProps = new Properties();
                ourProps.load(reader);

                // Attempt to connect
                boolean successful = dbConnection.createConnection(ourProps.getProperty("mysql-connection-string"), ourProps.getProperty("mysql-username"), ourProps.getProperty("mysql-password"));

                // If we have a successful connection, go ahead and do our queries
                if (successful) {
                    // Get the time stamp for the creation of the appointment in UTC
                    ZonedDateTime updateTimeStampInUTC = ZonedDateTime.now().withZoneSameInstant(ZoneId.of("Etc/UTC"));
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    String updateTimeStampFormatted = updateTimeStampInUTC.format(formatter);

                    // Grab our start ane end times
                    ZonedDateTime startTimeInUTC = ZonedDateTime.parse(textFieldEditStartDate.getText() + " " + textFieldEditStartTime.getText() + " " + ZoneId.systemDefault().getId(),
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")).withZoneSameInstant(ZoneId.of("Etc/UTC"));

                    ZonedDateTime endTimeInUTC = ZonedDateTime.parse(textFieldEditEndDate.getText() + " " + textFieldEditEndTime.getText() + " " + ZoneId.systemDefault().getId(),
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")).withZoneSameInstant(ZoneId.of("Etc/UTC"));

                    // Convert to formatted strings for the database update
                    String startTimeStampFormatted = startTimeInUTC.format(formatter);
                    String endTimeStampFormatted = endTimeInUTC.format(formatter);

                    // If we've made it this far go ahead and create our language resource bundle
                    ResourceBundle languageBundle = ResourceBundle.getBundle("main/lang", Locale.getDefault());

                    if (dbConnection.updateEntry(MySQLWrapper.TableNames.appointments, new String[]{"Title", "Description", "Location", "Type", "Start", "End",
                                    "Last_Update", "Last_Updated_By", "Customer_ID", "User_ID", "Contact_ID"},
                            new String[]{textFieldEditTitle.getText(), textFieldEditDescription.getText(), textFieldEditLocation.getText(), textFieldEditType.getText(), startTimeStampFormatted,
                                    endTimeStampFormatted, updateTimeStampFormatted, this.userName, String.valueOf(getCustomerIdFromCacheByName(comboEditCustomerId.getValue())),
                                    String.valueOf(getUserIdFromCacheByName(comboEditUserId.getValue())), String.valueOf(getContactIdFromCacheByName(comboEditContact.getValue()))},
                            "Appointment_ID = " + textFieldEditId.getText())) {

                        // And advise of our success
                        labelInfo.setText(languageBundle.getString("AppointmentEditSuccessful"));

                        // And let's go ahead and refresh
                        getAppointments();

                        // And also clear the edit controls
                        clearEditControls();
                    } else {
                        labelInfo.setText(languageBundle.getString("AppointmentEditUnsuccessful"));
                    }

                    // Close our connection once done
                    dbConnection.closeConnection();
                }
            } catch (FileNotFoundException ex) {
                // If we failed for whatever reason return false
                return false;
            }
        }

        // If we made it all the way to the end, return true
        return true;
    }

    /**
     * Removes an appointment from the database.
     * @return True if the operation was successful, otherwise false.
     * @throws IOException
     */
    private boolean deleteExistingAppointment() throws IOException {
        // Create our resource language bundle so that we can give errors to our user
        ResourceBundle languageBundle = ResourceBundle.getBundle("main/lang", Locale.getDefault());

        // Do a quick check and make sure that we have a valid ID selected, otherwise quit
        if (textFieldEditId.getText().trim().length() == 0) {
            labelInfo.setText(languageBundle.getString("AppointmentDeleteInvalidID"));
            return false;
        }

        // Now create our wrapper object
        MySQLWrapper dbConnection = new MySQLWrapper();

        try (FileReader reader = new FileReader("src/main/MyAppLanguages.properties")) {
            // Tap into our properties file and grab our mysql connection string
            Properties ourProps = new Properties();
            ourProps.load(reader);

            // Attempt to connect
            boolean successful = dbConnection.createConnection(ourProps.getProperty("mysql-connection-string"), ourProps.getProperty("mysql-username"), ourProps.getProperty("mysql-password"));

            // If we have a successful connection, go ahead and do our queries
            if (successful) {
                // Attempt to delete
                if (dbConnection.deleteEntry(MySQLWrapper.TableNames.appointments, "Appointment_ID = " + textFieldEditId.getText())) {
                    // Cache our appointment type
                    String appointmentType = getAppointmentTypeFromId(Integer.parseInt(textFieldEditId.getText()));

                    // Refresh the appointment field
                    getAppointments();

                    // Inform of success
                    labelInfo.setText(languageBundle.getString("AppointmentID") + ": " + textFieldEditId.getText() + " - " + languageBundle.getString("AppointmentType") + ": " +
                            appointmentType + "\n" + languageBundle.getString("AppointmentDeleteSuccessful"));

                    // And also clear our edit controls
                    clearEditControls();
                } else {
                    labelInfo.setText(languageBundle.getString("AppointmentDeleteUnsuccessful"));
                    return false;
                }

                // Close our connection once done
                dbConnection.closeConnection();
            }
        } catch (FileNotFoundException ex) {
            // If we failed for whatever reason return false
            return false;
        }

        // If we make it all the way to the end we were able to delete the appointment
        return true;
    }

    /**
     * Retrieves the appointment type from a given appointment in the cache.
     * @param id The id of the appointment the type should be retrieved from.
     * @return The appointment type if found, otherwise "".
     */
    private String getAppointmentTypeFromId(int id) {
        for (Map<String, String> appointment: cachedAppointments) {
            if (Integer.parseInt(appointment.get("Appointment_ID")) == id) {
                return appointment.get("Type");
            }
        }

        return "";
    }

    /**
     * Checks if an appointment passes all of the defined rules, such as within business hours in EST, does not overlap with another appointment with the same customer, or is less than 15 minutes in length.
     * @param startDate The start date of the appointment to check.
     * @param startTime The start time of the appointment to check.
     * @param endDate The end date of the appointment to check.
     * @param endTime The end time of the appointment to check.
     * @param customerId The id of the customer to check.
     * @param appointmentId The appointment id of the appointment to check if we're editing an appointment, pass -1 if we're submitting a new appointment.
     * @return True if the operation is successful, otherwise false.
     */
    private boolean appointmentTimePassesAllRules(String startDate, String startTime, String endDate, String endTime, int customerId, int appointmentId) {
        // Create resource bundle for languages
        ResourceBundle languageBundle = ResourceBundle.getBundle("main/lang", Locale.getDefault());

        labelInfo.setText("");

        // Make sure that we actually have values
        if (startDate.length() == 0 || startTime.length() == 0 || endDate.length() == 0 || endTime.length() == 0) {
            labelInfo.setText(languageBundle.getString("AppointmentInvalidTime"));
            return false;
        }

        // Convert date/time to EST
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
            ZonedDateTime startTimeEastern = ZonedDateTime.parse(startDate.trim() + " " + startTime.trim() + " " + ZoneId.systemDefault().getId(), formatter)
                    .withZoneSameInstant(ZoneId.of("America/New_York"));

            // Now check to make sure that the time is within business hours
            if (startTimeEastern.getHour() < 8 || startTimeEastern.getHour() > 20 && startTimeEastern.getMinute() > 45) {
                labelInfo.setText(languageBundle.getString("InvalidAppointmentTime"));
                return false;
            }

            // If we've made it this far go ahead and create our end time in EST
            ZonedDateTime endTimeEastern = ZonedDateTime.parse(endDate.trim() + " " + endTime.trim() + " " + ZoneId.systemDefault().getId(), formatter)
                    .withZoneSameInstant(ZoneId.of("America/New_York"));

            // Now check if we're within business hours
            if (endTimeEastern.getHour() > 22 || endTimeEastern.getHour() < 9 && endTimeEastern.getMinute() < 15) {
                labelInfo.setText(languageBundle.getString("InvalidAppointmentTime"));
                return false;
            }

            // An additional check let's do some sanity checks - that the appointment is in fact at least 15 minutes apart and on the same day
            if (startTimeEastern.getDayOfMonth() != endTimeEastern.getDayOfMonth() || startTimeEastern.getYear() != endTimeEastern.getYear()) {
                labelInfo.setText(languageBundle.getString("InvalidAppointmentDay"));
                return false;
            }

            if (endTimeEastern.getHour() - startTimeEastern.getHour() == 0 && endTimeEastern.getMinute() - startTimeEastern.getMinute() < 15) {
                labelInfo.setText(languageBundle.getString("InvalidAppointmentTime"));
                return false;
            }

            // Make sure that the appointment doesn't overlap with pre-existing appointments
            for (Map<String, String> appointment: cachedAppointments) {
                // Check if the user id matches and appointment id doesn't (we don't care if the same appointment we're editing overlaps)
                if (Integer.parseInt(appointment.get("Customer_ID")) == customerId && Integer.parseInt(appointment.get("Appointment_ID")) != appointmentId) {
                    // Create objects for comparison
                    ZonedDateTime appointmentStartTimeUTC = ZonedDateTime.parse(appointment.get("Start") + " Etc/UTC", formatter);
                    ZonedDateTime appointmentEndTimeUTC = ZonedDateTime.parse(appointment.get("End") + " Etc/UTC", formatter);

                    // Convert our start time to UTC
                    ZonedDateTime startTimeUTC = startTimeEastern.withZoneSameInstant(ZoneId.of("Etc/UTC"));
                    if (startTimeUTC.equals(appointmentStartTimeUTC) || startTimeUTC.isAfter(appointmentStartTimeUTC) && startTimeUTC.isBefore(appointmentEndTimeUTC)) {
                        labelInfo.setText(languageBundle.getString("InvalidDateOverlap"));
                        return false;
                    }

                    // Do the same for the end time
                    ZonedDateTime endTimeUTC = endTimeEastern.withZoneSameInstant(ZoneId.of("Etc/UTC"));
                    if (endTimeUTC.equals(appointmentEndTimeUTC) || endTimeUTC.isAfter(appointmentStartTimeUTC) && endTimeUTC.isBefore(appointmentEndTimeUTC)) {
                        labelInfo.setText(languageBundle.getString("InvalidDateOverlap"));
                        return false;
                    }
                }
            }

        } catch (DateTimeParseException ex) {
            // If we pop a parse error our user didn't input the time correctly, so advise
            labelInfo.setText(languageBundle.getString("AppointmentInvalidTime"));
            return false;
        }


        // If we make it all the way to the end we can go ahead and return true
        return true;
    }

    /**
     * Ensures that all add controls have a value.
     * @return True if all appointment add controls have a value. Otherwise, false.
     */
    private boolean addControlsAllHaveValues() {
        if (textFieldAddTitle.getText().trim().length() == 0 || textFieldAddDescription.getText().trim().length() == 0 || textFieldAddLocation.getText().trim().length() == 0
                || textFieldAddTitle.getText().trim().length() == 0) {
            return false;
        }

        return true;
    }

    /**
     * Ensures that all edit controls have a value.
     * @return True if all appointment edit controls have a value. Otherwise, false.
     */
    private boolean editControlsAllHaveValues() {
        if (textFieldEditId.getText().trim().length() == 0 || textFieldEditTitle.getText().trim().length() == 0 || textFieldEditDescription.getText().trim().length() == 0
                || textFieldEditLocation.getText().trim().length() == 0 || textFieldEditTitle.getText().trim().length() == 0) {
            return false;
        }

        return true;
    }

    /**
     * Checks if a date is within the current Monday-Sunday week.
     * @param dateToCheck The date to check.
     * @return True if the date falls within the current Monday-Sunday week, otherwise false.
     */
    private boolean checkIfDateIsWithinCurrentWeek(String dateToCheck) {
        // Figure out when right now is
        ZonedDateTime rightNow = ZonedDateTime.now().withZoneSameInstant(ZoneId.systemDefault());

        // Convert the date we're looking at to our local time and into being a zoneddatetime
        ZonedDateTime convertedDateToCheck = ZonedDateTime.parse(dateToCheck + " Etc/UTC",
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")).withZoneSameInstant(ZoneId.systemDefault());

        // Figure out the start of the week (Monday-Sunday)
        ZonedDateTime startOfWeek = rightNow.minusDays((rightNow.getDayOfWeek().getValue() - 1));
        ZonedDateTime endOfWeek = rightNow.plusDays(7 - (rightNow.getDayOfWeek().getValue()));

        // Do a quick comparison to make sure that we're between the correct dates
        if (convertedDateToCheck.isAfter(startOfWeek) && convertedDateToCheck.isBefore(endOfWeek)) {
            return true;
        }

        // If we made it this far we didn't have any positive checks so return a failure
        return false;
    }

    /**
     * Checks if an appointment is starting within the next 15 minutes.
     * @param dateToCheck The appointment start date and time to check.
     * @return True if the appointment start time is upcoming in the next 15 minutes, otherwise false.
     */
    private boolean checkIfAppointmentIsWithinNextFifteenMinutes(String dateToCheck) {
        // Figure out when right now is
        ZonedDateTime rightNow = ZonedDateTime.now().withZoneSameInstant(ZoneId.systemDefault());

        // Convert the date we're looking at to our local time and into being a zoneddatetime
        ZonedDateTime convertedDateToCheck = ZonedDateTime.parse(dateToCheck + " Etc/UTC",
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")).withZoneSameInstant(ZoneId.systemDefault());

        // Figure out what our time stamp would be for 15 minutes from now
        ZonedDateTime fifteenMinutesFromNow = rightNow.plusMinutes(15);

        // Do a quick comparison to make sure that we're between the correct dates/times
        if (convertedDateToCheck.isAfter(rightNow) && convertedDateToCheck.isBefore(fifteenMinutesFromNow)) {
            return true;
        }

        // If we made it here, the time doesn't fit within the next 15-minute time frame
        return false;
    }

    /**
     * Checks if a date is within a current month.
     * @param dateToCheck The date to check.
     * @return True if the date is within the current month, otherwise false.
     */
    private boolean checkIfDateIsWithinCurrentMonth(String dateToCheck) {
        // Figure out when right now is
        ZonedDateTime rightNow = ZonedDateTime.now().withZoneSameInstant(ZoneId.systemDefault());

        // Convert the date we're looking at to our local time and into being a zoneddatetime
        ZonedDateTime convertedDateToCheck = ZonedDateTime.parse(dateToCheck + " Etc/UTC",
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")).withZoneSameInstant(ZoneId.systemDefault());

        // Check if it's the same month on both times
        return rightNow.getMonth().getValue() == convertedDateToCheck.getMonth().getValue();
    }

    private boolean checkIfDateNotWithinCurrentMonth(String dateToCheck) {
        // Figure out when right now is
        ZonedDateTime rightNow = ZonedDateTime.now().withZoneSameInstant(ZoneId.systemDefault());

        // Convert the date we're looking at to our local time and into being a zoneddatetime
        ZonedDateTime convertedDateToCheck = ZonedDateTime.parse(dateToCheck + " Etc/UTC",
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")).withZoneSameInstant(ZoneId.systemDefault());

        // Check if it's the same month on both times
        return rightNow.getMonth().getValue() != convertedDateToCheck.getMonth().getValue();
    }

//    Used to display all appointments.
//    @return the list displays all cached appointments.


    private ObservableList<Appointment> getAllAppointments() {
        ObservableList<Appointment> list = FXCollections.observableArrayList();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
        cachedAppointments.stream()


                .forEach(result -> {

                    Appointment newAppointment = new Appointment(Integer.parseInt(result.get("Appointment_ID")), result.get("Title"),
                            result.get("Description"), result.get("Location"), result.get("Type"), ZonedDateTime.parse(result.get("Start") + " Etc/UTC", formatter),
                            ZonedDateTime.parse(result.get("End") + " Etc/UTC", formatter), Integer.parseInt(result.get("Customer_ID")),
                            Integer.parseInt(result.get("User_ID")), Integer.parseInt(result.get("Contact_ID")));

                    list.add(newAppointment);
                });

        return list;

    }





    /**
     * Filters appointments by week or by month.
     * @param filterByWeek True if the appointments should be filtered by week, false if they should be filtered by month.
     * @return The filtered results from the cached appointments.
     */
    private ObservableList<Appointment> getFilteredAppointments(boolean filterByWeek) {
        // Create the list that will hold the results
        ObservableList<Appointment> list = FXCollections.observableArrayList();

        // Create a date formatter for our date/times below
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

        // Let's loop through and create our appointments and add them to the observable list
        cachedAppointments.stream()
                .filter(appointment -> {
                    if (filterByWeek) {
                        return checkIfDateIsWithinCurrentWeek(appointment.get("Start"));
                    } else {
                        return checkIfDateIsWithinCurrentMonth(appointment.get("Start"));

                    }

                })
                .forEach(result -> {
                    // Create the new appointment
                    Appointment newAppointment = new Appointment(Integer.parseInt(result.get("Appointment_ID")), result.get("Title"),
                            result.get("Description"), result.get("Location"), result.get("Type"), ZonedDateTime.parse(result.get("Start") + " Etc/UTC", formatter),
                            ZonedDateTime.parse(result.get("End") + " Etc/UTC", formatter), Integer.parseInt(result.get("Customer_ID")),
                            Integer.parseInt(result.get("User_ID")), Integer.parseInt(result.get("Contact_ID")));

                    // Add it to the list
                    list.add(newAppointment);
                });

        // Return our results
        return list;



    }


    // Currently allows default ability to search 'all' appointments.

    public void searchText(){

        ObservableList<Appointment> allAppointments = getAllAppointments();
        FilteredList<Appointment> filteredData = new FilteredList<>(allAppointments, b -> true);

        tableViewAppointments.setItems(filteredData);
        tableColumnId.setCellValueFactory(new PropertyValueFactory<>("id"));
        tableColumnTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        tableColumnDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        tableColumnLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        tableColumnType.setCellValueFactory(new PropertyValueFactory<>("type"));
        tableColumnStart.setCellValueFactory(new PropertyValueFactory<>("formattedLocalTimeStart"));
        tableColumnEnd.setCellValueFactory(new PropertyValueFactory<>("formattedLocalTimeEnd"));
        tableColumnCustomerId.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        tableColumnUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        tableColumnContactId.setCellValueFactory(new PropertyValueFactory<>("contactId"));

        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {

        filteredData.setPredicate(Appointment -> {

            if (newValue.isEmpty() || newValue.isBlank() || newValue == null) {
                return true;
            }

            String searchKeyword = newValue.toLowerCase();

            if (Appointment.getDescription().toLowerCase().contains(searchKeyword)) {
                return true;
            } else if (Appointment.getTitle().toLowerCase().contains(searchKeyword)) {
                return true;
            } else if (Appointment.getType().toLowerCase().contains(searchKeyword)) {
                return true;
            } else if (Appointment.getLocation().toLowerCase().contains(searchKeyword)) {
                return true;
//                } else if (Appointment.getContactId().toString.indexOf(searchKeyword) > -1) {
//                    return true;
//                } else if (Appointment.getCustomerId().toString.indexOf(searchKeyword) > -1) {
//                    return true;
            } else
                return false;
        });
    });
        SortedList<Appointment> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableViewAppointments.comparatorProperty());
        tableViewAppointments.setItems(sortedData);
    }




    /**
     * Prints out all upcoming appointments within the next 15 minutes for the logged-in user to the information label on the GUI.
     */
    private void printOutCloseAppointments() {
        // Create our language bundle
        ResourceBundle languageBundle = ResourceBundle.getBundle("main/lang", Locale.getDefault());

        labelInfo.setText(languageBundle.getString("UpcomingAppointment") + "\n");

        // Go through the appointments and find ones for our user and check if they're within the next 15 minutes
        cachedAppointments.stream()
                .filter(appointment -> Integer.parseInt(appointment.get("User_ID")) == this.userId)
                .filter(appointment -> checkIfAppointmentIsWithinNextFifteenMinutes(appointment.get("Start")))
                .forEach(result -> {
                    System.out.println("We are making it to the foreach.");
                    // Get our start time
                    ZonedDateTime startTime = ZonedDateTime.parse(result.get("Start") + " Etc/UTC",
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")).withZoneSameInstant(ZoneId.systemDefault());

                    // Get our end time
                    ZonedDateTime endTime = ZonedDateTime.parse(result.get("End") + " Etc/UTC",
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")).withZoneSameInstant(ZoneId.systemDefault());

                    // Alert our user of the upcoming times
                    labelInfo.setText(labelInfo.getText() + result.get("Appointment_ID") + ") " + startTime + " - " + endTime + "\n");
                });

        // Check our label to see if we output any appointments
        if (labelInfo.getText().equals(languageBundle.getString("UpcomingAppointment") + "\n")) {
            labelInfo.setText(languageBundle.getString("NoAppointments"));
        }
    }
}
