package commands;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class VerifyCommandD { 
	
	private String username;
	private String destUsername;
	private String commandToDo; 

	
	public VerifyCommandD(String username, String destUsername, String commandToDo) {
		
		this.username = username;
		this.destUsername = destUsername;
		this.commandToDo = commandToDo;

	}
	
	/**
	 * Verify commandC and check what to do
	 * @throws ClassNotFoundException 
	 * @ObjectInputStream inStream
	 * @ObjectOutputStream outStream
	 */
	public void verify(ObjectInputStream inStream, ObjectOutputStream outStream) throws IOException, ClassNotFoundException {
		
		Boolean needsDestUserCert = (boolean) inStream.readObject();  
		
		if(needsDestUserCert) {
			String path = "../cloud/usersCert/"+destUsername+".cer";
			File cer = new File(path);
			if(!cer.exists()) {
				outStream.writeObject(false);
			}
			else {
				outStream.writeObject(true);
				FileInputStream destUserCert = new FileInputStream(cer); 
				
				outStream.writeObject(destUserCert.readAllBytes());
				if(this.commandToDo.equals("-c")) {
					new VerifyCommandC(this.destUsername,this.username);
				}
				if(this.commandToDo.equals("-e")) {
					new VerifyCommandE(this.destUsername,this.username);
				}
			}
		}
		
		else {
			if(this.commandToDo.equals("-c")) {
				new VerifyCommandC(this.destUsername,this.username);
			}
			if(this.commandToDo.equals("-e")) {
				new VerifyCommandE(this.destUsername,this.username);
			}
			
		}
	}
}
