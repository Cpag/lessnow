package net.creapage.lessnow;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class DirProject {

	// --
	// -- Fields
	// --

	private final String WATCH_LOCK = "WATCH_LOCK";
	private File dir;
	private ProjectConf conf;
	private CompilationReport report;
	private FilesWatcher filesWatcher;
	private Map<String, LessFileStatus> mapLessFileStatusByBaseName = new HashMap<String, LessFileStatus>();
	private List<File> ignoredList = new ArrayList<File>();
	private File fIgnore;
	private Timer scanDirTimer;
	private CompilationProcess compilProc;

	// --
	// -- Initialization
	// --

	public DirProject(File dir, ProjectConf conf, final CompilationReport report, CompilationProcess compilProc) {
		if (!dir.isDirectory())
			throw new BugError("Parameter must be a directory here: " + dir.getAbsolutePath());
		this.dir = dir;
		this.conf = conf;
		this.report = report;
		this.compilProc = compilProc;
		this.fIgnore = new File(dir, ".lessnow_ignore");
		// - Start files watcher
		filesWatcher = new FilesWatcher(conf.getScanDelayFilesS());
		filesWatcher.addFilesListener(new FilesWatcherListener() {
			@Override
			public void fileModified(FilesWatcherEvent e) {
				List<LessFileStatus> lst = new ArrayList<LessFileStatus>();
				synchronized (WATCH_LOCK) {
					for (File file : e.getFiles()) {
						String lfsKey = getPathAndBaseName(file);
						LessFileStatus lfs = mapLessFileStatusByBaseName.get(lfsKey);
						if (lfs != null) {
							lst.add(lfs);
							if (lfs.isDeleted())
								mapLessFileStatusByBaseName.remove(lfsKey);
						}
					}
				}
				// - Not synchronized in order to avoid dead locks
				boolean refreshGui = false;
				for (LessFileStatus lfs : lst) {
					if (lfs.isDeleted()) {
						report.remove(lfs);
						filesWatcher.removeFile(lfs.getFrom());
						filesWatcher.removeFile(lfs.getTo());
					} else if (!lfs.isUpToDate()) {
						refreshGui = true;
						compile(lfs);
					}
				}
				if (refreshGui)
					report.refreshAll();
			}
		});
		// - Scan files
		initIgnored();
		scanProjectLessFiles();
		// - Directory watcher
		scanDirTimer = new Timer();
		scanDirTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				scanProjectLessFiles();
			}
		}, 10000, conf.getScanDelayDirS() * 1000);
	}

	// --
	// -- Public
	// --

	public String getName() {
		return conf.getName();
	}

	public void compile(LessFileStatus lfs) {
		compilProc.addToQueue(new CompilationTask(lfs, conf.getCharset(), conf.isMinify()));
	}

	public void ignoreFile(final LessFileStatus lfs) {
		synchronized (WATCH_LOCK) {
			filesWatcher.removeFile(lfs.getFrom());
			filesWatcher.removeFile(lfs.getTo());
			try {
				if (!fIgnore.exists())
					fIgnore.createNewFile();
				BufferedWriter bw = new BufferedWriter(new FileWriter(fIgnore, true));
				bw.append(lfs.getFrom().getAbsolutePath());
				bw.newLine();
				bw.close();
			} catch (IOException e) {
				System.err.print(e.getMessage());
			}
		}
	}

	// --
	// -- Private
	// --

	private void initIgnored() {
		if (!fIgnore.isFile())
			return;
		String line;
		try {
			BufferedReader br = new BufferedReader(new FileReader(fIgnore));
			while ((line = br.readLine()) != null) {
				ignoredList.add(new File(line));
			}
			br.close();
		} catch (FileNotFoundException e) {
			// checked with ".exists()"
		} catch (IOException e) {
			System.err.print("Warning: I/O exception when reading " + fIgnore);
		}
	}

	private void scanProjectLessFiles() {
		List<LessFileStatus> lstAddedLfs = new ArrayList<LessFileStatus>();
		synchronized (WATCH_LOCK) {
			if (conf.isRecursive())
				recursiveScanDir("", dir, lstAddedLfs);
			else
				scanDir(dir, lstAddedLfs);
		}
		// - Not synchronized in order to avoid dead locks
		for (LessFileStatus lfs : lstAddedLfs)
			report.add(lfs);
	}

	private void scanDir(File dir, List<LessFileStatus> lstAddedLfs) {
		if (!dir.isDirectory())
			throw new BugError("Bad directory: " + dir);
		File[] subf = dir.listFiles();
		if (subf != null) {
			for (File f : subf) {
				if (f.isFile())
					scanFile(f.getName(), f, lstAddedLfs);
			}
		}
	}

	private void recursiveScanDir(String relativePath, File dirOrFile, List<LessFileStatus> lstAddedLfs) {
		if (dirOrFile.isDirectory()) {
			File[] subf = dirOrFile.listFiles();
			if (subf != null) {
				for (File current : subf) {
					String subRelPath = relativePath.isEmpty() ? current.getName() : relativePath + '/' + current.getName();
					recursiveScanDir(subRelPath, current, lstAddedLfs);
				}
			}
		} else if (dirOrFile.isFile())
			scanFile(relativePath, dirOrFile, lstAddedLfs);
	}

	private void scanFile(String relativePath, File f, List<LessFileStatus> lstAddedLfs) {
		if (!f.isFile())
			throw new BugError("Bad file: " + f);
		if (!f.getName().endsWith(".less") || ignoredList.contains(f))
			return;
		String lfsKey = getPathAndBaseName(f);
		if (mapLessFileStatusByBaseName.containsKey(lfsKey))
			return; // already added
		File to = new File(f.getAbsolutePath().substring(0, f.getAbsolutePath().lastIndexOf(".less")) + ".css");
		LessFileStatus lfs = new LessFileStatus(this, f, to, relativePath, conf.getShowUpdatedFilesDays());
		mapLessFileStatusByBaseName.put(lfsKey, lfs);
		filesWatcher.addFile(f);
		filesWatcher.addFile(to);
		lstAddedLfs.add(lfs);
		if (!lfs.isUpToDate())
			compile(lfs);
	}

	// --
	// -- Private tools
	// --

	private static String getPathAndBaseName(File f) {
		String s = f.getPath();
		int extIndex = s.lastIndexOf('.');
		if (extIndex > 0)
			return s.substring(0, extIndex);
		return s;
	}
}
