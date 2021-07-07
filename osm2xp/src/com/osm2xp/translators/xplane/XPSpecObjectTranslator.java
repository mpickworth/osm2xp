package com.osm2xp.translators.xplane;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.resources.ResourcesPlugin;

import com.osm2xp.model.osm.OsmPolygon;
import com.osm2xp.model.osm.OsmPolyline;
import com.osm2xp.utils.DsfObjectsProvider;
import com.osm2xp.utils.helpers.XplaneOptionsHelper;
import com.osm2xp.writers.IWriter;

import math.geom2d.Point2D;

/**
 * Basic translator for handling "special" objects - by selecting model with most suitable size from list
 * Size could mean different dimensions, most suitable for choosing object o f given type - e.g. height for chimney,
 * diameter or perimeter for cooling tower etc. - override getObjectSize() method to provide size of selected OSM poly 
 * @author 32kda
 *
 */
public abstract class XPSpecObjectTranslator extends XPWritingTranslator {
	
	private class ObjectDef {
		public final int size;
		public final String fileName;
		
		public ObjectDef(String fileName, int size) {
			this.fileName = fileName;
			this.size = size;
		}
	}

	protected DsfObjectsProvider objectsProvider;
	protected List<ObjectDef> objectDefs = new ArrayList<>(); 
	
	public XPSpecObjectTranslator(IWriter writer, DsfObjectsProvider objectsProvider) {
		super(writer);
		this.objectsProvider = objectsProvider;
		String preffix = getObjectFilePreffix();
		if (generationEnabled()) {
			File specObjectsFolder = new File(
					ResourcesPlugin.getWorkspace().getRoot().getLocation() + "/resources/specobjects");
			if (specObjectsFolder.isDirectory()) {
				String[] chimneyFiles = specObjectsFolder.list((parent, name) -> name.toLowerCase().startsWith(preffix) && name.toLowerCase().endsWith(".obj"));
				for (String currentFile : chimneyFiles) {
					int idx = currentFile.lastIndexOf('-');
					int idx2 = currentFile.lastIndexOf('.');
					if (idx > 0 && idx2 > idx) {
						int height = Integer.parseInt(currentFile.substring(idx + 1, idx2));
						objectDefs.add(new ObjectDef(currentFile, height));
					} 
				}
			}
			
		}
	}

	protected abstract boolean generationEnabled();

	@Override
	public boolean handlePoly(OsmPolyline osmPolyline) {
		if (!(osmPolyline instanceof OsmPolygon)) { //We support only polygon-based objects for now. Maybe we should support point-based also
			return false;
		}
		OsmPolygon osmPolygon = (OsmPolygon) osmPolyline;
		if (canProcess(osmPolygon)) {
			int size = getObjectSize(osmPolygon);
			if (size < 0) {
				return false;
			}
			Integer chimneyObjectIdx = objectsProvider.getSpecialObject(getSuitableModelFile(size));
			Point2D center = osmPolygon.getCenter();
			String objStr =  String.format(Locale.ROOT, "OBJECT %1d %2.9f %3.9f 0" + System.getProperty("line.separator"), chimneyObjectIdx, center.x, center.y);
			if (XplaneOptionsHelper.getOptions().isGenerateComments()) {
				String comment = getComment(osmPolygon);
				writer.write(comment);
			}
			writer.write(objStr);
			return true;
		}
		return false;
	}

	protected abstract String getComment(OsmPolygon osmPolygon);

	protected abstract boolean canProcess(OsmPolygon osmPolygon);

	private String getSuitableModelFile(int size) {
		int minDelta = Integer.MAX_VALUE;
		ObjectDef optimal = null;
		for (ObjectDef objectDef : objectDefs) {
			int delta = Math.abs(objectDef.size - size);
			if (delta < minDelta) {
				minDelta = delta;
				optimal = objectDef;
			}
		}
		return optimal.fileName;
	}

	/**
	 * Object size to choose object. Should return -1 if size is too small to add object
	 * @param osmPolygon
	 * @return
	 */
	protected abstract int getObjectSize(OsmPolygon osmPolygon);
	
	protected abstract String getObjectFilePreffix();

	@Override
	public void translationComplete() {
		// Do nothing
		
	}
	
}
