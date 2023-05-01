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
import java.security.PrivateKey;
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
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class CommandDE { 
	
	private String username;
	private String password;
	private String destUsername;
	private List<String> filesDestUsername; 

	public CommandDE(String username, String password, String destUsername, List<String> filesDestUsername) {
		
		this.username = username;
		this.password = password;
		this.destUsername = destUsername;
		this.filesDestUsername = filesDestUsername;
	}
	
	/**
	 * Method to initialize a signature
	 * @return an instance of Signature
	 */
	private Signature initSignature() throws NoSuchAlgorithmException,KeyStoreException, UnrecoverableKeyException, InvalidKeyException, CertificateException, IOException {
		
		//Set the signature
		Signature signature = Signature.getInstance("SHA256withRSA");   
		
		//Read the KeyStore File
		FileInputStream keyStorefile = new FileInputStream(new File("../keystore/" +this.username + ".keystore")); 
		
		//Get the instance of keyStore
		KeyStore kstore = KeyStore.getInstance("PKCS12"); 
		
		//verify password and load KeyStore file
		kstore.load(keyStorefile, this.password.toCharArray()); 
		
		//Get the private key 
		Key key = kstore.getKey(this.username, this.password.toCharArray()); 
		
		//turn key in instance of private key
		PrivateKey privatekey = (PrivateKey) key; 
		
		//Encrypted digital assign
		signature.initSign(privatekey); 		
		
		return signature;
		
	}
	
	/**
	 * While ciphers the file and save it on client, create signature to server
	 * and sent it to the server
	 * @String the fileName that wants to cipher
	 */
	private void cipherFile(String fileName, ObjectOutputStream outStream) throws NoSuchAlgorithmException, NoSuchPaddingException, IOException, InvalidKeyException, UnrecoverableKeyException, KeyStoreException, CertificateException, SignatureException {
		
	    KeyGenerator kg = KeyGenerator.getInstance("AES");
	    kg.init(128);
	    SecretKey key = kg.generateKey();

	    Cipher c = Cipher.getInstance("AES");
	    c.init(Cipher.ENCRYPT_MODE, key);

	    FileInputStream fis = new FileInputStream("../files/" + fileName);
	    FileOutputStream fos = new FileOutputStream("../files/" +fileName + ".seguro");
	    CipherOutputStream cos = new CipherOutputStream(fos, c);

	    int totalFileLength = fis.available();
		byte[] dataToBytes = new byte[Math.min(totalFileLength==0 ? 1 : totalFileLength , 1024)]; 
		
		Signature signature = initSignature();
		
	    int i = fis.read(dataToBytes);
	    
	    //While make signature, cipher the file
	    while (i > 0) {
	    	signature.update(dataToBytes,0,i);
	        cos.write(dataToBytes, 0, i);
	        i = fis.read(dataToBytes);
	    }
	    cos.close();
	    fis.close();
	    
	    outStream.writeObject(signature.sign());

	    byte[] keyEncoded = key.getEncoded();
	    FileOutputStream kos = new FileOutputStream("../files/" + fileName + ".key");
	    kos.write(keyEncoded);
	    kos.close();
		
	}
	
	/**
	 * Cipher the key and save it
	 * @String fileName with initial name
	 */
	private void cipherKey(String fileName) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException {
		
		String path = "../truststore/"+this.destUsername+".cer"; 
		
    	FileInputStream certToVerify = new FileInputStream(path);
    	
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Certificate cer = cf.generateCertificate(certToVerify);
        certToVerify.close();
        
        PublicKey pubKey = cer.getPublicKey();
    	
    	Cipher c = Cipher.getInstance("RSA"); 
    	
    	c.init(Cipher.WRAP_MODE, pubKey); 
    	
    	//create a new file with the key
    	File fKey = new File("../files/" + fileName + ".key");
    	
    	FileInputStream fis = new FileInputStream(fKey);
    	
    	byte[] AESkey = new byte[fis.available()]; 
    	fis.read(AESkey);
    	fis.close();
    	
    	//after save the key on buffer delete
    	fKey.delete();
    	
    	SecretKey keyAES = new SecretKeySpec(AESkey, "AES");
    	
    	// cipher key
    	byte[] wrappedKey = c.wrap(keyAES);	
    	
    	FileOutputStream fos = new FileOutputStream("../files/" + fileName + ".keykey");
    	
    	fos.write(wrappedKey); 
    	
    	fos.close();
    	
    	
	}
	
	/**
	 * Method to communicate with the server
	 */
	public void sendToServer(ObjectOutputStream outStream, ObjectInputStream inStream) throws UnknownHostException, IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, SignatureException, UnrecoverableKeyException, KeyStoreException, CertificateException, IllegalBlockSizeException, BadPaddingException, ClassNotFoundException {
		
		outStream.writeObject(this.filesDestUsername.size());
		
		for (String fileName : this.filesDestUsername) { 
			
			//check if file exists on client
			
			File f = new File("../files/" + fileName);
			
			Boolean fileExistClient = f.exists();
			
			if(fileExistClient) {

				//Send true to server know that file exists
				outStream.writeObject(fileExistClient);
				
				//Send the file name
				outStream.writeObject(fileName);
				
				Boolean fileExistServer = (Boolean) inStream.readObject();
				
				// Verify if file exists in the server
				//File does not exist
				if(!fileExistServer) {
					
					//cipher file
					cipherFile(fileName,outStream); 
					//cipher key
					cipherKey(fileName); 
					
					File secureFile = new File("../files/" + fileName + ".seguro");
			
					FileInputStream fileInStream = new FileInputStream("../files/" + fileName + ".seguro");
			
					//get total file length
					int totalFileLength = fileInStream.available();
					
					//send to server exact buffer size
					outStream.writeObject(totalFileLength);
			
					//byte array for file
					byte[] dataToBytes = new byte[Math.min(totalFileLength==0 ? 1 : totalFileLength , 1024)]; 
					
					//Length of the contents of the read file 
					int contentLength = fileInStream.read(dataToBytes); 
					
					//read files chunk 
					while(contentLength > 0 ) {
						//send data to server
						outStream.write(dataToBytes,0,contentLength);
						//continue to read fileInStream
						contentLength = fileInStream.read(dataToBytes);
					}
					fileInStream.close(); 
					secureFile.delete();
					
					File fileKeyCiph = new File("../files/" + fileName + ".keykey");
					
					FileInputStream fileInStreamkey = new FileInputStream(fileKeyCiph);  
					
					//send the cipher key to the server
					outStream.writeObject(fileInStreamkey.readAllBytes());
					
					fileInStreamkey.close();
					fileKeyCiph.delete();
					
					System.out.println("The file " + fileName + " have been sent correctly.");
					
				}
				else {
					System.err.println("The file " + fileName + " already exist in server.");
				}
			} else {
				outStream.writeObject(fileExistClient);
				System.err.println("The file " + fileName + " doesn't exist. You must provide a existing file.");
			}
		}
	}

}
