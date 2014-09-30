package com.ctransfer.application;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientImpl implements Client {
	
	// TODO - Constructor with host name, port
	// TODO - Separate console printing/input with SocketClient wrapper
	
	private Socket socket = null;

	@Override
	public void start() {

		try {
			
			Socket client = new Socket ("localhost", 9000);
			
			// TODO - print remote IP/port
			System.out.println("Connection established.");

			// TODO - Clean up resources
			BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
			PrintWriter out = new PrintWriter(client.getOutputStream(), true);

			Scanner sc = new Scanner(System.in);
			String fromServer = "";
			while ((fromServer = reader.readLine()) != null) {
				
			    System.out.println("Server response: " + fromServer);
			    
			    // TODO - Break loop when appropriate
			    
			    // TODO - Get input more .. elegantly
			    System.out.print(">");
			    String fromUser = sc.nextLine();
			    
			    if (fromUser != null) {
			        System.out.println("Client send: " + fromUser);
			        out.println(fromUser);
			    }
			    
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			
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
