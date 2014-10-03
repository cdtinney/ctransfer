package com.ctransfer.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.ctransfer.application.Application;
import com.ctransfer.enums.ResponseType;
import com.ctransfer.utils.ArrayUtils;
import com.ctransfer.utils.FileUtils;
import com.ctransfer.utils.OSUtils;
import com.google.common.io.Files;

// TODO - Comments
public class SocketServer {
	
	private final Integer port;
	
	private ServerSocket serverSocket = null;
	private Socket clientSocket = null;
	
	private HashMap<String, CommandHandler> commandHandlers;

	private String pwd = "";
	
	public SocketServer(Integer port) {
		this.port = port;
		
		commandHandlers = new HashMap<String, CommandHandler>();
		addCommands();
		
		setPwdByOS();
		
	}

	public void start() {
		
		try {
			
			// Bind to port 9000. No IP specified - defaults to 0.0.0.0 -> 127.0.0.1
			serverSocket = new ServerSocket(this.port);
			
			System.out.println("Waiting for a connection request...");
			System.out.println("[" + serverSocket.getInetAddress() +  ":" + serverSocket.getLocalPort() + "]");
			
			// Wait for a connection (note: this is a blocking call)
			clientSocket = serverSocket.accept();
			
			System.out.println("Client connected: " + clientSocket.getRemoteSocketAddress());
			
			listen();
			
		} catch (Exception e) {
			if(e instanceof BindException) {
				System.err.println("A Server is already running at this address.");
				Application.init();
			}
				
			e.printStackTrace();
			System.exit(-1);
		} 
		
	}

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
		    	
		    	System.out.println("Waiting for request...\n");
		    	String clientRequest = reader.readLine();
		    	if (clientRequest == null) {
		    		break;
		    	}

		    	System.out.println("Processing request: " + clientRequest + "\n");
		    	processRequest(clientRequest, writer);
		    	System.out.println("Request processed.\n");
		    	
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
		
		// Trim leading/trailing white space
		request = request.trim();
		
		// Split by whitespace, remove empty strings
		String[] parts = request.split(" ");
		String[] nonEmptyParts = ArrayUtils.removeEmptyStrings(parts);
		
		// Set the command to lower case, since that is how they're mapped
		String commandStr = nonEmptyParts[0].toLowerCase();
		
		CommandHandler command = commandHandlers.get(commandStr);
		if (command == null) {
			writer.println(ResponseType.ERROR + "- Unrecognized command: " + commandStr);
			return;
		}
		
		// Send the client the response type prior to the actual response
		writer.println(command.getResponseType());

		// Parse out the arguments
		String[] args = Arrays.copyOfRange(nonEmptyParts, 1, nonEmptyParts.length);
		
		// Run the command, passing the socket so it can send directly to the client
		command.handle(writer, args);
		
	}
	
	private void addCommands() {
		
		if (commandHandlers == null) {
			return;
		}
		
		commandHandlers.put("ls", new CommandHandler() {

			@Override
			public ResponseType getResponseType() {
				return ResponseType.FILE_LIST;
			}

			@Override
			public void handle(PrintWriter writer, String[] args) {

				// Send the present working directory (pwd)
				writer.println(pwd);
				
				List<String> fileNames = getFileNames();
				
				// Send the number of strings we are about to send
				writer.println(fileNames.size());
				
				// Send each line/file name
				for (String s : fileNames) {
					writer.println(s);
				}
				
			}
			
			private List<String> getFileNames() {
				
				List<File> files = FileUtils.listFiles(pwd, null, true);
				List<String> fileNames = new ArrayList<String>();
				
				for (File f : files) {
					
					StringBuilder sb = new StringBuilder();
					sb.append(f.getName());
					
					if (f.isDirectory()) {
						sb.append("\\");
					}
					
					fileNames.add(sb.toString());					
					
				}
				
				return fileNames;
				
			}
			
		});
		
		commandHandlers.put("delete", new CommandHandler() {

			@Override
			public ResponseType getResponseType() {
				return ResponseType.DELETE_FILE;
			}

			@Override
			public void handle(PrintWriter writer, String[] args) {

				String fileName = args[0];
				Boolean result = FileUtils.deleteFile(pwd, fileName);
				
				writer.println(result ? ("Successfully deleted: " + fileName) : ("File was not successfully deleted: " + fileName));
				
			}
			
		});
		
		commandHandlers.put("get", new CommandHandler() {

			@Override
			public ResponseType getResponseType() {
				return ResponseType.FILE_TRANSFER;
			}

			@Override
			public void handle(PrintWriter writer, String[] args) {
				
				try {

					if (args.length < 1) {
						writer.println(ResponseType.ERROR + " - No file named specified.");
						return;
					}
					
					String fileName = args[0];
					File file = new File(pwd + fileName);
					if (!file.exists()) {
						writer.println(ResponseType.ERROR + " - File does not exist.");
						return;
					}
					
					// Send file name
					writer.println(fileName);
					
					// Send file size
					writer.println(file.length());

					// Convert the file to a byte array, and send to the client
					byte[] data = Files.toByteArray(file);
					clientSocket.getOutputStream().write(data);				
					
				} catch (IOException e) {
					e.printStackTrace();
					
				}
				
			}
			
		});
		
		commandHandlers.put("exit", new CommandHandler() {

			@Override
			public ResponseType getResponseType() {
				return ResponseType.EXIT;
			}

			@Override
			public void handle(PrintWriter writer, String[] args) {
				
				writer.println("Thank you for using ctransfer! Goodbye.");
				
			}
			
		});
		
	}
	
	private void setPwdByOS() {
		
		String home = System.getProperty("user.home");
		
		if (OSUtils.isWindows()) {
			pwd = home + "\\server\\";
			
		} else if (OSUtils.isMac() || OSUtils.isUnix()) {
			pwd = home + "/server/";
			
		}
		
		boolean exists = FileUtils.createDirectory(pwd);
		if (!exists) {
			System.out.println("Failed to create PWD: " + pwd);
		}
	
	}
	
}
