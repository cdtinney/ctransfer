package com.ctransfer.application;

import java.util.Scanner;

import com.ctransfer.application.client.Client;
import com.ctransfer.application.client.ClientImpl;
import com.ctransfer.application.server.Server;
import com.ctransfer.application.server.ServerImpl;

public class Application {
	
	public static void print(String s) {
		System.out.println(s);
	}
	
	public static void init() {
		
		print("Network file service launched.");
		print("--------------------------------");
		print("Please make a selection.");
		print("\t1. Server");
		print("\t2. Client");
		print("--------------------------------");
		
		// try-with automatically closes resources
		try (Scanner sc = new Scanner(System.in)) {
		
			Integer selection = sc.nextInt();
			if (selection == 1) {
				Server server = new ServerImpl(9000);
				server.start();
				
			} else if (selection == 2) {
				Client client = new ClientImpl("localhost", 9000);
				client.start();
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		
		} 
		
	}

	public static void main(String[] args) {
	
		init();
		
	}

}
