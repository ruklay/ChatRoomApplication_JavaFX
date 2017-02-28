package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.*;
import javafx.event.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class UserController implements Initializable {

    @FXML
    Button btnAddUser;

    @FXML
    Button btnRemoveUser;
    
    @FXML
    Label txtMessage;

    @FXML
    TextField txtUserName;

    @FXML
    TextField txtPassword;

    @FXML
    Button btnBackServer;

    @FXML
    TableView<User> tblUser;

    @FXML
    TableColumn<User, String> tblColUserName;

	@FXML
    TableColumn<User, String> tblColPassword;

	public ObservableList<User> listUser = FXCollections.observableArrayList ();
	public List<User> listU = new ArrayList<User>();
	public File userFile = new File("Users.xml");

	@Override // This method is called by the FXMLLoader when initialization is complete
	public void initialize(URL fxmlFileLocation, ResourceBundle resources) {

		//get listUser object from file
		try {
			if(!userFile.exists()) {
				userFile.createNewFile();
			} else {
				JAXBContext context = JAXBContext.newInstance(Users.class);
				Unmarshaller um = context.createUnmarshaller();

				// Reading XML from the file and unmarshalling.
				Users wrapper = (Users) um.unmarshal(userFile);

				listU.clear();
				listU.addAll(wrapper.getUsers());
				listUser = FXCollections.observableArrayList (listU);
			}
		} catch (Exception e) {
			System.out.println("From init Users");
		}

		txtMessage.setText("");
		tblColUserName.setCellValueFactory(new PropertyValueFactory<User, String>("userName"));
		tblColPassword.setCellValueFactory(new PropertyValueFactory<User, String>("password"));
		tblUser.setItems(listUser);
		
	}

	@FXML
	private void handleButtonActionAdd (ActionEvent event) throws Exception{
		if(!txtUserName.getText().trim().equals("") && !txtPassword.getText().trim().equals("")) {
			if(!isDuplicateName(txtUserName.getText().trim())) {
				//do add the User to listUser
				User user = new User(txtUserName.getText().trim(), txtPassword.getText().trim());
				listUser.add(user);

				//write listUser to file
				try {

					JAXBContext context = JAXBContext.newInstance(Users.class);
					Marshaller m = context.createMarshaller();
					m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

					// Wrapping our person data.
					Users wrapper = new Users();
					wrapper.setUsers(new ArrayList<User>(listUser));

					// Marshalling and saving XML to the file.
					m.marshal(wrapper, userFile);
					
					txtMessage.setText("");
				} catch (Exception e) {
					System.out.println("From add button");
				}
			} else {
				txtMessage.setText("The username has already existed");
			}
		}

		txtUserName.clear();
		txtPassword.clear();
//		txtUserName.textProperty().addListener(new ChangeListener<String>() {
//            @Override
//            public void changed(ObservableValue<? extends String> observableValue, String s, String s2) {
//            	txtMessage.setText("");
//            }
//        });
	}

	@FXML
	private void handleButtonActionRemove (ActionEvent event) throws Exception{
		//do remove the User from listUser
		listUser.remove(tblUser.getSelectionModel().getSelectedIndex());

		//write listUser to file
		try {
			JAXBContext context = JAXBContext.newInstance(Users.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			// Wrapping our person data.
			Users wrapper = new Users();
			wrapper.setUsers(new ArrayList<User>(listUser));

			// Marshalling and saving XML to the file.
			m.marshal(wrapper, userFile);
		} catch (Exception e) {
			System.out.println("From remove button");
		}

	}
	
	public boolean isLoginSuccessful(String username, String password) {
		//System.out.println("Username = "+username+" , password = "+password);
		ArrayList<User> users = new ArrayList<User>(listUser);
		boolean flag = false;
		for(User user: users) {
			if(user.getUserName().equals(username) && user.getPassword().equals(password)) {
				flag = true;
				//break;
			}
		}
		return flag;
	}
	
	public boolean isDuplicateName(String username) {
		ArrayList<User> users = new ArrayList<User>(listUser);
		boolean flag = false;
		for(User user: users) {
			if(user.getUserName().equals(username)) {
				flag = true;
				//break;
			}
		}
		return flag;
	}

	@FXML
	private void handleButtonActionBackServer (ActionEvent event) throws Exception{

	}

}