package thread;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

import commands.*;

public class ServerThread extends Thread {

	// Server socket
	public Socket socket = null;

	// Thread server for each client
	public ServerThread(Socket inSoc) {
		this.socket = inSoc;
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
			
			if (option1.equals("-u")){
				String username = (String) inStream.readObject();
				String password = (String) inStream.readObject();

				Boolean login = new VerifyUser().searchUser(username, password);
				
				outStream.writeObject(login);
				
				if(login) {
					
					String option2 = (String) inStream.readObject();
					
					//true (vai buscar os outros comandos)
					if (option2.equals("-c")) {
						
						new VerifyCommandC().verify(inStream, outStream);
						
					} else if (option2.equals("-s")) {
						
						new VerifyCommandS().verify(inStream, outStream);
						
					} else if (option2.equals("-e")) {
						
						new VerifyCommandE().verify(inStream, outStream);
						
					} else if (option2.equals("-g")) {
						
						new VerifyCommandG().verify(inStream, outStream);			
					}
				} else {
					//false
				}
			} else if (option1.equals("-au")) {
				String username = (String) inStream.readObject();
				String password = (String) inStream.readObject();
				String cert = (String) inStream.readObject();
				
				Boolean login = new NewUser().searchUsername(username, password);
				
				outStream.writeObject(login);
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
		}
	}

	
	
	
	
	
	
	
	
}
