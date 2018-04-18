package com.osm2xp.translators.xp10;

import com.osm2xp.translators.IPolyHandler;
import com.osm2xp.writers.IWriter;

public abstract class XP10WritingTranslator implements IPolyHandler{

	protected IWriter writer;

	public XP10WritingTranslator() {
		super();
	}

	public XP10WritingTranslator(IWriter writer) {
		this.writer = writer;
	}

}