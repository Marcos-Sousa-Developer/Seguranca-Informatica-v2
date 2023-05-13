import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import commands.CommandAU;
import commands.CommandC;
import commands.CommandD;
import commands.CommandE;
import commands.CommandG;
import commands.CommandS;
import commands.CommandUP;
import commands.VerifyPort;

public class myCloud {

	/**
	 * Check if command is right
	 * 
	 * @String[] list of arguments to check
	 * @return Array of strings with host and port
	 */
	private static String[] verifyCommand(String[] args) {

		String ip = "";
		String port = "";

		if (args.length < 4) {
			System.err.println("Command not valid.");
			System.err.println("Example: myCloud -a <serverAddress> {-c || -s || -e || -g || -d} {<filenames>}+");
			System.exit(-1);
		}

		if (!args[0].equals("-a")) {
			System.err.println("You must provide -a option.");
			System.exit(-1);
		}
		switch (args[2]) {
		case "-au":
			if (!(args.length == 6)) {
				System.err.println("Command not valid.");
				System.err.println("Example: myCloud -a <serverAddress> -au <username> <password> <certificado>");
				System.exit(-1);
			}
			break;
		case "-u":
			if (!(args[4].equals("-p") && args.length > 6)) {
				System.err.println("Command not valid.");
				System.err.println(
						"Example: myCloud -a <serverAddress> -u <username> -p <password> {-c || -s || -e || -g || -d} {<filenames>}+");
				System.err.println(
						"Example: myCloud -a <serverAddress> -u <username> -p <password> -d <username de destinatário> {-c || -s || -e || -g || -d} {<filenames>}+");
				System.exit(-1);
			} else if (!args[6].equals("-d")) {

				String[] options = new String[] { "-c", "-s", "-e", "-g", "-d" };

				if (!Arrays.asList(options).contains(args[6])) {
					System.err.println("You must provide a valid option.");
					System.err.println("Valid options: -c || -s || -e || -g || -d");
					System.exit(-1);
				} else if (!(args.length > 7)) {
					System.err.println("Command not valid.");
					System.err.println(
							"Example: myCloud -a <serverAddress> -u <username> -p <password> {-c || -s || -e || -g || -d <destUsername> (-c || -s || -e)} {<filenames>}+");
					System.exit(-1);
				}
			} else { // Verifica caso a opcao -d seja dada
				String[] options = new String[] { "-c", "-s", "-e" };

				if (!((args.length > 9) && Arrays.asList(options).contains(args[8]))) {
					System.err.println("Command not valid.");
					System.err.println(
							"Example: myCloud -a <serverAddress> -u <username> -p <password> -d <destUsername> {-c || -s || -e } {<filenames>}+");
					System.exit(-1);
				}
			}
			break;
		}
		String[] address = args[1].split(":");
		if (address.length == 2 && !address[0].equals("")) {
			ip = address[0];

			port = new VerifyPort(address[1]).verifyPort();

		} else {
			System.err.println("You must provide a valid address.");
			System.err.println("Example: 127.0.0.1:23456");
			System.exit(-1);
		}

		return new String[] { ip, port };
	}

	/**
	 * @param certificate
	 * @param username
	 * @description check if certificate is valid
	 */
	public static void verifyCert(String cert, String username) {

		if (cert.split("[.]").length != 2) {
			System.out.println("ok");
			System.out.println("Certificate " + cert + " is not valid.");
			System.exit(-1);
		}

		if (!cert.split("[.]")[0].equals(username)) {
			System.out.println("The certificate must be the same name as the username, ex. username.cer");
			System.exit(-1);
		}

		try {
			// Load the certificate from the file
			FileInputStream certToVerify = new FileInputStream("../keystore/" + cert);
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			Certificate cer = cf.generateCertificate(certToVerify);
			certToVerify.close();

			// Verify the certificate
			X509Certificate x509cert = (X509Certificate) cer;
			x509cert.checkValidity();

		} catch (FileNotFoundException e) {
			System.out.println("Certificate not found");
			System.exit(-1);
		}

		catch (Exception e) {
			System.out.println("Certificate is not valid");
			System.exit(-1);
		}

	}

	public static void checkKeystore(String username, String password) {

		String path = "../keystore/" + username + ".keystore";

		if (!new File(path).exists()) {
			System.out.println("Alert, you don't have a keystore!");
			System.out.println("Check the folder keystore");
			System.out.println("keystore must be your username, ex. username.keystore");
			System.exit(-1);
		}

		try {
			// Load the keystore
			FileInputStream fis = new FileInputStream(path);
			KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			keystore.load(fis, password.toCharArray());
			fis.close();

			if (!keystore.containsAlias(username)) {
				throw new Exception(
						"Keystore should have same credentials of your account. alis=username and same account password!");
			}

		} catch (Exception e) {
			System.out.println("Alert, check the alias and password of your keystore!");
			System.out.println(
					"Keystore should have same credentials of your account. alis=username and same account password!");
			System.exit(-1);
		}

	}

	/**
	 * Manage type of request
	 * 
	 * @String[] list of arguments
	 */
	public static void main(String[] args) throws Exception { 

		String[] address = verifyCommand(args);

		String username = args[3];
		String password = args[4];

		if (args[2].equals("-au")) {
			checkKeystore(username, password);
			verifyCert(args[5], username);
		} else {
			password = args[5];
			checkKeystore(username, args[5]);
		}

		System.setProperty("javax.net.ssl.trustStore", "../keystore/" + username + ".keystore");
		System.setProperty("javax.net.ssl.trustStorePassword", password);
		System.setProperty("javax.net.ssl.keyStoreType", "PKCS12");
		Socket socket = null;

		try {
			SocketFactory sf = SSLSocketFactory.getDefault();
			socket = sf.createSocket(address[0], Integer.parseInt(address[1]));
		} catch (ConnectException e) {
			System.out.println("Connection refused, please check the Port");
			System.exit(-1);
		} catch (UnknownHostException e) {
			System.out.println("Connection refused, please check the Host");
			System.exit(-1);
		} catch (NoRouteToHostException e) {
			System.out.println("Connection refused, please check the Host");
			System.exit(-1);
		} catch (Exception e) {
			System.out.println("Error to connect");
			System.out.println("Check if you have " + username + ".keystore at keystore folder");
			System.out.println("Password of your keystore should be the same as the user account");
			System.exit(-1);
		}
		ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
		ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());

		switch (args[2]) {
		case "-au":
			Boolean iscreated = new CommandAU(username, password, new FileInputStream("../keystore/" + args[5]))
					.createUser(outStream, inStream);
			if (!iscreated) {
				System.err.println("This username already exists.");
				System.exit(-1);
			} else {
				System.err.println("User " + username + " created.");
				System.exit(0);
			}
			break;
		case "-u":

			outStream.writeObject("-u");

			Boolean login = new CommandUP(username, password).verifyLogin(outStream, inStream);

			if (login) {
				System.out.println("Authorized");

				// Split and get the files to manage
				List<String> files = new ArrayList<>(Arrays.asList(args)).subList(7, args.length);

				switch (args[6]) {

				case "-c":
					new CommandC(username, password, files).sendToServer(outStream, inStream);
					break;

				case "-s":
					new CommandS(username, password, files).sendToServer(outStream, inStream);
					break;

				case "-e":
					new CommandE(username, password, files).sendToServer(outStream, inStream);
					break;

				case "-g":
					String pathReceiveFiles = "../receivedFiles/";
					if(!new File(pathReceiveFiles).exists()) {
						new File(pathReceiveFiles).mkdir();
					}
					new CommandG(username, password, files).sendToServer(outStream, inStream);
					break;

				case "-d":
					String destUsername = args[7];
					String commandToDo = args[8];
					List<String> filesDestUsername = new ArrayList<>(Arrays.asList(args)).subList(9, args.length);

					new CommandD(username, password, destUsername, commandToDo, filesDestUsername)
							.sendToServer(outStream, inStream);

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