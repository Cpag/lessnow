package net.creapage.lessnow;

import java.io.File;

public class FilesWatcherEvent {

	private File[] files;

	public FilesWatcherEvent(File[] files) {
		this.files = files;
	}

	public File[] getFiles() {
		return files;
	}
}
