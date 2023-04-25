package commands;

import java.util.Base64;
import java.util.List;
import java.util.Scanner;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class CommandAU {

	private String newUsername;
	private String newPassword;
	private String cert;
	private String ip;
	private int port;
	private List<String> files;
	private static String salt;
	
	public CommandAU(String ip, int port, String newUsername, String newPassword, String cert, List<String> files) {
		this.ip = ip;
		this.port = port;		
		this.newUsername = newUsername;
		this.newPassword = newPassword;
		this.cert = cert;
		this.files = files;
	}

	public void searchUsername() throws FileNotFoundException, NoSuchAlgorithmException {
		String file = "../passwords.txt";

		String toSearch = this.newUsername;
		Scanner scanner = new Scanner(new File(file));
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine(); 
			String[] elements = line.split(";");
			String firstElement = elements[0];
			
			if (firstElement.equals(toSearch)) {
				
				System.err.println("This username already exists.");
		    	System.exit(-1);
			}
			else {
				write();
			}
		}
	}
	
	public static String saltPassword() {
		String saltKey = "";
		
		//gerar chave salt
		SecureRandom random = new SecureRandom();
		byte[] salt = new byte[16];
		random.nextBytes(salt);
		return Base64.getEncoder().encodeToString(salt);	
	}
	
	public static String getHashPassWithSalt(String passWithSalt) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] hash = digest.digest(passWithSalt.getBytes());
		return Base64.getEncoder().encodeToString(hash);
	}
	
	
	
	public void write() throws FileNotFoundException, NoSuchAlgorithmException {
		String file = "../passwords.txt";
		
		salt = saltPassword();
		String passWithSalt = salt + this.newPassword;
		String hashOfPassWithSalt = getHashPassWithSalt(passWithSalt);

		//Adição do user e da password ao ficheiro caso tudo esteja correto
		String[] addUser = {this.newUsername, passWithSalt};
	
		PrintWriter add = new PrintWriter(file);
			
			for (int i = 0; i < addUser.length; i++) {
				if(i>0) {
					add.print(";");
				}	
				
				add.print(addUser[i]);
			}		
	}
	
	public static String getSalt() {
		return salt;
	}
	
	public void setSalt(String salt) {
		this.salt = salt;
	}
	
	
	
	
}
