package com.ctransfer.application;

public interface Command {
	
	public String getCommandString();
	public String getResponse();
	
	public byte[] getResponseBytes();
	
	public ResponseType getResponseType();
	
	public void run();

}
