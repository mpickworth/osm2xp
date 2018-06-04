package com.onpositive.facadecreator;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;


public class FacadeViewer extends JFrame implements ActionListener {
	
	private static final String LAST_DIR = "LAST_DIR";
	private JMenuItem openItem;
	private JMenuItem exitItem;
	private JList list;
	private JLabel label;
	private Image currentImage;

	public FacadeViewer() {
		setTitle("Facade Marker");
		setSize(800, 600);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		JMenuBar mbar = new JMenuBar();
		JMenu m = new JMenu("File");
		openItem = new JMenuItem("Open");
		openItem.addActionListener(this);
//			openItem.setAccelerator(KeyStroke.getKeyStroke("ctrl o"));
		m.add(openItem);
		exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(this);
		m.add(exitItem);
		mbar.add(m);
		setJMenuBar(mbar);
		
		list = new JList<File>(); //data has type Object[]
		list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL);
		list.setVisibleRowCount(-1);
		JScrollPane listScroller = new JScrollPane(list);
		Container contentPane = getContentPane();
		contentPane.add(listScroller,"East");
		
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
	}
	
	public static void main(String[] args) {
		JFrame frame = new FacadeViewer();
		frame.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		Object source = evt.getSource();
		if (source == openItem) {
			Preferences prefs = Preferences.userNodeForPackage(this.getClass());
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setCurrentDirectory(new File(prefs.get(LAST_DIR, ".")));

			int r = chooser.showOpenDialog(this);
			if (r == JFileChooser.APPROVE_OPTION) {
				File selectedFile = chooser.getSelectedFile();
				ImageIcon icon = new ImageIcon(selectedFile.getAbsolutePath());
				prefs.put(LAST_DIR, selectedFile.getParentFile().getAbsolutePath());
				label.setIcon(icon);
				currentImage = icon.getImage();
			}
		} else if (source == exitItem)
			System.exit(0);
		
	}

}
