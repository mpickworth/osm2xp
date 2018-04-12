package com.osm2xp.gui.views;

import java.io.File;

import org.eclipse.help.HelpSystem;
import org.eclipse.help.IContext;
import org.eclipse.help.IContextProvider;
import org.eclipse.jface.dialogs.IMessageProvider;
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

import com.osm2xp.constants.Osm2xpConstants;
import com.osm2xp.gui.views.panels.xplane.BuildingsHeightPanel;
import com.osm2xp.gui.views.panels.xplane.FacadeSetPanel;
import com.osm2xp.gui.views.panels.xplane.FacadesExclusionsPanel;
import com.osm2xp.gui.views.panels.xplane.FacadesRulesPanel;

/**
 * XplaneBuildingsView.
 * 
 * @author Benjamin Blanchet
 * 
 */
public class XplaneBuildingsView extends ViewPart implements IContextProvider {
	private ScrolledForm form;

	public XplaneBuildingsView() {
		setTitleImage(null);
	}

	@Override
	public void createPartControl(Composite parent) {

		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		form.setImage(ResourceManager.getPluginImage("com.osm2xp",
				"images/toolbarsIcons/house_32.png"));
		form.setText("Facades buildings options");
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
		 * Facade set
		 */

		Section sectionFacadeSet = toolkit.createSection(form.getBody(),
				Section.TWISTIE | Section.EXPANDED | Section.TITLE_BAR);
		sectionFacadeSet.setLayoutData(new TableWrapData(
				TableWrapData.FILL_GRAB, TableWrapData.TOP, 1, 1));
		sectionFacadeSet.setText("Facade set");
		FacadeSetPanel facadeSetPanel = new FacadeSetPanel(sectionFacadeSet,
				SWT.BORDER);

		// display error if there is no facade set found
		if (!new File(Osm2xpConstants.FACADES_SETS_PATH).exists()
				|| (new File(Osm2xpConstants.FACADES_SETS_PATH).listFiles().length < 0)) {
			form.setMessage("No facades sets found", IMessageProvider.ERROR);
		}
		toolkit.adapt(facadeSetPanel, true, true);
		sectionFacadeSet.setClient(facadeSetPanel);

		/**
		 * Building height
		 */
		Section sectionBuildingHeight = toolkit.createSection(form.getBody(),
				Section.TWISTIE | Section.TITLE_BAR);
		sectionBuildingHeight.setLayoutData(new TableWrapData(
				TableWrapData.FILL, TableWrapData.TOP, 1, 1));

		sectionBuildingHeight.setText("Buildings min/max height");
		BuildingsHeightPanel buildingsHeightPanel3 = new BuildingsHeightPanel(
				sectionBuildingHeight, SWT.BORDER);
		toolkit.adapt(buildingsHeightPanel3, true, true);
		sectionBuildingHeight.setClient(buildingsHeightPanel3);

		/**
		 * Facade exclusions
		 */

		Section sectionFacadeExclusions = toolkit.createSection(form.getBody(),
				Section.TWISTIE | Section.TITLE_BAR);
		sectionFacadeExclusions.setLayoutData(new TableWrapData(
				TableWrapData.FILL, TableWrapData.TOP, 1, 1));

		sectionFacadeExclusions.setText("buildings exclusions");
		FacadesExclusionsPanel facadesExclusionsPanel = new FacadesExclusionsPanel(
				sectionFacadeExclusions, SWT.BORDER);
		toolkit.adapt(facadesExclusionsPanel, true, true);
		sectionFacadeExclusions.setClient(facadesExclusionsPanel);

		/**
		 * Facade rules
		 */

		Section sectionFacadeRules = toolkit.createSection(form.getBody(),
				Section.TWISTIE | Section.TITLE_BAR);
		sectionFacadeRules.setLayoutData(new TableWrapData(TableWrapData.FILL,
				TableWrapData.TOP, 1, 1));

		sectionFacadeRules.setText("facades rules");
		FacadesRulesPanel facadesRulesPanel = new FacadesRulesPanel(
				sectionFacadeRules, SWT.BORDER);
		toolkit.adapt(facadesRulesPanel, true, true);
		sectionFacadeRules.setClient(facadesRulesPanel);

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
		return HelpSystem.getContext("com.osm2xp.buildingsHelpContext");
	}

	@Override
	public String getSearchExpression(Object target) {
		return "buildings";
	}
}
