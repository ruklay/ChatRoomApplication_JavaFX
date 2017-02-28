package application;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.*;
import javafx.event.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.*;
import javafx.util.Callback;

import java.net.*;
import java.util.*;
import java.awt.Desktop;
import java.io.*;

public class ClientController implements Initializable {
	@FXML
	Button btnSend;

	@FXML
	Button btnExit;
	
	@FXML
	Button btnSendFile;

	@FXML
	Label txtName;

	@FXML
	TextFlow txtConsoleClient;
	
	@FXML
	ComboBox<String> comboBoxMember;
	
	@FXML
	ScrollPane txtScrollPaneClient;

	@FXML
	TextField txtSend;

	@FXML
	TableView<Member> tblMemberList;

	@FXML
    TableColumn<Member, String> tblColName;

	@FXML
    TableColumn<Member, String> tblColIP;

    @FXML
    TableColumn<Member, Integer> tblColPort;

    @FXML
    TableColumn<Member, Boolean> tblColBlock;

	public ObservableList<Member> listObvMember = FXCollections.observableArrayList ();
	public ObservableList<String> listObvMemberName = FXCollections.observableArrayList ();
	private ClientSocketConnection csc;

	@Override // This method is called by the FXMLLoader when initialization is complete
	public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
		assert btnSend != null;

		txtSend.requestFocus();
		txtSend.setOnAction(event -> {
			try {
				performSend(txtSend.getText());
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		// Set the member list
		tblColName.setCellValueFactory(new PropertyValueFactory<Member, String>("name"));
		tblColIP.setCellValueFactory(new PropertyValueFactory<Member, String>("IPAddress"));
		tblColPort.setCellValueFactory(new PropertyValueFactory<Member, Integer>("port"));
		
		// Set the block's check box
		// Value of isBlocked is automatically updated based on check box value 
		tblColBlock.setCellValueFactory(new PropertyValueFactory<Member, Boolean>("isBlocked"));
		tblColBlock.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Member, Boolean>, ObservableValue<Boolean>>() {
            @Override
            public ObservableValue<Boolean> call(CellDataFeatures<Member, Boolean> param) {
            	//System.out.println("Value is : "+param.getValue().isBlockedProperty());
                return param.getValue().isBlockedProperty();
            }
        });
		tblColBlock.setCellFactory( CheckBoxTableCell.forTableColumn(tblColBlock) );
		tblColBlock.setEditable( true );
		tblMemberList.setItems(listObvMember);
		
		// Set the "Send to" combo box
		listObvMemberName.add("All members");
		comboBoxMember.setItems(listObvMemberName);
		comboBoxMember.setValue("All members");

	}

	@FXML
	private void handleButtonActionSend (ActionEvent event) throws Exception{
		performSend(txtSend.getText());
	}

	public void performSend(String message) throws IOException {
		if (!message.trim().equals("")) {
			if(comboBoxMember.getValue() == null || comboBoxMember.getValue().equals("All members")) {
				// Send broadcast message
				this.csc.sendMessage(message);
			} else {
				// Send private message
				String sender = txtName.getText();
				String receiver = comboBoxMember.getValue();
				this.csc.sendMessage(ServerConstants.PRIVATE_MESSAGE, comboBoxMember.getValue()+"|"+message);
				if (!sender.equals(receiver)) {
					addStringConsole("From " + sender + " to "+receiver + "> " + message, ServerConstants.PRIVATE_MESSAGE);
				}
			}
		}
		txtSend.clear();
		txtSend.requestFocus();
	}

	@FXML
	private void handleButtonActionExit (ActionEvent event) throws Exception{
		csc.stopClient();

		Stage stage = (Stage) btnExit.getScene().getWindow();
	    stage.close();
		//Platform.exit();
	}

	public void addStringConsole(String str, int id){
		
		Text name;
		Text message;
		
		if (id == ServerConstants.BROADCAST_MESSAGE) {
			name = new Text(str.substring(0, str.indexOf(">")+1));
			message = new Text(str.substring(str.indexOf(">")+1, str.length())+"\n");
			
			name.setFill(Color.BLUE);
	
		} else if (id == ServerConstants.SYSTEM_MESSAGE) {
			name = new Text("");
			message = new Text(str+"\n");
			
		} else if (id == ServerConstants.PRIVATE_MESSAGE) {
			name = new Text(str.substring(0, str.indexOf(">")+1));
			message = new Text(str.substring(str.indexOf(">")+1, str.length())+"\n");
			
			name.setFill(Color.RED);
			
		} else if (id == ServerConstants.ERROR_MESSAGE) {
			name = new Text("");
			message = new Text(str+"\n");
			
			name.setFill(Color.RED);
			message.setFill(Color.RED);
			
		} else if (id == ServerConstants.SEND_ACK_FILE_TO_CLIENT) {
			name = new Text(str.substring(0, str.indexOf(">")+1));
			message = new Text(str.substring(str.indexOf(">")+1, str.length())+"\n");
			
			name.setFill(Color.BLUE);
			
		} else {
			name = new Text("");
			message = new Text(str+"\n");
		}
		
		Platform.runLater(new Runnable() {
			public void run() {
				txtConsoleClient.getChildren().addAll(name, message);
				txtScrollPaneClient.setVvalue(1.0); 
			}
		});
		
		System.out.println(str);

	}
	
	public void addLinkLoadFile(String fileName) {
		Hyperlink clickHere = new Hyperlink("Click here");
		Text toDownload = new Text(" to download ..\n");
		
		Platform.runLater(new Runnable() {
			public void run() {
				txtConsoleClient.getChildren().addAll(clickHere, toDownload);
				txtScrollPaneClient.setVvalue(1.0); 
			}
		});
		
		clickHere.setOnAction(event -> {
			try {
				csc.getFileFromServer(fileName);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		
	}
	
	public void addLinkOpenFile(String fileName) {
		Text toOpen = new Text("Click to open the file: ");
		Hyperlink linkFileName = new Hyperlink(fileName);
		
		Platform.runLater(new Runnable() {
			public void run() {
				txtConsoleClient.getChildren().addAll(toOpen, linkFileName);
				txtConsoleClient.getChildren().add(new Text("\n"));
				txtScrollPaneClient.setVvalue(1.0); 
			}
		});
		
		linkFileName.setOnAction(event -> {
			try {
				Desktop.getDesktop().open(new File("user_files\\"+txtName.getText()+"\\"+fileName));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		
	}
	
	@FXML
	private void handleButtonActionSendFile (ActionEvent event) throws Exception{
		FXMLLoader fxmlLoaderSendFile = new FXMLLoader(getClass().getResource("SendFile.fxml"));
		Pane rootSendFile = (Pane)fxmlLoaderSendFile.load();
		SendFileController sfc = fxmlLoaderSendFile.<SendFileController>getController();
		
		sfc.setClientController(this);
		sfc.setClientSocketConnection(this.csc);
		sfc.setListObvMemberName(this.listObvMemberName);

		Scene sceneSendFile = new Scene(rootSendFile);
		sceneSendFile.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		
		Stage stage = new Stage();
		stage.setScene(sceneSendFile);
		stage.setTitle ("Send a File ..");
		stage.show();
	}

	public void setClientSocketConnection (ClientSocketConnection csc) {
		this.csc = csc;
	}

}