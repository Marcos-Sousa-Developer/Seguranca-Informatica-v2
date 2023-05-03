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
import java.security.cert.CertificateException;
import java.util.List;
import java.io.File;


public class CommandS {
	
	private String username;
	private String password;
	private List<String> files;

	
	public CommandS(String username, String password, List<String> files) { 
		this.username = username;
		this.password = password;		
		this.files = files;
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
	public void sendToServer(ObjectOutputStream outStream, ObjectInputStream inStream) throws UnknownHostException, IOException, NoSuchAlgorithmException, SignatureException, ClassNotFoundException, UnrecoverableKeyException, InvalidKeyException, KeyStoreException, CertificateException {
		
		//Send the option first
		outStream.writeObject("-s");
		
		//Send numbers of files
		outStream.writeObject(this.files.size());
		
		for (String fileName : this.files) {
			
			File fileToRead = new File("../files/" + fileName);
			
			if(fileToRead.exists()) {
				
				//Send true to server know that file exists
				outStream.writeObject(true); 
				
				//Send the file name
				outStream.writeObject(fileName); 
				
				// Verify if file exists in the server
				//File does not exist
				if(!(Boolean) inStream.readObject()) {
					
					//Read the received file 
					FileInputStream fileInStream = new FileInputStream("../files/" + fileName); 
					
					//get signature object 
					Signature signature = initSignature(); 
					
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
