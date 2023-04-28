package commands;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class CommandAU {

	private String username;
	private String password;
	private String cert;

	public CommandAU(String username, String password, String cert) {	
		this.username = username;
		this.password = password;
		this.cert = cert;
	}

	public boolean createUser(ObjectOutputStream outStream, ObjectInputStream inStream) throws IOException, ClassNotFoundException {

		outStream.writeObject("-au");
		outStream.writeObject(this.username);
		outStream.writeObject(this.password);
		outStream.writeObject(this.cert);
		
		return (Boolean) inStream.readObject();
	}
	
	
	
	
}
