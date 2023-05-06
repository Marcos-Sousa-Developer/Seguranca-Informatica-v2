package thread;

import java.io.*;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import commands.*;

public class ServerThread extends Thread {

	// Server socket
	public Socket socket = null;
	private String macPassword;
	
	// Thread server for each client
	public ServerThread(Socket inSoc, String macPassword) {
		this.socket = inSoc;
		this.macPassword = macPassword;
	}
	
	/*
	 * Overrides Thread run 
	 * This method listen the option and redirect to the correct manager
	 */
	public void run() {
		try {
			
			ObjectInputStream inStream = new ObjectInputStream(this.socket.getInputStream());
			ObjectOutputStream outStream = new ObjectOutputStream(this.socket.getOutputStream());
			
			String option1 = (String) inStream.readObject();
			String username = (String) inStream.readObject();
			String password = (String) inStream.readObject();
			
			if (option1.equals("-u")){

				Boolean login = new VerifyUser().searchUser(username, password);
				
				outStream.writeObject(login);
				
				if(login) {
					
					String option2 = (String) inStream.readObject();
					
					//true (vai buscar os outros comandos)
					if (option2.equals("-c")) {
						
						new VerifyCommandC(username).verify(inStream, outStream);
						
					} else if (option2.equals("-s")) {
						
						new VerifyCommandS(username).verify(inStream, outStream);
						
					} else if (option2.equals("-e")) {
						
						new VerifyCommandE(username).verify(inStream, outStream);
						
					} else if (option2.equals("-g")) {
						
						new VerifyCommandG(username).verify(inStream, outStream);			
					
					}else if (option2.equals("-d")) {  
						
						
						String destUsername = (String) inStream.readObject();
						String commandToDo = (String) inStream.readObject();

						new VerifyCommandD(username, destUsername, commandToDo).verify(inStream, outStream);
						
					} 
				}
				
			} else if (option1.equals("-au")) {

				byte[] cert = inStream.readAllBytes();

				Boolean isNewUser = new NewUser(this.macPassword).searchUsername(username, password); 
				
				if(isNewUser) {
					FileOutputStream fos = new FileOutputStream("../cloud/usersCert/"+username+".cer");
					fos.write(cert);
					fos.close();
				}
				
				outStream.writeObject(isNewUser);
			}


			inStream.close();
			this.socket.close();

		} catch (IOException | ClassNotFoundException e) {
			System.out.println("I got an error or something was interrupted!");
			System.out.println(e);
			//e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	
	
	
	
	
	
	
}
