package com.osm2xp.startup;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.progress.UIJob;

import com.osm2xp.constants.Perspectives;
import com.osm2xp.gui.Activator;
import com.osm2xp.gui.views.MainSceneryFileView;

public class OSM2XPStartup implements IStartup {

	@Override
	public void earlyStartup() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
        new UIJob("Switching perspectives"){
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                try {
                    workbench.showPerspective(Perspectives.PERSPECTIVE_XPLANE10, workbench.getActiveWorkbenchWindow());
                    workbench.getActiveWorkbenchWindow().getActivePage().showView(MainSceneryFileView.ID);
                } catch (WorkbenchException e) {
                    return new Status(IStatus.ERROR,Activator.PLUGIN_ID,"Error while switching perspectives", e);
                }
                return Status.OK_STATUS;
            }}
        .run(new NullProgressMonitor());

	}

}
