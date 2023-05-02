package commands;


import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class CommandD {
	
	private String username;
	private String password; 
	private String destUsername;
	private String commandToDo; 
	private List<String> filesDestUsername;
	
	public CommandD(String username, String password, String destUsername, String commandToDo, List<String> filesDestUsername) {
		
		this.username = username;
		this.password = password;
		this.destUsername = destUsername;
		this.commandToDo = commandToDo;
		this.filesDestUsername = filesDestUsername;
		
	}
	
	private void getDestPublicCert(ObjectOutputStream outStream, ObjectInputStream inStream) throws IOException, ClassNotFoundException { 
		
		String path = "../keystore/"+this.destUsername+".cer"; 
		
		File destCert = new File(path); 
		
		if(!destCert.exists()) {
			outStream.writeObject(true);
			
			if((boolean) inStream.readObject() == false) {
				System.out.println("User " + this.destUsername + " does not exist!");
				System.exit(-1);
			}
			else {
				
				byte[] destCertbytes = inStream.readAllBytes(); 
				FileOutputStream newDestUserCert = new FileOutputStream(path); 
				newDestUserCert.write(destCertbytes);
				newDestUserCert.close();
			}
		}
		
		else {
			outStream.writeObject(false);
		}
	}
	
	public void sendToServer(ObjectOutputStream outStream, ObjectInputStream inStream) throws IOException, ClassNotFoundException, InvalidKeyException, UnrecoverableKeyException, NoSuchAlgorithmException, NoSuchPaddingException, KeyStoreException, CertificateException, IllegalBlockSizeException, SignatureException, BadPaddingException {
		
		//Send the option first
		outStream.writeObject("-d");
		outStream.writeObject(this.destUsername);
		outStream.writeObject(this.commandToDo); 
		
		getDestPublicCert(outStream, inStream);
				
		if(this.commandToDo.equals("-c")) {
			
			new CommandDC(this.username, this.password, this.destUsername, this.filesDestUsername).sendToServer(outStream, inStream);
		}
		
		if(this.commandToDo.equals("-e")) {
			
			new CommandDE(this.username, this.password, this.destUsername, this.filesDestUsername).sendToServer(outStream, inStream);
		}
		
		
	}	
}
	

	


