package com.ctransfer.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Scanner;

<<<<<<< HEAD:ctransfer/src/com/ctransfer/application/client/ClientImpl.java
import com.ctransfer.application.ResponseType;
import com.ctransfer.utils.OSValidator;
=======
import com.ctransfer.enums.ResponseType;
import com.ctransfer.utils.EnumUtils;
>>>>>>> 3e906c0fbe66fa3ed111e1e7def5a71afa176ddf:ctransfer/src/com/ctransfer/client/ClientImpl.java

// TODO - Allow user to specify IP/Port to connect to (?)
public class ClientImpl implements Client {

	// TODO - Change depending on system
	private String pwd = System.getProperty("user.dir");
	
	private final String hostName;
	private final Integer port;
	
	private Socket socket = null;
	
	private HashMap<ResponseType, ResponseHandler> responseHandlers;
	
	public ClientImpl(String hostName, Integer port) {
		
		setPwdByOS();
		
		this.hostName = hostName;
		this.port = port;		
		
		responseHandlers = new HashMap<ResponseType, ResponseHandler>();
		addResponseHandlers();
		
	}

	@Override
	public void start() throws Exception {
		
		BufferedReader reader = null;
		PrintWriter writer = null;
		
		Scanner sc = null;

		try {
			
			// Connects to hostName:port
			socket = new Socket(hostName, port);
			
			System.out.println("Connection established to: " + socket.getRemoteSocketAddress());

			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new PrintWriter(socket.getOutputStream(), true);
			
			sc = new Scanner(System.in);
			
			String response = null;
			while (true) {

			    String input = getUserInput(sc);
			    if (input != null) {
			        writer.println(input);
			    }
				
				response = reader.readLine();
				if (response == null) {
					break;
				}

			    System.out.println("\nReceived response: " + response + "\n");
			    
			    if (response.contains(ResponseType.ERROR.toString())) {
			    	System.out.println(response);
			    	continue;
			    }
			    
			    ResponseType responseType = EnumUtils.lookup(ResponseType.class, response);
			    if (responseType == null) {
			    	System.out.println("Unrecognized ResponseType: " + response);
			    	continue;
			    }
			    
			    ResponseHandler responseHandler = responseHandlers.get(responseType);
			    if (responseHandler == null) {
			    	System.out.println("No response handler found for: " + responseType);
			    	continue;
			    }
			    
			    responseHandler.handleResponse(reader);
				
			}
			
		} catch (SocketException e) {
			System.out.println("SocketException: the connection has most likely been closed.");
			
		} catch (Exception e) {
			e.printStackTrace();
			
		} finally {
			
			System.out.println("Cleaning up resources.");
			
			if (reader != null) {
				reader.close();
			}
			
			if (writer != null) {
				writer.close();
			}
			
			if (sc != null) {
				sc.close();
			}
			
		}
		
	}

	@Override
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
	
	public void setPwdByOS() {
		if(OSValidator.isWindows())
			pwd += "\\client\\";
		else if(OSValidator.isMac() || OSValidator.isUnix())
			pwd += "/client/";
	}

}
