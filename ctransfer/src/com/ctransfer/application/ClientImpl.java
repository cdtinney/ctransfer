package com.ctransfer.application;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

public class ClientImpl implements Client {
	
	private final String hostName;
	private final Integer port;
	
	private Socket socket = null;
	
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
			
			// TODO - print remote IP/port
			// TODO - Separate console printing/input with SocketClient wrapper
			System.out.println("Connection established.");

			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new PrintWriter(socket.getOutputStream(), true);
			
			sc = new Scanner(System.in);
			
			String response = null;
			while (true) {
				
				response = reader.readLine();
				if (response == null) {
					break;
				}

			    System.out.println("Server response: " + response);

			    // TODO - Get input more .. elegantly
			    System.out.print(">");
			    String input = sc.nextLine();
			    
			    if (input != null) {
			        System.out.println("Client send: " + input);
			        writer.println(input);
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

}
