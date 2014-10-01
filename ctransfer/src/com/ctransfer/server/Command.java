package com.ctransfer.server;

import java.io.PrintWriter;

import com.ctransfer.enums.ResponseType;

public interface Command {

	public ResponseType getResponseType();
	public void run(PrintWriter writer, String[] args);
	

}
