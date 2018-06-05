package com.onpositive.facademarker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

public class FacadeMarker extends JFrame {

	private static final String POINT_LABEL_PREFFIX = "Point:";

	private static enum Mode {
		NONE, HORIZ_MARK, VERT_MARK
	}

	private static final long serialVersionUID = -5434112452025836315L;

	private static final String LAST_DIR = "LAST_DIR";

	private List<Integer> xCoords = new ArrayList<>();
	private List<Integer> yCoords = new ArrayList<>();
	private Mode mode = Mode.NONE;
	private Image currentImage;

	private int currentX = -1;
	private int currentY = -1;

	private JLabel coordsLabel;

	private AbstractAction refreshAction;

	private JLabel label;

	private JButton markHorizontalBtn;

	private JButton markVerticalBtn;

	private JButton endMarkBtn;

	private File selectedFile;

	@SuppressWarnings("serial")
	public FacadeMarker() {
		setTitle("Facade Marker");
		setSize(800, 600);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setIconImage(new ImageIcon(getClass().getResource("/icons/mark_32.png")).getImage());

		AbstractAction openAction = new AbstractAction("Choose folder", new ImageIcon(getClass().getResource("/icons/open_32.png"))) {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				doOpen();
			}
		};
		refreshAction = new AbstractAction("Refresh", new ImageIcon(getClass().getResource("/icons/refresh_32.png"))) {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				doRefresh();
			}
		};
		AbstractAction exitAction = new AbstractAction("Exit", new ImageIcon(getClass().getResource("/icons/exit_32.png"))) {
			
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

		label = new JLabel() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				if (currentImage != null) {
					if (mode == Mode.HORIZ_MARK) {
						Graphics2D g2d = (Graphics2D) g;
						g2d.setColor(Color.GREEN);
						for (Integer xVal : xCoords) {
							g2d.drawLine(xVal, 0, xVal, currentImage.getHeight(null));
						}
						if (currentX >= 0) {
							g2d.setColor(Color.YELLOW);
							g2d.drawLine(currentX, 0, currentX, currentImage.getHeight(null));
						}
					}
					if (mode == Mode.VERT_MARK) {
						Graphics2D g2d = (Graphics2D) g;
						g2d.setColor(Color.MAGENTA);
						for (Integer yVal : yCoords) {
							g2d.drawLine(0, yVal, currentImage.getWidth(null), yVal);
						}
						if (currentY >= 0) {
							g2d.setColor(Color.YELLOW);
							g2d.drawLine(0, currentY, currentImage.getWidth(null), currentY);
						}
					}
				}
			}
			
			@Override
		    public Dimension getPreferredSize(){
				if (currentImage != null) {
					return new Dimension(currentImage.getWidth(null), currentImage.getHeight(null));
				}
				return super.getPreferredSize();
		    }
		};
		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					if (mode == Mode.HORIZ_MARK && currentX >= 0 && (xCoords.isEmpty() || xCoords.get(xCoords.size() - 1) != currentX)) {
						xCoords.add(currentX);
					}
					if (mode == Mode.VERT_MARK && currentY >= 0 && (yCoords.isEmpty() || yCoords.get(yCoords.size() - 1) != currentY)) {
						yCoords.add(currentY);
					}
				} else if (e.getButton() == MouseEvent.BUTTON2) {
					if (mode == Mode.HORIZ_MARK && xCoords.size() > 0) {
						xCoords.remove(xCoords.size() - 1);
					}
					if (mode == Mode.VERT_MARK && yCoords.size() > 0) {
						yCoords.remove(yCoords.size() - 1);
					}
				}

			}
			
		});
		label.addMouseMotionListener(new MouseMotionAdapter() {

			@Override
			public void mouseMoved(MouseEvent e) {
				if (mode == Mode.HORIZ_MARK) {
					currentX = e.getX();
					label.repaint();
				}
				if (mode == Mode.VERT_MARK) {
					currentY = e.getY();
					label.repaint();
				}
				if (currentImage != null) {
					double xCoord = 1.0 * e.getX() / currentImage.getWidth(null);
					double yCoord = 1.0 - (e.getY() * 1.0 / currentImage.getHeight(null));
					coordsLabel.setText(POINT_LABEL_PREFFIX + String.format("(%1.6f, %2.6f)", xCoord, yCoord));
				}
			}

		});
		JPanel subPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		subPanel.add(label);

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(subPanel, BorderLayout.NORTH);
		
		JScrollPane jsp = new JScrollPane(mainPanel);
		contentPane.add(jsp);

		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 4));
		panel.setBorder(new TitledBorder("Click a Button to Perform the Associated Operation..."));

		markHorizontalBtn = new JButton("Mark Horiz");
		markHorizontalBtn.addActionListener(e -> startMarkHorizontal());
		panel.add(markHorizontalBtn);
		markVerticalBtn = new JButton("Mark Vertical");
		markVerticalBtn.addActionListener(e -> startMarkVertical());
		panel.add(markVerticalBtn);
		endMarkBtn = new JButton("End Mark");
		endMarkBtn.addActionListener(e -> endMark());
		endMarkBtn.setToolTipText("Finish marking and copy result to clipboard");
		panel.add(endMarkBtn);
		coordsLabel = new JLabel(POINT_LABEL_PREFFIX);
		coordsLabel.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(coordsLabel);
		contentPane.add(panel, "South");
		updateButtonsEnablement();
	}

	private void updateButtonsEnablement() {
		markHorizontalBtn.setEnabled(mode == Mode.NONE && currentImage != null);
		markVerticalBtn.setEnabled(mode == Mode.NONE && currentImage != null);
		endMarkBtn.setEnabled(mode != Mode.NONE);
		refreshAction.setEnabled(currentImage != null);
	}

	private void startMarkVertical() {
		mode = Mode.VERT_MARK;
		yCoords.clear();
		updateButtonsEnablement();
	}

	private void endMark() {
		if (mode == Mode.HORIZ_MARK) {
			Collections.sort(xCoords);
			int width = currentImage.getWidth(null);
			List<Double> resList = new ArrayList<Double>();
			for (Integer coord : xCoords) {
				resList.add(coord * 1.0 / width);
			}
			if (resList.size() < 2) {
				int result = JOptionPane.showConfirmDialog(this,"Can't generate coords set, need at least 2 values specified. Do you want to continue marking?", "Can't finish action", JOptionPane.YES_NO_OPTION);
				if (result == JOptionPane.YES_OPTION) {
					return;
				} else {
					mode = Mode.NONE;
					clearCoords();
					updateButtonsEnablement();
					return;
				}
			}
			StringBuilder builder = new StringBuilder();
			for (int i = 1; i < resList.size(); i++) {
				builder.append(getHorizCoordsKeyword(i, resList.size()));
				builder.append(' ');
				builder.append(String.format("%.6f", resList.get(i-1)));
				builder.append(' ');
				builder.append(String.format("%.6f", resList.get(i)));
				builder.append('\n');
			}
			StringSelection stringSelection = new StringSelection(builder.toString());
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(stringSelection, null);
			
		} else if (mode == Mode.VERT_MARK) {
			Collections.sort(yCoords);
			int width = currentImage.getWidth(null);
			List<Double> resList = new ArrayList<Double>();
			for (Integer coord : yCoords) {
				resList.add(1.0 - (coord * 1.0 / width));
			}
			if (resList.size() < 2) {
				int result = JOptionPane.showConfirmDialog(this,"Can't generate coords set, need at least 2 values specified. Do you want to continue marking?", "Can't finish action", JOptionPane.YES_NO_OPTION);
				if (result == JOptionPane.YES_OPTION) {
					return;
				} else {
					mode = Mode.NONE;
					clearCoords();
					updateButtonsEnablement();
					return;
				}
			}
			Collections.reverse(resList);
			StringBuilder builder = new StringBuilder();
			for (int i = 1; i < resList.size(); i++) {
				builder.append(getVerticalCoordsKeyword(i, resList.size()));
				builder.append(' ');
				builder.append(String.format("%.9f", resList.get(i-1)));
				builder.append(' ');
				builder.append(String.format("%.9f", resList.get(i)));
				builder.append('\n');
			}
			StringSelection stringSelection = new StringSelection(builder.toString());
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(stringSelection, null);
			
		}
		
		mode = Mode.NONE;
		clearCoords();
		updateButtonsEnablement();
//		int srcX = (int) Math.round(hCoordsList.get(0) * bounds.width);
//		int w = (int) Math.round(hCoordsList.get(hCoordsList.size() - 1) * bounds.width - srcX);
//		
//		int srcY = (int) Math.round((1.0 - vCoordsList.get(0)) * bounds.height);
//		int h = (int) Math.round((1.0 - vCoordsList.get(vCoordsList.size() - 1)) * bounds.height - srcY);
	}

	protected void clearCoords() {
		xCoords.clear();
		yCoords.clear();
		repaint();
	}

	private String getHorizCoordsKeyword(int i, int size) {
		if (i == 1 && size > 3) {
			return "LEFT";
		}
		if (i == size - 1 && size > 2) {
			return "RIGHT";
		}
		return "CENTER";
	}
	
	private String getVerticalCoordsKeyword(int i, int size) {
		if (i == 1 && size > 3) {
			return "BOTTOM";
		}
		if (i == size - 1 && size > 2) {
			return "TOP";
		}
		return "MIDDLE";
	}

	private void startMarkHorizontal() {
		mode = Mode.HORIZ_MARK;
		clearCoords();
		updateButtonsEnablement();
	}

	private void doOpen() {
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File(prefs.get(LAST_DIR, ".")));

		chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
			public boolean accept(File f) {
				return f.getName().toLowerCase().endsWith(".png") || f.isDirectory();
			}

			public String getDescription() {
				return "PNG Images";
			}
		});

		int r = chooser.showOpenDialog(this);
		if (r == JFileChooser.APPROVE_OPTION) {
			selectedFile = chooser.getSelectedFile();
			ImageIcon icon = new ImageIcon(selectedFile.getAbsolutePath());
			prefs.put(LAST_DIR, selectedFile.getParentFile().getAbsolutePath());
			label.setIcon(icon);
			currentImage = icon.getImage();
			updateButtonsEnablement();
		}	
	}

	protected void doRefresh() {
		if (selectedFile != null && selectedFile.isFile()) {
			ImageIcon icon = new ImageIcon(selectedFile.getAbsolutePath());
			label.setIcon(icon);
			currentImage = icon.getImage();
		}
	}

	public static void main(String[] args) {
		JFrame frame = new FacadeMarker();
		frame.setVisible(true);
	}
}