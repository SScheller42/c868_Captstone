package main;

import controller.LoginScreen;
import database.JDBC;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loginFxmlLoader = new FXMLLoader(Main.class.getResource("/view/loginScreen.fxml"));

        Scene loginScene = new Scene(loginFxmlLoader.load());
        LoginScreen controller = loginFxmlLoader.getController();
        stage.setScene(loginScene);
        controller.setMyStage(stage);
        stage.show();
    }


    public static void main(String[] args){
       JDBC.openConnection();

          launch(args);


//        ResourceBundle rb = ResourceBundle.getBundle("c195/main/lang_fr.properties", Locale.getDefault());
//        if(Locale.getDefault().getLanguage().equals("fr"))

        JDBC.closeConnection();

    }
}
