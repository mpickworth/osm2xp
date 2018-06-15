package com.osm2xp.gui.views.panels.generic;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.osm2xp.gui.views.panels.Osm2xpPanel;
import com.osm2xp.utils.helpers.GuiOptionsHelper;

/**
 * GeneralOptionsPanel.
 * 
 * @author Benjamin Blanchet
 * 
 */
public class GeneralOptionsPanel extends Osm2xpPanel {
	private Button btnDataBaseMode;
	private Button btnApdTime;
	private Button btnApdCoords;
	private Button btnSimplifyShapes;
	private Button btnSinglePass;

	public GeneralOptionsPanel(final Composite parent, final int style) {
		super(parent, style);
	}

	@Override
	protected void initLayout() {
		final GridLayout gridLayout = new GridLayout(3, true);
		gridLayout.verticalSpacing = 15;
		setLayout(gridLayout);
		btnDataBaseMode.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
				false, 1, 1));
		btnSimplifyShapes.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
				false, 1, 1));
		btnApdTime.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false,
				1, 1));
		btnApdCoords.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
				false, 1, 1));
		btnSinglePass.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
				false, 1, 1));

	}

	@Override
	protected void initComponents() {
		btnDataBaseMode = new Button(this, SWT.CHECK);
		btnDataBaseMode.setText("Database mode");
		btnSimplifyShapes = new Button(this, SWT.CHECK);
		btnSimplifyShapes.setText("Simplify shapes");
		btnApdTime = new Button(this, SWT.CHECK);
		btnApdTime.setText("Add time to scene name");
		btnApdCoords = new Button(this, SWT.CHECK);
		btnApdCoords.setText("Add coordinates to scene name");
		btnSinglePass = new Button(this, SWT.CHECK);
		btnSinglePass.setText("Single pass mode");

	}

	@Override
	protected void bindComponents() {
		bindComponent(btnDataBaseMode, GuiOptionsHelper.getOptions(),
				"databaseMode");
		bindComponent(btnApdTime, GuiOptionsHelper.getOptions(), "appendHour");
		bindComponent(btnApdCoords, GuiOptionsHelper.getOptions(), "appendTile");
		bindComponent(btnSimplifyShapes, GuiOptionsHelper.getOptions(),
				"simplifyShapes");
		bindComponent(btnSinglePass, GuiOptionsHelper.getOptions(),
				"singlePass");

	}

	@Override
	protected void addComponentsListeners() {

	}

}
