package com.osm2xp.gui.handlers.modes;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.ResourcesPlugin;

import com.osm2xp.constants.Perspectives;
import com.osm2xp.gui.views.panels.generic.OutPutFormatPanel;
import com.osm2xp.utils.MiscUtils;
import com.osm2xp.utils.helpers.GuiOptionsHelper;

/**
 * CommandXplane10Mode.
 * 
 * @author Benjamin Blanchet
 * 
 */
public class CommandXplane10Mode extends AbstractHandler {

	private static final String HTML_FILE = ResourcesPlugin.getWorkspace()
			.getRoot().getLocation()
			+ File.separator
			+ "resources"
			+ File.separator
			+ "html"
			+ File.separator
			+ "modes"
			+ File.separator
			+ "xplane10"
			+ File.separator + "index.html";

	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		GuiOptionsHelper.getOptions().setOutputFormat(
				Perspectives.PERSPECTIVE_XPLANE10);
		MiscUtils.switchPerspective(Perspectives.PERSPECTIVE_XPLANE10);

		OutPutFormatPanel.updateBrowserUrl(HTML_FILE);
		return null;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean isHandled() {
		return true;
	}
	
}
