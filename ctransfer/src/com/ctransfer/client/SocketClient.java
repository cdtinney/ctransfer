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

/**
 * The SocketClient contains the main implementation of the client 
 * for the ctransfer application. Handles all aspects of the client
 * socket and communication with the server. Also contains the map of 
 * all accepted responses and handlers.  
 *
 * @author Ben Sweett & Colin Tinney
 * @version 1.0
 * @since 2014-09-29
 */
public class SocketClient {

	private String pwd;
	
	private String hostName;
	private Integer port;
	
	private Socket socket = null;
	
	private HashMap<ResponseType, ResponseHandler> responseHandlers;
	
	/**
	 * Constructor for the SocketClient. Opens a socket, collects
	 * all of the supported response types and their handlers, and 
	 * sets the present working directory. 
	 * 
	 * @param String host name
	 * @param Integer port
	 */
	public SocketClient(String hostName, Integer port) {
		this.hostName = hostName;
		this.port = port;		
		
		socket = new Socket();
		
		responseHandlers = new HashMap<ResponseType, ResponseHandler>();
		addResponseHandlers();
		
		setPwdByOS();	
	}

	/**
	 * After getting the host name and port name from the user
	 * the start function will attempt to connect to the server.
	 * While connected it will listen for user input and call 
	 * the function for processing commands. 
	 * 
	 * @throws Exception
	 */
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
			System.err.println("SocketException: The connection has most likely been closed.");
			System.exit(-1);
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
			
		} finally {
			
			if (sc != null) {
				sc.close();
			}
			
		}
	}

	/**
	 * The stop function handles the clean up of networking 
	 * resources on the client. It attempts to close the 
	 * socket safely.
	 * 
	 */
	public void stop() {
		
		try {
			
			if (socket != null) {
				socket.close();
			}
			
			System.out.println("Socket closed.");
		
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
	
		}
	}
	
	/**
	 * The connect function will handle the initial connection
	 * to the server. It creates a new socket and tries to 
	 * connect to the host name and address name given by the 
	 * user. 
	 * 
	 * @return boolean success or failure
	 */
	private boolean connect() {
		
		if (socket == null) {
			socket = new Socket();
		}
		
		try {
			
			// Connects to hostName:port, with a timeout of 5s
			socket.connect(new InetSocketAddress(hostName, port), 5000);			
			
			System.out.println("Connection established to: " + socket.getRemoteSocketAddress());
			
			return true;
			
		} catch (SocketTimeoutException e) {
			System.err.println("SocketTimeoutException: Connection attempt to [" + (hostName) + ":" + port + "] has timed out.");
			
		} catch (IOException e) {
			System.err.println("IOException: Connection attempt to [" + (hostName) + ":" + port + "] has failed.");
			System.err.println(e.getMessage());
			
		}
		
		return false;		
	}
	
	/**
	 * The Process Response function checks to make sure the 
	 * response from the server was not an error and the handler
	 * and response type exist. If everything is OK it calls the
	 * handler for the response type. 
	 * 
	 * @param String response from server
	 * @param BufferedReader input reader
	 * @return boolean success or failure
	 */
	private boolean processResponse(String response, BufferedReader reader) throws Exception {
	    
	    if (response.contains(ResponseType.ERROR.toString())) {
	    	System.out.println(response);
	    	return false;
	    }
	    
	    ResponseType responseType = EnumUtils.lookup(ResponseType.class, response);
	    if (responseType == null) {
	    	System.err.println("Unrecognized ResponseType: " + response);
	    	return false;
	    }
	    
	    ResponseHandler responseHandler = responseHandlers.get(responseType);
	    if (responseHandler == null) {
	    	System.err.println("No response handler found for: " + responseType);
	    	return false;
	    }
	    
	    responseHandler.handleResponse(reader);
	    return true;
	}
	
	/**
	 * Adds the accepted responses from the server to the map along
	 * side their handler. Handlers from each response are also 
	 * implemented here. Delete File prints out the server response 
	 * from the request. File List prints the pwd, number of files/
	 * folders, and all of their contents. File Transfer moves the
	 * file named from the server to a byte array and then attempts
	 * to write the data to a file. Exit terminates the program. 
	 * 
	 */
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
					System.err.println("Invalid file size: " + fileSize);
					return;
				}
				
				byte[] data = new byte[fileSize];
				int numRead = socket.getInputStream().read(data, 0, fileSize);
				if (numRead == -1) {
					System.out.println("End of stream reached.");
					return;
				}
				
				File folder = new File(pwd);
				if (!folder.exists() && !folder.mkdir()) {
					System.err.println("Failed to create directory: " + pwd);
					return;
				}
				
				System.out.println("Warning: If the file exists it will be overwritten.");
				
				try {
					
					File file = new File(pwd + fileName);
					file.createNewFile();
					
					FileOutputStream fileOutputStream = new FileOutputStream(file);
			        fileOutputStream.write(data);
			        fileOutputStream.close();
			        
				} catch (SecurityException | IOException e) {
					System.err.println(e.getLocalizedMessage());
					System.err.println("Failed to create file: " + fileName + " at pathname: " + pwd);
					return;
				}
		        		
		        System.out.println("File successfully transferred.");
			}
			
		});
		
		responseHandlers.put(ResponseType.EXIT, new ResponseHandler() {
			
			@Override
			public void handleResponse(BufferedReader reader) throws Exception {
				
				String response = reader.readLine();
				System.out.println(response + "\n");
				System.exit(0);
			}
		});
	}
	
	/**
	 * Checks the response for the ERROR type and if it ends 
	 * with a '.'.
	 * 
	 * @param String response from server
	 * @return boolean true if error otherwise false
	 */
	private boolean checkForErrors(String response) {
		return response.contains(ResponseType.ERROR.toString()) && response.endsWith(".");
	}
	
	/**
	 * Gets the users input from the console.
	 * 
	 * @param Scanner the input scanner
	 * @return String users input
	 */
	private String getUserInput(Scanner sc) {
		
		if (sc == null) {
			return null;
		}

	    System.out.print("\nctransfer > ");
	    return sc.nextLine();
	}
	
	/**
	 * Gets the users input for the hostname and port number
	 * If nothing is entered it uses the default for both.
	 * 
	 * @param Scanner the input scanner
	 */
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
	
	/**
	 * Sets the PWD based on the OS variant. Checks to make
	 * sure the directory exists. 
	 * 
	 */
	private void setPwdByOS() {
		
		String home = System.getProperty("user.home");
		
		if (OSUtils.isWindows()) {
			pwd = home + "\\client\\";
			
		} else if (OSUtils.isMac() || OSUtils.isUnix()) {
			pwd = home + "/client/";
			
		}
		
		boolean exists = FileUtils.createDirectory(pwd);
		if (!exists) {
			System.err.println("Failed to create PWD: " + pwd);
		}
	
	}

}
