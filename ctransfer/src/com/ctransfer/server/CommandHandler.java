package com.ctransfer.server;

import java.io.PrintWriter;

import com.ctransfer.enums.ResponseType;

public interface CommandHandler {

	public ResponseType getResponseType();
	public void handle(PrintWriter writer, String[] args);
	

}
