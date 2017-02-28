package application;

import javafx.application.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.fxml.*;

import java.io.*;

public class Client extends Application {
	ClientSocketConnection csc;

	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader fxmlLoaderClientLogin = new FXMLLoader(getClass().getResource("ClientLogin.fxml"));
			Pane rootClientLogin = (Pane)fxmlLoaderClientLogin.load();
			ClientLoginController cllc = fxmlLoaderClientLogin.<ClientLoginController>getController();

			Scene sceneLogin = new Scene(rootClientLogin);
			sceneLogin.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(sceneLogin);
			primaryStage.setTitle ("Client");
			primaryStage.show();

			FXMLLoader fxmlLoaderClient = new FXMLLoader(getClass().getResource("Client.fxml"));
			Parent rootClient = (Parent)fxmlLoaderClient.load();
			ClientController clc = fxmlLoaderClient.<ClientController>getController();

			csc = new ClientSocketConnection(clc, cllc);
			clc.setClientSocketConnection(csc);
			cllc.setClientSocketConnection(csc);

			Scene scene = new Scene(rootClient);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

			// First, display the login screen
			cllc.setSceneChat(scene);

			// The user can press enter to perform login
			cllc.btnLogin.setOnAction(event -> {
				cllc.doLogin(scene);
			});

			cllc.txtUserName.setOnAction(event -> {
				cllc.doLogin(scene);
			});

			cllc.txtPassword.setOnAction(event -> {
				cllc.doLogin(scene);
			});
			
			cllc.txtUserName.requestFocus();
			clc.txtSend.requestFocus();

		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stop() throws IOException {
		csc.stopClient();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
