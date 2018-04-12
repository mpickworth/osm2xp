package com.osm2xp.gui.views;

import org.eclipse.help.HelpSystem;
import org.eclipse.help.IContext;
import org.eclipse.help.IContextProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import com.osm2xp.gui.views.panels.xplane.XplaneObjectsRulesPanel;

/**
 * Xplane3DObjectsView.
 * 
 * @author Benjamin Blanchet
 * 
 */
public class Xplane3DObjectsView extends ViewPart implements IContextProvider {
	private ScrolledForm form;

	public Xplane3DObjectsView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		form.setImage(ResourceManager.getPluginImage("com.osm2xp",
				"images/toolbarsIcons/objects_32.png"));
		form.setText("3D Objects options");
		toolkit.decorateFormHeading(form.getForm());
		form.getForm().addMessageHyperlinkListener(new HyperlinkAdapter());

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
		 * objects rules
		 */

		Section sectionObjectsRules = toolkit.createSection(form.getBody(),
				Section.TWISTIE | Section.EXPANDED | Section.TITLE_BAR);
		sectionObjectsRules.setLayoutData(new TableWrapData(
				TableWrapData.FILL_GRAB, TableWrapData.TOP, 1, 1));

		sectionObjectsRules.setText("objects rules");
		XplaneObjectsRulesPanel objectsRulesPanel = new XplaneObjectsRulesPanel(
				sectionObjectsRules, SWT.BORDER);
		toolkit.adapt(objectsRulesPanel, true, true);
		sectionObjectsRules.setClient(objectsRulesPanel);

	}

	/**
	 * Passing the focus request to the form.
	 */
	public void setFocus() {
		form.setFocus();
	}

	@Override
	public int getContextChangeMask() {
		return 0;
	}

	@Override
	public IContext getContext(Object target) {
		return HelpSystem.getContext("com.osm2xp.objectsHelpContext");
	}

	@Override
	public String getSearchExpression(Object target) {
		return "object";
	}

}
