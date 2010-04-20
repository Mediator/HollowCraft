package org.opencraft.server.io;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import org.jnbt.*;
import java.io.*;

import org.opencraft.server.model.Position;
import org.opencraft.server.model.Rotation;
import org.opencraft.server.model.Level;
import org.opencraft.server.model.World;
import org.opencraft.server.model.Environment;

/**
 * A frontend to the JNBT file reader
 * @author Adam Liszka
 */
public final class NBTFileHandler {

	/**
	 * Default private constructor.
	 */
	private NBTFileHandler() { /* empty */ }
	
	/**
	 * Unzips a .mclevel level file, parses it, and returns the contained level
	 * http://www.minecraft.net/docs/levelformat.jsp
	 * @param filename The name of the file to unzip
	 * @return The uncompressed Level
	 */
	public static Level load(String filename) throws IOException {
		Level lvl = new Level();
		NBTInputStream nbtin;

		nbtin = new NBTInputStream(new FileInputStream(filename));
		CompoundTag root = (CompoundTag)(nbtin.readTag());
		Map<String, Tag> items = root.getValue();

		for (String key : items.keySet()) {
			if (key.equalsIgnoreCase("Environment")) {
				CompoundTag etag = (CompoundTag)(items.get(key));
				Map<String, Tag> eItems = etag.getValue();

				Environment env = new Environment();
				env.setSurroundingGroundHeight(((ShortTag)(eItems.get("SurroundingGroundHeight"))).getValue());
				env.setSurroundingGroundType(((ByteTag)(eItems.get("SurroundingGroundType"))).getValue());
				env.setSurroundingWaterHeight(((ShortTag)(eItems.get("SurroundingWaterHeight"))).getValue());
				env.setSurroundingWaterType(((ByteTag)(eItems.get("SurroundingWaterType"))).getValue());
				env.setCloudHeight(((ShortTag)(eItems.get("CloudHeight"))).getValue());
				env.setCloudColor(((IntTag)(eItems.get("CloudColor"))).getValue());
				env.setSkyColor(((IntTag)(eItems.get("SkyColor"))).getValue());
				env.setFogColor(((IntTag)(eItems.get("FogColor"))).getValue());
				env.setSkyBrightness(((ByteTag)(eItems.get("SkyBrightness"))).getValue());
				lvl.setEnvironment(env);

			} else if (key.equalsIgnoreCase("Entities")) {
				//TODO
			} else if (key.equalsIgnoreCase("Map")) {
				CompoundTag map = (CompoundTag)(items.get(key));
				Map<String, Tag> mapItems = map.getValue();

				// Don't mess with this. It just works somehow
				int depth = ((ShortTag)(mapItems.get("Height"))).getValue();
				int height = ((ShortTag)(mapItems.get("Length"))).getValue();
				int width  = ((ShortTag)(mapItems.get("Width"))).getValue();

				byte[] fblocks = ((ByteArrayTag)(mapItems.get("Blocks"))).getValue();
				byte[] fdata = ((ByteArrayTag)(mapItems.get("Data"))).getValue();
				byte[][][]blocks = new byte[width][height][depth];
				byte[][][]data = new byte[width][height][depth];

				int i = 0;
				for (int z = 0; z < depth; z++) {
					for (int y = 0; y < height; y++) {
						for (int x = 0; x < width; x++) {
							blocks[x][y][z] = fblocks[i];
							data[x][y][z] = fdata[i];
							i += 1;
						}
					}
				}

				lvl.setBlocks(blocks, data, width, height, depth);

				ListTag spawn = (ListTag)(mapItems.get("Spawn"));
				List<Tag> spawnCoords = spawn.getValue();
				lvl.setSpawnPosition(new Position(
							((ShortTag)(spawnCoords.get(0))).getValue(),
							((ShortTag)(spawnCoords.get(1))).getValue(),
							((ShortTag)(spawnCoords.get(2))).getValue()
						    ));

				//This is not in the mclevel specifications
				if (spawnCoords.size() > 4) {
					lvl.setSpawnRotation(new Rotation(
							((ShortTag)(spawnCoords.get(3))).getValue(),
							((ShortTag)(spawnCoords.get(4))).getValue()
						    ));
				} else {
					lvl.setSpawnRotation(new Rotation(0, 0));
				}

			} else if (key.equalsIgnoreCase("About")) {
				CompoundTag about = (CompoundTag)(items.get(key));
				Map<String, Tag> aboutItems = about.getValue();
				lvl.setName(((StringTag)(aboutItems.get("Name"))).getValue());
				lvl.setAuthor(((StringTag)(aboutItems.get("Author"))).getValue());
				lvl.setCreationDate(((LongTag)(aboutItems.get("CreatedOn"))).getValue());
			}
		}
		nbtin.close();

		lvl.setFileType("mclevel");
		return lvl;
	}

	public static void save(Level lvl, String filename, boolean addNonStandardInfo) {
		HashMap<String, Tag> rootItems = new HashMap();

			HashMap<String, Tag> eItems = new HashMap();
			Environment env = lvl.getEnvironment();

			ShortTag sgh = new ShortTag("SurroundingGroundHeight", (short)env.getSurroundingGroundHeight());
			ByteTag  sgt = new ByteTag("SurroundingGroundType", env.getSurroundingGroundType());
			ShortTag swh = new ShortTag("SurroundingWaterHeight", (short)env.getSurroundingWaterHeight());
			ByteTag  swt = new ByteTag("SurroundingWaterType", env.getSurroundingWaterType());
			ShortTag clh = new ShortTag("CloudHeight", (short)env.getCloudHeight());
			IntTag   clc = new IntTag("CloudColor", env.getCloudColor());
			IntTag   skc = new IntTag("SkyColor", env.getSkyColor());
			IntTag   fgc = new IntTag("FogColor", env.getFogColor());
			ByteTag  skb = new ByteTag("SkyBrightness", env.getSkyBrightness());
			eItems.put("SurroundingGroundHeight", sgh);
			eItems.put("SurroundingGroundType", sgt);
			eItems.put("SurroundingWaterHeight", swh);
			eItems.put("SurroundingWaterType", swt);
			eItems.put("CloudHeight", clh);
			eItems.put("CloudColor", clc);
			eItems.put("SkyColor", skc);
			eItems.put("FogColor", fgc);
			eItems.put("SkyBrightness", skb);

			CompoundTag etag = new CompoundTag("Environment", eItems);

		rootItems.put("Environment",etag);

			//
			// Map Items
			//
			HashMap<String, Tag> mapItems = new HashMap();
 
			// Map data
			// The heights here are NOT the same things
			int depth  = lvl.getDepth();
			int height = lvl.getHeight();
			int width  = lvl.getWidth();
			ShortTag depthTag = new ShortTag("Height", (short)depth);
			ShortTag heightTag = new ShortTag("Length", (short)height);
			ShortTag widthTag = new ShortTag("Width", (short)width);
			mapItems.put("Height", depthTag);
			mapItems.put("Length", heightTag);
			mapItems.put("Width", widthTag);

			byte[] fdata = new byte[lvl.getDepth() * lvl.getHeight() * lvl.getWidth()];
			byte[] fblocks = new byte[fdata.length];
			byte[][][]blocks = lvl.getBlocks();
			byte[][][]data = lvl.getData();

			int i = 0;
			for (int z = 0; z < depth; z++) {
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						fblocks[i] = blocks[x][y][z];
						fdata[i] = data[x][y][z];
						i += 1;
					}
				}
			}

			ByteArrayTag blockTag = new ByteArrayTag("Blocks", fblocks);
			ByteArrayTag dataTag = new ByteArrayTag("Data", fblocks);
			mapItems.put("Blocks", blockTag);
			mapItems.put("Data", dataTag);

			// Spawn Items
			ArrayList<Tag> spawnItems = new ArrayList(addNonStandardInfo ? 5 : 3);

			Position pos = lvl.getSpawnPosition();
			ShortTag posx = new ShortTag("X", (short)pos.getX());
			ShortTag posy = new ShortTag("Y", (short)pos.getY());
			ShortTag posz = new ShortTag("Z", (short)pos.getZ());
			spawnItems.add(posx);
			spawnItems.add(posy);
			spawnItems.add(posz);

			//This is not in the mclevel specifications
			if (addNonStandardInfo) {
				Rotation r = lvl.getSpawnRotation();
				ShortTag rotation = new ShortTag("Rotation", (short)r.getRotation());
				ShortTag look = new ShortTag("Look", (short)r.getLook());
				spawnItems.add(rotation);
				spawnItems.add(look);
			}

			ListTag spawn = new ListTag("Spawn", ShortTag.class, spawnItems);
			mapItems.put("Spawn", spawn);

			CompoundTag map = new CompoundTag("Map", mapItems);

		rootItems.put("Map", map);

			//
			// About Items
			//
			HashMap<String, Tag> aboutItems = new HashMap();
			StringTag name = new StringTag("Name",    lvl.getName());
			StringTag auth = new StringTag("Author",  lvl.getAuthor());
			LongTag   date = new LongTag("CreatedOn", lvl.getCreationDate());
			aboutItems.put("Name", name);
			aboutItems.put("Author", auth);
			aboutItems.put("CreatedOn", date);

			CompoundTag aboutTag = new CompoundTag("About", aboutItems);

		rootItems.put("About", aboutTag);

		CompoundTag root = new CompoundTag("MinecraftLevel", rootItems);


		try {
			NBTOutputStream nbtout = new NBTOutputStream(new FileOutputStream(filename));
			nbtout.writeTag(root);
			nbtout.close();
		} catch (Exception e) {
			//Log this?
		}
	}
}
