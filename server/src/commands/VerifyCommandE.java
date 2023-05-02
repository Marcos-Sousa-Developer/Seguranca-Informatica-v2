package commands;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class VerifyCommandE {
	
	private String username; 
	private String from = null;
	
	public VerifyCommandE(String username) {
		this.username = username;
	}
	
	public VerifyCommandE(String username, String from) {
		this.username = username;
		this.from = from;
	}

	/**
	 * Verify commandE and check what to do
	 * @ObjectInputStream inStream
	 * @ObjectOutputStream outStream
	 */
	public void verify(ObjectInputStream inStream, ObjectOutputStream outStream) throws ClassNotFoundException, IOException {
		
		String type = this.from == null ? "" : "." + this.from;
		
		int numFiles = (int) inStream.readObject(); 
		
		for (int i = 0; i < numFiles; i++) {
			
			Boolean fileExistClient = (Boolean) inStream.readObject();
			
		
			if (fileExistClient) {
				String fileName = (String) inStream.readObject();
				
				// Check file exists on server
				File fcifrado = new File("../cloud/"+this.username+"/files/" + fileName + ".cifrado" + type);
				File fassinado = new File("../cloud/"+this.username+"/files/" + fileName + ".assinado" + type);
				File fseguro = new File("../cloud/"+this.username+"/files/" + fileName + ".seguro" + type);
				
				Boolean fileExistServer = fcifrado.exists() || fassinado.exists() || fseguro.exists();
								
				outStream.writeObject(fileExistServer);

				if (!fileExistServer) {
		
					FileOutputStream out = new FileOutputStream("../cloud/"+this.username+"/files/" + fileName + ".seguro" + type);
					
					//save the signature of the file
					FileOutputStream outSignature = new FileOutputStream("../cloud/"+this.username+"/signatures/" + fileName + ".assinatura" + type); 
					
					outSignature.write((byte[]) inStream.readObject()); 
					
					outSignature.close();
					
					//get total cipher text
					int totalFileLength = (int) inStream.readObject();
			
					//byte array for file
					byte[] dataToBytes = new byte[Math.min(totalFileLength==0 ? 1 : totalFileLength , 1024)]; 
					
					//Length of the contents of the read file 
					int contentLength = inStream.read(dataToBytes); 
					
					while(contentLength > 0  &&  totalFileLength > 0) { 
						
						if(totalFileLength >= contentLength) {
							out.write(dataToBytes,0,contentLength);
						} 
						
						else {
							out.write(dataToBytes,0,totalFileLength);
						}
	
						totalFileLength -= contentLength;
						
						if(contentLength > 0  &&  totalFileLength > 0) {
							//continue to read fileInStream
							contentLength = inStream.read(dataToBytes);
						}
							
					}
					
					out.close(); 
					
					//Save the cipher secret key
					FileOutputStream outKey = new FileOutputStream("../cloud/"+this.username+"/keys/" + fileName + ".chave_secreta" + type); 
					
					outKey.write((byte[]) inStream.readObject()); 
					
					outKey.close();
					System.out.println("The file " + fileName + " received!");
					
				} else {
					System.out.println("The file " + fileName + " already exist in server.");
				}
			}
		}
	}
}
