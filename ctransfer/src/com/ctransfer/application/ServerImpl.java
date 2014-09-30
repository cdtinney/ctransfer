package com.ctransfer.application;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class ServerImpl implements Server {
	
	private ServerSocket serverSocket = null;
	private Socket clientSocket = null;
	
	// Command map
	private HashMap<String, Command> commands;
	
	public ServerImpl() {
		
		commands = new HashMap<String, Command>();
		
		addCommands();
		
	}

	@Override
	public void start() {
		
		try {
			
			// Bind to port 9000. No IP specified - defaults to 0.0.0.0 -> 127.0.0.1
			serverSocket = new ServerSocket(9000);
			
			System.out.println("Waiting for a connection request...");
			System.out.println("[" + serverSocket.getInetAddress() +  ":" + serverSocket.getLocalPort() + "]");
			
			// Wait for a connection (note: this is a blocking call)
			clientSocket = serverSocket.accept();
			
			System.out.println("..connected to: " + clientSocket.getRemoteSocketAddress());
			
			listen();
			
		} catch (Exception e) {
			e.printStackTrace();
			
		} 
		
	}

	@Override
	public void stop() {
		
		try {
			
			if (clientSocket != null) {
				System.out.println("Closing the client socket...");
				clientSocket.close();
				System.out.println("..successfully closed.");
			}
		
			if (serverSocket != null) {
				System.out.println("Closing the server socket...");
				serverSocket.close();
				System.out.println("..successfully closed.");
			}
		
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		
	}
	
	public void listen() throws Exception {
		
		try {

			// TODO - Close resources
			BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
			
		    // Initiate
		    out.println("Initial response.");

			String inputLine;
		    while ((inputLine = reader.readLine()) != null) {
		    	
		    	System.out.println("Server received: " + inputLine);
		        out.println("you sent me: " + inputLine);
		    	
		    	// TODO - Process client request
		    	// TODO - Send proper response
		        
		        // TODO - Break loop when appropriate
		        
		    }
			
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		
	}
	
	private void addCommands() {
		
		commands.put("ls", new Command() {

			@Override
			public String getCommandString() {
				return "ls";
			}

			@Override
			public void run() {
				System.out.println("running ls");
			}

			@Override
			public String getResponse() {
				return "bunch of files";
			}
			
		});
		
		
	}

}
