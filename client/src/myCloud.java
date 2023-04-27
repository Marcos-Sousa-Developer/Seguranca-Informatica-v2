import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import commands.CommandAU;
import commands.CommandC;
import commands.CommandE;
import commands.CommandG;
import commands.CommandS;
import commands.CommandUP;
import commands.VerifyPort;

public class myCloud {
	
	/**
	 * Check if command is right
	 * @String[] list of arguments to check 
	 * @return Array of strings with host and port
	 */
	private static String[] verifyCommand(String[] args) {
		
		String ip = "";
		String port = ""; 
		
		if(args.length < 4) {
			System.err.println("Command not valid.");
			System.err.println("Example: myCloud -a <serverAddress> {-c || -s || -e || -g} {<filenames>}+");
	    	System.exit(-1);
		}

		if(!args[0].equals("-a")){
			System.err.println("You must provide -a option.");
	    	System.exit(-1);
		}
		switch (args[2]) {
			case "-au":
				if(!(args.length == 6)){
					System.err.println("Command not valid.");
					System.err.println("Example: myCloud -a <serverAddress> -au <username> <password> <certificado>");
			    	System.exit(-1);
				}
				break;
			case "-u":
				if(!(args[4].equals("-p") && args.length > 6)){
					System.err.println("Command not valid.");
					System.err.println("Example: myCloud -a <serverAddress> -u <username> -p <password> {-c || -s || -e || -g} {<filenames>}+");
					System.err.println("Example: myCloud -a <serverAddress> -u <username> -p <password> -d <username de destinatário> {-c || -s || -e || -g} {<filenames>}+");
					System.exit(-1);
				}
				else if(!args[6].equals("-d")) {
					
					String[] options = new String[]{"-c", "-s", "-e", "-g"};
					
					if(!Arrays.asList(options).contains(args[6])){
						System.err.println("You must provide a valid option.");
						System.err.println("Valid options: -c || -s || -e || -g");
				    	System.exit(-1);
					}
					else if (!(args.length > 7)){
						System.err.println("Command not valid.");
						System.err.println("Example: myCloud -a <serverAddress> -u <username> -p <password> {-c || -s || -e || -g} {<filenames>}+");
				    	System.exit(-1);
					}
				} else { //Verifica caso a opcao -d seja dada
					String[] options = new String[]{"-c", "-s", "-e", "-g"};
					
					if(!((args.length > 9) && Arrays.asList(options).contains(args[8]))){
						System.err.println("Command not valid.");
						System.err.println("Example: myCloud -a <serverAddress> -u <username> -p <password> -d <username de destinatário> {-c || -s || -e || -g} {<filenames>}+");
				    	System.exit(-1);
					}
				}
				break;
		}
		String[] address = args[1].split(":");
		if(address.length == 2 && !address[0].equals("")) {
			ip = address[0];
			
			port = new VerifyPort(address[1]).verifyPort();
		
		} else {
			System.err.println("You must provide a valid address.");
			System.err.println("Example: 127.0.0.1:23456");
	    	System.exit(-1);
		}
		
		return new String[]{ip, port};
	}

	/**
	 * Manage type of request
	 * @String[] list of arguments
	 */
	public static void main(String[] args) throws Exception {
		
		
		String[] address = verifyCommand(args);
		
		//Split and get the files to manage
		List<String> files = new ArrayList<>(Arrays.asList(args)).subList(3, args.length);
		
		
		switch (args[2]) {
			case "-au":
				Boolean iscreated = new CommandAU(address[0], Integer.parseInt(address[1]), args[3], args[4], args[5]).createUser();
				if(!iscreated) {
					System.err.println("This username already exists.");
			    	System.exit(-1);
				}
				else {
					System.err.println("User created.");
			    	System.exit(0);
				}
				break;
			case "-u":
				Boolean login = new CommandUP(address[0], Integer.parseInt(address[1]), args[3], args[5]).verifyLogin();
				
				if(login) {
					System.out.println("Authorized");
					
					int optionIndex = 6;
					
					if(args[6].equals("-d")) {
						optionIndex = 8;
						//enviar ficheiros para o servidor para outros utilizadores
						//se não tiver previamente o certificado do destinatário, pede ao servidor
						//Se a maria enviar ficheiros para alice, os ficheiros devem de ficar com o nome "aa.pdf.assinado.maria"
						//new CommandD(address[0], Integer.parseInt(address[1]), files).sendToServer();
					}
					
					switch (args[optionIndex]) {
					case "-c":
						new CommandC(address[0], Integer.parseInt(address[1]), files).sendToServer();

						break;
					case "-s":
						new CommandS(address[0], Integer.parseInt(address[1]), files).sendToServer();

						break;
					case "-e":
						new CommandE(address[0], Integer.parseInt(address[1]), files).sendToServer();

						break;
					case "-g":
						new CommandG(address[0], Integer.parseInt(address[1]), files).sendToServer();

						break;
				}
				} else {
					System.err.println("Not authorized");
					System.err.println("Username or password invalid");
					System.exit(-1);
				}
				break;
		}
	}
}