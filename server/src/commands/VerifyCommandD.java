package commands;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class VerifyCommandD {
	
	/**
	 * Verify commandC and check what to do
	 * @ObjectInputStream inStream
	 * @ObjectOutputStream outStream
	 */
	public void verify(ObjectInputStream inStream, ObjectOutputStream outStream) throws IOException {
		int numFiles = (int) inStream.readObject();

		for (int i = 0; i < numFiles; i++) {
			
			Boolean fileExistClient = (Boolean) inStream.readObject();
		
			if (fileExistClient) {
				//check if files exists - server~
				/*if (!fileExistServer) {
					
				}
				else {
					System.out.println("The file " + fileName + " already exist in server.");
				}*/
			}
		}
	}
}
