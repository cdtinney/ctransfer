package com.ctransfer.utils;

public class EnumUtils {
	
	public static <E extends Enum<E>> E lookup(Class<E> e, String id) {   

		try {          
			return Enum.valueOf(e, id);
		} catch (IllegalArgumentException ex) {
			// Do nothing
		}
  
		return null;
	      
	}

}
