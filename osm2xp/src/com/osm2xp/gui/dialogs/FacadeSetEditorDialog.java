package com.osm2xp.gui.dialogs;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.osm2xp.exceptions.Osm2xpBusinessException;
import com.osm2xp.gui.Activator;
import com.osm2xp.gui.dialogs.utils.Osm2xpDialogsHelper;
import com.osm2xp.model.facades.BarrierType;
import com.osm2xp.model.facades.Facade;
import com.osm2xp.model.facades.FacadeSet;
import com.osm2xp.utils.helpers.FacadeSetHelper;
import com.osm2xp.utils.helpers.ScaleChangeHelper;
import com.osm2xp.utils.logging.Osm2xpLogger;

/**
 * FacadeSetEditorDialog.
 * 
 * @author Benjamin Blanchet
 * 
 */
public class FacadeSetEditorDialog extends Dialog {
	private Text textName;
	private Text textAuthor;
	private Text textRoofColor;
	private Text textWallColor;
	private String facadeSetFolder;
	private Table table;
	private TableViewer viewer;
	private Facade currentFacade;
	private FacadeSet facadeSet;
	private Button btnIndustrial;
	private Button btnCommercial;
	private Button btnResidential;
	private Button btnSlopedRoof;
	private Button btnSimplebuildingOnly;
	private Spinner spinnerMinVector;
	private Spinner spinnerMaxVector;
	private Spinner spinnerMinHeight;
	private Spinner spinnerMaxHeight;
	private Button buildingButton;
	private Button fenceButton;
	private Button wallButton;
	private Composite buildingParamsComposite;
	private Button adjustScaleButton;
	private TabFolder filePropsFolder;
	private Canvas previewCanvas;
	private Image previewImage;
	private Image errorPreviewImage = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID,"images/preview_error.png").createImage();

	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	public FacadeSetEditorDialog(Shell parentShell, String facadeSetFolder, FacadeSet facadeSet) {
		super(parentShell);
		this.facadeSetFolder = facadeSetFolder;
		this.facadeSet = facadeSet;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Facade set editor - " + facadeSetFolder);
	}

	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new FillLayout(SWT.HORIZONTAL));

		Group groupFacadesList = new Group(container, SWT.NONE);
		groupFacadesList.setLayout(new FillLayout(SWT.HORIZONTAL));
		viewer = new TableViewer(groupFacadesList, SWT.FULL_SELECTION);
		final TableViewerColumn colFile = new TableViewerColumn(viewer,
				SWT.NONE, 0);
		final TableColumn column = colFile.getColumn();
		column.setText("file");
		column.setWidth(450);
		column.setResizable(false);
		column.setMoveable(false);

		colFile.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Facade p = (Facade) element;
				return p.getFile();
			}
		});

		table = viewer.getTable();
		table.setHeaderVisible(false);
		table.setLinesVisible(false);
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setInput(facadeSet.getFacades());

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) viewer
						.getSelection();
				currentFacade = (Facade) selection.getFirstElement();
				updateProperties();

			}

		});

		Group groupProperties = new Group(container, SWT.NONE);
		groupProperties.setLayout(new GridLayout(1, false));

		Group grpFacadeSetProperties = new Group(groupProperties, SWT.NONE);
		GridData gd_grpFacadeSetProperties = new GridData(SWT.FILL, SWT.CENTER,
				true, false, 1, 1);
		gd_grpFacadeSetProperties.heightHint = 187;
		grpFacadeSetProperties.setLayoutData(gd_grpFacadeSetProperties);
		grpFacadeSetProperties.setText("Facade set properties");
		grpFacadeSetProperties.setBounds(0, 0, 70, 80);
		grpFacadeSetProperties.setLayout(new GridLayout(2, false));

		Label labelName = new Label(grpFacadeSetProperties, SWT.NONE);
		labelName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 1, 1));
		labelName.setText("Facade set name : ");

		textName = new Text(grpFacadeSetProperties, SWT.BORDER);
		textName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				facadeSet.setName(textName.getText());
			}
		});
		textName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));
		if (facadeSet.getName() != null) {
			textName.setText(facadeSet.getName());
		}
		Label labelAuthor = new Label(grpFacadeSetProperties, SWT.NONE);
		labelAuthor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 1, 1));
		labelAuthor.setText("Author : ");

		textAuthor = new Text(grpFacadeSetProperties, SWT.BORDER);
		textAuthor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		textAuthor.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				facadeSet.setAuthor(textAuthor.getText());
			}
		});

		if (facadeSet.getAuthor() != null) {
			textAuthor.setText(facadeSet.getAuthor());
		}
		Label labelDescription = new Label(grpFacadeSetProperties, SWT.NONE);
		labelDescription.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				false, false, 1, 1));
		labelDescription.setText("Description");

		final StyledText styledTextDescription = new StyledText(
				grpFacadeSetProperties, SWT.BORDER);
		styledTextDescription.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				true, true, 1, 1));
		styledTextDescription.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				facadeSet.setDescription(styledTextDescription.getText());
			}
		});
		if (facadeSet.getDescription() != null) {
			styledTextDescription.setText(facadeSet.getDescription());
		}
		
		filePropsFolder = new TabFolder(groupProperties, SWT.TOP);
		filePropsFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		filePropsFolder.setVisible(false);
		
		TabItem tab1 = new TabItem(filePropsFolder, SWT.NONE);
	    tab1.setText("File properties");
		
		Composite filePropsComposite = new Composite(filePropsFolder, SWT.NONE);
		filePropsComposite.setVisible(false);
		filePropsComposite.setLayout(new GridLayout(3, false));
		filePropsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true, 1, 1));
//		fileProps.setText("Facade file");
		tab1.setControl(filePropsComposite);
		
		buildingButton = new Button(filePropsComposite, SWT.RADIO);
		buildingButton.setText("Building");
		fenceButton = new Button(filePropsComposite, SWT.RADIO);
		fenceButton.setText("Fence");
		wallButton = new Button(filePropsComposite, SWT.RADIO);
		wallButton.setText("Wall");
		
		buildingParamsComposite = new Composite(filePropsComposite, SWT.NONE);
		buildingParamsComposite.setLayout(new GridLayout(2, false));
		GridDataFactory.fillDefaults().grab(true, true).span(3,1).applyTo(buildingParamsComposite);
		
		SelectionAdapter facadeTypeAdapter = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				buildingParamsComposite.setVisible(buildingButton.getSelection());
				buildingParamsComposite.setEnabled(buildingButton.getSelection());
				if (buildingButton.getSelection()) {
					currentFacade.setBarrierType(null);
				}
				if (fenceButton.getSelection()) {
					currentFacade.setBarrierType(BarrierType.FENCE);
				}
				if (wallButton.getSelection()) {
					currentFacade.setBarrierType(BarrierType.WALL);
				}
			}
		};
		buildingButton.addSelectionListener(facadeTypeAdapter);
		fenceButton.addSelectionListener(facadeTypeAdapter);
		wallButton.addSelectionListener(facadeTypeAdapter);

		Label labelRoofColor = new Label(buildingParamsComposite, SWT.NONE);
		labelRoofColor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 1, 1));
		labelRoofColor.setText("Roof color : ");

		textRoofColor = new Text(buildingParamsComposite, SWT.BORDER);

		textRoofColor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));

		textRoofColor.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				ColorDialog dlg = new ColorDialog(getShell());
				dlg.setText("Choose roof Color");
				RGB rgb = dlg.open();
				if (rgb != null) {
					textRoofColor.setText(rgb.red + "," + rgb.green + ","
							+ rgb.blue);
					currentFacade.setRoofColor(textRoofColor.getText());
				}
			}
		});
		Label labelWallColor = new Label(buildingParamsComposite, SWT.NONE);
		labelWallColor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 1, 1));
		labelWallColor.setText("Wall color :");

		textWallColor = new Text(buildingParamsComposite, SWT.BORDER);
		textWallColor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));

		textWallColor.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				ColorDialog dlg = new ColorDialog(getShell());
				dlg.setText("Choose wall Color");
				RGB rgb = dlg.open();
				if (rgb != null) {
					textWallColor.setText(rgb.red + "," + rgb.green + ","
							+ rgb.blue);
					currentFacade.setWallColor(textWallColor.getText());
				}
			}
		});
		new Label(buildingParamsComposite, SWT.NONE);
		new Label(buildingParamsComposite, SWT.NONE);

		btnIndustrial = new Button(buildingParamsComposite, SWT.CHECK);
		btnIndustrial.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				currentFacade.setIndustrial(btnIndustrial.getSelection());
			}
		});
		btnIndustrial.setText("Industrial");

		btnResidential = new Button(buildingParamsComposite, SWT.CHECK);
		btnResidential.setText("Residential");
		btnResidential.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				currentFacade.setResidential(btnResidential.getSelection());
			}
		});
		btnCommercial = new Button(buildingParamsComposite, SWT.CHECK);
		btnCommercial.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 1, 1));
		btnCommercial.setText("Commercial");
		btnCommercial.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				currentFacade.setCommercial(btnCommercial.getSelection());
			}
		});
		btnSimplebuildingOnly = new Button(buildingParamsComposite, SWT.CHECK);
		btnSimplebuildingOnly.setText("Simple building only");
		btnSimplebuildingOnly.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				currentFacade.setSimpleBuildingOnly(btnSimplebuildingOnly
						.getSelection());
			}
		});
		btnSlopedRoof = new Button(buildingParamsComposite, SWT.CHECK);
		btnSlopedRoof.setText("Sloped roof");
		btnSlopedRoof.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				currentFacade.setSloped(btnSlopedRoof.getSelection());
				currentFacade.setResidential(true);
				currentFacade.setSimpleBuildingOnly(true);
			}
		});
		new Label(buildingParamsComposite, SWT.NONE);
		new Label(buildingParamsComposite, SWT.NONE);
		new Label(buildingParamsComposite, SWT.NONE);
		new Label(buildingParamsComposite, SWT.NONE);
		new Label(buildingParamsComposite, SWT.NONE);

		Label labelMinVector = new Label(buildingParamsComposite, SWT.NONE);
		labelMinVector.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		labelMinVector.setText("Minimum vector length :");

		spinnerMinVector = new Spinner(buildingParamsComposite, SWT.BORDER);
		spinnerMinVector.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (spinnerMinVector.getSelection() > 0) {
					currentFacade.setMinVectorLength((double) spinnerMinVector
							.getSelection() / 100);
				}

			}
		});
		spinnerMinVector.setMaximum(1000);
		spinnerMinVector.setIncrement(100);
		spinnerMinVector.setDigits(2);
		spinnerMinVector.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				false, false, 1, 1));

		Label lblNewLabel = new Label(buildingParamsComposite, SWT.NONE);
		lblNewLabel.setText("Maximum vector length :");

		spinnerMaxVector = new Spinner(buildingParamsComposite, SWT.BORDER);
		spinnerMaxVector.setIncrement(100);
		spinnerMaxVector.setMaximum(1000);
		spinnerMaxVector.setDigits(2);
		spinnerMaxVector.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				false, false, 1, 1));

		Label lblMinimumHeightmeters = new Label(buildingParamsComposite, SWT.NONE);
		lblMinimumHeightmeters.setText("Minimum height (meters) :");

		spinnerMinHeight = new Spinner(buildingParamsComposite, SWT.BORDER);
		spinnerMinHeight.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				false, false, 1, 1));

		Label lblMaximumHeightmeters = new Label(buildingParamsComposite, SWT.NONE);
		lblMaximumHeightmeters.setText("Maximum height (meters) : ");

		spinnerMaxHeight = new Spinner(buildingParamsComposite, SWT.BORDER);
		spinnerMaxHeight.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				false, false, 1, 1));

		spinnerMaxHeight.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (spinnerMaxHeight.getSelection() > 0) {
					currentFacade.setMaxHeight(spinnerMaxHeight.getSelection());
				}

			}
		});

		spinnerMinHeight.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (spinnerMinHeight.getSelection() > 0) {
					currentFacade.setMinHeight(spinnerMinHeight.getSelection());
				}

			}
		});
		spinnerMaxVector.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (spinnerMaxVector.getSelection() > 0) {

					currentFacade.setMaxVectorLength((double) spinnerMaxVector
							.getSelection() / 100);
				}

			}
		});
		Button deleteButton = new Button(buildingParamsComposite, SWT.PUSH);
		deleteButton.setImage(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/remove.gif").createImage());
		deleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (currentFacade != null) {
					int mode = MessageDialog.open(MessageDialog.QUESTION, getShell(),"Remove facade?", "Remove facade " + currentFacade.getFile() + "?", SWT.NONE, "Remove", "Remove with file", "Cancel");
					if (mode == 2) {
						return;
					}
					List<Facade> facadesList = facadeSet.getFacades();
					int idx = facadesList.indexOf(currentFacade);
					if (idx >= 0) {
						facadesList.remove(currentFacade);
						if (mode == 1) {
							new File(facadeSetFolder, currentFacade.getFile()).delete();
						}
						if (facadesList.size() > idx) {
							currentFacade = facadesList.get(idx);
						} else if (facadesList.size() > 0) {
							currentFacade = facadesList.get(0);
						} else {
							currentFacade = null;
						}
						viewer.setInput(facadeSet.getFacades());
						if (currentFacade != null) {
							viewer.setSelection(new StructuredSelection(currentFacade));
						}
						updateProperties();
					}
				}
			}
		});

		adjustScaleButton = new Button(grpFacadeSetProperties, SWT.PUSH);
		adjustScaleButton.setText("Adjust facade scale");
		GridDataFactory.swtDefaults().applyTo(adjustScaleButton);
		adjustScaleButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doAdjustFacadeScale();
			}
		});
		Button generateDefaultsButton = new Button(grpFacadeSetProperties, SWT.PUSH);
		generateDefaultsButton.setText("Generate stubs");
		generateDefaultsButton.setToolTipText("Generate descriptors for .fac files present in folder, but missing from XML facades descriptor");
		GridDataFactory.swtDefaults().applyTo(generateDefaultsButton);
		generateDefaultsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doGenerateStubDescriptors();
			}
		});
		
		TabItem tab2 = new TabItem(filePropsFolder, SWT.NONE);
	    tab2.setText("Preview");
	    
	    previewCanvas = new Canvas(filePropsFolder, SWT.NONE);
	    tab2.setControl(previewCanvas);
		previewCanvas.addPaintListener(e -> {
			if (previewImage != null) {
				 Rectangle rect = ((Canvas) e.widget).getBounds();
				 e.gc.drawImage(previewImage,0,0,previewImage.getBounds().width, previewImage.getBounds().height, rect.x, rect.y, rect.width, rect.height);
			}
		});
	    
		return container;
	}

	protected void doAdjustFacadeScale() {
		File facadeFile = new File(facadeSetFolder, currentFacade.getFile());
		InputDialog inputDialog = new InputDialog(getShell(), "Adjust facade scale","WARNING: feature is experimental. Use with care!\n"
				+ "Current scale " + ScaleChangeHelper.getScaleStr(facadeFile) + "\n" +
				"Enter factor:", "1.0", newText -> {
					try {
						Double.parseDouble(newText);
					} catch (Exception e) {
						return "Enter valid number";
					}
					return null;
				} );
		if (inputDialog.open() == Dialog.OK) {
			try {
				ScaleChangeHelper.changeScale(facadeFile, Double.parseDouble(inputDialog.getValue()));
			} catch (NumberFormatException e) {
				Activator.log(e);
			} catch (IOException e) {
				MessageDialog.openError(getShell(), "Error changing scale for " + currentFacade.getFile(), "Error changing scale: " + e.getMessage());
			}
		}
	}
	
	/**
	 * Generate descriptors for .fac files contained in facade set folder, but not contained in facade set description
	 */
	protected void doGenerateStubDescriptors() {
		Job facadeGenerationJob = new Job("Generate facade stub") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				Set<String> describedFiles = facadeSet.getFacades().stream().map(facade -> facade.getFile().toLowerCase()).collect(Collectors.toSet());
				File folder = new File(facadeSetFolder);
				if (!folder.isDirectory()) {
					Osm2xpDialogsHelper.displayErrorDialog("Error generating stubs", facadeSetFolder + " is not a directory");
				}
				String[] facFiles = folder.list((dir, name) -> name.endsWith(".fac"));
				for (String fileName : facFiles) {
					if (!describedFiles.contains(fileName.toLowerCase())) {
						facadeSet.getFacades().add(FacadeSetHelper.generateDefaultDescriptor(new File(facadeSetFolder, fileName)));
					}
				}
				Display.getDefault().asyncExec(() ->{
					viewer.setInput(facadeSet.getFacades());
				});
				return Status.OK_STATUS;
			}
			
		};
		facadeGenerationJob.setUser(false);
		facadeGenerationJob.schedule();
	}

	private void updateProperties() {
		previewImage = null;
		filePropsFolder.setVisible(currentFacade != null);
		adjustScaleButton.setEnabled(currentFacade != null);
		if (currentFacade == null) {
			return;
		}
		if (currentFacade.getRoofColor() != null) {
			textRoofColor.setText(currentFacade.getRoofColor());
		} else {
			textRoofColor.setText("");
		}

		if (currentFacade.getWallColor() != null) {
			textWallColor.setText(currentFacade.getWallColor());
		} else {
			textWallColor.setText("");
		}

		btnResidential.setSelection(currentFacade.isResidential());
		btnCommercial.setSelection(currentFacade.isCommercial());
		btnIndustrial.setSelection(currentFacade.isIndustrial());
		btnSlopedRoof.setSelection(currentFacade.isSloped());
		btnSimplebuildingOnly
				.setSelection(currentFacade.isSimpleBuildingOnly());
		if (currentFacade.getMinVectorLength() > 0) {
			spinnerMinVector.setSelection((int) (currentFacade
					.getMinVectorLength() * 100));
		} else {
			spinnerMinVector.setSelection(0);
		}
		if (currentFacade.getMaxVectorLength() > 0) {
			spinnerMaxVector.setSelection((int) (currentFacade
					.getMaxVectorLength() * 100));
		} else {
			spinnerMaxVector.setSelection(0);
		}
		if (currentFacade.getMaxHeight() > 0) {
			spinnerMaxHeight.setSelection(currentFacade.getMaxHeight());
		} else {
			spinnerMaxHeight.setSelection(0);

		}
		if (currentFacade.getMinHeight() > 0) {
			spinnerMinHeight.setSelection(currentFacade.getMinHeight());
		} else {
			spinnerMinHeight.setSelection(0);
		}
		
		BarrierType barrierType = currentFacade.getBarrierType();
		buildingParamsComposite.setEnabled(barrierType == null);
		buildingParamsComposite.setVisible(barrierType == null);
		buildingButton.setSelection(barrierType == null);
		fenceButton.setSelection(barrierType == BarrierType.FENCE);
		wallButton.setSelection(barrierType == BarrierType.WALL);
		previewImage = FacadeSetHelper.getPreviewImage(new File(facadeSetFolder, currentFacade.getFile()));
		if (previewImage == null) {
			previewImage = errorPreviewImage;
		}
		previewCanvas.redraw();
	}

	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button button = createButton(parent, IDialogConstants.OK_ID,
				IDialogConstants.OK_LABEL, true);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					FacadeSetHelper.saveFacadeSet(facadeSet, facadeSetFolder);
				} catch (Osm2xpBusinessException e1) {
					Osm2xpLogger.error("Error saving facade set", e1);
				}
			}
		});
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(733, 700);
	}

}
