package commands;

import java.util.List;

public class CommandD {
	
	private String ip;
	private int port;
	private List<String> files;

	public CommandD(String ip, int port, List<String> files) {
		this.ip = ip;
		this.port = port;
		this.files = files;
	}

}
