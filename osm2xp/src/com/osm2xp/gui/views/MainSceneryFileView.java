package com.osm2xp.gui.views;

import org.eclipse.help.HelpSystem;
import org.eclipse.help.IContext;
import org.eclipse.help.IContextProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import com.osm2xp.gui.views.panels.generic.GeneralOptionsPanel;
import com.osm2xp.gui.views.panels.generic.OutPutFormatPanel;
import com.osm2xp.gui.views.panels.generic.SceneryFilePanel;

/**
 * MainSceneryFileView.
 * 
 * @author Benjamin Blanchet
 * 
 */
public class MainSceneryFileView extends ViewPart implements IContextProvider {
	
	public static final String ID = "com.osm2xp.viewSceneryTab";
	
	private ScrolledForm form;

	public MainSceneryFileView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		form.setImage(ResourceManager.getPluginImage("com.osm2xp",
				"images/toolbarsIcons/prefs_32.png"));
		form.setText("Scenery options");
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
		 * file and scene name
		 */
		Section sectionFile = toolkit.createSection(form.getBody(),
				Section.TWISTIE | Section.EXPANDED | Section.TITLE_BAR);
		sectionFile.setLayoutData(new TableWrapData(TableWrapData.FILL,
				TableWrapData.TOP, 1, 1));
		sectionFile.setText("File and scene name");
		SceneryFilePanel sceneryFilePanel = new SceneryFilePanel(sectionFile,
				SWT.BORDER, getSite());
		toolkit.adapt(sceneryFilePanel, true, true);
		sectionFile.setClient(sceneryFilePanel);
		new Label(sceneryFilePanel, SWT.NONE);
		new Label(sceneryFilePanel, SWT.NONE);
		/**
		 * Scenery options
		 */
		Section sectionSceneryOptions = toolkit.createSection(form.getBody(),
				Section.TWISTIE | Section.TITLE_BAR);
		sectionSceneryOptions.setLayoutData(new TableWrapData(
				TableWrapData.FILL, TableWrapData.TOP, 1, 1));
		sectionSceneryOptions.setText("Scenery options");
		GeneralOptionsPanel sceneryOptionsPanel = new GeneralOptionsPanel(
				sectionSceneryOptions, SWT.BORDER);
		toolkit.adapt(sceneryOptionsPanel, true, true);
		sectionSceneryOptions.setClient(sceneryOptionsPanel);
		new Label(sceneryOptionsPanel, SWT.NONE);
		new Label(sceneryOptionsPanel, SWT.NONE);
		new Label(sceneryOptionsPanel, SWT.NONE);
		/**
		 * Output Format
		 */

		Section outputFormatSection = toolkit.createSection(form.getBody(),
				Section.TWISTIE | Section.EXPANDED | Section.TITLE_BAR);
		TableWrapData twd_outputFormatSection = new TableWrapData(
				TableWrapData.FILL_GRAB, TableWrapData.TOP, 1, 1);
		twd_outputFormatSection.heightHint = 800;
		outputFormatSection.setLayoutData(twd_outputFormatSection);
		outputFormatSection.setText("Output Format");
		OutPutFormatPanel outputFormatPanel = new OutPutFormatPanel(
				outputFormatSection, SWT.BORDER);
		toolkit.adapt(outputFormatPanel, true, true);
		outputFormatSection.setClient(outputFormatPanel);

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
		return HelpSystem.getContext("com.osm2xp.sceneryHelpContext");
	}

	@Override
	public String getSearchExpression(Object target) {
		return "scenery";
	}
}
