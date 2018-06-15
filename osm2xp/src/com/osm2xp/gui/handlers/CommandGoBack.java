package com.osm2xp.gui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

import com.osm2xp.constants.Osm2xpConstants;
import com.osm2xp.constants.Perspectives;
import com.osm2xp.gui.Activator;
import com.osm2xp.utils.MiscUtils;

public class CommandGoBack extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			IEclipsePreferences node = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
			String id = node.get(Osm2xpConstants.LAST_PERSP_PROP, Perspectives.PERSPECTIVE_XPLANE10);
			MiscUtils.switchPerspective(id);
		} catch (Exception e) {
			Activator.log(e);
		}
		return null;
	}

}
