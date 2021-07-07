package com.osm2xp.gui.dialogs.utils;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * Osm2xpDialogsHelper.
 * 
 * @author Benjamin Blanchet
 * 
 */
public class Osm2xpDialogsHelper {

	private static Shell lShell = PlatformUI.getWorkbench()
			.getActiveWorkbenchWindow().getShell();

	public static void displayErrorDialog(String message) {
		displayErrorDialog("Error", message);
	}
	
	public static boolean displayQuestionDialog(String title, String message) {
		return MessageDialog.openQuestion(lShell, title, message);
	}
	
	public static void displayErrorDialog(String title, String message) {
		MessageDialog.openError(lShell, title, message);
	}

	public static void displayInfoDialog(String message) {
		MessageDialog.openInformation(lShell, "Info", message);
	}

	public static void displayWarningDialog(String message) {
		MessageDialog.openWarning(lShell, "Warning", message);
	}

	public static void displayExitDialog(String message) {
		displayWarningDialog(message);
		final IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.close();
	}

}
