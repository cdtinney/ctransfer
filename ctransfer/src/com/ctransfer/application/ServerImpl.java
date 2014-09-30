package com.ctransfer.application;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.ctransfer.utils.FileUtils;

// TODO - Comments
public class ServerImpl implements Server {
	
	private ServerSocket serverSocket = null;
	private Socket clientSocket = null;
	
	private HashMap<String, Command> commands;
	
	private String pwd = "C:\\";
	
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
				clientSocket.close();
			}
		
			if (serverSocket != null) {
				serverSocket.close();
			}
			
			System.out.println("Socket closed.");
		
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		
	}
	
	public void listen() throws Exception {
		
		BufferedReader reader = null;
		PrintWriter writer = null;
		
		try {

			reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			writer = new PrintWriter(clientSocket.getOutputStream(), true);
			
		    writer.println("Initial response.");
		    
		    while (true) {
		    	
		    	String clientRequest = reader.readLine();
		    	if (clientRequest == null) {
		    		break;
		    	}
		    	
		    	String response = processRequest(clientRequest);
		    	writer.println(response);
		    	
		    }
		    
		} catch (SocketException e) {
			System.out.println("SocketException: the connection has most likely been closed.");
			
		} catch (Exception e) {
			e.printStackTrace();

		// Clean up resources
		} finally {
			
			System.out.println("Cleaning up resources.");
			
			if (reader != null) {
				reader.close();
			}
			
			if (writer != null) {
				writer.close();
			}
			
			stop();
			
		}
		
	}
	
	private String processRequest(String request) {
		
		// Trim extra white space
		request = request.trim();
		
		if (!commands.containsKey(request))  {
			return "Unrecognized command";
		}
		
		Command command = commands.get(request);
		
		// Run the command
		command.run();
		
		// Return the computed response
		return command.getResponse();
		
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
				
				List<File> files = FileUtils.listFiles(pwd, null);
				
				StringBuilder sb = new StringBuilder();
				for (File f : files) {
					
					// TODO - Display folders differently
					sb.append(f.getName());
					sb.append("\t");
					
				}
				
				return sb.toString();
				
			}
			
		});
		
		commands.put("help", new Command() {

			@Override
			public String getCommandString() {
				return "help";
			}

			@Override
			public String getResponse() {
				
				Set<String> commandSet = commands.keySet();
				StringBuilder sb = new StringBuilder();
				
				for (String s : commandSet) {
					sb.append(s);
					sb.append("\t");
				}
				
				return sb.toString();
				
			}

			@Override
			public void run() {
				System.out.println("running help");
			}
			
		});
		
		
	}

}
