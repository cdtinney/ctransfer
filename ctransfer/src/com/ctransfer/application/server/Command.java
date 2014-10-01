package com.ctransfer.application.server;

import java.io.PrintWriter;

import com.ctransfer.application.ResponseType;

public interface Command {

	public ResponseType getResponseType();
	public void run(PrintWriter writer, String[] args);
	

}
