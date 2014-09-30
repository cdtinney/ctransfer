package com.ctransfer.application;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerImpl implements Server {

	@Override
	public void start() {
		
		System.out.println("Waiting for a connection request...");
		
		ServerSocket listener = null;
		Socket socket = null;
		try {
			
			listener = new ServerSocket(9000);
			
			// Wait for a connection (note: this is a blocking call).
			socket = listener.accept();
			
			System.out.println("..connected to: " + socket.getRemoteSocketAddress());
						
			
		} catch (IOException e) {
			e.printStackTrace();
			
		} finally {
			
			
		}
		
	}

	@Override
	public void send(String s) {
		// TODO Auto-generated method stub
		
	}

}
