package com.ctransfer.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Scanner;

import com.ctransfer.enums.ResponseType;
import com.ctransfer.utils.EnumUtils;
import com.ctransfer.utils.FileUtils;
import com.ctransfer.utils.OSUtils;

// TODO - Allow user to specify IP/Port to connect to (?)
public class SocketClient {

	private String pwd;
	
	private String hostName;
	private Integer port;
	
	private Socket socket = null;
	
	private HashMap<ResponseType, ResponseHandler> responseHandlers;
	
	public SocketClient(String hostName, Integer port) {
		this.hostName = hostName;
		this.port = port;		
		
		socket = new Socket();
		
		responseHandlers = new HashMap<ResponseType, ResponseHandler>();
		addResponseHandlers();
		
		setPwdByOS();
		
	}

	public void start() throws Exception {
		
		Scanner sc = new Scanner(System.in);
		
		// Ask the user for a host name and port
		getHostnameAndPort(sc);
		
		// Connect the socket
		if (!connect()) {
			return;
		}
				
		try (
				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
		) {
			
			String response = null;
			while (true) {

			    String input = getUserInput(sc);
			    if (input == null || input.trim().isEmpty()) {
			    	continue;
			    }
			    
			    writer.println(input);
			    
				response = reader.readLine();
				if (response == null) {
					break;
				}

			    processResponse(response, reader);
				
			}
			
		} catch (SocketException e) {
			System.out.println("SocketException: The connection has most likely been closed.");
			
		} catch (Exception e) {
			e.printStackTrace();
			
		} finally {
			
			if (sc != null) {
				sc.close();
			}
			
		}
		
	}

	public void stop() {
		
		try {
			
			if (socket != null) {
				socket.close();
			}
			
			System.out.println("Socket closed.");
		
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		
	}
	
	private boolean connect() {
		
		if (socket == null) {
			socket = new Socket();
		}
		
		try {
			
			// Connects to hostName:port, with a timeout of 1s
			socket.connect(new InetSocketAddress(hostName, port), 1000);			
			
			System.out.println("Connection established to: " + socket.getRemoteSocketAddress());
			
			return true;
			
		} catch (SocketTimeoutException e) {
			System.out.println("SocketTimeoutException: Connection attempt to [" + (hostName) + ":" + port + "] has timed out.");
			
		} catch (IOException e) {
			System.out.println("IOException: Connection attempt to [" + (hostName) + ":" + port + "] has failed.");
			System.out.println(e.getMessage());
			
		}
		
		return false;		
		
	}
	
	private boolean processResponse(String response, BufferedReader reader) throws Exception {
	    
	    if (response.contains(ResponseType.ERROR.toString())) {
	    	System.out.println(response);
	    	return false;
	    }
	    
	    ResponseType responseType = EnumUtils.lookup(ResponseType.class, response);
	    if (responseType == null) {
	    	System.out.println("Unrecognized ResponseType: " + response);
	    	return false;
	    }
	    
	    ResponseHandler responseHandler = responseHandlers.get(responseType);
	    if (responseHandler == null) {
	    	System.out.println("No response handler found for: " + responseType);
	    	return false;
	    }
	    
	    responseHandler.handleResponse(reader);
	    return true;
		
	}
	
	private void addResponseHandlers() {
		
		if (responseHandlers == null) {
			return;
		}
		
		responseHandlers.put(ResponseType.DELETE_FILE, new ResponseHandler() {

			@Override
			public void handleResponse(BufferedReader reader) throws Exception {
				System.out.println(reader.readLine());				
			}
			
		});
		
		responseHandlers.put(ResponseType.FILE_LIST, new ResponseHandler() {

			@Override
			public void handleResponse(BufferedReader reader) throws Exception {
				
				String pwd = reader.readLine();
				System.out.println("Present Working Directory: " + pwd);
				
				Integer numFiles = Integer.parseInt(reader.readLine());
				System.out.println("# Files/Folders: " + numFiles + "\n");
				
				if (numFiles == -1) {
					System.out.println("End of stream reached");
					return;
				}
				
				for (int i=0; i<numFiles; i++) {
					System.out.println("\t" + reader.readLine());
				}
				
				System.out.println("\nAll contents listed.");
				
			}
			
		});
		
		responseHandlers.put(ResponseType.FILE_TRANSFER, new ResponseHandler() {

			@Override
			public void handleResponse(BufferedReader reader) throws Exception {
				
				String fileName = reader.readLine();
				if (checkForErrors(fileName)) {
					System.out.println(fileName);
					return;
				}
				
				Integer fileSize = Integer.parseInt(reader.readLine());
				if (fileSize < 0) {
					System.out.println("Invalid file size: " + fileSize);
					return;
				}
				
				byte[] data = new byte[fileSize];
				int numRead = socket.getInputStream().read(data, 0, fileSize);
				if (numRead == -1) {
					System.out.println("End of stream reached.");
					return;
				}
				
				// TODO - Handle errors here
				File folder = new File(pwd);
				if (!folder.exists() && !folder.mkdir()) {
					System.out.println("Failed to create directory: " + pwd);
					return;
				}
				
				File file = new File(pwd + fileName);
				file.createNewFile();
				
		        FileOutputStream fileOutputStream = new FileOutputStream(file);
		        fileOutputStream.write(data);
		        fileOutputStream.close();		
		        
		        System.out.println("File successfully transferred.");
			}
			
		});
		
	}
	
	private boolean checkForErrors(String response) {
		return response.contains(ResponseType.ERROR.toString());
	}
	
	private String getUserInput(Scanner sc) {
		
		if (sc == null) {
			return null;
		}

	    System.out.print("\nctransfer > ");
	    return sc.nextLine();
		
	}
	
	private void getHostnameAndPort(Scanner sc) {

		System.out.print("\nPlease enter a hostname: ");
		String hostName = sc.nextLine();
		
		System.out.print("Please enter a port: ");
		String input = sc.nextLine();
		if (!input.isEmpty()) {
			this.port = Integer.parseInt(input);
		}
		
		if (hostName != null && !hostName.trim().isEmpty()) {
			this.hostName = hostName;
		}
		
	}
	
	private void setPwdByOS() {
		
		String home = System.getProperty("user.home");
		
		if (OSUtils.isWindows()) {
			pwd = home + "\\client\\";
			
		} else if (OSUtils.isMac() || OSUtils.isUnix()) {
			pwd = home + "/client/";
			
		}
		
		boolean exists = FileUtils.createDirectory(pwd);
		if (!exists) {
			System.out.println("Failed to create PWD: " + pwd);
		}
	
	}

}
