package com.osm2xp.utils.helpers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.Files;
import com.osm2xp.exceptions.Osm2xpBusinessException;
import com.osm2xp.exceptions.Osm2xpTechnicalException;
import com.osm2xp.gui.Activator;
import com.osm2xp.model.facades.BarrierType;
import com.osm2xp.model.facades.Facade;
import com.osm2xp.model.facades.FacadeSet;
import com.osm2xp.utils.FilesUtils;

/**
 * FacadeSetHelper.
 * 
 * @author Benjamin Blanchet
 * 
 */
public class FacadeSetHelper {

	public static final String FACADE_SET_DESCRIPTOR_FILE_NAME = "osm2xpFacadeSetDescriptor.xml";
	
	private static ImageRegistry imageRegistry = new ImageRegistry(); 

	/**
	 * @param file
	 * @param osm2XpProject
	 * @throws Osm2xpBusinessException
	 */
	public static void saveFacadeSet(FacadeSet facadeSet, String facadeSetFolder)
			throws Osm2xpBusinessException {
		File facadeSetFile = new File(facadeSetFolder + File.separator
				+ FACADE_SET_DESCRIPTOR_FILE_NAME);
		try {
			JAXBContext jc = JAXBContext.newInstance(FacadeSet.class
					.getPackage().getName());
			Marshaller marshaller = jc.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
					Boolean.TRUE);

			marshaller.marshal(facadeSet, facadeSetFile);
		} catch (JAXBException e) {
			throw new Osm2xpBusinessException(
					"Error saving facade set descriptor to file "
							+ facadeSetFile.getPath());
		}
	}

	public static FacadeSet getFacadeSet(String facadeSetFolder) {
		File facadeSetFile = new File(facadeSetFolder + File.separator
				+ FACADE_SET_DESCRIPTOR_FILE_NAME);
		if (facadeSetFile.exists()) {
			return loadFacadeSet(facadeSetFile.getPath());
		} else {
			FacadeSet facadeSet = new FacadeSet();

			for (String facadeFile : FilesUtils
					.listFacadesFiles(facadeSetFolder)) {
				Facade facade = FacadeSetHelper.generateDefaultDescriptor(new File(facadeSetFolder, facadeFile));
				if (facade != null) {
					facade.setFile(facadeFile);
					facadeSet.getFacades().add(facade);
				}
			}
			return facadeSet;
		}
	}

	/**
	 * @param filePath
	 * @throws Osm2xpBusinessException
	 */
	public static FacadeSet loadFacadeSet(String filePath) {
		FacadeSet result = new FacadeSet();
		File facadeSetFile = new File(filePath);
		try {
			JAXBContext jc = JAXBContext.newInstance(FacadeSet.class
					.getPackage().getName());
			Unmarshaller u = jc.createUnmarshaller();
			result = (FacadeSet) u.unmarshal(facadeSetFile);
		} catch (JAXBException e) {
			throw new Osm2xpTechnicalException("Error loading facadeSet "
					+ filePath + "\n" + e.getCause().getMessage());
		}
		return result;
	}
	
	public static Facade generateDefaultDescriptor(File facFile) {
		if (facFile.isFile()) {
			Map<String, String> values = getValuesFromFac(facFile);
			Facade facade = new Facade();
			facade.setFile(facFile.getName());
			boolean fence =  "0".equals(values.get("RING")) || "1".equals(values.get("TWO_SIDED"));
			if (fence) {
				facade.setBarrierType(BarrierType.FENCE);
			}
			//Enable it for all building types by default
			facade.setResidential(!fence);
			facade.setIndustrial(!fence);
			facade.setCommercial(!fence);
			String slope = values.get("ROOF_SLOPE");
			if (!StringUtils.isEmpty(slope)) {
				try {
					double slopedVal = Double.parseDouble(slope);
					facade.setSloped(slopedVal > 0);
				} catch (Exception e) {
					// Ignore
				}
			}
			double levelHeight = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).getDouble("levelHeight", 3);
			String minHeightStr = values.get("FLOORS_MIN");
			if (!StringUtils.isEmpty(minHeightStr)) {
				try {
					double val = Double.parseDouble(minHeightStr);
					facade.setMinHeight((int) Math.round(val * levelHeight));
				} catch (Exception e) {
					// Ignore
				}
			}
			String maxHeightStr = values.get("FLOORS_MAX");
			if (!StringUtils.isEmpty(maxHeightStr)) {
				try {
					double val = Double.parseDouble(maxHeightStr);
					facade.setMaxHeight((int) Math.round(val * levelHeight));
				} catch (Exception e) {
					// Ignore
				}
			}
			return facade;
		}
		return null;
	}
	
	private static Map<String, String> getValuesFromFac(File facadeFile) {
		Map<String, String> result = new HashMap<String, String>();
		try {
			List<String> lines = Files.readLines(facadeFile, Charset.forName("utf-8"));
			for (String string : lines) {
				string = string.trim();
				int idx = string.indexOf(' ');
				if (idx > 0) {
					String key = string.substring(0, idx).trim();
					String value = string.substring(idx, string.length()).trim();
					if (!key.isEmpty() && !value.isEmpty()) {
						result.put(key, value);
					}
				}
			}
		} catch (IOException e) {
			Activator.log(e);
		}
		return result;
	}
	
	private static Multimap<String, String> getMultiValuesFromFac(File facadeFile) {
		Multimap<String, String> result = HashMultimap.create();
		try {
			List<String> lines = Files.readLines(facadeFile, Charset.forName("utf-8"));
			for (String string : lines) {
				string = string.trim();
				int idx = string.indexOf(' ');
				if (idx > 0) {
					String key = string.substring(0, idx).trim();
					String value = string.substring(idx, string.length()).trim();
					if (!key.isEmpty() && !value.isEmpty()) {
						result.put(key, value);
					}
				}
			}
		} catch (IOException e) {
			Activator.log(e);
		}
		return result;
	}
	
	public static Image getPreviewImage(File facadeFile) {
		Image img = imageRegistry.get(facadeFile.getAbsolutePath());
		if (img != null) {
			return img;
		}
		Multimap<String, String> valuesMap = getMultiValuesFromFac(facadeFile);
		String imgFileName = valuesMap.get("TEXTURE").iterator().next();
		File imgFile = new File(facadeFile.getParentFile(), imgFileName);
		if (!imgFile.isFile()) {
			return null;
		}
		Set<Double> hCoords = new HashSet<Double>();
		Set<Double> vCoords = new HashSet<Double>();
		
		valuesMap.get("BOTTOM").stream().forEach(str -> vCoords.add(Double.parseDouble(str)));
		valuesMap.get("MIDDLE").stream().forEach(str -> vCoords.add(Double.parseDouble(str)));
		valuesMap.get("TOP").stream().forEach(str -> vCoords.add(Double.parseDouble(str)));
		
		valuesMap.get("LEFT").stream().forEach(str -> hCoords.add(Double.parseDouble(str)));
		valuesMap.get("CENTER").stream().forEach(str -> hCoords.add(Double.parseDouble(str)));
		valuesMap.get("RIGHT").stream().forEach(str -> hCoords.add(Double.parseDouble(str)));
		List<Double> hCoordsList = new ArrayList<Double>(hCoords);
		List<Double> vCoordsList = new ArrayList<Double>(vCoords);
		Collections.sort(hCoordsList);
		Collections.sort(vCoordsList);
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
			int w = (int) Math.round(hCoordsList.get(hCoordsList.size() - 1) * bounds.width);
			
			int srcY = (int) Math.round(vCoordsList.get(0) * bounds.width);
			int h = (int) Math.round(vCoordsList.get(vCoordsList.size() - 1) * bounds.height);
			
			final Image destImage = new Image(Display.getDefault(), w, h);

		    final GC g = new GC(destImage);
		    g.drawImage(commonImg, srcX, srcY, w, h, 0, 0, w, h);
		    g.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
		    
		    for (int i = 1; i < hCoordsList.size() - 1; i++) {
		    	int xCoord = (int) Math.round(hCoordsList.get(i) * bounds.width) - srcX;
				g.drawLine(xCoord,0,xCoord,h);
			}
		    
		    for (int i = 1; i <vCoordsList.size() - 1; i++) {
		    	int yCoord = (int) Math.round(vCoordsList.get(i) * bounds.height) - srcX;
				g.drawLine(0,yCoord,bounds.width,yCoord);
			}
		    g.dispose();
		    imageRegistry.put(facadeFile.getAbsolutePath(), destImage);
		    return destImage;
		}
		return null;
		
	}

}
