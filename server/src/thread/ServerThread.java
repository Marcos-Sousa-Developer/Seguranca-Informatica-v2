package thread;

import java.io.*;
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
			
			String username = (String) inStream.readObject();
			String password = (String) inStream.readObject();
			
			if (option1.equals("-u")){

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
					
					}else if (option2.equals("-d")) {
					
						String option3 = (String) inStream.readObject();
						
						new VerifyCommandD().verify(inStream, outStream);
						
						if (option3.equals("-c")) {
							
							new VerifyCommandC().verify(inStream, outStream);
							
						} else if (option3.equals("-s")) {
								
							new VerifyCommandS().verify(inStream, outStream);
								
						} else if (option3.equals("-e")) {
								
							new VerifyCommandE().verify(inStream, outStream);

						}
					} else {
						//false
					}
				}
				
			} else if (option1.equals("-au")) {

				String cert = (String) inStream.readObject();
			
				Boolean newUser = new NewUser().searchUsername(username, password);
			
				outStream.writeObject(newUser);
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
