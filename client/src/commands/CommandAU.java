package commands;

import java.util.Base64;
import java.util.List;
import java.util.Scanner;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class CommandAU {

	private String ip;
	private int port;
	private String username;
	private String password;
	private String cert;

	public CommandAU(String ip, int port, String username, String password, String cert) {
		this.ip = ip;
		this.port = port;		
		this.username = username;
		this.password = password;
		this.cert = cert;
	}

	public boolean createUser() throws IOException, ClassNotFoundException {
		Socket socket = null;
		try {
			 socket = new Socket(this.ip, this.port);
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
		
		outStream.writeObject("-au");
		outStream.writeObject(this.username);
		outStream.writeObject(this.password);
		outStream.writeObject(this.cert);
		
		return (Boolean) inStream.readObject();
	}
	
	
	
	
}
