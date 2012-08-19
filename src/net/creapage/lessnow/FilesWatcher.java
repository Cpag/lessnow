package net.creapage.lessnow;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FilesWatcher {

	// --
	// -- Fields
	// --

	private final String LOCK_WATCHERS = "LOCK_WATCHERS";
	private long period;
	private Timer timer;
	private List<FileWatcher> watchers = new ArrayList<FileWatcher>();
	private List<FilesWatcherListener> listeners = new LinkedList<FilesWatcherListener>();

	// --
	// -- Initialization
	// --

	public FilesWatcher(int periodInSec) {
		period = periodInSec * 1000;
		timer = new Timer();

		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				List<File> hasChanged = new ArrayList<File>();
				synchronized (LOCK_WATCHERS) {
					for (FileWatcher watcher : watchers) {
						if (watcher.hasChanged())
							hasChanged.add(watcher.getFile());
					}
				}
				// - Not synchronized in order to avoid dead locks
				if (!hasChanged.isEmpty()) {
					File[] changedFiles = new File[hasChanged.size()];
					changedFiles = hasChanged.toArray(changedFiles);
	
					for (FilesWatcherListener t : listeners) {
						t.fileModified(new FilesWatcherEvent(changedFiles));
					}
				}
			}
		}, 10000, period);
	}

	// --
	// -- Public
	// --

	public void addFile(File file) {
		synchronized (LOCK_WATCHERS) {
			watchers.add(new FileWatcher(file));
		}
	}

	public void removeFile(File file) {
		synchronized (LOCK_WATCHERS) {
			int i = 0;
			while (i < watchers.size()) {
				if (watchers.get(i).getFile().equals(file)) {
					watchers.remove(i);
					break;
				}
				i++;
			}
		}
	}

	// --
	// -- Public - Listeners
	// --

	public void addFilesListener(FilesWatcherListener listener) {
		listeners.add(listener);
	}

	public boolean removeFilesListener(FilesWatcherListener listener) {
		return listeners.remove(listener);
	}

	// --
	// -- Private class FileWatcher
	// --

	private static class FileWatcher {
	
		private boolean exists;
		private long timestamp;
		private File file;
	
		public FileWatcher(File file) {
			this.file = file;
			this.exists = file.exists();
			if (exists)
				this.timestamp = file.lastModified();
		}
	
		public boolean hasChanged() {
			if (!file.exists()) {
				if (exists) {
					exists = false;
					return true;
				}
				return false;
			}
			if (!exists) {
				exists = true;
				this.timestamp = file.lastModified();
				return true;
			}
			long current = file.lastModified();
			if (current != timestamp) {
				timestamp = current;
				return true;
			}
			return false;
		}

		public File getFile() {
			return file;
		}
	}
}