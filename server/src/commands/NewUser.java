package commands;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Scanner;

public class NewUser {
	
	public NewUser() {
			
	}
	
	public Boolean searchUsername(String username, String password) throws NoSuchAlgorithmException, IOException {

		String toSearch = username;
		Scanner scanner = new Scanner(new File("../cloud/passwords.txt"));
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine(); 
			String[] elements = line.split(";");
			String firstElement = elements[0];
			
			if (firstElement.equals(toSearch)) {
				System.err.println("This username already exists.");
		    	System.exit(-1);
			}
			else {
				write(username, password);
			}
		}
		return true;
	}
	
	public static String saltPassword() {
		
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
	
	public void write(String username, String password) throws NoSuchAlgorithmException, IOException {
		String file = "../cloud/passwords.txt";
		
		String salt = saltPassword();
		String passWithSalt = salt + password;
		String hashOfPassWithSalt = getHashPassWithSalt(passWithSalt);

		//Adição do user e da password ao ficheiro caso tudo esteja correto
		FileWriter fw = new FileWriter(file, true);
		
		BufferedWriter bw = new BufferedWriter(fw);
		
		PrintWriter pw = new PrintWriter(bw);
		
		pw.println(username + ";" + hashOfPassWithSalt + ";" + salt);	
		
		pw.close();
		bw.close();
		fw.close();
	}
}
