package com.ctransfer.utils;

/**
 * OSUtils contains a collection of functions for determining 
 * what OS the application is running on.
 *
 * @author Ben Sweett
 * @version 1.0
 * @since 2014-09-30
 */
public class OSUtils {
	
	private static String OS = System.getProperty("os.name").toLowerCase();
	
	/**
	 * Returns true if application is running on Windows
	 * 
	 * @return boolean
	 */
	public static boolean isWindows() {		 
		return (OS.indexOf("win") >= 0);
	}
 
	/**
	 * Returns true if application is running on Mac OS X
	 * 
	 * @return boolean
	 */
	public static boolean isMac() {
		return (OS.indexOf("mac") >= 0);
	}
 
	/**
	 * Returns true if application is running on Unix
	 * 
	 * @return boolean
	 */
	public static boolean isUnix() {
		return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );
	}
	
}
