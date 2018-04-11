package com.osm2xp.translators.impl;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.osm2xp.gui.Activator;
import com.osm2xp.model.osm.OsmPolygon;
import com.osm2xp.translators.ITranslationListener;
import com.osm2xp.utils.helpers.GuiOptionsHelper;

import math.geom2d.Box2D;
import math.geom2d.Point2D;

/**
 * 
 * @author 32kda
 * 
 * <b>Important</b> x and y should be switched while obtaining, since y should be latitude, x - longtitude. 
 * Otherwise map appears rotated and squished (because of bad longtitude scaling)
 * Seems to be a bug in osm2xp code
 */
public class ImageDebugTranslationListener implements ITranslationListener {
	
	private static final int IMGXIZE_X = 2048;
	private static final int IMGXIZE_Y = 2048;
	BufferedImage image = new BufferedImage(IMGXIZE_X,IMGXIZE_Y, BufferedImage.TYPE_INT_ARGB);
	Graphics2D g2d = (Graphics2D) image.getGraphics();
	double baseX = Double.MIN_VALUE;
	double baseY = Double.MIN_VALUE;
	double latScale = 111000; //1 degree ~ 111km, but need recals for lat/long
	double longScale = latScale;
	
	public ImageDebugTranslationListener() {
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0,0,IMGXIZE_X, IMGXIZE_Y);
	}

	@Override
	public void processBuilding(OsmPolygon polygon, Integer facade) {
		checkSetBase(polygon);
		g2d.setColor(Color.RED);
		Polygon drawPoly = calculateDrawPoly(polygon);
		if (drawPoly != null) {
			g2d.fillPolygon(drawPoly);
		}
	}

	protected Polygon calculateDrawPoly(OsmPolygon polygon) {
		Polygon drawPoly = null;
		Point2D center = polygon.getCenter();
		int drawCenterX = (int)(longScale * (center.y - baseX));
		int drawCenterY = (int)(latScale * (center.x - baseY));
		if (drawCenterX >= 0 && drawCenterX < IMGXIZE_X && drawCenterY >= 0 && drawCenterY < IMGXIZE_Y) {
			Shape awtShape = polygon.getPolygon().getAsAWTShape();
			Point2D[] pointArray = polygon.getPolygon().getPointArray();
			drawPoly = new Polygon();
			for (int i = 0; i < pointArray.length; i++) {
				drawPoly.addPoint( (int)(longScale * (pointArray[i].y - baseX)), IMGXIZE_Y - (int) (latScale * (pointArray[i].x - baseY)));
			}
		}
		return drawPoly;
	}

	protected void checkSetBase(OsmPolygon polygon) {
		if (baseX == Double.MIN_VALUE) {
			Box2D boundingBox = polygon.getPolygon().getBoundingBox();
			baseX =  boundingBox.getMinY();
			baseY =  boundingBox.getMinX();
			longScale = latScale * Math.cos(Math.toRadians(baseY));
		}
	}

	@Override
	public void process3dObject(OsmPolygon polygon) {
		checkSetBase(polygon);

	}

	@Override
	public void processStreetLights(OsmPolygon polygon) {
		checkSetBase(polygon);

	}

	@Override
	public void processForest(OsmPolygon polygon) {
		checkSetBase(polygon);
		g2d.setColor(Color.GREEN);
		Polygon drawPoly = calculateDrawPoly(polygon);
		if (drawPoly != null) {
			g2d.fillPolygon(drawPoly);
		}
	}

	@Override
	public void complete() {
		g2d.dispose();
		try {
			File inputFile = new File(GuiOptionsHelper.getOptions()
					.getCurrentFilePath());
			ImageIO.write(image, "png", getTestPngName(inputFile));
		} catch (IOException e) {
			Activator.log(e);
		}
	}

	protected File getTestPngName(File inputFile) {
		int i = 0;
		File curFile;
		while((curFile = new File(inputFile.getParentFile(), GuiOptionsHelper.getSceneName() + i + ".png")).exists()) {
			i++;
		}
		return curFile;
	}

}
