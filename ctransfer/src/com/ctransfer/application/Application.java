package com.ctransfer.application;

import java.util.InputMismatchException;
import java.util.Scanner;

import com.ctransfer.client.SocketClient;
import com.ctransfer.server.SocketServer;

/**
 * The ctransfer program implements an application that allows users
 * to transfer files between a server and a client.
 *
 * @author Ben Sweett & Colin Tinney
 * @version 1.0
 * @since 2014-09-29
 */
public class Application {
	
	private static final Integer DEFAULT_PORT = 9000;
	
	/**
	 * Print function for simply printing a string to the standard 
	 * output
	 * 
	 * @param String to be printed
	 */
	public static void print(String s) {
		System.out.println(s);
	}
	
	/**
	 * Init function allows for user to select which program to
	 * run. Waits for user to input a 1 for server and a 2 for
	 * client. Opens a Server and client socket on port 9000.
	 * 
	 */
	public static void init() {
		
		print("\nNetwork file service launched.");
		print("--------------------------------");
		print("Please make a selection.");
		print("\t1. Server");
		print("\t2. Client");
		print("--------------------------------");
		
		// try-with automatically closes resources
		try (Scanner sc = new Scanner(System.in)) {
		
			Integer selection = sc.nextInt();
			if (selection == 1) {
				SocketServer server = new SocketServer(DEFAULT_PORT);	
				server.start();
				
			} else if (selection == 2) {
				SocketClient client = new SocketClient("localhost", DEFAULT_PORT);
				client.start();
				
			} else {
				System.err.println("Invalid selection. Exiting.");
				
			}
			
		} catch (InputMismatchException e) {
			System.err.println("Invalid selection. Exiting.");
			
		} catch (Exception e) {
			e.printStackTrace();
		
		} 
		
	}

	/**
	 * Main execution point for the application
	 * 
	 * @param String[] arguments
	 */
	public static void main(String[] args) {
		
		init();
		
	}

}
