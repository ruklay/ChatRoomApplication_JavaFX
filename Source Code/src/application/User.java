package application;

import javafx.beans.property.SimpleStringProperty;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class User {
	private final SimpleStringProperty username;
	private final SimpleStringProperty password;

	public User() {
		this.username = new SimpleStringProperty("Ruklay");
		this.password = new SimpleStringProperty("1234");
	}

	public User(String username, String password) {
		this.username = new SimpleStringProperty(username);
		this.password = new SimpleStringProperty(password);
	}
	public String getUserName() {
		return username.get();
	}
	@XmlElement
	public void setUserName(String username) {
		this.username.set(username);
	}
	public String getPassword() {
		return password.get();
	}
	@XmlElement
	public void setPassword(String password) {
		this.password.set(password);
	}
}
