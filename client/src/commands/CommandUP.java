package commands;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.UnknownHostException;

public class CommandUP {
	
	private String username;
	private String password;

	public CommandUP(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	public Boolean verifyLogin(ObjectOutputStream outStream, ObjectInputStream inStream) throws IOException, ClassNotFoundException {
		
		outStream.writeObject("-u");
		outStream.writeObject(this.username);
		outStream.writeObject(this.password);
		
		return (Boolean) inStream.readObject();
	}
}
