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

import com.osm2xp.gui.views.panels.xplane.GeneratedItemsPanel;
import com.osm2xp.gui.views.panels.xplane.SceneryExclusionsPanel;
import com.osm2xp.gui.views.panels.xplane.SceneryOptionsPanel;
import com.osm2xp.gui.views.panels.xplane.StatsOptionsPanel;

/**
 * XplaneAdvancedOptionsView.
 * 
 * @author Benjamin Blanchet
 * 
 */
public class XplaneAdvancedOptionsView extends ViewPart implements
		IContextProvider {
	private ScrolledForm form;

	public XplaneAdvancedOptionsView() {

	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		form.setImage(ResourceManager.getPluginImage("com.osm2xp",
				"images/toolbarsIcons/advanced_32.png"));
		form.setText("Advanced options");
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
		 * Generated items
		 */
		Section sectionGeneratedItems = toolkit.createSection(form.getBody(),
				Section.TWISTIE | Section.EXPANDED | Section.TITLE_BAR);
		sectionGeneratedItems.setLayoutData(new TableWrapData(
				TableWrapData.FILL_GRAB, TableWrapData.TOP, 1, 1));
		sectionGeneratedItems.setText("generated items");
		GeneratedItemsPanel scGeneratedItemsPanel = new GeneratedItemsPanel(
				sectionGeneratedItems, SWT.BORDER);
		toolkit.adapt(scGeneratedItemsPanel, true, true);
		sectionGeneratedItems.setClient(scGeneratedItemsPanel);

		/**
		 * Scenery exclusions
		 */
		Section sectionExclusions = toolkit.createSection(form.getBody(),
				Section.TWISTIE | Section.TITLE_BAR);
		sectionExclusions.setLayoutData(new TableWrapData(
				TableWrapData.FILL_GRAB, TableWrapData.TOP, 1, 1));
		sectionExclusions.setText("Scenery exclusions");
		SceneryExclusionsPanel sceneryExclusionsPanel = new SceneryExclusionsPanel(
				sectionExclusions, SWT.BORDER);
		toolkit.adapt(sceneryExclusionsPanel, true, true);
		sectionExclusions.setClient(sceneryExclusionsPanel);

		/**
		 * Scenery options
		 */
		Section sectionSceneryOptions = toolkit.createSection(form.getBody(),
				Section.TWISTIE | Section.TITLE_BAR);
		sectionSceneryOptions.setLayoutData(new TableWrapData(
				TableWrapData.FILL_GRAB, TableWrapData.TOP, 1, 1));
		sectionSceneryOptions.setText("Scenery options");
		SceneryOptionsPanel sceneryOptionsPanel = new SceneryOptionsPanel(
				sectionSceneryOptions, SWT.BORDER);
		toolkit.adapt(sceneryOptionsPanel, true, true);
		sectionSceneryOptions.setClient(sceneryOptionsPanel);
		/**
		 * Stats options
		 */

		Section statsOptionsSection = toolkit.createSection(form.getBody(),
				Section.TWISTIE | Section.TITLE_BAR);
		statsOptionsSection.setLayoutData(new TableWrapData(
				TableWrapData.FILL_GRAB, TableWrapData.TOP, 1, 1));
		statsOptionsSection.setText("Stats options");
		StatsOptionsPanel statsOptionsPanel = new StatsOptionsPanel(
				statsOptionsSection, SWT.BORDER);

		toolkit.adapt(statsOptionsPanel, true, true);
		statsOptionsSection.setClient(statsOptionsPanel);
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
		return HelpSystem.getContext("com.osm2xp.advancedHelpContext");
	}

	@Override
	public String getSearchExpression(Object target) {
		return "advanced";
	}
	
}
