package commands;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class VerifyUser {
	
	public VerifyUser() {}

	public Boolean searchUser(String username, String password) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
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
	
	public static String getHashPassWithSalt(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
		//password, salt, iterator number, length
		PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		byte[] hashedPassword = factory.generateSecret(spec).getEncoded();
		return Base64.getEncoder().encodeToString(hashedPassword);
	}
	 
	public Boolean comparePassword(String password, String passWithSalt, String salt) throws NoSuchAlgorithmException, InvalidKeySpecException{ //IR BUSCAR O SALT AO FICHEIRO PASSWORDS
		
		byte[] saltBytes = Base64.getDecoder().decode(salt);
		
		String hashedPassword = getHashPassWithSalt(password,saltBytes);
		
		if (hashedPassword.equals(passWithSalt)){
			return true;
		}
		else {
			return false;
		}	
	}
}
