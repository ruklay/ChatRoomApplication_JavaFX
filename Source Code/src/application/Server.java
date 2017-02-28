package application;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.text.Text;
import javafx.fxml.*;
import java.io.*;

public class Server extends Application {
	ServerSocketConnection ssc;

	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Server.fxml")); 
			Parent root = (Parent)fxmlLoader.load(); 
			ServerController svc = fxmlLoader.<ServerController>getController();
			
			FXMLLoader fxmlLoaderUser = new FXMLLoader(getClass().getResource("Users.fxml")); 
			Parent rootUser = (Parent)fxmlLoaderUser.load(); 
			UserController usc = fxmlLoaderUser.<UserController>getController();
			Scene sceneUser = new Scene(rootUser);
			
			svc.addStringConsole("Server has started ..", ServerConstants.SYSTEM_MESSAGE);
			
			ssc = new ServerSocketConnection(svc, usc);
			svc.setServerSocketConnection(ssc);
			
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.setTitle ("Chat Server");
			primaryStage.show();
			
			// Toggle between Chat Server scene and Manage Users scene
			svc.btnManageUser.setOnAction(new EventHandler<ActionEvent>() {
				public void handle(ActionEvent t) {
					primaryStage.setScene(sceneUser);
					primaryStage.setTitle ("Manage Users");
					primaryStage.show();
				}
			});
			
			usc.btnBackServer.setOnAction(new EventHandler<ActionEvent>() {
				public void handle(ActionEvent t) {
					primaryStage.setScene(scene);
					primaryStage.setTitle ("Chat Server");
					primaryStage.show();
				}
			});
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void stop() throws IOException {
		//if(ssc.isStart()) 
			ssc.stopServer();	
	}

	public static void main(String[] args) {
		launch(args);
	}
}
