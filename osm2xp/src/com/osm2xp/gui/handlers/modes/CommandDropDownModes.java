package com.osm2xp.gui.handlers.modes;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

/**
 * CommandDropDownModes.
 * 
 * @author Benjamin Blanchet
 * 
 */
public class CommandDropDownModes extends AbstractHandler{


	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		new CommandXplane10Mode().execute(event);
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
