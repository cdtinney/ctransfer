package com.ctransfer.application;

import java.io.PrintWriter;

public interface Command {

	public ResponseType getResponseType();
	public void run(PrintWriter writer, String[] args);
	

}
