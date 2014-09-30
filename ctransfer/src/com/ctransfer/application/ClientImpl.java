package com.ctransfer.application;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

public class ClientImpl implements Client {
	
	// TODO - Constructor with host name, port
	// TODO - Separate console printing/input with SocketClient wrapper
	
	private Socket socket = null;

	@Override
	public void start() throws Exception {
		
		BufferedReader reader = null;
		PrintWriter writer = null;
		
		Scanner sc = new Scanner(System.in);

		try {
			
			socket = new Socket("localhost", 9000);
			
			// TODO - print remote IP/port
			System.out.println("Connection established.");

			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new PrintWriter(socket.getOutputStream(), true);
			
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
				System.out.println("Closing the socket...");
				socket.close();
				System.out.println("..successfully closed.");
			}
		
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		
	}

	@Override
	public void send(String s) {
		// TODO Auto-generated method stub
		
	}

}
