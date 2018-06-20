package com.osm2xp.gui;

import java.io.PrintStream;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.ActionSetRegistry;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;

import com.osm2xp.exceptions.Osm2xpBusinessException;
import com.osm2xp.utils.helpers.FlightGearOptionsHelper;
import com.osm2xp.utils.helpers.FlyLegacyOptionsHelper;
import com.osm2xp.utils.helpers.FsxOptionsHelper;
import com.osm2xp.utils.helpers.GuiOptionsHelper;
import com.osm2xp.utils.helpers.StatsHelper;
import com.osm2xp.utils.helpers.XplaneOptionsHelper;
import com.osm2xp.utils.logging.Osm2xpLogger;

/**
 * ApplicationWorkbenchWindowAdvisor
 * 
 * @author Benjamin Blanchet
 * 
 */
public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

	public ApplicationWorkbenchWindowAdvisor(
			IWorkbenchWindowConfigurer configurer) {
		super(configurer);
	}

	public ActionBarAdvisor createActionBarAdvisor(
			IActionBarConfigurer configurer) {
		return new ApplicationActionBarAdvisor(configurer);
	}

	public void preWindowOpen() {
		MessageConsole myConsole = new MessageConsole("", null);
		ConsolePlugin.getDefault().getConsoleManager()
				.addConsoles(new IConsole[] { myConsole });
		MessageConsoleStream stream = myConsole.newMessageStream();
		PrintStream myS = new PrintStream(stream);

		System.setOut(myS); // link standard output stream to the console
		System.setErr(myS); // link error output stream to the console

		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		configurer.setInitialSize(new Point(800, 600));
		configurer.setShowStatusLine(false);
		configurer.setShowCoolBar(true);
		configurer.setTitle("Osm2xp");
		configurer.setShowPerspectiveBar(true);
		configurer.setShowProgressIndicator(true);
		PlatformUI.getPreferenceStore().setValue(
				IWorkbenchPreferenceConstants.SHOW_OTHER_IN_PERSPECTIVE_MENU,
				false);
		PlatformUI.getPreferenceStore().setValue(
				IWorkbenchPreferenceConstants.SHOW_OPEN_ON_PERSPECTIVE_BAR,
				false);
		PlatformUI.getPreferenceStore().setValue(
				IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS,
				false);
		configurer.setShowPerspectiveBar(false);
		removeUnwantedToolItems();
	}

	/**
	 * 
	 */
	@SuppressWarnings("restriction")
	private void removeUnwantedToolItems() {
		ActionSetRegistry reg = WorkbenchPlugin.getDefault()
				.getActionSetRegistry();
		IActionSetDescriptor[] actionSets = reg.getActionSets();
		// remove unwanted menu items
		String actionSetId = "org.eclipse.ui.edit.text.actionSet";
		String searchSetId = "org.eclipse.search.searchActionSet";
		for (int i = 0; i < actionSets.length; i++) {
			if (actionSets[i].getId().equalsIgnoreCase(searchSetId)
					|| actionSets[i].getId().startsWith(actionSetId)) {
				IExtension ext = actionSets[i].getConfigurationElement()
						.getDeclaringExtension();
				reg.removeExtension(ext, new Object[] { actionSets[i] });
			}
		}

	}

	@Override
	public void postWindowCreate() {
		super.postWindowCreate();
//		Shell shell = PlatformUI.getWorkbench().getWorkbenchWindows()[0]
//				.getShell();
//		shell.setMaximized(true);
		Osm2xpLogger.displayWelcomeMessage();
		// reset last used file
		GuiOptionsHelper.getOptions().setCurrentFilePath(null);
	}

	@Override
	public void dispose() {

		try {
			GuiOptionsHelper.saveOptions();
			XplaneOptionsHelper.saveOptions();
			FsxOptionsHelper.saveOptions();
			FlyLegacyOptionsHelper.saveOptions();
			FlightGearOptionsHelper.saveOptions();
		} catch (Osm2xpBusinessException e) {
			Osm2xpLogger.error(e.getMessage());
		}
		StatsHelper.getStatsList();
		super.dispose();
	}
}
