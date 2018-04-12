package com.osm2xp.gui.views;

import org.eclipse.help.HelpSystem;
import org.eclipse.help.IContext;
import org.eclipse.help.IContextProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import com.osm2xp.gui.views.panels.xplane.ForestsRulesPanel;

/**
 * XplaneForestsView.
 * 
 * @author Benjamin Blanchet
 * 
 */

public class XplaneForestsView extends ViewPart implements IContextProvider {

	private ScrolledForm form;

	public XplaneForestsView() {
	}

	@Override
	public void createPartControl(Composite parent) {

		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		form.setImage(ResourceManager.getPluginImage("com.osm2xp",
				"images/toolbarsIcons/leaf_32.png"));
		form.setText("Forests options");
		toolkit.decorateFormHeading(form.getForm());

		toolkit = new FormToolkit(parent.getDisplay());
		FillLayout fl_parent = new FillLayout(SWT.HORIZONTAL);
		fl_parent.marginWidth = 5;
		fl_parent.spacing = 5;
		fl_parent.marginHeight = 5;
		parent.setLayout(fl_parent);

		TableWrapLayout layout = new TableWrapLayout();
		layout.topMargin = 30;
		layout.verticalSpacing = 20;
		layout.bottomMargin = 10;
		layout.horizontalSpacing = 10;
		form.getBody().setLayout(layout);

		/**
		 * Forests Rules
		 */

		Section sectionForestRules = toolkit.createSection(form.getBody(),
				Section.TWISTIE | Section.EXPANDED | Section.TITLE_BAR);
		sectionForestRules.setLayoutData(new TableWrapData(
				TableWrapData.FILL_GRAB, TableWrapData.TOP, 1, 1));
		sectionForestRules.setText("Forests rules");
		ForestsRulesPanel forestsRulesPanel = new ForestsRulesPanel(
				sectionForestRules, SWT.BORDER);
		toolkit.adapt(forestsRulesPanel, true, true);
		sectionForestRules.setClient(forestsRulesPanel);
	}

	/**
	 * Passing the focus request to the form.
	 */
	public void setFocus() {
		form.setFocus();
	}

	/**
	 * Disposes the toolkit
	 */
	public void dispose() {
		super.dispose();
	}

	@Override
	public int getContextChangeMask() {
		return 0;
	}

	@Override
	public IContext getContext(Object target) {
		return HelpSystem.getContext("com.osm2xp.forestsHelpContext");
	}

	@Override
	public String getSearchExpression(Object target) {
		return "forest";
	}
}