package com.osm2xp.gui.views.panels.xplane;

import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.osm2xp.gui.views.panels.Osm2xpPanel;
import com.osm2xp.utils.helpers.XplaneOptionsHelper;

/**
 * GeneratedItemsPanel.
 * 
 * @author Benjamin Blanchet
 * 
 */
public class GeneratedItemsPanel extends Osm2xpPanel {

	private Button btnGenerateBuildings;
	private Button btnGenerateObjects;
	private Button btnGenerateForests;
	private Button btnGenerateStreetLights;
	private Button btnGenerateRoads;
	private Button btnGenerateRail;
	private Button btnGeneratePower;
	private Button btnGenerateFence;
	private Button btnGenerateTanks;
	private Button btnGenerateChimneys;
	private Button btnGenerateBridges;

	public GeneratedItemsPanel(final Composite parent, final int style) {
		super(parent, style);
	}

	@Override
	protected void initComponents() {
		btnGenerateBuildings = new Button(this, SWT.CHECK);
		btnGenerateBuildings.setText("Generate facades buildings");
		GridDataFactory.fillDefaults().applyTo(btnGenerateBuildings);
		btnGenerateObjects = new Button(this, SWT.CHECK);
		btnGenerateObjects.setText("Generate 3D objects");
		GridDataFactory.fillDefaults().applyTo(btnGenerateObjects);
		btnGenerateForests = new Button(this, SWT.CHECK);
		btnGenerateForests.setText("Generate Forests");
		GridDataFactory.fillDefaults().applyTo(btnGenerateForests);
		btnGenerateRoads = new Button(this, SWT.CHECK);
		btnGenerateRoads.setText("Generate Roads");
		GridDataFactory.fillDefaults().applyTo(btnGenerateRoads);
		btnGenerateRail = new Button(this, SWT.CHECK);
		btnGenerateRail.setText("Generate Railways");
		GridDataFactory.fillDefaults().applyTo(btnGenerateRail);
		btnGeneratePower= new Button(this, SWT.CHECK);
		btnGeneratePower.setText("Generate Powerlines");
		GridDataFactory.fillDefaults().applyTo(btnGeneratePower);
		btnGenerateFence= new Button(this, SWT.CHECK);
		btnGenerateFence.setText("Generate Fence");
		GridDataFactory.fillDefaults().applyTo(btnGenerateFence);
		
		btnGenerateTanks= new Button(this, SWT.CHECK);
		btnGenerateTanks.setText("Generate Tanks/Gasometers");
		GridDataFactory.fillDefaults().applyTo(btnGenerateTanks);
		
		btnGenerateChimneys = new Button(this, SWT.CHECK);
		btnGenerateChimneys.setText("Generate Chimneys");
		GridDataFactory.fillDefaults().applyTo(btnGenerateChimneys);
		
		btnGenerateStreetLights = new Button(this, SWT.CHECK);
		btnGenerateStreetLights.setText("Generate street lights");
		GridDataFactory.fillDefaults().applyTo(btnGenerateStreetLights);
		
		btnGenerateBridges = new Button(this, SWT.CHECK);
		btnGenerateBridges.setText("Generate bridges (EXPERIMENTAL)");
		btnGenerateBridges.setToolTipText("Experimental feature! Results can be poor.");
		GridDataFactory.fillDefaults().applyTo(btnGenerateBridges);
	
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void bindComponents() {
		bindingContext.bindValue(WidgetProperties.selection().observe(btnGenerateBuildings),		
				PojoProperties.value("generateBuildings").observe(XplaneOptionsHelper.getOptions()));
		bindingContext.bindValue(WidgetProperties.selection().observe(btnGenerateObjects),		
				PojoProperties.value("generateObj").observe(XplaneOptionsHelper.getOptions()));
		bindingContext.bindValue(WidgetProperties.selection().observe(btnGenerateForests),		
				PojoProperties.value("generateFor").observe(XplaneOptionsHelper.getOptions()));
		bindingContext.bindValue(WidgetProperties.selection().observe(btnGenerateStreetLights),		
				PojoProperties.value("generateStreetLights").observe(XplaneOptionsHelper.getOptions()));
//		bindingContext.bindValue(SWTObservables
//				.observeSelection(btnGenerateBuildings), PojoObservables
//				.observeValue(XplaneOptionsHelper.getOptions(),
//						"generateBuildings"));
//		bindingContext.bindValue(SWTObservables
//				.observeSelection(btnGenerateObjects), PojoObservables
//				.observeValue(XplaneOptionsHelper.getOptions(), "generateObj"));
//		bindingContext.bindValue(SWTObservables
//				.observeSelection(btnGenerateForests), PojoObservables
//				.observeValue(XplaneOptionsHelper.getOptions(), "generateFor"));
//		bindingContext.bindValue(SWTObservables
//				.observeSelection(btnGenerateStreetLights), PojoObservables
//				.observeValue(XplaneOptionsHelper.getOptions(),
//						"generateStreetLights"));
		bindingContext.bindValue(WidgetProperties.selection().observe(btnGenerateRoads),		
				PojoProperties.value("generateRoads").observe(XplaneOptionsHelper.getOptions()));
		bindingContext.bindValue(WidgetProperties.selection().observe(btnGenerateRail),		
				PojoProperties.value("generateRailways").observe(XplaneOptionsHelper.getOptions()));
		bindingContext.bindValue(WidgetProperties.selection().observe(btnGeneratePower),		
				PojoProperties.value("generatePowerlines").observe(XplaneOptionsHelper.getOptions()));
		bindingContext.bindValue(WidgetProperties.selection().observe(btnGenerateFence),		
				PojoProperties.value("generateFence").observe(XplaneOptionsHelper.getOptions()));
		bindingContext.bindValue(WidgetProperties.selection().observe(btnGenerateTanks),		
				PojoProperties.value("generateTanks").observe(XplaneOptionsHelper.getOptions()));
		bindingContext.bindValue(WidgetProperties.selection().observe(btnGenerateChimneys),		
				PojoProperties.value("generateChimneys").observe(XplaneOptionsHelper.getOptions()));
		bindingContext.bindValue(WidgetProperties.selection().observe(btnGenerateBridges),		
				PojoProperties.value("generateBridges").observe(XplaneOptionsHelper.getOptions()));
	}

	@Override
	protected void addComponentsListeners() {

	}

	@Override
	protected void initLayout() {
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.verticalSpacing = 15;
		gridLayout.horizontalSpacing = 15;
		gridLayout.marginHeight = 15;
		setLayout(gridLayout);
	}

}
