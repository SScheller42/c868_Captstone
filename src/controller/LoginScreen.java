package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import main.Main;
import model.MySQLWrapper;

import java.io.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;



    public class LoginScreen {
        @FXML
        private Label localeLabel;
        @FXML
        private Label infoLabel;

        @FXML
        private TextField userNameTextField;

        @FXML
        private PasswordField passwordTextField;

        @FXML
        private Button submitButton;

        private Stage myStage;

        /**
         * Called after setMyStage, runs necessary initialization code.
         */
        private void initialize() {
            setControlText();

            // Set up our action event
            submitButton.setOnAction(event -> {
                try {
                    onSubmitClicked();
                } catch (IOException ex) {
                    // Nothing really to do here
                }
            });
        }

        /**
         * Sets the appropriate region specific language.
         */
        private void setControlText() {
            // Grab our resource bundle
            ResourceBundle languageBundle = ResourceBundle.getBundle("main/lang", Locale.getDefault());

            // Set the default text depending on language
            this.myStage.setTitle(languageBundle.getString("LoginTitle"));
            userNameTextField.setPromptText(languageBundle.getString("LoginUserName"));
            passwordTextField.setPromptText(languageBundle.getString("LoginPassword"));
            submitButton.setText(languageBundle.getString("LoginSubmit"));

            // This is how we get the default zoneid - DO NOT FORGET TO MAKE THE ZONE ID LOOK NATIVE
            localeLabel.setText(languageBundle.getString("LoginLocale") + ZoneId.systemDefault().getId());
        }

        /**
         * Sets the internal reference to this controller's stage.
         *
         * @param myStage The stage reference for this controller.
         */
        public void setMyStage(Stage myStage) {
            this.myStage = myStage;
            initialize();
        }

        /**
         * Loads the appointment form once the user has successfully logged in.
         *
         * @param userName The username of the account that's logged in.
         * @param userId   The user id of the account that's logged in.
         */
        private void loadAppointmentForm(String userName, int userId) {
            try {
                // Load the appointment form
                Stage customerStage = new Stage();
                FXMLLoader customerLoader = new FXMLLoader(Main.class.getResource("/view/appointments.fxml"));
                Scene customerScene = new Scene(customerLoader.load());
                AppointmentController controller = customerLoader.getController();
                customerStage.setScene(customerScene);
                controller.initialize(customerStage, userName, userId);
                customerStage.show();

                // Finally, close this window
                this.myStage.close();
            } catch (IOException ex) {
                // Nothing really to do here, the program is likely irreparably broken
            }
        }

        /**
         * Event fired when submit button on login.fxml is clicked. Attempts to log in using the text fields for username and password.
         * Provides UI error feedback if unsuccessful, calls loadAppointmentForm if successful.
         *
         * @throws IOException
         */
        @FXML
        private void onSubmitClicked() throws IOException {
            // Grab our language resource bundle
            ResourceBundle languageBundle = ResourceBundle.getBundle("main/lang", Locale.getDefault());

            // Verify that we have a valid username and text
            if (userNameTextField.getText().length() == 0 || passwordTextField.getText().length() == 0) {
                // Alert the user they have to input a username and password
                infoLabel.setText(languageBundle.getString("LoginMissing"));

                // Since we have no info go ahead and exit
                return;
            }

            // Now create our wrapper object
            MySQLWrapper dbConnection = new MySQLWrapper();

            try (FileReader reader = new FileReader("src/main/MyAppLanguages.properties")) {
                // Tap into our properties file and grab our mysql connection string
                Properties ourProps = new Properties();
                ourProps.load(reader);

                infoLabel.setText(languageBundle.getString("LoginAttempt"));

                // Attempt to connect
                boolean successful = dbConnection.createConnection(ourProps.getProperty("mysql-connection-string"), ourProps.getProperty("mysql-username"), ourProps.getProperty("mysql-password"));

                if (successful) {
                    // Submit our username and password
                    ArrayList<Map<String, String>> queryResults = dbConnection.selectFromTable(MySQLWrapper.TableNames.users, "User_ID",
                            "User_Name = '" + userNameTextField.getText() + "' AND Password = '" + passwordTextField.getText() + "'", "", "", 0);

                    // Close our connection once done
                    dbConnection.closeConnection();

                    // If we get a result back then we're good to proceed to the next form, if we get 0 back then we had the wrong username or password
                    if (queryResults == null || queryResults.stream().count() == 0) {
                        infoLabel.setText(languageBundle.getString("LoginIncorrect"));

                        // Print out our failure
                        printLoginAttempt(userNameTextField.getText(), false);
                    } else {
                        // Print out our success
                        printLoginAttempt(userNameTextField.getText(), true);

                        loadAppointmentForm(userNameTextField.getText(), Integer.parseInt(queryResults.get(0).get("User_ID")));
                    }

                } else {
                    infoLabel.setText(languageBundle.getString("LoginDBConnectFailure"));
                }
            } catch (FileNotFoundException ex) {
                // Alert our user that we were unable to read the properties file
                infoLabel.setText(languageBundle.getString("LoginConfigFileMissing"));
            }
        }

        /**
         * Prints login attempts to login_activity.txt file.
         *
         * @param userName      The username connected to the attempted login.
         * @param wasSuccessful True if the connection attempt was successful, false if not.
         */
        private void printLoginAttempt(String userName, boolean wasSuccessful) {
            // Grab our time stamp
            ZonedDateTime nowInUTC = ZonedDateTime.now().withZoneSameInstant(ZoneId.of("Etc/UTC"));

            // Resource bundle for language
            ResourceBundle languageBundle = ResourceBundle.getBundle("main/lang", Locale.getDefault());

            // Create our string builder
            StringBuilder builder = new StringBuilder("[");
            builder.append(nowInUTC.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            builder.append("] ");
            builder.append(userName);
            builder.append(" ");
            builder.append(languageBundle.getString("LoginAttemptLogMessage"));
            builder.append(wasSuccessful ? languageBundle.getString("LoginAttemptLogMessageSuccess") : languageBundle.getString("LoginAttemptLogMessageUnsuccessful"));


            // Output to our file
            try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("login_activity.txt", true))) {
                bufferedWriter.write(builder.toString());
                bufferedWriter.newLine();
            } catch (IOException ex) {
                // Nothing really to do here but catch the error
            }
        }

        public void onButtonAction(ActionEvent actionEvent) {
            System.out.println("Logging in");
        }
    }
