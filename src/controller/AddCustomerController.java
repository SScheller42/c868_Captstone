package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import main.Main;
import model.MySQLWrapper;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * The controller logic for addcustomer.fxml
 */
public class AddCustomerController {
    private Stage myStage = null;
    private ArrayList<Map<String, String>> firstLevelDivisions = null;
    private ArrayList<Map<String, String>> countries = null;
    private String userName = null;
    private int userId = -1;

    @FXML
    private TextField textFieldName;
    @FXML
    private TextField textFieldPhone;
    @FXML
    private TextField textFieldAddress;
    @FXML
    private TextField textFieldPostalCode;
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
    private  Label labelInfo;
    @FXML
    private Button buttonSubmit;
    @FXML
    private Button buttonClear;
    @FXML
    private Button buttonCancel;

    @FXML
    private ComboBox<String> comboCountry;
    @FXML
    private ComboBox<String> comboFirstLevelDivision;

    /**
     * Initializes the controller class, grabs basic info from the MySQL database for operations and sets region specific text.
     * Lambda Justification: Utilizing setOn... callbacks is easier than specifying multiple events in the FXML, and using a lambda is much cleaner and easier than doing multiple overrides.
     * @param myStage A reference to the controller's stage.
     */
    public void initialize(Stage myStage, String userName, int userId) {
        this.myStage = myStage;
        this.myStage.setOnCloseRequest(event -> onStageClose());
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

        } catch (IOException ex) {
            labelInfo.setText(languageBundle.getString("LoginConfigFileMissing"));
        }

        // Handling entirely through code vs .fxml scene editor seems to work better
        comboCountry.setOnAction(event -> onCountryClicked());

        // Set our button events
        buttonSubmit.setOnAction(event -> {
            try {
                submitCustomer();
            } catch (IOException ex) {
                // Alert our user of a corrupt or missing config file
                labelInfo.setText(languageBundle.getString("LoginConfigFileMissing"));
            }
        });
        this.myStage.setOnCloseRequest(event -> onStageClose());
        buttonCancel.setOnAction(event -> onStageClose());
        buttonClear.setOnAction(event -> clearControls());

        // Translate our controls to appropriate region
        setControlText();
    }

    /**
     * Called by initialize. Sets all the control text to the appropriate region language.
     */
    private void setControlText() {
        // Grab our resource bundle reference
        ResourceBundle languageBundle = ResourceBundle.getBundle("main/lang", Locale.getDefault());

        // Set the title of the stage
        this.myStage.setTitle(languageBundle.getString("CustomerAddTitle"));

        // Set the labels
        labelName.setText(languageBundle.getString("CustomerName"));
        labelAddress.setText(languageBundle.getString("CustomerAddress"));
        labelPhone.setText(languageBundle.getString("CustomerPhone"));
        labelPostalCode.setText(languageBundle.getString("CustomerPostalCode"));
        labelCountry.setText(languageBundle.getString("CustomerCountry"));
        labelFLD.setText(languageBundle.getString("CustomerFLD"));

        // Set the buttons
        buttonSubmit.setText(languageBundle.getString("LoginSubmit"));
        buttonCancel.setText(languageBundle.getString("ButtonCancel"));
        buttonClear.setText(languageBundle.getString("ButtonClear"));
    }

    /**
     * Gets all the country and first level information from the MySQL database.
     * Lambda Justification: Collection.stream()... is usually more readable and cleaner to read than a traditional foreach loop and easier to maintain than multiple if/else statements.
     * Using lambdas to take advantage of the stream()... functions helps maintain the readability of the code and therefore makes the most sense to use.
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
     * Lambda Justification: stream()... functions are cleaner and more maintainable than a traditional foreach loop and if/else statements. Lambdas are the best way to maintain that readability and take advantage of those functions.
     */
    private void onCountryClicked() {
        // Empty the values currently in first level district
        comboFirstLevelDivision.getItems().clear();

        // Grab the value for the selected country
        String country = comboCountry.getValue();

        // Make sure that our value isn't somehow empty
        if (country.trim().length() > 0) {
            // Loop through and add in all the first level divisions that belong to the selected country
            this.firstLevelDivisions.stream()
                    .filter(fld -> fld.get("Country").equals(country))
                    .forEach(fld -> comboFirstLevelDivision.getItems().add(fld.get("Division")));
        }
    }

    /**
     * Sets all the controls to empty values after a customer has been deleted
     */
    private void clearControls() {
        textFieldName.setText("");
        textFieldAddress.setText("");
        textFieldPhone.setText("");
        textFieldPostalCode.setText("");
        textFieldPostalCode.setText("");
    }

    /**
     * Attempts to insert the customer's information into the MySQL database.
     * @throws IOException
     */
    private void submitCustomer() throws IOException {
        // Make sure that we have everything filled out
        if (validInfoEntered()) {
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

                    for (Map<String, String> fld: firstLevelDivisions) {
                        if (fld.get("Division").equals(comboFirstLevelDivision.getValue())) {
                            divisionId = Integer.parseInt(fld.get("Division_ID"));
                        }
                    }

                    // Run into a brick wall
                    assert (divisionId > -1);

                    // Attempt to insert the new customer information
                    boolean insertSuccessful = dbConnection.insertEntry(MySQLWrapper.TableNames.customers,
                            new String[]{"Customer_Name", "Address", "Postal_Code", "Phone", "Create_Date", "Created_By", "Last_Update", "Last_updated_By", "Division_ID"},
                            new String[]{textFieldName.getText(), textFieldAddress.getText(), textFieldPostalCode.getText(), textFieldPhone.getText(), formattedDate, this.userName,
                                    formattedDate, this.userName, String.valueOf(divisionId)});


                    // Close our connection once done
                    dbConnection.closeConnection();

                    // Alert the user to our success and clear all the information
                    if (insertSuccessful) {
                        labelInfo.setText(languageBundle.getString("CustomerAddSuccessful"));
                        clearControls();
                    }
                }
            } catch (FileNotFoundException ex) {
                labelInfo.setText(languageBundle.getString("LoginConfigFileMissing"));
            }
        }
    }

    /**
     * Loads the appointment form
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
     * Checks if the user actually input values into the form
     * @return True if all controls have valid information, otherwise false.
     */
    private boolean validInfoEntered () {
        // Create our resource bundle
        ResourceBundle languageBundle = ResourceBundle.getBundle("main/lang", Locale.getDefault());

        if (textFieldName.getText().trim().length() == 0 || textFieldPhone.getText().trim().length() == 0 || textFieldAddress.getText().trim().length() == 0
                || textFieldPostalCode.getText().trim().length() == 0 || comboFirstLevelDivision.getValue().length() == 0) {
            labelInfo.setText(languageBundle.getString("CustomerAddMissingInfo"));
            return false;
        }

        // If we made it this far we're good
        return true;
    }
}

