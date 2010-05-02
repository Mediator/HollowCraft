package org.opencraft.server.io;

import java.io.IOException;
import org.opencraft.server.model.Level;
import java.io.File;
import org.slf4j.*;

/**
 * A Class to handle loading and saving of Levels
 * @author Adam Liszka
 */
public final class LevelManager {

	private static final Logger logger = LoggerFactory.getLogger(LevelManager.class);

	/**
	 * Default private constructor.
	 */
	private LevelManager() { /* empty */ }

	public static int getLatestVersion(String mapName) {
		File base = new File("data/maps/"+mapName+"/");
		int highest = -1;
		if (base.exists()) {
			for(File f : base.listFiles()) {
				String baseName = f.getName().replace(mapName+"-", "");
				String[] tokens = baseName.split("\\.");
				try {
				int value = Integer.parseInt(tokens[0]);
				if (value > highest)
					highest = value;
				} catch (NumberFormatException e) {
					logger.info("Invalid file name {}.", f);
				}
			}
		}
		return highest;
	}

	private static File getLatestFile(String mapName) {
		return new File("data/maps/"+mapName+"/"+mapName+"-"+getLatestVersion(mapName)+".mclevel");
	}

	private static File getNextFile(String mapName) {
		File base = new File("data/maps/"+mapName);
		if (!base.exists())
			base.mkdirs();
		return new File("data/maps/"+mapName+"/"+mapName+"-"+(getLatestVersion(mapName)+1)+".mclevel");
	}

	/**
	 * Determines the appropriate file type and loads it.
	 * @param filename The name of the file to unzip
	 * @return The Level
	 */
	public static Level load(String mapName) {
		File mapFile = getLatestFile(mapName);

		try {
			return NBTFileHandler.load(mapFile.getPath());
		} catch (IOException e) {
			logger.debug("IOException loading {}" + mapFile, e);
		}

		try {
			return BINFileHandler.load(mapFile.getPath());
		} catch (IOException e) {
			logger.debug("IOException loading {}", mapFile, e);
		}

		logger.info("Generating level instead of loading.");
		Level lvl = new Level();
		lvl.generateLevel();
		lvl.setName(mapName);
		return lvl;
	}

	/**
	 * Determines the appropriate file type and saves the Level.
	 * @param filename The name of the file to unzip
	 * @return The Level
	 */
	public static void save(Level lvl) {
		// TODO: Boolean for saving nonstandard info should come from server config file
		NBTFileHandler.save(lvl, getNextFile(lvl.getName()).getPath(), true);
	}
}
