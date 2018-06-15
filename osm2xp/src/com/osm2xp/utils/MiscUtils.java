package com.osm2xp.utils;

import java.awt.Color;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.osm2xp.constants.Osm2xpConstants;
import com.osm2xp.gui.Activator;
import com.osm2xp.utils.logging.Osm2xpLogger;

/**
 * MiscUtils.
 * 
 * @author Benjamin Blanchet
 * 
 */
public class MiscUtils {

	/**
	 * switch to another perspective.
	 * 
	 * @param perspectiveID
	 *            rcp perspective id.
	 */
	public static void switchPerspective(String perspectiveID) {
		if (perspectiveID != null) {
			// switch perspective
			IWorkbench workbench = PlatformUI.getWorkbench();
			if (workbench != null) {
				try {
					IWorkbenchWindow win = workbench.getActiveWorkbenchWindow();
					if (win != null) {
						IWorkbenchPage page = win.getActivePage();
						if (page != null) {
							IPerspectiveDescriptor perspective = page.getPerspective();
							String lastId = perspective.getId();
							IEclipsePreferences node = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
							node.put(Osm2xpConstants.LAST_PERSP_PROP, lastId);
							node.flush();
						}
						
					}
					
					workbench.showPerspective(perspectiveID,
							win);
				} catch (Exception e) {
					Osm2xpLogger.warning("Error switching perspective", e);
				}
			}
		}
	}

	/**
	 * extract numeric values from the given string
	 * 
	 * @param str
	 * @return String
	 */
	private static String getOnlyNumerics(String str) {

		if (str == null) {
			return null;
		}

		StringBuffer strBuff = new StringBuffer();
		char c;

		for (int i = 0; i < str.length(); i++) {
			c = str.charAt(i);

			if (Character.isDigit(c) || Character.toString(c).equals(".")) {
				strBuff.append(c);
			}
		}
		return strBuff.toString();
	}

	/**
	 * extract numeric values from the given string
	 * 
	 * @param value
	 * @return Integer
	 */
	public static Integer extractNumbers(String value) {
		value = value.replaceAll(",", ".");
		value = getOnlyNumerics(value);
		try {
			if (value != null && !value.equals("")) {
				return (int) Double.parseDouble(value);
			}
		} catch (Exception e) {
			Osm2xpLogger.warning(
					"Error extracting height from osm tag, input :" + value, e);
		}

		return null;
	}

	/**
	 * return a random integer beetween given min/max values
	 * 
	 * @param min
	 * @param max
	 * @return Integer
	 */
	public static Integer getRandomSize(Integer min, Integer max) {
		if (min.equals(max)) {
			return max;
		} else {
			Random randomGenerator = new Random();
			return randomGenerator.nextInt(max - min) + min;

		}

	}

	/**
	 * compute time difference beetween two dates
	 * 
	 * @param dateOne
	 * @param dateTwo
	 * @return String the time difference beetween two dates
	 */
	public static String getTimeDiff(Date dateOne, Date dateTwo) {
		long timeDiff = Math.abs(dateOne.getTime() - dateTwo.getTime());
		long hours = TimeUnit.MILLISECONDS.toHours(timeDiff);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(timeDiff)
				- TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS
						.toHours(timeDiff));
		long seconds = TimeUnit.MILLISECONDS.toSeconds(timeDiff)
				- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
						.toMinutes(timeDiff));
		return hours + "h" + minutes + "m" + seconds + "s";
	}

	/**
	 * check if we are on a windows platform
	 * 
	 * @return true if we are on a windows platform
	 */
	public static boolean isWindows() {
		return System.getProperties().get("os.name").toString().toLowerCase()
				.contains("windows");
	}

	/**
	 * check if we are on a mac platform
	 * 
	 * @return true if we are on a windows platform
	 */
	public static boolean isMac() {
		return System.getProperties().get("os.name").toString().toLowerCase()
				.contains("mac");
	}

	/**
	 * execute an external programm
	 * 
	 * @param command
	 */
	public static void execProgramm(String command) {
		try {
			Runtime rt = Runtime.getRuntime();

			Process proc = rt.exec(command);
			// any error message?
			StreamGobbler errorGobbler = new StreamGobbler(
					proc.getErrorStream(), "ERROR");

			// any output?
			StreamGobbler outputGobbler = new StreamGobbler(
					proc.getInputStream(), "OUTPUT");

			// kick them off
			errorGobbler.start();
			outputGobbler.start();

		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	/**
	 * Return the "distance" between two colors. The rgb entries are taken to be
	 * coordinates in a 3D space [0.0-1.0], and this method returnes the
	 * distance between the coordinates for the first and second color.
	 * 
	 * @param r1
	 *            , g1, b1 First color.
	 * @param r2
	 *            , g2, b2 Second color.
	 * @return Distance bwetween colors.
	 */
	public static double colorDistance(Color color1, Color color2) {
		double a = color2.getRed() - color1.getRed();
		double b = color2.getGreen() - color1.getGreen();
		double c = color2.getBlue() - color1.getBlue();

		return Math.sqrt(a * a + b * b + c * c);
	}
}
