package org.opencraft.server.io;

import java.io.IOException;
import org.opencraft.server.model.Level;
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
	
	/**
	 * Determines the appropriate file type and loads it.
	 * @param filename The name of the file to unzip
	 * @return The Level
	 */
	public static Level load(String filename) {
		String extension = filename.substring(filename.lastIndexOf(".") + 1);

		if (extension.equalsIgnoreCase("dat") || extension.equalsIgnoreCase("mine")) {
			try {
				return BINFileHandler.load("data/maps/" + filename);
			} catch (IOException e) {
				logger.debug("IOException loading 'data/maps/" + filename + "' : " + e.getMessage());
			}
		//} else if (extension.equalsIgnoreCase("mclevel")) {
		}

		try {
			return NBTFileHandler.load("data/maps/" + filename);
		} catch (IOException e) {
			logger.debug("IOException loading 'data/maps/" + filename + "' : " + e.getMessage());
		}

		logger.info("Generating level instead of loading.");
		Level lvl = new Level();
		lvl.generateLevel();
		String name = filename;
		if (name.lastIndexOf(".") == -1) {
			name += ".mclevel";
		}
		lvl.setName(name);
		return lvl;
	}

	/**
	 * Determines the appropriate file type and saves the Level.
	 * @param filename The name of the file to unzip
	 * @return The Level
	 */
	public static void save(Level lvl) {
		String type = lvl.getFileType();

		if (type.equalsIgnoreCase("mclevel") || type.equalsIgnoreCase("NBT")) {
			// TODO: Boolean should come from server config file
			NBTFileHandler.save(lvl, "data/maps/" + lvl.getName()+"."+type, true);
			return;
		// Default Case
		} else {
			// TODO: Boolean should come from server config file
			NBTFileHandler.save(lvl, "data/maps/" + lvl.getName()+".mclevel", true);
			lvl.setFileType("mclevel");
			return;
		}
	}
}
