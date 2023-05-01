package commands;

import java.util.List;

public class CommandDC {

	private String username;
	private String password;
	private String destUsername;
	private List<String> filesDestUsername; 

	public CommandDC(String username, String password, String destUsername, List<String> filesDestUsername) {
		
		this.username = username;
		this.password = password;
		this.destUsername = destUsername;
		this.filesDestUsername = filesDestUsername;
	}

}
