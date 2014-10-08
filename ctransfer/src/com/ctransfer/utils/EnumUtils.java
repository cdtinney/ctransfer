package com.ctransfer.utils;

/**
 * EnumUtils contains a collection of functions for processing ENUMs
 *
 * @author Colin Tinney
 * @version 1.0
 * @since 2014-09-29
 */
public class EnumUtils {
	
	/**
	 * Checks the value of the ENUM against a string.
	 * 
	 * @param Class<E> e
	 * @param String id
	 * @return E
	 */
	public static <E extends Enum<E>> E lookup(Class<E> e, String id) {   

		try {          
			return Enum.valueOf(e, id);
		} catch (IllegalArgumentException ex) {
			// Do nothing
		}
  
		return null;
	      
	}

}
