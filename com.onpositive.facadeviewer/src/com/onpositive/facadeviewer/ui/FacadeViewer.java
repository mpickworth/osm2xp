package com.onpositive.facadeviewer.ui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;

import com.onpositive.facadeviewer.model.FacadesHelper;



@SuppressWarnings("serial")
public class FacadeViewer extends JFrame {
	
	private static final String LAST_DIR = "LAST_DIR";
	private JList<File> list;
	private JLabel label;
	private Image currentImage;
	private AbstractAction refreshAction;
	private File selectedFolder;

	public FacadeViewer() {
		setTitle("Facade Viewer");
		setSize(800, 600);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setIconImage(new ImageIcon(getClass().getResource("house_32.png")).getImage());
		
		AbstractAction openAction = new AbstractAction("Choose folder", new ImageIcon(getClass().getResource("open_32.png"))) {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				doOpen();
			}
		};
		refreshAction = new AbstractAction("Refresh", new ImageIcon(getClass().getResource("refresh_32.png"))) {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				doRefresh();
			}
		};
		AbstractAction exitAction = new AbstractAction("Exit", new ImageIcon(getClass().getResource("exit_32.png"))) {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		};
		
		JToolBar toolBar = new JToolBar("Formatting");
		toolBar.add(openAction);
		toolBar.add(refreshAction);
		toolBar.add(exitAction);
		refreshAction.setEnabled(false);
		
		Container contentPane = getContentPane();
		contentPane.add(toolBar, "North");
		
		list = new JList<File>(); //data has type Object[]
		list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL);
		list.setVisibleRowCount(-1);
		list.setCellRenderer(new SimpleListCellRenderer() {
			@Override
			protected String buildText(Object value) {
				if (value instanceof File) {
					return ((File) value).getName();
				}
				return super.buildText(value);
			}
		});
		list.addListSelectionListener(event -> {
			File selectedFile = list.getSelectedValue();
			if (selectedFile != null) {
				doSelectFile(selectedFile);
			}
		});
		JScrollPane listScroller = new JScrollPane(list);
		contentPane.add(listScroller,"West");
		
		label = new JLabel() {
			private static final long serialVersionUID = 1L;

			@Override
		    public Dimension getPreferredSize(){
				if (currentImage != null) {
					return new Dimension(currentImage.getWidth(null), currentImage.getHeight(null));
				}
				return super.getPreferredSize();
		    }
		};
		
		JScrollPane jsp = new JScrollPane(label);
		contentPane.add(jsp);
		
		String lastDir = Preferences.userNodeForPackage(this.getClass()).get(LAST_DIR, null);
		if (lastDir != null && new File(lastDir).isDirectory()) {
			doSelectFolder(new File(lastDir));
		}
	}
	
	private void doSelectFile(File selectedFile) {
		Image previewImage = FacadesHelper.getPreviewImage(selectedFile);
		if (previewImage != null) {
			label.setIcon(new ImageIcon(previewImage));
		}
//		ImageIcon icon = new ImageIcon(selectedFolder.getAbsolutePath());
		currentImage = previewImage;
	}

	public static void main(String[] args) {
		JFrame frame = new FacadeViewer();
		frame.setVisible(true);
	}
	
	protected void doOpen() {
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setCurrentDirectory(new File(prefs.get(LAST_DIR, ".")));

		int r = chooser.showOpenDialog(this);
		if (r == JFileChooser.APPROVE_OPTION) {
			selectedFolder = chooser.getSelectedFile();
			if (selectedFolder.isDirectory()) {
				prefs.put(LAST_DIR, selectedFolder.getAbsolutePath());
				doSelectFolder(selectedFolder);
			}
		}
	}

	private void doSelectFolder(File selectedFolder) {
		refreshAction.setEnabled(true);
		File[] files = selectedFolder.listFiles((f,s) -> s.endsWith(".fac"));
		list.setListData(files);
		label.setIcon(null);
	}
	
	protected void doRefresh() {
		if (selectedFolder != null && selectedFolder.isDirectory()) {
			doSelectFolder(selectedFolder);
		}
	}

}
