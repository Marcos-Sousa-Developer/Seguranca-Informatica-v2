package commands;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class VerifyUser {
	
	public VerifyUser() {}

	public Boolean searchUser(String username, String password) throws IOException, NoSuchAlgorithmException {
		//primeiro procurar o username e obter a linha em que está
		BufferedReader br = new BufferedReader(new FileReader("../cloud/passwords.txt"));
		String lines; 
		while ((lines = br.readLine()) != null) {
			String[] elements = lines.split(";");
			if (elements[0].equals(username)) {
				return comparePassword(password, elements[1], elements[2]);
			}
		}
		br.close();
		return false;	
	}
	
	public static String getHashPassWithSalt(String passWithSalt) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] hash = digest.digest(passWithSalt.getBytes());
		return Base64.getEncoder().encodeToString(hash);
	}
	 
	public Boolean comparePassword(String password, String passWithSalt, String salt) throws NoSuchAlgorithmException{ //IR BUSCAR O SALT AO FICHEIRO PASSWORDS
		String hashedPassword = getHashPassWithSalt(salt + password);
		if (hashedPassword.equals(passWithSalt)){
			return true;
		}
		else {
			return false;
		}	
	}
}
