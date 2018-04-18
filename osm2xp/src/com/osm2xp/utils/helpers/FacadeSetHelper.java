package com.osm2xp.utils.helpers;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.osm2xp.exceptions.Osm2xpBusinessException;
import com.osm2xp.exceptions.Osm2xpTechnicalException;
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
				Facade facade = new Facade();
				facade.setFile(facadeFile);
				facadeSet.getFacades().add(facade);
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

}
