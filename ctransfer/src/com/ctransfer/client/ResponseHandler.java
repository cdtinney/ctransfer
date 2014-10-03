package com.ctransfer.client;

import java.io.BufferedReader;

/**
 * The ResponseHandler interface  declares the methods for
 * handling responses from the server.
 *
 * @author Ben Sweett & Colin Tinney
 * @version 1.0
 * @since 2014-09-29
 */
public interface ResponseHandler {

	public void handleResponse(BufferedReader reader) throws Exception;
	
}
