package com.osm2xp.translators.xplane;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.resources.ResourcesPlugin;

import com.osm2xp.constants.Osm2xpConstants;
import com.osm2xp.model.osm.OsmPolygon;
import com.osm2xp.utils.DsfObjectsProvider;
import com.osm2xp.utils.GeomUtils;
import com.osm2xp.utils.helpers.XplaneOptionsHelper;
import com.osm2xp.writers.IWriter;

import math.geom2d.Point2D;

public class XPChimneyTranslator extends XPWritingTranslator {
	
	private class ChimneyDef {
		public final int height;
		public final String fileName;
		
		public ChimneyDef(String fileName, int height) {
			this.fileName = fileName;
			this.height = height;
		}
	}

	protected DsfObjectsProvider objectsProvider;
	protected List<ChimneyDef> chimneyDefs = new ArrayList<>(); 
	
	public XPChimneyTranslator(IWriter writer, DsfObjectsProvider objectsProvider) {
		super(writer);
		this.objectsProvider = objectsProvider;
		if (XplaneOptionsHelper.getOptions().isGenerateChimneys()) {
			File specObjectsFolder = new File(
					ResourcesPlugin.getWorkspace().getRoot().getLocation() + "/resources/specobjects");
			if (specObjectsFolder.isDirectory()) {
				String[] chimneyFiles = specObjectsFolder.list((parent, name) -> name.toLowerCase().startsWith("chimney") && name.toLowerCase().endsWith(".obj"));
				for (String currentFile : chimneyFiles) {
					int idx = currentFile.lastIndexOf('-');
					int idx2 = currentFile.lastIndexOf('.');
					if (idx > 0 && idx2 > idx) {
						int height = Integer.parseInt(currentFile.substring(idx + 1, idx2));
						chimneyDefs.add(new ChimneyDef(currentFile, height));
					} 
				}
			}
			
		}
	}

	@Override
	public boolean handlePoly(OsmPolygon osmPolygon) {
		if (XplaneOptionsHelper.getOptions().isGenerateChimneys() &&
				"chimney".equalsIgnoreCase(osmPolygon.getTagValue(Osm2xpConstants.MAN_MADE_TAG))) {
			int height = getChimneyHeight(osmPolygon);
			Integer chimneyObjectIdx = objectsProvider.getChimneyObject(getSuitableModelFile(height));
			Point2D center = osmPolygon.getCenter();
			String objStr =  String.format(Locale.ROOT, "OBJECT %1d %2.9f %3.9f 0" + System.getProperty("line.separator"), chimneyObjectIdx, center.x, center.y);
			Point2D point = GeomUtils.cleanCoordinatePoint(osmPolygon.getCenter());
			if (XplaneOptionsHelper.getOptions().isGenerateComments()) {
				StringBuilder commentBuilder = new StringBuilder("#Chimney");
				if (osmPolygon.getHeight() != null) {
					commentBuilder.append(" ");
					commentBuilder.append(osmPolygon.getHeight());
					commentBuilder.append("m");
				}
				commentBuilder.append(System.getProperty("line.separator"));
				writer.write(commentBuilder.toString(), point);
			}
			writer.write(objStr, point);
			return true;
		}
		return false;
	}

	private String getSuitableModelFile(int height) {
		int minDelta = Integer.MAX_VALUE;
		ChimneyDef optimal = null;
		for (ChimneyDef chimneyDef : chimneyDefs) {
			int delta = Math.abs(chimneyDef.height - height);
			if (delta < minDelta) {
				minDelta = delta;
				optimal = chimneyDef;
			}
		}
		return optimal.fileName;
	}

	private int getChimneyHeight(OsmPolygon osmPolygon) {
		Integer height = osmPolygon.getHeight();
		if (height == null) {
			return 50; //Default value
		}
		return height;
	}

	@Override
	public void translationComplete() {
		// Do nothing
		
	}
	
}
