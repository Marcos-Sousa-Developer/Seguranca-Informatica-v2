package commands;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.UnknownHostException;

public class CommandUP {
	
	private String ip;
	private int port;
	private String username;
	private String password;

	public CommandUP(String ip, int port, String username, String password) {
		this.ip = ip;
		this.port = port;
		this.username = username;
		this.password = password;
	}
	
	public Boolean verifyLogin() throws IOException, ClassNotFoundException {
		Socket socket = null;
		try {
			 socket = new Socket(this.ip, this.port);
			 
			//---------------TLS------------------
			/*Socket socket;
	
		    System.setProperty("javax.net.ssl.trustStore", "truststore.client");
		    System.setProperty("javax.net.ssl.trustStorePassword", "123456");
		    SocketFactory sf = SSLSocketFactory.getDefault( );
		    socket = sf.createSocket("127.0.0.1", 9096);*/
		   //------------------------------------
		}
		catch (ConnectException e) {
			System.out.println("Connection refused, please check the Port");
			System.exit(-1);
		}
		catch (UnknownHostException e) {
			
			System.out.println("Connection refused, please check the Host");
			System.exit(-1);
		} 
		catch (NoRouteToHostException e) {
			System.out.println("Connection refused, please check the Host");
			System.exit(-1);
		} 
		
		ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
		ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
		
		outStream.writeObject("-u");
		outStream.writeObject(this.username);
		outStream.writeObject(this.password);
		
		return (Boolean) inStream.readObject();
	}
}
