package commands;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class VerifyCommandS {
	
	private String username; 
	private String from = null;	
	
	public VerifyCommandS(String username) {
		this.username = username;
	}
	
	public VerifyCommandS(String username, String from) {
		this.username = username;
		this.from = from;
	}

	
	/**
	 * Verify commandS and check what to do
	 * @ObjectInputStream inStream
	 * @ObjectOutputStream outStream
	 */
	public void verify(ObjectInputStream inStream, ObjectOutputStream outStream)
			throws IOException, ClassNotFoundException {

		String type = this.from == null ? "" : "." + this.from;

		// Get numbers of files
		int numbersOfFiles = (int) inStream.readObject();

		for (int i = 0; i < numbersOfFiles; i++) {

			// check if file exists to continue
			if ((boolean) inStream.readObject()) {

				// Read the file name received by client
				String fileName = (String) inStream.readObject();

				// Check file exists on server
				File fassinado = new File("../cloud/"+this.username+"/files/" + fileName + ".assinado" + type);
				File fseguro = new File("../cloud/"+this.username+"/files/" + fileName + ".seguro" + type);
				
				Boolean fileExistServer = fassinado.exists() || fseguro.exists();

				// Verify if file exists
				if (!fileExistServer) {

					// File does not exist
					outStream.writeObject(false);

					// Create new fileOutput ".assign"
					FileOutputStream outFile = new FileOutputStream("../cloud/"+this.username+"/files/" + fileName + ".assinado" + type);

					// get the total buffer size for each file Math.min(totalbytesOfFile,1024)
					int totalFileLength = (int) inStream.readObject();

					// Buffer
					byte[] bufferData = new byte[Math.min(totalFileLength==0 ? 1 : totalFileLength , 1024)];

					// Read chunk file
					int contentLength = inStream.read(bufferData);

					while (totalFileLength > 0 && contentLength > 0) {
						if (totalFileLength >= contentLength) {
							outFile.write(bufferData, 0, contentLength);
						} else {
							outFile.write(bufferData, 0, totalFileLength);
						}
						totalFileLength -= contentLength;
						contentLength = inStream.read(bufferData);
					}
					outFile.close();
					
					// Get Signature
					FileOutputStream outSignature = new FileOutputStream("../cloud/"+this.username+"/signatures/" + fileName + ".assinatura" + type);

					// Get out put of signature
					outSignature.write((byte[]) inStream.readObject());
					outSignature.close();

				}
				// File exist on server
				else {
					outStream.writeObject(true);
					System.out.println("The file " + fileName + " already exist in server.");
				}
			}
		}
	}
}
