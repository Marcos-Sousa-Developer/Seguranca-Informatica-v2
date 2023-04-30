package commands;


import java.io.*;
import java.security.*;
import java.util.*;

import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class CommandD {
	
	private static final String True = null;
	private static final String False = null;
	private List<String> filesDestUsername;
	private String ip;
	private int port;
	//duvidas com o certificado
	private String destUsername;
	private String commandToDo; 
	private String username;
	private Long dimFileCif;
	
	public CommandD(String ip, int port, String username, List<String> filesDestUsername, String destUsername, String commandToDo) {
		this.ip = ip;
		this.port = port;
		this.filesDestUsername = filesDestUsername;
		this.destUsername = destUsername;
		this.commandToDo = commandToDo;
		this.username = username;
	}
	
//FALTA ENVIAR PARA A PASTA CORRETA DO UTILIZADOR
	
	public void verifyCert() {
		
	}
	
	public void sendToServer(ObjectOutputStream outStream, ObjectInputStream inStream) {
		
		for (String fileName : this.filesDestUsername) {
			
			File f = new File("../files/" + fileName);
			Boolean fileExistClient = f.exists();
			
			
			if(fileExistClient) {
				
				outStream.writeObject(fileExistClient); 
				outStream.writeObject(fileName);
				
				Boolean fileExistServer = (Boolean) inStream.readObject();

				if(!fileExistServer) {
					String[] options = new String[] {"-c", "-s", "-e"};
					if (Arrays.asList(options).contains(commandToDo)) {
						
						switch(commandToDo) {
						
						case "-c":
							outStream.writeObject("-c");
							
							outStream.writeObject(this.filesDestUsername.size());
							
							
							//CommandC cipherF = new CommandC(filesDestUsername);
							//CommandC.cipherFile(fileName);
							
							//CommandC cipherK = new CommandC(filesDestUsername);
							//CommandC.cipherKey(fileName);
							
							File fileCif = new File("../files/" + fileName + ".cifrado" + this.username);
					        Long dimFileCif = fileCif.length();
						
					        outStream.writeObject(fileName + ".cifrado" + this.username);
					        outStream.writeObject(dimFileCif);
					        
					        BufferedInputStream myFileCif = new BufferedInputStream(new FileInputStream("../files/" + fileName + ".cifrado" + this.username));
					        
					        int dimFileCifInt = dimFileCif.intValue();
					        
							byte[] dataToBytesCif = new byte[Math.min(dimFileCifInt, 1024)]; 
						    
							int contentLengthCif = myFileCif.read(dataToBytesCif);
						    while (contentLengthCif > 0) {
						    	outStream.write(dataToBytesCif, 0, contentLengthCif);
						    	contentLengthCif = myFileCif.read(dataToBytesCif);
						    }
					        myFileCif.close();
					        //after send it can delete the file .cipher
					        fileCif.delete();
					        
					      //get the file.key
					        File keyCif = new File("../files/" + fileName + ".chave_secreta" + this.username);
					        Long dimKeyCif = keyCif.length();
					        
					        outStream.writeObject(fileName + ".chave_secreta");
					        outStream.writeObject(dimKeyCif);    
							break;
							
						case "-s":
							outStream.writeObject("-s");
							
							outStream.writeObject(this.filesDestUsername.size());
							
							CommandS sign = new CommandS(filesDestUsername);
							CommandS.initSignature();
							
							FileInputStream fileInStream = new FileInputStream("../files/" + fileName); 

							int totalFileLength = fileInStream.available();
							
							outStream.writeObject(totalFileLength);
							
							byte[] dataToBytes = new byte[Math.min(totalFileLength==0 ? 1 : totalFileLength , 1024)]; 

							int contentLength = fileInStream.read(dataToBytes); 
							
							while(contentLength > 0 ) {
								//Hash the data
								Signature signature.update(dataToBytes,0,contentLength);
								//send data to server
								outStream.write(dataToBytes,0,contentLength);
								//continue to read fileInStream
								contentLength = fileInStream.read(dataToBytes);
								
								outStream.writeObject(signature.sign());
								fileInStream.close();
								
								 System.out.println("The file " + fileName + " have been sent correctly.");
							}
	
							
							break;
						case "-e":
							outStream.writeObject("-e");
							outStream.writeObject(this.filesDestUsername.size());
							
							CommandE cifFile = new CommandE(filesDestUsername);
							CommandE.cipherFile(fileName,outStream); 
							//cipher key
							CommandE cifKey = new CommandE(filesDestUsername);
							CommandE.cipherKey(fileName); 
							
							File secureFile = new File("../files/" + fileName + ".seguro" + this.username);

							FileInputStream fileInStream = new FileInputStream("../files/" + fileName + ".seguro" + this.username);

							int totalFileLengthE = fileInStream.available();

							outStream.writeObject(totalFileLength);

							byte[] dataToBytesE = new byte[Math.min(totalFileLength==0 ? 1 : totalFileLength , 1024)]; 

							int contentLengthE = fileInStream.read(dataToBytes); 

							while(contentLengthE > 0 ) {
								outStream.write(dataToBytes,0,contentLengthE);
								contentLengthE = fileInStream.read(dataToBytesE);
							}
							fileInStream.close(); 
							secureFile.delete();
							
							File fileKeyCiph = new File("../files/" + fileName + ".keykey" + this.username);

							FileInputStream fileInStreamkey = new FileInputStream(fileKeyCiph);  

							outStream.writeObject(fileInStreamkey.readAllBytes());

							fileInStreamkey.close();
							fileKeyCiph.delete();
							
							System.out.println("The file " + fileName + " have been sent correctly.");

							
							break;
						}
	
							outStream.writeObject(fileExistClient); 
							outStream.writeObject(fileName);
						}
					else {
						System.err.println("Command not valid.");
						System.err.println("Example: myCloud -a <serverAddress> -u <username> -p <password> {-c || -s || -e || -g || -d} {<filenames>}+");
						System.err.println("Example: myCloud -a <serverAddress> -u <username> -p <password> -d <username de destinatário> {-c || -s || -e || -g || -d} {<filenames>}+");
						System.exit(-1);		
					}
				}
			else {
				outStream.writeObject(fileExistClient);
				System.err.println("The file " + fileName + " doesn't exist. You must provide a existing file.");		
			}
				
				}
			
			
		}
	}	
}
	

	


