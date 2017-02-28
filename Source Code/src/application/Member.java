package application;

import javafx.beans.property.*;

public class Member {
	private final SimpleStringProperty name;
	private final SimpleStringProperty IPAddress;
	private final SimpleIntegerProperty port;
	private final SimpleBooleanProperty isBlocked;

	public Member() {
		this.name = new SimpleStringProperty("Ruklay");
		this.IPAddress = new SimpleStringProperty("0.0.0.0");
		this.port = new SimpleIntegerProperty(1531);
		this.isBlocked = new SimpleBooleanProperty(false);
	}

	public Member(String name, String IPAddress, int port, boolean isBlocked) {
		this.name = new SimpleStringProperty(name);
		this.IPAddress = new SimpleStringProperty(IPAddress);
		this.port = new SimpleIntegerProperty(port);
		this.isBlocked = new SimpleBooleanProperty(false);
	}
	
	public final StringProperty nameProperty() {
        return this.name;
    }
    public final java.lang.String getName() {
        return this.nameProperty().get();
    }
    public final void setName(final java.lang.String name) {
        this.nameProperty().set(name);
    }
    
    public final StringProperty IPProperty() {
        return this.IPAddress;
    }
    public final java.lang.String getIPAddress() {
        return this.IPProperty().get();
    }
    public final void setIPAddress(final java.lang.String IPAddress) {
        this.nameProperty().set(IPAddress);
    }
    
    public final IntegerProperty portProperty() {
        return this.port;
    }
    public final int getPort() {
        return this.portProperty().get();
    }
    public final void setPort(final int port) {
        this.portProperty().set(port);
    }

    public final BooleanProperty isBlockedProperty() {
        return this.isBlocked;
    }
    public final boolean getIsBlocked() {
        return this.isBlockedProperty().get();
    }
    public final void setIsBlocked(final boolean isBlocked) {
        this.isBlockedProperty().set(isBlocked);
    }
}
