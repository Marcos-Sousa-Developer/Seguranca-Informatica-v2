package commands;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import java.io.File;


public class CommandDS {

	private String username;
	private String password;
	private String destUsername;
	private List<String> filesDestUsername; 

	
	public CommandDS(String username, String password, String destUsername, List<String> filesDestUsername) {
		
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
	 * Method to communicate with the server
	 */
	/**
	 * Method to communicate with the server
	 */
	public void sendToServer(ObjectOutputStream outStream, ObjectInputStream inStream) throws UnknownHostException, IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, SignatureException, UnrecoverableKeyException, KeyStoreException, CertificateException, IllegalBlockSizeException, BadPaddingException, ClassNotFoundException{
		//Send numbers of files

		outStream.writeObject(this.filesDestUsername.size());

		String path = "../keystore/"+this.destUsername+".cer"; 

		FileInputStream certToVerify = new FileInputStream(path);

		CertificateFactory cf = CertificateFactory.getInstance("X.509");

        Certificate cer = cf.generateCertificate(certToVerify);

        certToVerify.close();
		
		for (String fileName : this.filesDestUsername) {

			File fileToRead = new File("../files/" + fileName);

			if(fileToRead.exists()) {

				//Send true to server know that file exists				
				//Send the file name
				outStream.writeObject(fileName); 
				
				
				
				System.out.println(fileName);
				
				
				
				// Verify if file exists in the server
				//File does not exist
				if(!(Boolean) inStream.readObject()) {
					
					
					//Read the received file 
					
					FileInputStream fileInStream = new FileInputStream("../files/" + fileName); 
					
					
					System.out.println("5555555555555");
					
					
					//get signature object 
					Signature signature = initSignature(); 
					
					
					System.out.println("6666666666666666");
					
					
					//get total file length
					int totalFileLength = fileInStream.available();
					
					System.out.println(totalFileLength);
					
					
					
					//send to server exact buffer size
					outStream.writeObject(totalFileLength);

					
					System.out.println("77777777777777777");
					
					
					
					//byte array for file
					byte[] dataToBytes = new byte[Math.min(totalFileLength==0 ? 1 : totalFileLength , 1024)]; 
					
					
					System.out.println("88888888888888888888888");
					
					
					//Length of the contents of the read file 
					int contentLength = fileInStream.read(dataToBytes); 
					
					
					System.out.println("99999999999999999999");
					
					
					
					//read files chunk 
					while(contentLength > 0 ) {
						//Hash the data
						signature.update(dataToBytes,0,contentLength);
						//send data to server
						outStream.write(dataToBytes,0,contentLength);
						//continue to read fileInStream
						contentLength = fileInStream.read(dataToBytes);
					}
					
					//send signature to server
					outStream.writeObject(signature.sign());
					fileInStream.close();
					
					
			        System.out.println("The file " + fileName + " have been sent correctly.");
				}
				//File exist on the server
				else {
					System.out.println("File " + fileName + " is already on the server!");
				}
			}
			//File does not exists on the client
			else {
				outStream.writeObject(false);
				System.err.println("The file " + fileName + " doesn't exist. You must provide a existing file.");
			}
		}
	}
}