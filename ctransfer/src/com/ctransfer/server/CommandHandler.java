package com.ctransfer.server;

import java.io.PrintWriter;

import com.ctransfer.enums.ResponseType;

/**
 * The CommandHandler interface declares the methods for
 * handling commands from the client.
 *
 * @author Ben Sweett & Colin Tinney
 * @version 1.0
 * @since 2014-09-29
 */
public interface CommandHandler {

	public ResponseType getResponseType();
	public void handle(PrintWriter writer, String[] args);
	
}
