package com.ctransfer.utils;

import java.util.ArrayList;
import java.util.List;

public class ArrayUtils {
	
	public static String[] removeEmptyStrings(String[] arr) {
		
		List<String> result = new ArrayList<>();
		for (int i=0; i<arr.length; i++) {
			if (!arr[i].isEmpty()) result.add(arr[i]);
		}
		
		return result.toArray(new String[result.size()]);
		
	}

}
