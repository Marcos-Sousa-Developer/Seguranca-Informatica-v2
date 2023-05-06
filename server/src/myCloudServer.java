import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import commands.ProtectPasswordFile;
import commands.VerifyPort;
import thread.ServerThread;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;


public class myCloudServer {

	/**
	 * Check if port number is valid
	 * @String[] list of arguments to check 
	 * @return Port in integer
	 */
	private static int verifyPort(String[] args) {
		
		String port = "";
		if(args.length == 1){
			port = new VerifyPort(args[0]).verifyPort();
		} else {
			System.err.println("You must provide a port.");
	    	System.exit(-1);
		}
		return Integer.parseInt(port);
	}
	
	private static String verifyMac() throws InvalidKeyException, NoSuchAlgorithmException, IllegalStateException, IOException {
		
		String password = "";
		
		if(new File("../cloud/passwords.txt").exists()) {
			
			Scanner scanner = new Scanner(System.in);
			String flagVerify = "";
			String flagCreate = "";
			
			if(new File("../cloud/passwords.mac").exists()) {
				flagVerify = "proceed";
			}
			
			else {
				System.out.println("No MAC is protecting the password file");
				System.out.println("Do you want to calculate the MAC [yes/no] ?  "); 
				flagCreate = scanner.nextLine().toLowerCase();
			
			}
			
			
			
			if(new ArrayList<>(Arrays.asList("y", "yes")).contains(flagCreate) || !flagVerify.equals("")) {
				while (password.isEmpty()) {
					System.out.println();
					System.out.print("Please enter a password: ");
					password = scanner.nextLine();
					if(password.isEmpty()) {
						System.out.println("Password cannot be empty. Please try again.");
						System.out.print("Please enter a password: ");
					}
				}
			}
			
			if(new ArrayList<>(Arrays.asList("y", "yes")).contains(flagCreate)) {
				new ProtectPasswordFile(password).protectFileWithMac();
			}
			else if(flagVerify.equals("proceed")) {
				if(!new ProtectPasswordFile(password).verifyFileIntegrity()) {
					System.out.println("The server could not verify the mac in the password file.");
					System.exit(-1);
				}
			}
		}
		
		return password;
	}
	
	/**
	 * Initialize server
	 * @throws IllegalStateException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @String[] list of arguments to check 
	 */
	public static void main(String[] args) throws IOException, InvalidKeyException, NoSuchAlgorithmException, IllegalStateException {
		
		int port = verifyPort(args);
		
		System.setProperty("javax.net.ssl.keyStore", "../cloud/server.keystore");
		System.setProperty("javax.net.ssl.keyStorePassword", "12345678");
		System.setProperty("javax.net.ssl.keyStoreType", "PKCS12");  
		
		String macPassword = verifyMac();
		
		myCloudServer server = new myCloudServer();
		server.startServer(port, macPassword);
	}
	
	/**
	 * Method to connect the server
	 * @integer port number
	 */
	private void startServer(int port, String macPassword) throws IOException {
		
		ServerSocketFactory ssf = SSLServerSocketFactory.getDefault();
		SSLServerSocket servSocket = null;
		servSocket = (SSLServerSocket) ssf.createServerSocket(port);
		
		System.out.println("Server connected");
		
		while(true) {
			try {
				SSLSocket inSoc = (SSLSocket) servSocket.accept();
				ServerThread newServerThread = new ServerThread(inSoc, macPassword);
				newServerThread.start();
		    }
		    catch (IOException e) {
		        e.printStackTrace();
		    }
		}
	}
}
