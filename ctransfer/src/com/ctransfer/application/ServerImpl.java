package com.ctransfer.application;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;

import com.ctransfer.utils.FileUtils;
import com.google.common.io.Files;

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
		    
		    while (true) {
		    	
		    	System.out.println("Waiting for request...");
		    	String clientRequest = reader.readLine();
		    	if (clientRequest == null) {
		    		break;
		    	}

		    	System.out.println("Processing request: " + clientRequest);
		    	processRequest(clientRequest, writer);
		    	System.out.println("Request processed.");
		    	
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
	
	private void processRequest(String request, PrintWriter writer) {
		
		// Trim leading/trailing white space and set to lower case
		// TODO - This will set args to lower case
		request = request.trim().toLowerCase();
		
		String[] parts = request.split(" ");
		request = parts[0];
		
		// No command found for the client request
		if (!commands.containsKey(request)) {
			writer.println("Unrecognized command: " + request);
			return;
		}
		
		Command command = commands.get(request);
		ResponseType responseType = command.getResponseType();
		
		// Send the client the response type prior to the actual response
		writer.println(responseType);
		
		// TODO - Refactor (modify Command interface)
		if (responseType == ResponseType.FILE_LIST) {
			
			// Send the client the PWD.
			writer.println(pwd);
			
			String lines[] = command.getResponse().split("\\n");
			
			// Send the client the number of strings we are about to send
			writer.println(lines.length);
			
			for (String s : lines) {
				writer.println(s);
			}
			
		} else if (responseType == ResponseType.DELETE_FILE) {

			String fileName = parts[1];
			Boolean result = FileUtils.deleteFile(pwd, fileName);
			
			writer.println("Successfully deleted? " + result);
			
			
		} else if (responseType == ResponseType.FILE_TRANSFER) {
		
			String fileName = parts[1];
			
			File file = new File(pwd + fileName);
			if (!file.exists()) {
				writer.println("File does not exist!");
				// TODO - How to cancel transfer on client at this point
			}
			
			// Send file name
			writer.println(fileName);
			
			// Send file size
			writer.println(file.length());
			
			try {

				// Convert the file to a byte array, and send to the client
				byte[] data = Files.toByteArray(file);
				clientSocket.getOutputStream().write(data);				
				
			} catch (IOException e) {
				e.printStackTrace();
				
			}
			
		} else {
			writer.println(command.getResponse());
			
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
				
				List<File> files = FileUtils.listFiles(pwd, null, true);
				
				StringBuilder sb = new StringBuilder();
				for (File f : files) {
					
					sb.append(f.getName());
					
					if (f.isDirectory()) {
						sb.append("\\");
					}
					
					sb.append("\n");
					
				}
				
				return sb.toString();
				
			}

			@Override
			public ResponseType getResponseType() {
				return ResponseType.FILE_LIST;
			}

			@Override
			public byte[] getResponseBytes() {
				
				String response = getResponse();
				return response.getBytes();
				
			}
			
		});
		
		commands.put("delete", new Command() {

			@Override
			public String getCommandString() {
				return "delete";
			}

			@Override
			public String getResponse() {
				return null;
			}

			@Override
			public void run() {
				System.out.println("running delete");
			}

			@Override
			public ResponseType getResponseType() {
				return ResponseType.DELETE_FILE;
			}

			@Override
			public byte[] getResponseBytes() {
				
				String response = getResponse();
				return response.getBytes();
				
			}
			
		});
		
		commands.put("get", new Command() {

			@Override
			public String getCommandString() {
				return "get";
			}

			@Override
			public String getResponse() {
				return null;
			}

			@Override
			public void run() {
				System.out.println("running get");
			}

			@Override
			public ResponseType getResponseType() {
				return ResponseType.FILE_TRANSFER;
			}

			@Override
			public byte[] getResponseBytes() {
				
				String response = getResponse();
				return response.getBytes();
				
			}
			
		});
		
		
	}

}
