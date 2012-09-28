package net.creapage.lessnow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.InvalidParameterException;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class LessNow {

	public final static String VERSION = "0.3";

	public static void main(String[] args) {
		try {
			Gui gui = new Gui();
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception e) {
				// using fallback >> crossplatform look and feel.
			} finally {
				SwingUtilities.updateComponentTreeUI(gui);
			}
			config(args, gui);
			gui.setVisible(true);
		} catch (BugError e) {
			e.printStackTrace();
		}
	}

	private static void config(String[] args, Gui gui) {
		if (args.length == 0) {
			gui.setConfigLocation(null, null);
			return; // no configuration
		}
		if (args.length != 2 || !"-conf".equals(args[0]))
			printHelpThenExit();
		JSONParser parser = new JSONParser();
		try {
			JSONObject json = (JSONObject) parser.parse(new FileReader(args[1]));
			initGuiConfig(json, gui);
			ProjectConf defConf = createProjectDefaults(json);
			gui.setDefaultProjectConf(defConf);
			startProjects(json, defConf, gui);
			gui.showGui();
		} catch (FileNotFoundException e) {
			System.err.println("File not found: " + args[1]);
			System.exit(1);
		} catch (IOException e) {
			System.err.println("I/O error when reading file: " + args[1]);
			System.exit(1);
		} catch (ParseException e) {
			System.err.println("Bad JSON syntax: " + e.getMessage());
			System.exit(1);
		} catch (BadConfig e) {
			System.err.println("Bad configuration: " + e.getMessage());
			System.exit(1);
		} catch (InvalidParameterException e) {
			System.err.println("Bad configuration: " + e.getMessage());
			System.exit(1);
		}
	}

	private static void initGuiConfig(JSONObject json, Gui gui) throws BadConfig {
		JSONObject guiConfig = toJSONObject("gui-config", json.get("gui-config"), false);
		try {
			if (guiConfig == null) {
				gui.setConfigLocation(null, null);
				return;
			}
			gui.setConfigSize(toInteger("width", guiConfig.get("width"), false), toInteger("height", guiConfig.get("height"), false));
			gui.setConfigLocation(toString("location-x", guiConfig.get("location-x"), false), toString("location-y",
					guiConfig.get("location-y"), false));
		} catch (InvalidParameterException e) {
			throw new BadConfig(e.getMessage());
		}
	}

	private static ProjectConf createProjectDefaults(JSONObject json) throws BadConfig {
		JSONObject def = toJSONObject("project-defaults", json.get("project-defaults"), false);
		ProjectConf conf = new ProjectConf("project-defaults");
		if (def == null)
			return conf;
		conf.setCharset(toString("project-defaults/charset", def.get("charset"), false));
		conf.setMinify(toBoolean("project-defaults/minify", def.get("minify"), false));
		conf.setRecursive(toBoolean("project-defaults/recursive", def.get("recursive"), false));
		conf.setScanDelayDirS(toInteger("project-defaults/scan-delay-dir-s", def.get("scan-delay-dir-s"), false));
		conf.setScanDelayFilesS(toInteger("project-defaults/scan-delay-files-s", def.get("scan-delay-files-s"), false));
		conf.setShowUpdatedFiles(toString("project-defaults/show-updated-files", def.get("show-updated-files"), false));
		conf.setAutoAddDirAsProjects(toBoolean("project-defaults/auto-add-dir-as-projects", def.get("auto-add-dir-as-projects"), false));
		conf.setAutoAddDirRegexp(toString("project-defaults/auto-add-dir-regexp", def.get("auto-add-dir-regexp"), false));
		conf.setAutoAddDirNameCount(toInteger("project-defaults/auto-add-dir-name-count", def.get("auto-add-dir-name-count"), false));
		return conf;
	}

	private static void startProjects(JSONObject json, ProjectConf defConf, Gui gui) throws BadConfig {
		//JSONArray paths = (JSONArray) json.get("watch");
		JSONObject projects = toJSONObject("projects", json.get("projects"), true);
		for (Object o : projects.keySet()) {
			String name = (String) o;
			JSONObject p = toJSONObject(name, projects.get(name), true);
			String path = (String) p.get("path");
			File fPath = new File(path);
			if (!fPath.isDirectory()) {
				System.err.println("Warning - Bad project's directory path: " + path);
				continue;
			}
			ProjectConf conf = new ProjectConf(name, defConf);
			conf.setCharset(toString(name + "/charset", p.get("charset"), false));
			conf.setMinify(toBoolean(name + "/minify", p.get("minify"), false));
			conf.setRecursive(toBoolean(name + "/recursive", p.get("recursive"), false));
			conf.setScanDelayDirS(toInteger(name + "/scan-delay-dir-s", p.get("scan-delay-dir-s"), false));
			conf.setScanDelayFilesS(toInteger(name + "/scan-delay-files-s", p.get("scan-delay-files-s"), false));
			conf.setShowUpdatedFiles(toString(name + "/show-updated-files", p.get("show-updated-files"), false));
			conf.setAutoAddDirAsProjects(toBoolean(name + "/auto-add-dir-as-projects", p.get("auto-add-dir-as-projects"), false));
			conf.setAutoAddDirRegexp(toString(name + "/auto-add-dir-regexp", p.get("auto-add-dir-regexp"), false));
			conf.setAutoAddDirNameCount(toInteger(name + "/auto-add-dir-name-count", p.get("auto-add-dir-name-count"), false));
			gui.addLessProject(fPath, conf);
		}
	}

	private static Integer toInteger(String paramName, Object o, boolean required) throws BadConfig {
		if (o == null) {
			if (required)
				throw new BadConfig("Parameter \"" + paramName + "\" is required");
			return null;
		}
		if (!(o instanceof Number))
			throw new BadConfig("In parameter \"" + paramName + "\", value \"" + o + "\" should be an integer");
		return ((Number) o).intValue();
	}

	private static Boolean toBoolean(String paramName, Object o, boolean required) throws BadConfig {
		if (o == null) {
			if (required)
				throw new BadConfig("Parameter \"" + paramName + "\" is required");
			return null;
		}
		if (!(o instanceof Boolean))
			throw new BadConfig("In parameter \"" + paramName + "\", value \"" + o + "\" should be a boolean");
		return (Boolean) o;
	}

	private static String toString(String paramName, Object o, boolean required) throws BadConfig {
		if (o == null) {
			if (required)
				throw new BadConfig("Parameter \"" + paramName + "\" is required");
			return null;
		}
		if (!(o instanceof String))
			throw new BadConfig("In parameter \"" + paramName + "\", value \"" + o + "\" should be a string");
		return (String) o;
	}

	private static JSONObject toJSONObject(String paramName, Object o, boolean required) throws BadConfig {
		if (o == null) {
			if (required)
				throw new BadConfig("Parameter \"" + paramName + "\" is required");
			return null;
		}
		if (!(o instanceof JSONObject))
			throw new BadConfig("In parameter \"" + paramName + "\", value \"" + o + "\" should be an object");
		return (JSONObject) o;
	}

	private static void printHelpThenExit() {
		System.out.println("Syntax: -conf \"path-to-config-file.json\"");
		try {
			InputStream stream = LessNow.class.getResourceAsStream("/res/help-syntax.txt");
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
			String line;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}
		} catch (Exception e) {
			System.err.println("Problem when reading the help file: " + e.getMessage());
		}
		System.exit(0);
	}
	
	private static class BadConfig extends Exception {
		private static final long serialVersionUID = 1L;

		public BadConfig(String message) {
			super(message);
		}
	}
}
