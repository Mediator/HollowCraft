package org.opencraft.server.io;

import java.util.Map;
import java.util.List;
import org.jnbt.*;
import java.io.*;

import org.opencraft.server.model.Position;
import org.opencraft.server.model.Level;
import org.opencraft.server.model.World;
import org.opencraft.server.model.Environment;

/**
 * A frontend to the JNBT file reader
 * @author Adam Liszka
 */
public final class NBTLevelCreator {
	
	/**
	 * The singleton instance.
	 */
	private static final NBTLevelCreator INSTANCE = new NBTLevelCreator();
	
	/**
	 * Gets the NBTLevelCreator instance
	 * @return The NBELevelCreator.
	 */
	public static NBTLevelCreator getNBTLevelCreator() {
		return INSTANCE;
	}
	
	/**
	 * Default private constructor.
	 */
	private NBTLevelCreator() {
		/* empty */
	}
	
	/**
	 * Unzips a .dat level file and returns the contained level
	 * @param filename The name of the file to unzip
	 * @return The uncompressed Level
	 */
	public Level load(String filename) {
		Level lvl;
		try {
			String lvlname = "unnamed";
			String author = "ACM OpenCraft Crew";
			long created = 0;
			Environment env = new Environment();
			int width = 128, height = 128, depth = 32;
			Position spawnPos = new Position(width*16, height*16, depth*32);
			byte[][][] blocks = new byte[width][height][depth];

			NBTInputStream nbtin = new NBTInputStream(new FileInputStream("data/acmpc.mclevel"));

			CompoundTag root = (CompoundTag)(nbtin.readTag());
			//lvlname = root.getName(); //?
			Map<String, Tag> items = root.getValue();

			for (String key : items.keySet()) {
				if (key.equalsIgnoreCase("Environment")) {
					CompoundTag etag = (CompoundTag)(items.get(key));
					Map<String, Tag> eItems = etag.getValue();

					env.setSurroundingGroundHeight(((ShortTag)(eItems.get("SurroundingGroundHeight"))).getValue());
					env.setSurroundingGroundType(((ByteTag)(eItems.get("SurroundingGroundType"))).getValue());
					env.setSurroundingWaterHeight(((ShortTag)(eItems.get("SurroundingWaterHeight"))).getValue());
					env.setSurroundingWaterType(((ByteTag)(eItems.get("SurroundingWaterType"))).getValue());
					env.setCloudHeight(((ShortTag)(eItems.get("CloudHeight"))).getValue());
					env.setCloudColor(((IntTag)(eItems.get("CloudColor"))).getValue());
					env.setSkyColor(((IntTag)(eItems.get("SkyColor"))).getValue());
					env.setFogColor(((IntTag)(eItems.get("FogColor"))).getValue());
					env.setSkyBrightness(((ByteTag)(eItems.get("SkyBrightness"))).getValue());

				} else if (key.equalsIgnoreCase("Entities")) {
					//TODO
				} else if (key.equalsIgnoreCase("Map")) {
					CompoundTag map = (CompoundTag)(items.get(key));
					Map<String, Tag> mapItems = map.getValue();

					// Don't mess with this. It just works somehow
					height = ((ShortTag)(mapItems.get("Length"))).getValue();
					depth = ((ShortTag)(mapItems.get("Height"))).getValue();
					width  = ((ShortTag)(mapItems.get("Width"))).getValue();

					byte[] blk = ((ByteArrayTag)(mapItems.get("Blocks"))).getValue();
					blocks = new byte[width][height][depth];
					int i = 0;
					for (int z = 0; z < depth; z++) {
						for (int y = 0; y < height; y++) {
							for (int x = 0; x < width; x++) {
								blocks[x][y][z] = blk[i];
								i += 1;
							}
						}
					}

					ListTag spawn = (ListTag)(mapItems.get("Spawn"));
					List<Tag> spawnCoords = spawn.getValue();
					spawnPos = new Position(
							((ShortTag)(spawnCoords.get(0))).getValue(),
							((ShortTag)(spawnCoords.get(1))).getValue(),
							((ShortTag)(spawnCoords.get(2))).getValue()
						       );

				} else if (key.equalsIgnoreCase("About")) {
					CompoundTag about = (CompoundTag)(items.get(key));
					Map<String, Tag> aboutItems = about.getValue();
					lvlname = ((StringTag)(aboutItems.get("Name"))).getValue();
					author  = ((StringTag)(aboutItems.get("Author"))).getValue();
					created = ((ShortTag)(aboutItems.get("CreatedOn"))).getValue();
				}
			}
			lvl = new Level(lvlname, author, created, width, height, depth, blocks, env, spawnPos);
		} catch (IOException e) {
			lvl = new Level();
		}

		return lvl;
	}
}
