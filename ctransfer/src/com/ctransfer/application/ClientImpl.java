package com.ctransfer.application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

// TODO - Allow user to specify IP/Port to connect to (?)
public class ClientImpl implements Client {
	
	private final String hostName;
	private final Integer port;
	
	private Socket socket = null;
	
	private String pwd = "C:\\client\\";
	
	public ClientImpl(String hostName, Integer port) {
		this.hostName = hostName;
		this.port = port;		
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
				
				if (response.equals(ResponseType.FILE_LIST.toString())) {
					processFileList(reader);
				} 
				
				if (response.equals(ResponseType.DELETE_FILE.toString())) {
					System.out.println(reader.readLine());
				}
				
				if (response.equals(ResponseType.FILE_TRANSFER.toString())) {
					processFileTransfer(reader);
				}
				
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
	
	private void processFileList(BufferedReader reader) throws Exception {
		
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
	
	private void processFileTransfer(BufferedReader reader) throws Exception {
		
		// TODO - Use a constant str
		String fileName = reader.readLine();
		if (fileName.equals("File does not exist!")) {
			System.out.println("File not found!");
			return;
		}
		
		Integer fileSize = Integer.parseInt(reader.readLine());
		if (fileSize <= 0) {
			System.out.println("File size <= 0.");
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
	
	private String getUserInput(Scanner sc) {
		
		if (sc == null) {
			return null;
		}

	    System.out.print("\nctransfer > ");
	    return sc.nextLine();
		
	}

}
