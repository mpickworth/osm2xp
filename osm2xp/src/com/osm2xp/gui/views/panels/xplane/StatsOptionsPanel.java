package com.osm2xp.gui.views.panels.xplane;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.osm2xp.utils.helpers.XplaneOptionsHelper;

/**
 * StatsOptionsPanel.
 * 
 * @author Benjamin Blanchet
 * 
 */
public class StatsOptionsPanel extends Composite {

	public StatsOptionsPanel(final Composite parent, final int style) {
		super(parent, style);
		DataBindingContext bindingContext = new DataBindingContext();
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.verticalSpacing = 15;
		gridLayout.horizontalSpacing = 15;
		gridLayout.marginHeight = 15;
		setLayout(gridLayout);
		Button btnGenerateXmlStats = new Button(this, SWT.CHECK);
		btnGenerateXmlStats.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				false, false, 1, 1));
		btnGenerateXmlStats.setText("Generate XML stats");
		bindingContext.bindValue(WidgetProperties.selection().observe(btnGenerateXmlStats),		
				PojoProperties.value("generateXmlStats").observe(XplaneOptionsHelper.getOptions()));

		Button btnGeneratePdfStats = new Button(this, SWT.CHECK);
		btnGeneratePdfStats.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				false, false, 1, 1));
		btnGeneratePdfStats.setText("Generate PDF stats");
		bindingContext.bindValue(WidgetProperties.selection().observe(btnGeneratePdfStats),		
				PojoProperties.value("generatePdfStats").observe(XplaneOptionsHelper.getOptions()));
		
		Button btnGenerateDebugImg = new Button(this, SWT.CHECK);
		btnGenerateDebugImg.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				false, false, 1, 1));
		btnGenerateDebugImg.setText("Generate Debug images");
		btnGenerateDebugImg.setToolTipText("Generate 2048x2048 image for each tile, using first encountered object as top-left corner. "
				+ "Generated buildings/objects etc. are marked on it using scale 1px = 1m");
		bindingContext.bindValue(WidgetProperties.selection().observe(btnGenerateDebugImg),		
				PojoProperties.value("generateDebugImg").observe(XplaneOptionsHelper.getOptions()));
	}

}
