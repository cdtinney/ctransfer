package com.ctransfer.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * ArrayUtils contains a collection of utility functions for processing
 * arrays.
 *
 * @author Colin Tinney
 * @version 1.0
 * @since 2014-09-28
 */
public class ArrayUtils {
	
	/**
	 * Removes all empty strings from an array
	 * 
	 * @param arr
	 * @return String[] 
	 */
	public static String[] removeEmptyStrings(String[] arr) {
		
		List<String> result = new ArrayList<>();
		for (int i=0; i<arr.length; i++) {
			if (!arr[i].isEmpty()) result.add(arr[i]);
		}
		
		return result.toArray(new String[result.size()]);
		
	}

}
