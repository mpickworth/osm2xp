package com.osm2xp.gui.handlers;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.preferences.InstanceScope;

import com.osm2xp.constants.Perspectives;
import com.osm2xp.controllers.BuildController;
import com.osm2xp.exceptions.Osm2xpBusinessException;
import com.osm2xp.gui.Activator;
import com.osm2xp.gui.dialogs.utils.Osm2xpDialogsHelper;
import com.osm2xp.model.facades.FacadeSetManager;
import com.osm2xp.utils.helpers.FsxOptionsHelper;
import com.osm2xp.utils.helpers.GuiOptionsHelper;
import com.osm2xp.utils.logging.Osm2xpLogger;

/**
 * CommandBuildScene.
 * 
 * @author Benjamin Blanchet
 * 
 */
public class CommandBuildScene extends AbstractHandler{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		if (getConfigurationErrors() == null) {
			BuildController bc = new BuildController();
			try {
				bc.launchBuild();
			} catch (Osm2xpBusinessException e) {
				Osm2xpLogger.error("Error building scene.", e);
			}
		} else {
			Osm2xpDialogsHelper
					.displayErrorDialog("Bad configuration", "Please check following errors:\n"
							+ getConfigurationErrors());
		}
		return null;
	}

	private String getConfigurationErrors() {

		StringBuilder errors = new StringBuilder();
		// Common validation
		if (GuiOptionsHelper.getOptions().getCurrentFilePath() == null) {
			errors.append("-No osm file selected.\n");
		}
		// Xplane validation
		if (GuiOptionsHelper.getOptions().getOutputFormat()
				.equals(Perspectives.PERSPECTIVE_XPLANE10)
				|| GuiOptionsHelper.getOptions().getOutputFormat()
						.equals(Perspectives.PERSPECTIVE_XPLANE9)) {
			String[] setPaths = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).get(FacadeSetManager.FACADE_SETS_PROP,"").split(File.pathSeparator);
			if (setPaths == null || setPaths.length == 0) {
				errors.append(" - Zero facade sets selected.\n");
			} else {
				boolean oneExists = Arrays.asList(setPaths).stream().anyMatch(str -> !str.trim().isEmpty() && new File(str).exists());
				if (!oneExists) {
					errors.append(" - All speciefied facade sets are invalid.\n");
				}
			}
		}
		// FSX validation
		if (GuiOptionsHelper.getOptions().getOutputFormat()
				.equals(Perspectives.PERSPECTIVE_FSX)) {
			if (StringUtils.isBlank(FsxOptionsHelper.getOptions()
					.getBglCompPath())) {
				errors.append("-bglComp.exe location not set!\n");
			}
		}
		if (errors.length() > 0) {
			return errors.toString();
		}
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
