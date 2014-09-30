package com.ctransfer.application;

public interface Client {

	public void start() throws Exception;
	public void stop();
	
	public void send(String s);
	

}
