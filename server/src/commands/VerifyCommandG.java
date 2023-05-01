package commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class VerifyCommandG {
	
	private String username; 
	
	public VerifyCommandG(String username) {
		this.username = username;
	}
	
	/**
	 * Verify commandG and check what to do
	 * @ObjectInputStream inStream
	 * @ObjectOutputStream outStream
	 */
	public void verify(ObjectInputStream inStream, ObjectOutputStream outStream) throws IOException, ClassNotFoundException {

		int numbersOfFiles = (int) inStream.readObject();
				
		for (int i = 0; i < numbersOfFiles; i++) {
			
			boolean alreadySent = (boolean) inStream.readObject(); 
			
			if(!alreadySent) {
				
				String fileName = (String) inStream.readObject(); 
				
				// Check file exists on server
				File fcifrado = new File("../cloud/"+this.username+"/files/" + fileName + ".cifrado");
				File fassinado = new File("../cloud/"+this.username+"/files/" + fileName + ".assinado");
				File fseguro = new File("../cloud/"+this.username+"/files/" + fileName + ".seguro");
				
				Boolean fileExistServer = fcifrado.exists() || fassinado.exists() || fseguro.exists();

				outStream.writeObject(fileExistServer);
				
				if(fileExistServer) { 
					
					//case file is type ASSINADO
					File fileToReadSign = new File("../cloud/"+this.username+"/files/" + fileName + ".assinado");
					if(fileToReadSign.exists()){				
						sendToClient(outStream, "-s", fileToReadSign, fileName);
					}
					else {
						//case file is type CIFRADO
						File fileToReadCif = new File("../cloud/"+this.username+"/files/" + fileName + ".cifrado");
						if(fileToReadCif.exists()){
							sendToClient(outStream, "-c", fileToReadCif, fileName);
						}
						//case file is type SEGURO
						else {
							File fileToReadSecure = new File("../cloud/"+this.username+"/files/" + fileName + ".seguro");
							if(fileToReadSecure.exists()){ 
								sendToClient(outStream, "-e", fileToReadSecure, fileName);
								
							}
						}
					}
					System.out.println("The file " + fileName + " already sent!");				
				}
				else {
					System.out.println("The file " + fileName + " is not recognized!");
				}
			}
		}				
	}
	
	/**
	 * Send files to client
	 * @ObjectOutputStream outStream
	 * @String option client manager option
	 * @File fileToRead 
	 * @String fileName
	 */
	private void sendToClient(ObjectOutputStream outStream, String option, File fileToRead, String fileName) throws IOException {
		
		outStream.writeObject(option); 
		
		if(option.equals("-c")) {
			FileInputStream fileInStreamSecretKey = new FileInputStream("../cloud/"+this.username+"/keys/" + fileName + ".chave_secreta"); 
			outStream.write(fileInStreamSecretKey.readAllBytes());
			fileInStreamSecretKey.close();
		} 
		
		else if (option.equals("-s")) {
			FileInputStream fileInStreamSignature = new FileInputStream("../cloud/"+this.username+"/signatures/" + fileName + ".assinatura"); 
			outStream.write(fileInStreamSignature.readAllBytes()); 
			fileInStreamSignature.close();
		}
		
		else {
			FileInputStream fileInStreamSecretKey = new FileInputStream("../cloud/"+this.username+"/keys/" + fileName + ".chave_secreta"); 
			outStream.write(fileInStreamSecretKey.readAllBytes());
			fileInStreamSecretKey.close();
			
			FileInputStream fileInStreamSignature = new FileInputStream("../cloud/"+this.username+"/signatures/" + fileName + ".assinatura"); 
			outStream.write(fileInStreamSignature.readAllBytes()); 
			fileInStreamSignature.close();
		}
	
		FileInputStream fileInStream = new FileInputStream(fileToRead); 
				
		int totalFileLength = fileInStream.available();
		
		outStream.writeObject(totalFileLength);

		byte[] dataToBytes = new byte[Math.min(totalFileLength==0 ? 1 : totalFileLength , 1024)];
		
		int contentLength = fileInStream.read(dataToBytes);  
								
		while(contentLength > 0) {
			outStream.write(dataToBytes,0,contentLength); 
			outStream.flush();						
			contentLength = fileInStream.read(dataToBytes);
		}
	}
	
}
