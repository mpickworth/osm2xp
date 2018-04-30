package com.osm2xp.utils.helpers;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.preferences.InstanceScope;

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
			boolean fence =  "0".equals(values.get("RING")) || "1".equals(values.get("TWO_SIDED"));
			if (fence) {
				facade.setBarrierType(BarrierType.FENCE);
				facade.setResidential(false);
				facade.setIndustrial(false);
				facade.setCommercial(false);
			}
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

}
