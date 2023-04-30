import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import commands.CommandAU;
import commands.CommandC;
<<<<<<< HEAD
import commands.CommandD;
=======
//import commands.CommandD;
>>>>>>> 2e1e7016b13e63fd58cd3af3dcd533e2b6e12d31
import commands.CommandE;
import commands.CommandG;
import commands.CommandS;
import commands.CommandUP;
import commands.VerifyPort;

public class myCloud {
	
	/**
	 * Check if command is right
	 * @String[] list of arguments to check 
	 * @return Array of strings with host and port
	 */
	private static String[] verifyCommand(String[] args) {
		
		String ip = "";
		String port = ""; 
		
		if(args.length < 4) {
			System.err.println("Command not valid.");
			System.err.println("Example: myCloud -a <serverAddress> {-c || -s || -e || -g || -d} {<filenames>}+");
	    	System.exit(-1);
		}

		if(!args[0].equals("-a")){
			System.err.println("You must provide -a option.");
	    	System.exit(-1);
		}
		switch (args[2]) {
			case "-au":
				if(!(args.length == 6)){
					System.err.println("Command not valid.");
					System.err.println("Example: myCloud -a <serverAddress> -au <username> <password> <certificado>");
			    	System.exit(-1);
				}
				break;
			case "-u":
				if(!(args[4].equals("-p") && args.length > 6)){
					System.err.println("Command not valid.");
					System.err.println("Example: myCloud -a <serverAddress> -u <username> -p <password> {-c || -s || -e || -g || -d} {<filenames>}+");
					System.err.println("Example: myCloud -a <serverAddress> -u <username> -p <password> -d <username de destinatário> {-c || -s || -e || -g || -d} {<filenames>}+");
					System.exit(-1);
				}
				else if(!args[6].equals("-d")) {
					
					String[] options = new String[]{"-c", "-s", "-e", "-g", "-d"};
					
					if(!Arrays.asList(options).contains(args[6])){
						System.err.println("You must provide a valid option.");
						System.err.println("Valid options: -c || -s || -e || -g || -d");
				    	System.exit(-1);
					}
					else if (!(args.length > 7)){
						System.err.println("Command not valid.");
						System.err.println("Example: myCloud -a <serverAddress> -u <username> -p <password> {-c || -s || -e || -g || -d <destUsername> (-c || -s || -e)} {<filenames>}+");
				    	System.exit(-1);
					}
				} else { //Verifica caso a opcao -d seja dada
					String[] options = new String[]{"-c", "-s", "-e"};
					
					if(!((args.length > 9) && Arrays.asList(options).contains(args[8]))){
						System.err.println("Command not valid.");
						System.err.println("Example: myCloud -a <serverAddress> -u <username> -p <password> -d <destUsername> {-c || -s || -e } {<filenames>}+");
				    	System.exit(-1);
					}
				}
				break;
		}
		String[] address = args[1].split(":");
		if(address.length == 2 && !address[0].equals("")) {
			ip = address[0];
			
			port = new VerifyPort(address[1]).verifyPort();
		
		} else {
			System.err.println("You must provide a valid address.");
			System.err.println("Example: 127.0.0.1:23456");
	    	System.exit(-1);
		}
		
		return new String[]{ip, port};
	}

	/**
	 * Manage type of request
	 * @String[] list of arguments
	 */
	public static void main(String[] args) throws Exception {
		
		String[] address = verifyCommand(args);
		
		String username = args[3];
		String userPassword = null;
		
		System.out.println(args[2]);
		
		if(args[2].equals("-au")) {
			userPassword = args[4];	
		} 
		if(args[2].equals("-u")) {
			userPassword = args[5];
		}
		
		Socket socket = null;
		
		try {
			//-----------Substituir---------------
			 socket = new Socket(address[0], Integer.parseInt(address[1]));
			 
			//---------------TLS------------------
			/*
			 Criar keystores de cada cliente quando cria o cliente caso ainda não tenha sido criado e 
			 depois envia para o servidor, caso não exista, depois de criar tem de extrair o certificado 
			*/
			/*
		     System.setProperty("javax.net.ssl.trustStore", username + ".truststore"); 
			 System.setProperty("javax.net.ssl.trustStorePassword", userPassword);
		     SocketFactory sf = SSLSocketFactory.getDefault( );
		     socket = sf.createSocket(address[0], Integer.parseInt(address[1])); //ERRO AQUI
		     */
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
		
		switch (args[2]) {
			case "-au":
				Boolean iscreated = new CommandAU(args[3], args[4], args[5]).createUser(outStream, inStream);
				if(!iscreated) {
					System.err.println("This username already exists.");
			    	System.exit(-1);
				}
				else {
					System.err.println("User " + "username" + " created.");
			    	System.exit(0);
				}
				break;
			case "-u":
				Boolean login = new CommandUP(args[3], args[5]).verifyLogin(outStream, inStream);
				
				if(login) {
					System.out.println("Authorized");
<<<<<<< HEAD

=======
					
					int optionIndex = 6;
					
					if(args[6].equals("-d")) {
						optionIndex = 8;
						optionIndex = 7;
						String destUsername = args[7];
						String commandToDo = args[8];
						//enviar ficheiros para o servidor para outros utilizadores
						//se não tiver previamente o certificado do destinatário, pede ao servidor
						//Se a maria enviar ficheiros para alice, os ficheiros devem de ficar com o nome "aa.pdf.assinado.maria"
						List<String> filesDestUsername = new ArrayList<>(Arrays.asList(args)).subList(optionIndex, args.length);
						//new CommandD(address[0], Integer.parseInt(address[1]), args[3], filesDestUsername, destUsername, commandToDo).sendToServer(outStream, inStream);
					}	
					
>>>>>>> 2e1e7016b13e63fd58cd3af3dcd533e2b6e12d31
					//Split and get the files to manage
					List<String> files = new ArrayList<>(Arrays.asList(args)).subList(7, args.length);
					
					//System.out.println(files);
					
<<<<<<< HEAD
					switch (args[6]) {
=======
					switch (args[optionIndex]) {
>>>>>>> 2e1e7016b13e63fd58cd3af3dcd533e2b6e12d31
					case "-c":
						new CommandC(files).sendToServer(outStream, inStream);

						break;
					case "-s":
						new CommandS(files).sendToServer(outStream, inStream);

						break;
					case "-e":
						new CommandE(files).sendToServer(outStream, inStream);

						break;
					case "-g":
						new CommandG(files).sendToServer(outStream, inStream);
					case "-d":
						String destUsername = args[7];
						String commandToDo = args[8];
						List<String> filesDestUsername = null;
						filesDestUsername = new ArrayList<>(Arrays.asList(args)).subList(9, args.length);
						
						new CommandD(address[0], Integer.parseInt(address[1]), args[3], filesDestUsername, destUsername, commandToDo).sendToServer(outStream, inStream);


						break;
				}
				} else {
					System.err.println("Not authorized");
					System.err.println("Username or password invalid");
					System.exit(-1);
				}
				break;
		}
		socket.close();
	}
}