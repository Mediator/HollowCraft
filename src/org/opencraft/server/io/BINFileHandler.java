package org.opencraft.server.io;

import java.io.IOException;
import org.opencraft.server.model.Level;
import org.opencraft.server.model.Environment;

/**
 * A Wrapper for SerializableLevel
 * @author Adam Liszka
 */
public final class BINFileHandler {

	/**
	 * Default private constructor.
	 */
	private BINFileHandler() { /* empty */ }
	
	/**
	 * Provides a convient wrapper around the SerializableLevel class to return an opencraft Level
	 * @param filename The name of the file to unzip
	 * @return The uncompressed Level
	 */
	public static Level load(String filename) throws IOException {
		Level lvl = new Level();

		SerializableLevel loader = new SerializableLevel(filename);
		loader.load();

		if (loader.isLoadSuccess()) {
			Environment env = new Environment();

			int width  = loader.getWidth();
			int height = loader.getHeight();
			int depth  = loader.getDepth();
			byte[] fblocks = loader.getBlocks();
			byte[][][] blocks = new byte[width][height][depth];

			int i = 0;
			for (int z = 0; z < depth; z++) {
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						blocks[x][y][z] = fblocks[i];
						System.out.print(fblocks[i] + "  ");
						i += 1;
					}
				}
			}

			lvl.setEnvironment(env);

			lvl.setBlocks(blocks, new byte[width][height][depth], width, height, depth);

			lvl.setSpawnPosition(loader.getSpawnPoint());
			lvl.setSpawnRotation(loader.getSpawnRotation());

			lvl.setName(loader.getName());
			lvl.setAuthor(loader.getCreator());
			lvl.setCreationDate(loader.getCreateTime());

			lvl.setFileType("bin");
		} else {
			throw new IOException("Failed to load BIN file");
		}
		

		return lvl;
	}

	public static void save(Level lvl, String filename) {

	}
}
