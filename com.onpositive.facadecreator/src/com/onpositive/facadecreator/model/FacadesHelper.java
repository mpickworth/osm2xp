package com.onpositive.facadecreator.model;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

public class FacadesHelper {
	public static Image getPreviewImage(File facadeFile) {
		Image img = imageRegistry.get(facadeFile.getAbsolutePath());
		if (img != null) {
			return img;
		}
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
			Image commonImg = imageRegistry.get(imgFile.getAbsolutePath());
			if (commonImg == null) {
				try (BufferedInputStream input = new BufferedInputStream(new FileInputStream(imgFile))) {
					ImageData[] data = new ImageLoader().load(input);
					commonImg = new Image(Display.getDefault(), data[0]);
					imageRegistry.put(imgFile.getAbsolutePath(), commonImg);
				} catch (Exception e) {
					Activator.log(e);
					return null;
				} 
			}
			Rectangle bounds = commonImg.getBounds();
			int srcX = (int) Math.round(hCoordsList.get(0) * bounds.width);
			int w = (int) Math.round(hCoordsList.get(hCoordsList.size() - 1) * bounds.width - srcX);
			
			int srcY = (int) Math.round((1.0 - vCoordsList.get(0)) * bounds.height);
			int h = (int) Math.round((1.0 - vCoordsList.get(vCoordsList.size() - 1)) * bounds.height - srcY);
			
			final Image destImage = new Image(Display.getDefault(), w, h);

		    final GC g = new GC(destImage);
		    g.drawImage(commonImg, srcX, srcY, w, h, 0, 0, w, h);
		    g.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
		    
		    for (int i = 1; i < hCoordsList.size() - 1; i++) {
		    	int xCoord = (int) Math.round(hCoordsList.get(i) * bounds.width) - srcX;
				g.drawLine(xCoord,0,xCoord,h);
			}
		    
		    for (int i = 1; i <vCoordsList.size() - 1; i++) {
		    	int yCoord = (int) Math.round((1.0 - vCoordsList.get(i)) * bounds.height) - srcY;
				g.drawLine(0,yCoord,bounds.width,yCoord);
			}
		    g.dispose();
		    imageRegistry.put(facadeFile.getAbsolutePath(), destImage);
		    return destImage;
		}
		return null;
		
	}
}
