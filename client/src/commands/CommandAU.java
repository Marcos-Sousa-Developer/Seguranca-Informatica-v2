package commands;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class CommandAU {

	private String username;
	private String password;
	private FileInputStream certInFile;

	public CommandAU(String username, String password, FileInputStream certInFile) {	
		this.username = username;
		this.password = password;
		this.certInFile = certInFile;
	}

	public boolean createUser(ObjectOutputStream outStream, ObjectInputStream inStream) throws IOException, ClassNotFoundException {

		outStream.writeObject("-au");
		outStream.writeObject(this.username);
		outStream.writeObject(this.password);
		outStream.writeObject(this.certInFile.readAllBytes());
		
		return (Boolean) inStream.readObject();
	}
	
	
	
	
}
