package net.creapage.lessnow;

import java.security.InvalidParameterException;

public class ProjectConf {

	// --
	// -- Fields
	// --

	private String name, charset = "UTF-8", showUpdatedFiles = "always";
	private boolean minify = true, recursive = false;
	private int scanDelayDirS = 10, scanDelayFilesS = 3;
	private int showUpdatedFilesDays = -1;

	// --
	// -- Initialization
	// --

	public ProjectConf(String name) {
		this.name = name;
	}

	public ProjectConf(String name, ProjectConf defConf) {
		this(name);
		if (defConf != null) {
			setCharset(defConf.getCharset());
			setMinify(defConf.isMinify());
			setRecursive(defConf.isRecursive());
			setScanDelayDirS(defConf.getScanDelayDirS());
			setScanDelayFilesS(defConf.getScanDelayFilesS());
			setShowUpdatedFiles(defConf.getShowUpdatedFiles());
		}
	}

	public void setCharset(String charset) {
		if (charset != null)
			this.charset = charset;
	}

	public void setMinify(Boolean minify) {
		if (minify != null)
			this.minify = minify;
	}

	public void setRecursive(Boolean recursive) {
		if (recursive != null)
			this.recursive = recursive;
	}

	public void setScanDelayDirS(Integer scanDelayDirS) {
		if (scanDelayDirS != null)
			this.scanDelayDirS = scanDelayDirS;
	}

	public void setScanDelayFilesS(Integer scanDelayFilesS) {
		if (scanDelayFilesS != null)
			this.scanDelayFilesS = scanDelayFilesS;
	}

	public void setShowUpdatedFiles(String showUpdatedFiles) {
		if (showUpdatedFiles == null)
			return;
		if ("always".equals(showUpdatedFiles)) {
			this.showUpdatedFilesDays = -1;
		} else {
			if (!showUpdatedFiles.endsWith("d"))
				throw new InvalidParameterException("Bad parameter \"show-update-files\": " + showUpdatedFiles);
			try {
				this.showUpdatedFilesDays = Integer.parseInt(showUpdatedFiles.substring(0, showUpdatedFiles.length() - 1));
				if (showUpdatedFilesDays < 0)
					throw new InvalidParameterException("Bad parameter \"show-update-files\": " + showUpdatedFiles
							+ " (positive number required, or \"always\")");
			} catch (NumberFormatException e) {
				throw new InvalidParameterException("Bad parameter \"show-update-files\": " + showUpdatedFiles);
			}
		}
		this.showUpdatedFiles = showUpdatedFiles;
	}

	// --
	// -- Public
	// --

	public String getName() {
		return name;
	}

	public String getCharset() {
		return charset;
	}

	public boolean isMinify() {
		return minify;
	}

	public boolean isRecursive() {
		return recursive;
	}

	public int getScanDelayDirS() {
		return scanDelayDirS;
	}

	public int getScanDelayFilesS() {
		return scanDelayFilesS;
	}

	public String getShowUpdatedFiles() {
		return showUpdatedFiles;
	}

	/** @return -1 means "always" */
	public int getShowUpdatedFilesDays() {
		return showUpdatedFilesDays;
	}
}
