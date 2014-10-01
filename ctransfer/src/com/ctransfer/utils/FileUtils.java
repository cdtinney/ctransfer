package com.ctransfer.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class FileUtils {
	
	private static Logger LOGGER = Logger.getLogger(FileUtils.class.getName());
	
    /**
     * Returns a list of files/folders within a directory.
     * 
     * @param directoryName The root directory to list contents of.
     * @param extensions A list of valid extensions. If not null/empty, only files with an extension contained in this list will be added.
     * @param includeFolders If true, folders are included in the list.
     * @return A list of files/folders contained within the directory.
     */
    public static List<File> listFiles(String directoryName, String[] extensions, Boolean includeFolders) {
    	
    	List<File> results = new ArrayList<File>();    	
    	
        File folder = new File(directoryName);
        
        File[] fList = folder.listFiles();
        if (fList == null) {
        	LOGGER.warning("Cannot list files of a directory which does not exit - " + directoryName);
        	return results;
        }
        
        for (File file : fList) {
        	
        	if ((includeFolders && file.isDirectory()) || extensions == null) {
    			results.add(file);
    			continue;
        	}
    		
    		if (validExtension(file, extensions)) {
    			results.add(file);
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
