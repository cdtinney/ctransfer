package com.ctransfer.application;

public interface Command {
	
	public String getCommandString();
	public String getResponse();
	
	public void run();

}
