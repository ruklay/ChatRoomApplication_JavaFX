package application;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.*;
import javafx.event.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class ServerController implements Initializable {

	@FXML
	TextFlow txtConsole;
	
	@FXML
	ScrollPane txtScrollPane;

	@FXML
	Button btnStart;
	
	@FXML
	Button btnStop;
	
	@FXML
	Button btnManageUser;
	
	@FXML
	ListView<String> lstMember = new ListView<String>();
	
	@FXML
	TableView<Member> tblMemberList;
	
	@FXML
	TextField txtPort;

	@FXML
    TableColumn<Member, String> tblColName;
	
	@FXML
    TableColumn<Member, String> tblColIP;
    
    @FXML
    TableColumn<Member, Integer> tblColPort;

	public ObservableList<Member> listObvMember = FXCollections.observableArrayList ();

	private ServerSocketConnection ssc;	

	@Override // This method is called by the FXMLLoader when initialization is complete
	public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
		assert txtConsole != null;
		assert btnStop != null;
		assert lstMember != null;
		assert txtPort != null;
		
		txtPort.setText("5000");
		btnStop.setDisable(true);
		
		//lstMember.setItems(listObvMember);
		tblColName.setCellValueFactory(new PropertyValueFactory<Member, String>("name"));
		tblColIP.setCellValueFactory(new PropertyValueFactory<Member, String>("IPAddress"));
		tblColPort.setCellValueFactory(new PropertyValueFactory<Member, Integer>("port"));
		tblMemberList.setItems(listObvMember);
		
	}

	@FXML
	private void handleButtonActionStart (ActionEvent event) throws Exception{
		ssc.startServer();
		btnStart.setDisable(true);
		btnStop.setDisable(false);
		txtPort.setDisable(true);
	}

	@FXML
	private void handleButtonActionStop (ActionEvent event){
		btnStart.setDisable(true);
		btnStop.setDisable(true);
		//if(ssc.isStart()) 
			ssc.stopServer();	
		addStringConsole("Server has stopped", ServerConstants.SYSTEM_MESSAGE);
	}
	
	@FXML
	private void handleButtonActionManageUser (ActionEvent event){
		
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
			
		} else {
			name = new Text("");
			message = new Text(str+"\n");
		}
		
		Platform.runLater(new Runnable() {
			public void run() {
				txtConsole.getChildren().addAll(name, message);
				txtScrollPane.setVvalue(1.0); 
			}
		});
		
		System.out.println(str);
	}
	
	public void setServerSocketConnection (ServerSocketConnection ssc) {
		this.ssc = ssc;
	}
}