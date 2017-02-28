package application;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.io.*;

public class ClientLoginController implements Initializable {
	@FXML
	Button btnLogin;

	@FXML
	Button btnExit;

	@FXML
	TextField txtUserName;

	@FXML
	PasswordField txtPassword;

	@FXML
	TextField txtPort;

	@FXML
	Label txtMessage;

	private ClientSocketConnection csc;
	private Scene sceneChat;

	@Override // This method is called by the FXMLLoader when initialization is complete
	public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
		assert btnLogin != null;

		// Default the cursor to the input username
		txtUserName.requestFocus();
		txtPort.setText("5000");
		txtMessage.setText("");

	}

	@FXML
	private void handleButtonActionLogin (ActionEvent event) throws Exception{

	}

	@FXML
	private void handleButtonActionExit (ActionEvent event) throws Exception{
		Stage stage = (Stage) btnExit.getScene().getWindow();
	    stage.close();
	}

	public void doLogin(Scene scene) {
		// Perform login
		// If success, change scene to chat scene (by methos showChatScene)
		// If not, show error, clear all inputs, set cursor to the input username

		String userName = txtUserName.getText().trim();
		String password = txtPassword.getText().trim();

		if(!userName.equals("") && !password.equals("")) {
			csc.setPort(Integer.parseInt(txtPort.getText()));
			txtMessage.setText("Loging in ..");

			try {
				csc.startClient();
				Platform.runLater(new Runnable() {
					public void run() {
						try {
							TimeUnit.SECONDS.sleep(1);
							csc.doValidateUserNamePassword(userName, password);
						} catch (Exception e) {
							txtMessage.setText("Cannot connect to the server (Code 1).\nPlease try again later.");
							disableInput();
							e.printStackTrace();
						}
					}
				});
			} catch (Exception e) {
				txtMessage.setText("Cannot connect to the server (Code 2).\nPlease try again later.");
				disableInput();
				e.printStackTrace();
			}
		}
	}

	public void clearAndFocusText() {
		txtUserName.setText("");
		txtPassword.setText("");
		txtUserName.requestFocus();
	}

	public void disableInput() {
		txtUserName.setDisable(true);
		txtPassword.setDisable(true);
		txtPort.setDisable(true);
		btnLogin.setDisable(true);
	}

	public void showChatScene(String username) throws IOException {
		// Switch to the chat scene
		// This method is called from login controller, after successful authentication

		Stage stage = (Stage) btnLogin.getScene().getWindow();

		stage.setTitle (username+"'s Chat Room");
		stage.setScene(sceneChat);
		stage.show();
	}

	public void setClientSocketConnection (ClientSocketConnection csc) {
		this.csc = csc;
	}

	public void setSceneChat(Scene scene) {
		this.sceneChat = scene;
	}
}