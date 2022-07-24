package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import main.Main;
import model.Customer;
import model.MySQLWrapper;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Controller class for customer.fxml, used to process all logic for editing or deleting customers.
 */
public class EditCustomerController {
    private Stage myStage = null;
    private ArrayList<Map<String, String>> firstLevelDivisions = null;
    private ArrayList<Map<String, String>> countries = null;
    private String userName = null;
    private int userId = -1;

    @FXML
    private TextField textFieldId;
    @FXML
    private TextField textFieldName;
    @FXML
    private TextField textFieldPhone;
    @FXML
    private TextField textFieldAddress;
    @FXML
    private TextField textFieldPostalCode;
    @FXML
    private Label labelId;
    @FXML
    private Label labelName;
    @FXML
    private Label labelPhone;
    @FXML
    private Label labelAddress;
    @FXML
    private Label labelPostalCode;
    @FXML
    private Label labelFLD;
    @FXML
    private Label labelCountry;
    @FXML
    private Label labelInfo;
    @FXML
    private Button buttonUpdate;
    @FXML
    private Button buttonDelete;
    @FXML
    private ComboBox<String> comboCountry;
    @FXML
    private ComboBox<String> comboFirstLevelDivision;

    @FXML
    private TableView<Customer> tableviewCustomer;
    @FXML
    private TableColumn<Customer, Integer> tableColumnId;
    @FXML
    private TableColumn<Customer, String> tableColumnName;
    @FXML
    private TableColumn<Customer, String> tableColumnAddress;
    @FXML
    private TableColumn<Customer, String> tableColumnPostalCode;
    @FXML
    private TableColumn<Customer, String> tableColumnPhone;
    @FXML
    private TableColumn<Customer, String> tableColumnFLD;
    @FXML
    private TableColumn<Customer, String> tableColumnCountry;

    /**
     * Initializes the controller class, grabs basic info from the MySQL database for operations and sets region specific text.
     *
     * @param myStage A reference to the controller's stage.
     */
    public void initialize(Stage myStage, String userName, int userId) {
        this.myStage = myStage;
        this.userName = userName;
        this.userId = userId;

        // Create our resource bundle
        ResourceBundle languageBundle = ResourceBundle.getBundle("main/lang", Locale.getDefault());

        // Try to grab our country and fld info
        try {
            // Try to grab our country and fld info
            if (!getCountryAndFirstLevelInformation()) {
                labelInfo.setText(languageBundle.getString("LoginDBConnectFailure"));
            }

            // Next try to pull our customer information
            if (!getCustomerInformation()) {
                labelInfo.setText(languageBundle.getString("LoginDBConnectFailure"));
            }

        } catch (IOException ex) {
            labelInfo.setText(languageBundle.getString("LoginConfigFileMissing"));
        }

        // Handling entirely through code vs .fxml scene editor seems to work better
        comboCountry.setOnAction(event -> onCountryClicked());
        buttonUpdate.setOnAction(event -> {
            try {
                onUpdateButtonClicked();
            } catch (IOException ex) {
                labelInfo.setText(languageBundle.getString("LoginConfigFileMissing"));
            }
        });
        buttonDelete.setOnAction(event -> {
            try {
                onDeleteButtonClicked();
            } catch (IOException ex) {
                labelInfo.setText(languageBundle.getString("LoginConfigFileMissing"));
            }
        });

        // When the stage is closing fire up the application form
        this.myStage.setOnCloseRequest(event -> onStageClose());

        // And adjust our control text
        setControlText();
    }

    /**
     * Called by initialize. Sets all the control text to the appropriate region language.
     */
    private void setControlText() {
        // Grab our resource bundle
        ResourceBundle languageBundle = ResourceBundle.getBundle("main/lang", Locale.getDefault());

        // Set the title of the stage
        this.myStage.setTitle(languageBundle.getString("CustomerEditTitle"));

        // Set the labels
        labelId.setText(languageBundle.getString("CustomerID"));
        labelName.setText(languageBundle.getString("CustomerName"));
        labelAddress.setText(languageBundle.getString("CustomerCountry"));
        labelCountry.setText(languageBundle.getString("CustomerCountry"));
        labelPhone.setText(languageBundle.getString("CustomerPhone"));
        labelPostalCode.setText(languageBundle.getString("CustomerPostalCode"));
        labelCountry.setText(languageBundle.getString("CustomerCountry"));
        labelFLD.setText(languageBundle.getString("CustomerFLD"));

        // Set the buttons
        buttonUpdate.setText(languageBundle.getString("CustomerUpdate"));
        buttonDelete.setText(languageBundle.getString("CustomerDelete"));

        // Google translate our table
        tableColumnId.setText(languageBundle.getString("CustomerID"));
        tableColumnName.setText(languageBundle.getString("CustomerName"));
        tableColumnCountry.setText(languageBundle.getString("CustomerCountry"));
        tableColumnFLD.setText(languageBundle.getString("CustomerFLD"));
        tableColumnPhone.setText(languageBundle.getString("CustomerPhone"));
        tableColumnPostalCode.setText(languageBundle.getString("CustomerPostalCode"));
        tableColumnAddress.setText(languageBundle.getString("CustomerAddress"));
    }

    /**
     * Grabs all the customer's from the MySQL database.
     *
     * @return True if the operation is successful, false if not.
     * @throws IOException
     */
    private boolean getCustomerInformation() throws IOException {
        // Now create our wrapper object
        MySQLWrapper dbConnection = new MySQLWrapper();

        try (FileReader reader = new FileReader("src/main/MyAppLanguages.properties")) {
            // Tap into our properties file and grab our mysql connection string
            Properties ourProps = new Properties();
            ourProps.load(reader);

            // Attempt to connect
            boolean successful = dbConnection.createConnection(ourProps.getProperty("mysql-connection-string"), ourProps.getProperty("mysql-username"), ourProps.getProperty("mysql-password"));

            String customerTable = dbConnection.getTableNameFromEnum(MySQLWrapper.TableNames.customers);
            String fldTable = dbConnection.getTableNameFromEnum(MySQLWrapper.TableNames.firstLevelDivisions);
            String countryTable = dbConnection.getTableNameFromEnum(MySQLWrapper.TableNames.countries);

            // If we have a successful connection, do our queries
            if (successful) {
                ArrayList<Map<String, String>> queryResults = dbConnection.selectFromTable(MySQLWrapper.TableNames.customers,
                        customerTable + ".Customer_ID, " + customerTable + ".Customer_Name, " + customerTable + ".Address, " +
                                customerTable + ".Postal_Code, " + customerTable + ".Phone, " + customerTable + ".Division_ID, " +
                                fldTable + ".Division, " + countryTable + ".Country", "",
                        "INNER JOIN " + fldTable + " ON " + customerTable + ".Division_ID = " + fldTable + ".Division_ID" +
                                " INNER JOIN " + countryTable + " ON " + fldTable + ".Country_ID = " + countryTable + ".Country_ID",
                        customerTable + ".Customer_ID ASC", 0);

                if (queryResults == null || queryResults.stream().count() == 0) {
                    // Something went wrong, advise and abort
                    return false;
                }

                // Close our connection once done
                dbConnection.closeConnection();

                // Create our observable list for the table view
                ObservableList<Customer> customers = FXCollections.observableArrayList();

                // Let's loop through and create our customers and add them to the observable list
                queryResults.stream()
                        .forEach(result -> {
                            // Create the new customer
                            Customer newCustomer = new Customer(Integer.parseInt(result.get("Customer_ID")), result.get("Customer_Name"),
                                    result.get("Address"), result.get("Postal_Code"), result.get("Phone"), Integer.parseInt(result.get("Division_ID")),
                                    result.get("Division"), result.get("Country"));

                            // Add it to the list
                            customers.add(newCustomer);
                        });

                // Set up our tableview
                tableviewCustomer.setItems(customers);
                tableColumnId.setCellValueFactory(new PropertyValueFactory<>("id"));
                tableColumnName.setCellValueFactory(new PropertyValueFactory<>("name"));
                tableColumnPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
                tableColumnAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
                tableColumnPostalCode.setCellValueFactory(new PropertyValueFactory<>("postalCode"));
                tableColumnFLD.setCellValueFactory(new PropertyValueFactory<>("firstLevelDivision"));
                tableColumnCountry.setCellValueFactory(new PropertyValueFactory<>("country"));
            }
        } catch (FileNotFoundException ex) {
            // If we failed return false
            return false;
        }

        // If we make it all the way to the end, return true
        return true;
    }

    /**
     * Gets all the country and first level information from the MySQL database.
     *
     * @return True if the operation is successful, false if not.
     * @throws IOException
     */
    private boolean getCountryAndFirstLevelInformation() throws IOException {
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
                String countryTable = dbConnection.getTableNameFromEnum(MySQLWrapper.TableNames.countries);
                String firstLevelDivisionsTable = dbConnection.getTableNameFromEnum(MySQLWrapper.TableNames.firstLevelDivisions);

                this.countries = dbConnection.selectFromTable(MySQLWrapper.TableNames.countries, "Country_ID, Country",
                        "", "", "Country_ID ASC", 0);

                this.firstLevelDivisions = dbConnection.selectFromTable(MySQLWrapper.TableNames.firstLevelDivisions,
                        firstLevelDivisionsTable + ".Division_ID, " + firstLevelDivisionsTable + ".Division, " + firstLevelDivisionsTable + ".Country_ID, countries.Country",
                        "", "INNER JOIN " + countryTable + " ON " + firstLevelDivisionsTable + ".Country_ID = " + countryTable + ".Country_ID",
                        firstLevelDivisionsTable + ".Division_ID ASC", 0);

                // Close our connection once done
                dbConnection.closeConnection();

                // Make sure that we actually got information
                if (this.countries == null || this.firstLevelDivisions == null || this.countries.stream().count() == 0 || this.firstLevelDivisions.stream().count() == 0) {
                    // Code here to handle our error
                    return false;
                }

                // Loop through and add all of our countries
                this.countries.stream().forEach(country -> comboCountry.getItems().add(country.get("Country")));

                // Fill in for the U.S. when first opening
                this.firstLevelDivisions.stream()
                        .filter(fld -> Integer.parseInt(fld.get("Country_ID")) == 1)
                        .forEach(fld -> comboFirstLevelDivision.getItems().add(fld.get("Division")));

                // Set to U.S. and first state
                comboCountry.setValue(this.countries.get(0).get("Country"));
                comboFirstLevelDivision.setValue((this.firstLevelDivisions.get(0).get("Division")));
            }
        } catch (FileNotFoundException ex) {
            // If we failed for whatever reason return false
            return false;
        }

        // If we make it all the way to the end, return true
        return true;
    }

    /**
     * When the user selects a country option, filters the appropriate first level divisions that correspond.
     */
    private void onCountryClicked() {
        // Empty the values currently in first level district
        comboFirstLevelDivision.getItems().clear();

        // Grab the value for the selected country
        String country = comboCountry.getValue();

        // Make sure that our value isn't empty
        if (country.trim().length() > 0) {
            // Loop through and add in all the first level divisions that belong to the selected country
            this.firstLevelDivisions.stream()
                    .filter(fld -> fld.get("Country").equals(country))
                    .forEach(fld -> comboFirstLevelDivision.getItems().add(fld.get("Division")));
        }
    }

    /**
     * Updates a selected customer in the MySQL database.
     *
     * @throws IOException
     */
    private void onUpdateButtonClicked() throws IOException {
        // Before we begin make sure that there's an ID in the text field
        if (textFieldId.getLength() == 0) {
            // If it is at 0 exit
            return;
        }

        // Create our resource bundle
        ResourceBundle languageBundle = ResourceBundle.getBundle("main/lang", Locale.getDefault());

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
                // Grab our translated timezone
                ZonedDateTime timeStamp = ZonedDateTime.now().withZoneSameInstant(ZoneId.of("Etc/UTC"));
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedDate = timeStamp.format(formatter);

                int divisionId = -1;

                for (Map<String, String> fld : firstLevelDivisions) {
                    if (fld.get("Division").equals(comboFirstLevelDivision.getValue())) {
                        divisionId = Integer.parseInt(fld.get("Division_ID"));
                    }
                }

                // Run into a brick wall
                assert (divisionId > -1);

                // Send the request to update the customer
                boolean updated = dbConnection.updateEntry(MySQLWrapper.TableNames.customers, new String[]{"Customer_Name", "Address", "Postal_Code", "Phone", "Last_Update", "Last_Updated_By", "Division_ID"},
                        new String[]{textFieldName.getText(), textFieldAddress.getText(), textFieldPostalCode.getText(), textFieldPhone.getText(), formattedDate, "user", String.valueOf(divisionId)},
                        "Customer_ID =" + textFieldId.getText());

                // If we're successful go ahead and update the customer table
                if (updated) {
                    getCustomerInformation();
                    labelInfo.setText(languageBundle.getString("CustomerUpdateSuccessful"));
                }

                // Close our connection once done
                dbConnection.closeConnection();
            }
        } catch (FileNotFoundException ex) {
            labelInfo.setText(languageBundle.getString("LoginConfigFileMissing"));
        }
    }

    /**
     * Deletes a customer and their corresponding appointments from the MySQL database.
     *
     * @throws IOException
     */
    private void onDeleteButtonClicked() throws IOException {
        // Before we begin make sure that there's an ID in the text field
        if (textFieldId.getLength() == 0) {
            // If it is at 0 exit
            return;
        }

        // Create our resource bundle
        ResourceBundle languageBundle = ResourceBundle.getBundle("main/lang", Locale.getDefault());

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
                // Delete appointments - There may be no appointments to delete so this request may come back false; we don't track the result beyond any thrown errors
                dbConnection.deleteEntry(MySQLWrapper.TableNames.appointments, "Customer_ID = " + textFieldId.getText());

                // Actually delete the customer
                boolean deleteSuccessful = dbConnection.deleteEntry(MySQLWrapper.TableNames.customers, "Customer_ID = " + textFieldId.getText());

                // Close our connection once done
                dbConnection.closeConnection();

                // Delete the customer
                if (deleteSuccessful) {
                    // Advise that we were successful
                    labelInfo.setText(languageBundle.getString("CustomerDeleteSuccessful"));

                    // Refresh the customer list
                    getCustomerInformation();

                    // Clear the controls as well
                    clearControls();
                } else {
                    labelInfo.setText(languageBundle.getString("LoginDBConnectFailure"));
                }
            }
        } catch (FileNotFoundException ex) {
            labelInfo.setText(languageBundle.getString("LoginConfigFileMissing"));
        }
    }

    /**
     * Sets all the controls to empty values after a customer has been deleted
     */
    private void clearControls() {
        textFieldId.setText("");
        textFieldName.setText("");
        textFieldAddress.setText("");
        textFieldPhone.setText("");
        textFieldPostalCode.setText("");
        textFieldPostalCode.setText("");
    }

    /**
     * Loads the appointment form when this window closes
     */
    private void onStageClose() {
        try {
            // Load the appointment form
            Stage customerStage = new Stage();
            FXMLLoader customerLoader = new FXMLLoader(Main.class.getResource("/view/appointments.fxml"));
            Scene customerScene = new Scene(customerLoader.load());
            AppointmentController controller = customerLoader.getController();
            customerStage.setScene(customerScene);
            controller.initialize(customerStage, this.userName, this.userId);
            customerStage.show();

            // Finally, close this window
            this.myStage.close();
        } catch (IOException ex) {
            // Nothing really to do here, the program is likely irreparably broken
        }
    }

    /**
     * Loads the text boxes with a selected customer's information when clicked.
     */
    @FXML
    private void onCustomerTableClicked() {
        // Grab our selected customer reference
        Customer selectedCustomer = tableviewCustomer.getSelectionModel().getSelectedItem();

        // Make sure that we're only working with valid data
        if (selectedCustomer != null) {
            // Pull all of our information and put it in the text boxes
            textFieldId.setText(String.valueOf(selectedCustomer.getId()));
            textFieldName.setText(selectedCustomer.getName());
            textFieldAddress.setText(selectedCustomer.getAddress());
            textFieldPhone.setText(selectedCustomer.getPhone());
            textFieldPostalCode.setText(selectedCustomer.getPostalCode());
            comboCountry.setValue(selectedCustomer.getCountry());
            comboFirstLevelDivision.setValue(selectedCustomer.getFirstLevelDivision());
        }

    }
}
