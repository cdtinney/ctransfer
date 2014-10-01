package com.ctransfer.application.client;

import java.io.BufferedReader;

public interface ResponseHandler {

	public void handleResponse(BufferedReader reader) throws Exception;
	
}
