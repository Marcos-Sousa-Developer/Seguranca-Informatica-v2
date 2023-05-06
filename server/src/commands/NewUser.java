package commands;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Scanner;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class NewUser {
	
	private String macPassword;
	
	public NewUser(String macPassword) {
		this.macPassword = macPassword;
	}
	
	public Boolean searchUsername(String username, String password) throws NoSuchAlgorithmException, IOException, InvalidKeyException, IllegalStateException, InvalidKeySpecException {
		
		if(!new File("../cloud/passwords.txt").exists()) {
			new File("../cloud/passwords.txt").createNewFile();
		}

		Scanner scanner = new Scanner(new File("../cloud/passwords.txt"));
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine(); 
			String[] elements = line.split(";");
			String firstElement = elements[0];
			
			if (firstElement.equals(username)) {
				return false;
			}
		}
		write(username, password); 
		
		if(!this.macPassword.isEmpty()) {
			new ProtectPasswordFile(this.macPassword).protectFileWithMac();
		}
		
		new File("../cloud/"+username).mkdirs();
		new File("../cloud/"+username+"/files").mkdirs();
		new File("../cloud/"+username+"/keys").mkdirs();
		new File("../cloud/"+username+"/signatures").mkdirs();
		return true;
	}
	
	public static byte[] saltPassword() {
		
		//gerar chave salt
		SecureRandom random = new SecureRandom();
		byte[] salt = new byte[16];
		random.nextBytes(salt);
		
		return salt; //Base64.getEncoder().encodeToString(salt);	
	}
	
	public static String getHashPassWithSalt(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
		//password, salt, iterator number, length
		PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		byte[] hashedPassword = factory.generateSecret(spec).getEncoded();
		return Base64.getEncoder().encodeToString(hashedPassword);
	}
	
	public void write(String username, String password) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
		
		String file = "../cloud/passwords.txt"; 
		
		File passFile = new File(file = "../cloud/passwords.txt");
		
		byte[] salt = saltPassword();
		String hashOfPassWithSalt = getHashPassWithSalt(password, salt);

		FileWriter fw = new FileWriter(file, true);
		
		BufferedWriter bw = new BufferedWriter(fw);
				
		bw.append(username + ";" + hashOfPassWithSalt + ";" + Base64.getEncoder().encodeToString(salt) + '\n');	
		
		bw.close();
		fw.close();
	}
}
