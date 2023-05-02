package commands;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class VerifyCommandC { 
	
	private String username;
	private String from = null;
	
	public VerifyCommandC(String username) {
		this.username = username;
	}
	
	public VerifyCommandC(String username, String from) {
		this.username = username;
		this.from = from;
	}
	
	/**
	 * Verify commandC and check what to do
	 * @ObjectInputStream inStream
	 * @ObjectOutputStream outStream
	 */
	public void verify(ObjectInputStream inStream, ObjectOutputStream outStream)
			throws ClassNotFoundException, IOException {
		
		String type = from.equals(null) ? "" : this.from;
		
		int filesDim = (int) inStream.readObject();

		for (int i = 0; i < filesDim; i++) {

			Boolean fileExistClient = (Boolean) inStream.readObject();

			// check if file exists on client
			if (fileExistClient) {
				String fileName = (String) inStream.readObject();

				File fcifrado = new File("../cloud/"+this.username+"/files/"+ fileName + ".cifrado" + type);
				File fseguro = new File("../cloud/"+this.username+"/files/" + fileName + ".seguro" + type);

				//check if file does not exists on the server
				Boolean fileExistServer = fcifrado.exists() || fseguro.exists();

				//send the output
				outStream.writeObject(fileExistServer);

				//if does not exists
				if (!fileExistServer) {

					// ---------------Receives the cipher file----------------------

					String fileNameCif = (String) inStream.readObject();

					FileOutputStream outFileStreamCif = new FileOutputStream("../cloud/"+this.username+"/files/" + fileNameCif + type);
					BufferedOutputStream outFileCif = new BufferedOutputStream(outFileStreamCif);

					try {
						Long fileSizeCif = (Long) inStream.readObject();

						int fileSizeCifInt = fileSizeCif.intValue();

						byte[] bufferDataCif = new byte[Math.min(fileSizeCifInt, 1024)];

						int contentLengthCif = inStream.read(bufferDataCif);

						while (fileSizeCifInt > 0 && contentLengthCif > 0) {
							if (fileSizeCifInt >= contentLengthCif) {
								outFileCif.write(bufferDataCif, 0, contentLengthCif);
							} else {
								outFileCif.write(bufferDataCif, 0, fileSizeCifInt);
							}
							contentLengthCif = inStream.read(bufferDataCif);
							fileSizeCifInt -= contentLengthCif;
						}

					} catch (ClassNotFoundException e1) {
						e1.printStackTrace();
					}
					outFileCif.close();

					// ---------------Receives the cipher key----------------------

					String fileNameKey = (String) inStream.readObject();

					FileOutputStream outFileStreamKey = new FileOutputStream("../cloud/"+this.username+"/keys/" + fileNameKey + type);

					BufferedOutputStream outFileKey = new BufferedOutputStream(outFileStreamKey);

					try {

						Long fileSizeKey = (Long) inStream.readObject();

						int fileSizeKeyInt = fileSizeKey.intValue();

						byte[] bufferDataKey = new byte[Math.min(fileSizeKeyInt, 1024)];

						int contentLengthKey = inStream.read(bufferDataKey);

						while (fileSizeKeyInt > 0 && contentLengthKey > 0) {
							if (fileSizeKeyInt >= contentLengthKey) {
								outFileKey.write(bufferDataKey, 0, contentLengthKey);
							} else {
								outFileKey.write(bufferDataKey, 0, fileSizeKeyInt);
							}
							contentLengthKey = inStream.read(bufferDataKey);
							fileSizeKeyInt -= contentLengthKey;
						}

					} catch (ClassNotFoundException e1) {
						e1.printStackTrace();
					}
					outFileKey.close();
					System.out.println("The file " + fileName + " received!");
					
				//can't proceed if the file already exists on the server
				} else {
					System.out.println("The file " + fileName + " already exist in server.");
				}
			} 
		}
	}

}
