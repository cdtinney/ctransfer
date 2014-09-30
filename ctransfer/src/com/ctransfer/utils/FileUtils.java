package com.ctransfer.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class FileUtils {
	
	private static Logger LOGGER = Logger.getLogger(FileUtils.class.getName());

	/*
	 * Returns a list of all the files within a directory with valid extensions.
	 * If no extensions are specified, all files are returned.
	 */
    public static List<File> listFiles(String directoryName, String[] extensions) {
    	
    	List<File> results = new ArrayList<File>();    	
    	
        File folder = new File(directoryName);
        
        File[] fList = folder.listFiles();
        if (fList == null) {
        	LOGGER.warning("Cannot list files of a directory which does not exit - " + directoryName);
        	return results;
        }
        
        for (File file : fList) {
        	
        	if (file.isDirectory()) {
        		
        		results.add(file);
        		
        		// Recursively add files within sub-directories
        		//results.addAll(listFiles(file.getAbsolutePath(), extensions));
        		
        	} else {
        		
        		if (extensions == null) {
        			results.add(file);
        			continue;
        		}
        		
        		if (validExtension(file, extensions)) {
        			results.add(file);
        		}
        	}
        	
        }
        
        return results;
        
    }
    
    private static boolean validExtension(File file, String[] extensions) {
    	
    	boolean valid = false;
    	
    	for (String extension : extensions) {
			
			if (file.getName().endsWith(extension) || file.getName().endsWith("." + extension)) {
				valid = true;
				break;
			}
			
		}
    	
    	return valid;
    	
    }

}
