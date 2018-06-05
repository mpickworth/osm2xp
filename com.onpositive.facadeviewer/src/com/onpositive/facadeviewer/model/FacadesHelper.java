package com.onpositive.facadeviewer.model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.ImageIcon;

public class FacadesHelper {
	public static Image getPreviewImage(File facadeFile) {
//		Image img = imageRegistry.get(facadeFile.getAbsolutePath());
//		if (img != null) {
//			return img;
//		}
		FacadeDefinition definition = FacadeDefinitionParser.parse(facadeFile);
		
		Collection<String> textures = definition.getProperties().get("TEXTURE");                  
		if (textures == null || textures.isEmpty()) {                            
			return null;                                                         
		}                                                                        
		String imgFileName = textures.iterator().next();         
		File imgFile = new File(facadeFile.getParentFile(), imgFileName); 
		if (!imgFile.isFile()) {                                          
			return null;                                                  
		}                                                                 
		List<Wall> walls = definition.getWalls();
		if (walls.isEmpty()) {
			return null;
		}
		
		List<Double> hCoordsList = new ArrayList<Double>(walls.get(0).getxCoords());
		List<Double> vCoordsList = new ArrayList<Double>(walls.get(0).getyCoords());
		Collections.sort(hCoordsList);
		Collections.sort(vCoordsList);
		Collections.reverse(vCoordsList);
		if (hCoordsList.size() > 1 && vCoordsList.size() > 1) {
//			Image commonImg = imageRegistry.get(imgFile.getAbsolutePath());
//			if (commonImg == null) {
//				try (BufferedInputStream input = new BufferedInputStream(new FileInputStream(imgFile))) {
//					ImageData[] data = new ImageLoader().load(input);
//					commonImg = new Image(Display.getDefault(), data[0]);
//					imageRegistry.put(imgFile.getAbsolutePath(), commonImg);
//				} catch (Exception e) {
//					Activator.log(e);
//					return null;
//				} 
//			}
			ImageIcon icon = new ImageIcon(imgFile.getAbsolutePath());
			int width = icon.getIconWidth();
			int height = icon.getIconHeight();
			
			int srcX = (int) Math.round(hCoordsList.get(0) * width);
			int w = (int) Math.round(hCoordsList.get(hCoordsList.size() - 1) * width - srcX);
			
			int srcY = (int) Math.round((1.0 - vCoordsList.get(0)) * height);
			int h = (int) Math.round((1.0 - vCoordsList.get(vCoordsList.size() - 1)) * height - srcY);
			
			final BufferedImage destImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			final Graphics2D g2d = (Graphics2D) destImage.getGraphics();
		    g2d.drawImage(icon.getImage(), 0, 0, w, h, srcX, srcY, w + srcX, h + srcY, null);
		    g2d.setColor(Color.RED);
		    
		    for (int i = 1; i < hCoordsList.size() - 1; i++) {
		    	int xCoord = (int) Math.round(hCoordsList.get(i) * width) - srcX;
				g2d.drawLine(xCoord,0,xCoord,h);
			}
		    
		    for (int i = 1; i <vCoordsList.size() - 1; i++) {
		    	int yCoord = (int) Math.round((1.0 - vCoordsList.get(i)) * height) - srcY;
				g2d.drawLine(0,yCoord,width,yCoord);
			}
		    g2d.dispose();
//		    imageRegistry.put(facadeFile.getAbsolutePath(), destImage);
		    return destImage;
		}
		return null;
		
	}
}
