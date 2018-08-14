package com.osm2xp.jobs;

import java.io.File;
import java.util.List;

import org.eclipse.core.runtime.jobs.Job;

import com.osm2xp.model.osm.Relation;

public abstract class GenerateJob extends Job {

	protected String family;
	protected transient File currentFile;
	protected transient String folderPath;
	protected transient List<Relation> relationsList;
	
	public GenerateJob(String name, File currentFile, 
			String folderPath, List<Relation> relationsList, String family) {
		super(name);
		this.currentFile = currentFile;
		this.folderPath = folderPath;
		this.relationsList = relationsList;
		this.family = family;
	}

	public boolean belongsTo(Object family) {
		return family.equals(family);
	}

	public String getFamily() {
		return family;
	}

	public void setFamily(String familly) {
		this.family = familly;
	}

}