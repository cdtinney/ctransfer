package com.ctransfer.application;

import java.util.Scanner;

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
		
		Scanner sc = null;
		
		try {
			
			sc = new Scanner(System.in);
		
			Integer selection = sc.nextInt();
			if (selection == 1) {
				Server server = new ServerImpl();
				server.start();
				
			} else if (selection == 2) {
				Client client = new ClientImpl();
				client.start();
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		
		} finally {
			
			if (sc != null) {
				sc.close();
			}
			
		}
		
	}

	public static void main(String[] args) {
	
		init();
		
	}

}
