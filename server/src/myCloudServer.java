import java.io.IOException;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import commands.VerifyPort;
import thread.ServerThread;

public class myCloudServer {

	/**
	 * Check if port number is valid
	 * @String[] list of arguments to check 
	 * @return Port in integer
	 */
	private static int verifyPort(String[] args) {
		
		String port = "";
		if(args.length == 1){
			port = new VerifyPort(args[0]).verifyPort();
		} else {
			System.err.println("You must provide a port.");
	    	System.exit(-1);
		}
		return Integer.parseInt(port);
	}
	
	/**
	 * Initialize server
	 * @String[] list of arguments to check 
	 */
	public static void main(String[] args) throws IOException {
		
		int port = verifyPort(args);
		
		System.setProperty("javax.net.ssl.keyStore", "../cloud/server.keystore");
		System.setProperty("javax.net.ssl.keyStorePassword", "12345678");
		System.setProperty("javax.net.ssl.keyStoreType", "PKCS12");
		
		myCloudServer server = new myCloudServer();
		server.startServer(port);
	}
	
	/**
	 * Method to connect the server
	 * @integer port number
	 */
	private void startServer(int port) throws IOException {
		
		ServerSocketFactory ssf = SSLServerSocketFactory.getDefault();
		SSLServerSocket servSocket = null;
		servSocket = (SSLServerSocket) ssf.createServerSocket(port);
		
		System.out.println("Server connected");
		
		while(true) {
			try {
				SSLSocket inSoc = (SSLSocket) servSocket.accept();
				ServerThread newServerThread = new ServerThread(inSoc);
				newServerThread.start();
		    }
		    catch (IOException e) {
		        e.printStackTrace();
		    }
		}
	}
}
