package com.osm2xp.gui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.osgi.service.prefs.BackingStoreException;

import com.osm2xp.gui.Activator;
import com.osm2xp.gui.dialogs.FacadeSetEditorDialog;
import com.osm2xp.model.facades.FacadeSet;
import com.osm2xp.utils.helpers.FacadeSetHelper;

/**
 * CommandFacadeSetEditor.
 * 
 * @author Benjamin Blanchet
 * 
 */
public class CommandFacadeSetEditor extends AbstractHandler {

	private static final String FACADE_EDITOR_PATH_PROP = "facadeEditorPath";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		DirectoryDialog dialog = new DirectoryDialog(Display.getCurrent()
				.getActiveShell());
		dialog.setText("Choose facade set folder");
		dialog.setMessage("Choose facade set folder to edit descriptor for");
		IEclipsePreferences node = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		dialog.setFilterPath(node.get(FACADE_EDITOR_PATH_PROP, null));
		String folderPath = dialog.open();

		if (folderPath != null) {
			try {
				FacadeSet facadeSet = FacadeSetHelper.getFacadeSet(folderPath);
				if ((facadeSet.getFacades() == null || facadeSet.getFacades().isEmpty()) &&
						!MessageDialog.openQuestion(Display.getCurrent().getActiveShell(),"Facade set missing or empty", "Facade set in " + folderPath + " is missing or emtpy. Continue?")) {
					return null;
				}
				node.put(FACADE_EDITOR_PATH_PROP, folderPath);
				try {
					node.flush();
				} catch (BackingStoreException e) {
					Activator.log(e);
				}
				FacadeSetEditorDialog facadeSetEditorDialog = new FacadeSetEditorDialog(
						Display.getCurrent().getActiveShell(), folderPath, facadeSet);
				facadeSetEditorDialog.open();
			} catch (Exception e) {
				MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error reading facade set", "Error reading facade set from " + folderPath + " :" + e.getMessage());
				Activator.log(e);
			}

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
