package commands;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


public class ProtectPasswordFile {
	
	private String password;
	
	public ProtectPasswordFile(String password) {
		this.password = password;
	}
	
	private SecretKey generateKeyByPassword() {
		byte [] pass = this.password.getBytes(); 
		SecretKey key = new SecretKeySpec(pass, "HmacSHA256");  	
		return key;
	}
	
	private byte[] generateMac() throws NoSuchAlgorithmException, InvalidKeyException, IllegalStateException, IOException {
		
		FileInputStream passwordFile = new FileInputStream("../cloud/passwords.txt");
		
		SecretKey key = generateKeyByPassword();
		
		Mac m = Mac.getInstance("HmacSHA256"); 
		
		m.init(key); 

		m.update(passwordFile.readAllBytes()); 
		
		passwordFile.close();

		return m.doFinal();
	}
	
	
	public void protectFileWithMac() throws InvalidKeyException, NoSuchAlgorithmException, IllegalStateException, IOException { 
		
		byte[] mac = generateMac();
		
		FileOutputStream outFile = new FileOutputStream("../cloud/passwords.mac"); 
		
		outFile.write(mac);
		
		outFile.close();		
		
	}
	
	public boolean verifyFileIntegrity() throws InvalidKeyException, NoSuchAlgorithmException, IllegalStateException, IOException {
		
		byte[] mac = generateMac();

		FileInputStream mac_passwordFile = new FileInputStream("../cloud/passwords.mac"); 
		byte[] savedMacBytes = mac_passwordFile.readAllBytes();
		
		String password_string = Base64.getEncoder().encodeToString(mac); 
		String mac_string = Base64.getEncoder().encodeToString(savedMacBytes);  
		
		
		return password_string.equals(mac_string);


		
	}
}
