package commands;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class VerifyUser {
	
	public VerifyUser() {}

	public Boolean searchUser(String username, String password) throws IOException, NoSuchAlgorithmException {
		//primeiro procurar o username e obter a linha em que está
		BufferedReader br = new BufferedReader(new FileReader("../cloud/passwords.txt"));
		String lines; 
		while ((lines = br.readLine()) != null) {
			String[] elements = lines.split(";");
			if (elements[0].equals(username)) {
				return comparePassword(password, elements[2]);
			}
		}
		br.close();
		return false;	
	}
	
	private static String hash(String input) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(input.getBytes());
		byte[] digest = md.digest();
		StringBuilder sb = new StringBuilder();
		for (byte b : digest) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}
	 
	public Boolean comparePassword(String password, String salt) throws NoSuchAlgorithmException{ //IR BUSCAR O SALT AO FICHEIRO PASSWORDS
		String hashedPassword = hash(password + salt);
		if (hashedPassword.equals(password)){
			return true;
		}
		else {
			return false;
		}	
	}
}
