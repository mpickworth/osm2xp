package com.osm2xp.translators.xplane;

import com.osm2xp.translators.IPolyHandler;
import com.osm2xp.writers.IWriter;

public abstract class XPWritingTranslator implements IPolyHandler{

	protected IWriter writer;

	public XPWritingTranslator(IWriter writer) {
		this.writer = writer;
	}
	
	

}