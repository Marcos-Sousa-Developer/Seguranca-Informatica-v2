package commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


public class CommandG {
	
	private String username;
	private String password;
	private List<String> files;

	public CommandG(String username, String password, List<String> files) {
		this.username = username;
		this.password = password;
		this.files = files;
	}
	
	private void getDestPublicCert(String destUsername, ObjectOutputStream outStream, ObjectInputStream inStream) throws IOException, ClassNotFoundException { 
		String path = "../keystore/"+destUsername+".cer"; 
		
		File destCert = new File(path); 
		
		if(!destCert.exists()) {
			
			outStream.writeObject(true); 
			
			byte[] destCertbytes = inStream.readAllBytes(); 
			FileOutputStream newDestUserCert = new FileOutputStream(path); 
			newDestUserCert.write(destCertbytes);
			newDestUserCert.close();
			
		}
		
		else {
			outStream.writeObject(false);
		}
	}
	
	/**
	 * Verify if file was not tempered.
	 * @byte[] signatureInByte received signature in bytes
	 * @String fileToVerify file that you want to verify
	 */
	private void initVerifyFile(byte[] signatureInByte, String fileName) throws NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException, InvalidKeyException, UnrecoverableKeyException, ClassNotFoundException, SignatureException {
		
		Signature s = Signature.getInstance("SHA256withRSA");
		
    	boolean needUserCert = 
    			(fileName.contains(".assinado") && !fileName.endsWith(".assinado")) ||
    			(fileName.contains(".seguro") && !fileName.endsWith(".seguro"));
				
    	
    	if(!needUserCert) {
    		
    		FileInputStream kfile =  new FileInputStream(new File("../keystore/" +this.username + ".keystore"));
    		KeyStore keystore = KeyStore.getInstance("PKCS12");
    		keystore.load(kfile, this.password.toCharArray()); 
    		Certificate cert = keystore.getCertificate(this.username);
    		PublicKey publicKey = cert.getPublicKey(); 	
    		s.initVerify(publicKey);
    		
    	}
    	else {
    		System.out.println(fileName);
    		int dotIndex = fileName.lastIndexOf(".");
            String extension = fileName.substring(dotIndex + 1);
    		String path = "../keystore/"+extension+".cer"; 
        	FileInputStream certToVerify = new FileInputStream(path);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate cer = cf.generateCertificate(certToVerify);
            certToVerify.close();
            PublicKey pubKey = cer.getPublicKey();  	    
    	    s.initVerify(pubKey);
    	}
		
		
		
		//get the received file to verify
		FileInputStream fileToVerify = new FileInputStream("../receivedFiles/"+fileName);
		
		//get total file length
		int totalFileLength =  fileToVerify.available();
		
		byte[] bufferData = new byte[Math.min(totalFileLength==0 ? 1 : totalFileLength , 1024)];
										
		int contentFileLength = fileToVerify.read(bufferData);
		
		//verify file
		while (contentFileLength > 0) {
			s.update(bufferData,0,contentFileLength);
			contentFileLength = fileToVerify.read(bufferData);
		}
		fileToVerify.close(); 
		
		//check if file was not tempered
		boolean bool = s.verify(signatureInByte);
    	if(bool) {
            System.out.println("Signature of file " +  fileName + " was verified.");   
         } else {
            System.out.println("Signature of file " + fileName + " was failed.");
         }	
	}

	/**
	 * Receive a secret key encoded and make a DECRYPT
	 * than initialize a cipher DECRYPT mode.
	 * @byte[] AESkey
	 * @return Cipher initialized
	 */
	private Cipher initDecryptMode(byte[] AESkey) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		
		FileInputStream kfile = new FileInputStream("../keystore/" + this.username + ".keystore"); 
	    KeyStore kstore = KeyStore.getInstance("PKCS12");
	    kstore.load(kfile, this.password.toCharArray());
	    
	    Key privateKey = kstore.getKey(this.username, this.password.toCharArray());
		
	    Cipher cRSA = Cipher.getInstance("RSA");
	    
	    cRSA.init(Cipher.UNWRAP_MODE, privateKey);

	    Key unwrappedKey = cRSA.unwrap(AESkey, "AES", Cipher.SECRET_KEY);
	    
	    Cipher c = Cipher.getInstance("AES");
        
        c.init(Cipher.DECRYPT_MODE, unwrappedKey);
	    
	    return c;
	}
	
	/**
	 * Read a chunk files, DECRYPT file and save it on client machine
	 * @byte[] secretKeyInByte
	 * @ObjectInputStream inStream
	 * @fileName fileName to get the fileOutput
	 */
	private void decryptFile(byte[] secretKeyInByte, ObjectInputStream inStream, String fileName) throws UnrecoverableKeyException, InvalidKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException, ClassNotFoundException {
		
		//get cipher in DECRYPT mode
		Cipher c = initDecryptMode(secretKeyInByte);
		
		FileOutputStream fileOutput = new FileOutputStream("../receivedFiles/" + fileName);
		
		CipherOutputStream cipherOut= new CipherOutputStream(fileOutput, c);
		
		int totalFileLength = (int) inStream.readObject();
		
		byte[] bufferData = new byte[Math.min(totalFileLength==0 ? 1 : totalFileLength , 1024)];
										
		int contentFileLength = inStream.read(bufferData);
										
		while (contentFileLength > 0 && totalFileLength > 0) {
			if (totalFileLength >= contentFileLength) {
				cipherOut.write(bufferData, 0, contentFileLength);
				
			} else {
				cipherOut.write(bufferData, 0, totalFileLength);
				
			}
			totalFileLength -= contentFileLength;
			
			if(totalFileLength > 0) {
				contentFileLength = inStream.read(bufferData);
			}
		}
		
		//cipherOut.flush();
		cipherOut.close();
		fileOutput.close();
		
		System.out.println("File " + fileName + " already decrypted!");
	}
	
	/**
	 * Method to communicate with the server
	 */
	public void sendToServer(ObjectOutputStream outStream, ObjectInputStream inStream) throws UnknownHostException, IOException, ClassNotFoundException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, UnrecoverableKeyException, KeyStoreException, CertificateException, IllegalBlockSizeException, SignatureException, BadPaddingException {

		//send type option to get a correct manager
		outStream.writeObject("-g");
		outStream.writeObject(this.files.size()); 
		
		for (String fileName : this.files) {
			
			outStream.writeObject(fileName); 
			
			int numberFilesToVerify = (int) inStream.readObject(); 
			
			if(numberFilesToVerify != 0 ) {
				
				for (int i = 0; i < numberFilesToVerify; i++) {  
					
					boolean proceed = (boolean) inStream.readObject();
					
					if(proceed) {  
						
						//needs cert
						if((boolean) inStream.readObject()) {
							getDestPublicCert((String) inStream.readObject(), outStream, inStream);
						}
						
						String option = (String) inStream.readObject();
						String nameFileToSave = (String) inStream.readObject(); 
						
						if(!new File("../receivedFiles/"+nameFileToSave).exists()) {
							
							outStream.writeObject(true);
							
							if(option.equals("-c")) {
								byte[] secretKeyInByte = new byte[256];
								inStream.read(secretKeyInByte);
								decryptFile(secretKeyInByte, inStream, nameFileToSave);
							}
							
							else if (option.equals("-s")) {
								
								//get signature
								byte[] signatureInByte = new byte[256];
								inStream.read(signatureInByte);
								
								FileOutputStream outFile = new FileOutputStream("../receivedFiles/" + nameFileToSave); 
												
								int totalFileLength = (int) inStream.readObject();
								
								byte[] bufferData = new byte[Math.min(totalFileLength==0 ? 1 : totalFileLength , 1024)];
																
								int contentFileLength = inStream.read(bufferData);
								
								//get file chunks and store in "../receivedFiles/"
								while (contentFileLength > 0 && totalFileLength > 0) {
									if (totalFileLength >= contentFileLength) {
										outFile.write(bufferData, 0, contentFileLength);
									} else {
										outFile.write(bufferData, 0, totalFileLength);
									}
									totalFileLength -= contentFileLength; 
									
									if(contentFileLength > 0 && totalFileLength > 0) {
										contentFileLength = inStream.read(bufferData);
									}
								}
								outFile.close(); 
								//initialize verify file
								initVerifyFile(signatureInByte, nameFileToSave);
								
							}
							
							else if (option.equals("-e")) {
								//get secret key
								byte[] secretKeyInByte = new byte[256];
								inStream.read(secretKeyInByte);
								
								//get signature
								byte[] signatureInByte = new byte[256];
								inStream.read(signatureInByte); 
													
								decryptFile(secretKeyInByte, inStream, nameFileToSave); 
								initVerifyFile(signatureInByte, nameFileToSave);

							}
						}
						else {
							
							System.out.println("You already have the file " + nameFileToSave + ".");
							
						}
					}	
				}
				
			}
			else {
				System.err.println("The file " + fileName + " doesn't exist on the server. You must provide a existing file.");
			}
	
		}
	}
}