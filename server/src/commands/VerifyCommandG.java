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
			
			String fileName = (String) inStream.readObject(); 
			
			//boolean alreadySent = (boolean) inStream.readObject(); 
			
			File directory = new File("../cloud/"+this.username+"/files");
			File[] files = directory.listFiles();
			
			outStream.writeObject(files.length); 
			
			int count = 0;
			
			if(files.length != 0) {				
				for (File file : files) {
		            if (file.getName().contains(fileName + ".")) {
		                // The file exists with the specified name and extension
		            	count++;
		            	
		            	outStream.writeObject(true);
		                
		            	boolean needCert =  
		            			(file.getName().contains(".assinado") && !file.getName().endsWith(".assinado")) ||
		            			(file.getName().contains(".seguro") && !file.getName().endsWith(".seguro"));
		            	
		            	outStream.writeObject(needCert);
		            	
		            	String extension = "";
		            	
		            	if(needCert) {
		            		int dotIndex = file.getName().lastIndexOf(".");
		                    extension = file.getName().substring(dotIndex + 1);
		                    System.out.println(".............");
		                    System.out.println(extension);
		                    
		                    outStream.writeObject(extension);
		                    
		                    boolean provide = (boolean) inStream.readObject();
		                    
		                    if(provide) {
			                    
			                    System.out.println("File extension: " + extension); 
			                    String path = "../cloud/usersCert/" + extension + ".cer";
			                    File cer = new File(path);
			                    FileInputStream destUserCert = new FileInputStream(cer); 
			                    outStream.writeObject(destUserCert.readAllBytes());
			                    destUserCert.close();
			            	}
		            	}
		            	
		            	if(file.getName().contains(".cifrado") && !file.getName().endsWith(".cifrado")) {
		            		int dotIndex = file.getName().lastIndexOf(".");
		            		extension = file.getName().substring(dotIndex + 1);
		            	}
		                
		                if(file.getName().contains(".cifrado")) {
		                	sendToClient(inStream, outStream, "-c", file, fileName, extension);
		                }
		                
		                else if(file.getName().contains(".assinado")) {
		                	sendToClient(inStream, outStream, "-s", file, fileName, extension);
		                }
		                
		                else if(file.getName().contains(".seguro")) {
		                	sendToClient(inStream, outStream, "-e", file, fileName, extension);
		                }
		                
		                System.out.println("The file " + file.getName() + " was sent!");
		            }
		            else {
		            	outStream.writeObject(false);
		            }
				}
				if (count == 0) {
					outStream.writeObject(false); 
					System.out.println("The file " + fileName + " is not recognized!");
				} else {
					outStream.writeObject(true); 
				}
	        }
			else {
				System.out.println("The file " + fileName + " is not recognized!");
			}
		}				
	}
	
	/**
	 * Send files to client
	 * @throws ClassNotFoundException 
	 * @ObjectOutputStream outStream
	 * @String option client manager option
	 * @File fileToRead 
	 * @String fileName
	 */
	private void sendToClient(ObjectInputStream inStream, ObjectOutputStream outStream, String option, File fileToRead, String fileName, String extension) throws IOException, ClassNotFoundException {
		
		String fileToCheck = fileToRead.getName();
		
		outStream.writeObject(option);  
		String fileToSave = fileToCheck.contains("cifrado") ? fileToCheck.replace("cifrado", "decifrado") : fileToCheck;
		outStream.writeObject(fileToSave); 
		
		if((boolean) inStream.readObject()) {
			
			extension = extension.equals("") ? extension : "." + extension;
			
			if(option.equals("-c")) {
				String concat = fileName + ".chave_secreta" + extension;
				FileInputStream fileInStreamSecretKey = new FileInputStream("../cloud/"+this.username+"/keys/" + concat); 
				outStream.write(fileInStreamSecretKey.readAllBytes());
				fileInStreamSecretKey.close();
			} 
			
			else if (option.equals("-s")) {
				String concat = fileName + ".assinatura" + extension;
				FileInputStream fileInStreamSignature = new FileInputStream("../cloud/"+this.username+"/signatures/" + concat); 
				outStream.write(fileInStreamSignature.readAllBytes()); 
				fileInStreamSignature.close();
			}
			
			else if (option.equals("-e")){
				String concat = fileName + ".chave_secreta" + extension;
				FileInputStream fileInStreamSecretKey = new FileInputStream("../cloud/"+this.username+"/keys/" + concat); 
				outStream.write(fileInStreamSecretKey.readAllBytes());
				fileInStreamSecretKey.close();
				concat = fileName + ".assinatura" + extension;
				FileInputStream fileInStreamSignature = new FileInputStream("../cloud/"+this.username+"/signatures/" + concat); 
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
		
		else {
			
			System.out.println(this.username + " already have the file " + fileToSave);
		}
		
		
	}
	
}
