package application;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class SendFileController implements Initializable {
	@FXML
	Button btnBrowse;
	
	@FXML
	Button btnSend;
	
	@FXML
	Button btnExit;

	@FXML
	TextField txtFileName;
	
	@FXML
	ComboBox<String> comboBoxMember;

	private ClientController clc;
	private ClientSocketConnection csc;
	
	public ObservableList<String> listObvMemberName = FXCollections.observableArrayList ();

	@Override // This method is called by the FXMLLoader when initialization is complete
	public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
		
	}

	@FXML
	private void handleButtonActionBrowse(ActionEvent event) {
		Stage stage = (Stage) btnBrowse.getScene().getWindow();
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Resource File");
		File selectedFile = fileChooser.showOpenDialog(stage);
		if (selectedFile != null) {
			txtFileName.setText(selectedFile.getAbsolutePath());
		}
	}
	
	@FXML
	private void handleButtonActionSend(ActionEvent event) {
		String fileName = txtFileName.getText();
		
		if(!fileName.equals("") && (new File(fileName).isFile())) {
			clc.addStringConsole("Sending a file .. "+fileName, ServerConstants.SYSTEM_MESSAGE);
			clc.txtSend.clear();
			clc.txtSend.requestFocus();
			
			csc.performSendFile(comboBoxMember.getValue()+"|"+fileName);
		}
		
		Stage stage = (Stage) btnExit.getScene().getWindow();
	    stage.close();
	}
	
	@FXML
	private void handleButtonActionExit(ActionEvent event) {
		Stage stage = (Stage) btnExit.getScene().getWindow();
	    stage.close();
	}
	
	public void setClientController(ClientController clc) {
		this.clc = clc;
	}
	
	public void setClientSocketConnection(ClientSocketConnection csc) {
		this.csc = csc;
	}
	
	public void setListObvMemberName(ObservableList<String> listObvMemberName) {
		this.listObvMemberName = listObvMemberName;
		comboBoxMember.setItems(this.listObvMemberName);
		comboBoxMember.setValue("All members");
	}

}