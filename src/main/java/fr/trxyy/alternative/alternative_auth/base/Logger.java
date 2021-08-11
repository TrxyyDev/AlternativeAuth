package fr.trxyy.alternative.alternative_auth.base;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @author Trxyy
 */
public class Logger {

	/**
	 * Log a text
	 * @param s The text to log
	 */
	public static void log(String s) {
		System.out.println(getTime() + s);
	}

	/**
	 * Log a text with error
	 * @param s The text to log
	 */
	public static void err(String s) {
		System.err.println(getTime() + s);
	}

	/**
	 * @return The current time
	 */
	private static String getTime() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		return "[" + sdf.format(cal.getTime()) + "]";
	}

}
