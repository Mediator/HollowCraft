package org.hollowcraft.io;
/*
 * HollowCraft License
 *  Copyright (c) 2010 Caleb Champlin.
 *  All rights reserved
 *  This license must be include in all copied, cloned and derived works 
 */
/*
 * OpenCraft License
 * 
 * Copyright (c) 2009 Graham Edgecombe, Søren Enevoldsen and Brett Russell.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *       
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *       
 *     * Neither the name of the OpenCraft nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 *ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.hollowcraft.model.ClassicEnvironment;
import org.hollowcraft.model.ClassicLevel;
import org.hollowcraft.model.Position;
import org.hollowcraft.model.Rotation;
import org.jnbt.*;
import java.io.*;

import org.slf4j.*;

/**
 * A frontend to the JNBT file reader
 * @author Adam Liszka
 * @author Caleb champlin
 */
public final class NBTFileHandler {

	/**
	 * Default private constructor.
	 */
	private NBTFileHandler() { /* empty */ }

	private static final Logger logger = LoggerFactory.getLogger(NBTFileHandler.class);
	
	/**
	 * Unzips a .mclevel level file, parses it, and returns the contained level
	 * http://www.minecraft.net/docs/levelformat.jsp
	 * @param filename The name of the file to Load
	 * @return The loaded Level
	 */
	public static ClassicLevel load(String filename) throws IOException {
		ClassicLevel lvl = new ClassicLevel();
		NBTInputStream nbtin;

		logger.trace("Loading in {}", filename);
		nbtin = new NBTInputStream(new FileInputStream(filename));
		CompoundTag root = (CompoundTag)(nbtin.readTag());
		Map<String, Tag> items = root.getValue();

		unpackEnvironment(lvl, items);
		unpackMap(lvl, items);
		unpackEntities(lvl, items);
		unpackTileEntities(lvl, items);
		unpackAbout(lvl, items);

		nbtin.close();

		return lvl;
	}

	private static void unpackEnvironment(ClassicLevel lvl, Map<String, Tag> items) {
		CompoundTag etag = (CompoundTag)(items.get("Environment"));
		Map<String, Tag> eItems = etag.getValue();
		ClassicEnvironment env = new ClassicEnvironment();

		// A single missing property should not stop any other property from being loaded. All vars already have a default variable
		try { env.setSurroundingGroundHeight(((ShortTag)(eItems.get("SurroundingGroundHeight"))).getValue());	} catch (NullPointerException e) { }
		try { env.setSurroundingGroundType(((ByteTag)(eItems.get("SurroundingGroundType"))).getValue());	} catch (NullPointerException e) { }
		try { env.setSurroundingWaterHeight(((ShortTag)(eItems.get("SurroundingWaterHeight"))).getValue());	} catch (NullPointerException e) { }
		try { env.setSurroundingWaterType(((ByteTag)(eItems.get("SurroundingWaterType"))).getValue());		} catch (NullPointerException e) { }
		try { env.setTimeOfDay(((ShortTag)(eItems.get("TimeOfDay"))).getValue());				} catch (NullPointerException e) { }
		try { env.setCloudHeight(((ShortTag)(eItems.get("CloudHeight"))).getValue());				} catch (NullPointerException e) { }
		try { env.setCloudColor(((IntTag)(eItems.get("CloudColor"))).getValue());				} catch (NullPointerException e) { }
		try { env.setSkyColor(((IntTag)(eItems.get("SkyColor"))).getValue());					} catch (NullPointerException e) { }
		try { env.setFogColor(((IntTag)(eItems.get("FogColor"))).getValue());					} catch (NullPointerException e) { }
		try { env.setSkyBrightness(((ByteTag)(eItems.get("SkyBrightness"))).getValue());			} catch (NullPointerException e) { }

		lvl.setEnvironment(env);
	}

	private static void unpackMap(ClassicLevel lvl, Map<String, Tag> items) {
		CompoundTag map = (CompoundTag)(items.get("Map"));
		Map<String, Tag> mapItems = map.getValue();

		//
		// Blocks and Data
		//

		// Don't mess with this. It just works somehow
		int depth  = ((ShortTag)(mapItems.get("Height"))).getValue();
		int height = ((ShortTag)(mapItems.get("Length"))).getValue();
		int width  = ((ShortTag)(mapItems.get("Width"))).getValue();

		byte[] fblocks   = ((ByteArrayTag)(mapItems.get("Blocks"))).getValue();
		byte[] fdata     = ((ByteArrayTag)(mapItems.get("Data"))).getValue();
		byte[][][]blocks = new byte[width][height][depth];
		byte[][][]data   = new byte[width][height][depth];

		int i = 0;
		for (int z = 0; z < depth; z++) {
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					if (fblocks[i] <= 49) {
						blocks[x][y][z] = fblocks[i];
					} else {
						blocks[x][y][z] = 0;
					}
					data[x][y][z] = fdata[i];
					i += 1;
				}
			}
		}

		lvl.setBlocks(blocks, data, width, height, depth);

		//
		// Spawn Pos and Rotation
		//
		ListTag spawn = (ListTag)(mapItems.get("Spawn"));
		List<Tag> spawnCoords = spawn.getValue();
		lvl.setSpawnPosition(new Position(
					((ShortTag)(spawnCoords.get(0))).getValue(),
					((ShortTag)(spawnCoords.get(1))).getValue(),
					((ShortTag)(spawnCoords.get(2))).getValue()
				    ));

		//This is not in the mclevel specifications
		try {
			ListTag rot = (ListTag)(mapItems.get("Rotation"));
			List<Tag> rotCoords = rot.getValue();
			lvl.setSpawnRotation(new Rotation(
					((ShortTag)(rotCoords.get(0))).getValue(),
					((ShortTag)(rotCoords.get(1))).getValue()
				    ));
		} catch (Exception e) {
			lvl.setSpawnRotation(new Rotation(0, 0));
		}
	}

	private static void unpackEntities(ClassicLevel lvl, Map<String, Tag> items) {
		//TODO
	}

	private static void unpackTileEntities(ClassicLevel lvl, Map<String, Tag> items) {
		//TODO
	}

	private static void unpackAbout(ClassicLevel lvl, Map<String, Tag> items) {
		CompoundTag about = (CompoundTag)(items.get("About"));
		Map<String, Tag> aboutItems = about.getValue();
		lvl.setTitle(((StringTag)(aboutItems.get("Name"))).getValue());
		lvl.setAuthor(((StringTag)(aboutItems.get("Author"))).getValue());
		lvl.setCreationDate(((LongTag)(aboutItems.get("CreatedOn"))).getValue());
	}

	//
	//
	//
	// Saving a Level
	//
	//
	//

	public static void save(ClassicLevel lvl, String filename) {
		HashMap<String, Tag> rootItems = new HashMap<String, Tag>();

			
		rootItems.put("Environment",packEnvironment(lvl));


		rootItems.put("Map", packMap(lvl));
			
		rootItems.put("Entities", packEntities(lvl));
		rootItems.put("About", packAbout(lvl));
		CompoundTag root = new CompoundTag("MinecraftLevel", rootItems);

		try {
			logger.debug("Writing NBT to {}", filename);
			FileOutputStream out = new FileOutputStream(filename);
			NBTOutputStream nbtout = new NBTOutputStream(out);
			nbtout.writeTag(root);
			nbtout.close();
			out.flush();
			out.close();
			logger.debug("Successfully wrote to {}", filename);
		} catch (Exception e) {
			logger.warn("Error writing save file", e);
		}
	}

	private static CompoundTag packEnvironment(ClassicLevel lvl) {
		HashMap<String, Tag> eItems = new HashMap<String, Tag>();
		ClassicEnvironment env = lvl.getEnvironment();

		ShortTag sgh = new ShortTag("SurroundingGroundHeight", (short)env.getSurroundingGroundHeight());
		ByteTag  sgt = new ByteTag("SurroundingGroundType", (byte)env.getSurroundingGroundType());
		ShortTag swh = new ShortTag("SurroundingWaterHeight", (short)env.getSurroundingWaterHeight());
		ByteTag  swt = new ByteTag("SurroundingWaterType", (byte)env.getSurroundingWaterType());
		ShortTag clh = new ShortTag("CloudHeight", (short)env.getCloudHeight());
		ShortTag tod = new ShortTag("TimeOfDay", env.getTimeOfDay());
		IntTag   clc = new IntTag("CloudColor", env.getCloudColor());
		IntTag   skc = new IntTag("SkyColor", env.getSkyColor());
		IntTag   fgc = new IntTag("FogColor", env.getFogColor());
		ByteTag  skb = new ByteTag("SkyBrightness", env.getSkyBrightness());
		eItems.put("SurroundingGroundHeight", sgh);
		eItems.put("SurroundingGroundType", sgt);
		eItems.put("SurroundingWaterHeight", swh);
		eItems.put("SurroundingWaterType", swt);
		eItems.put("CloudHeight", clh);
		eItems.put("TimeOfDay", tod);
		eItems.put("CloudColor", clc);
		eItems.put("SkyColor", skc);
		eItems.put("FogColor", fgc);
		eItems.put("SkyBrightness", skb);

		return new CompoundTag("Environment", eItems);
	}

	private static CompoundTag packMap(ClassicLevel lvl) {
		HashMap<String, Tag> mapItems = new HashMap<String, Tag>();
 
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

		// Spawn Position
		ArrayList<Tag> sposItems = new ArrayList<Tag>(3);

		Position spos = lvl.getSpawnPosition();
		ShortTag sposx = new ShortTag("X", (short)spos.getX());
		ShortTag sposy = new ShortTag("Y", (short)spos.getY());
		ShortTag sposz = new ShortTag("Z", (short)spos.getZ());
		sposItems.add(sposx);
		sposItems.add(sposy);
		sposItems.add(sposz);

		ListTag spawnPos = new ListTag("Spawn", ShortTag.class, sposItems);
		mapItems.put("Spawn", spawnPos);
		// Not in the Specification, but would be consistant with the indev conventions for Entities
		spawnPos = new ListTag("Pos", ShortTag.class, sposItems);
		mapItems.put("Pos", spawnPos);

		// Spawn Rotation
		// This is not in the mclevel specifications, but probably will be sometime soon because it fits with the above comment
		ArrayList<Tag> srotItems = new ArrayList<Tag>(2);
		Rotation srot = lvl.getSpawnRotation();
		ShortTag srotation = new ShortTag("Rotation", (short)srot.getRotation());
		ShortTag slook = new ShortTag("Look", (short)srot.getLook());
		srotItems.add(srotation);
		srotItems.add(slook);

		ListTag spawnRotation = new ListTag("Rotation", ShortTag.class, srotItems);
		mapItems.put("Rotation", spawnRotation);

		return new CompoundTag("Map", mapItems);

	}

	private static CompoundTag packEntities(ClassicLevel lvl) {
		HashMap<String, Tag> entities = new HashMap<String, Tag>();

		//
		// LocalPlayer
		//
		HashMap<String, Tag> localPlayer = new HashMap<String, Tag>();

		// id
		StringTag id = new StringTag("id", "LocalPlayer");
		localPlayer.put("id", id);

		// Position
		Position pos = lvl.getSpawnPosition();
		ArrayList<Tag> posItems = new ArrayList<Tag>(3);
		FloatTag posx = new FloatTag("X", (float)pos.getX());
		FloatTag posy = new FloatTag("Y", (float)pos.getY());
		FloatTag posz = new FloatTag("Z", (float)pos.getZ());
		posItems.add(posx);
		posItems.add(posy);
		posItems.add(posz);
		ListTag spawnPos = new ListTag("Spawn", FloatTag.class, posItems);
		localPlayer.put("Pos", spawnPos);

		// Rotation
		ArrayList<Tag> rotItems = new ArrayList<Tag>(2);
		Rotation rot = lvl.getSpawnRotation();
		FloatTag rotation = new FloatTag("Rotation", (float)rot.getRotation());
		FloatTag look = new FloatTag("Look", (float)rot.getLook());
		rotItems.add(rotation);
		rotItems.add(look);
		ListTag spawnRotation = new ListTag("Rotation", FloatTag.class, rotItems);
		localPlayer.put("Rotation", spawnRotation);

		entities.put("LocalPlayer", new CompoundTag("LocalPlayer", localPlayer));

		return new CompoundTag("Entities", entities);
	}

	private static CompoundTag packAbout(ClassicLevel lvl) {
		HashMap<String, Tag> aboutItems = new HashMap<String, Tag>();

		StringTag name;
		if (lvl.getName() == null) {
			name = new StringTag("Name", "");
		} else {
			name = new StringTag("Name", lvl.getName());
		}

		StringTag auth;
		if (lvl.getAuthor() == null) {
			auth = new StringTag("Author", "");
		} else {
			auth = new StringTag("Author", lvl.getAuthor());
		}

		LongTag date = new LongTag("CreatedOn", lvl.getCreationDate());

		aboutItems.put("Name", name);
		aboutItems.put("Author", auth);
		aboutItems.put("CreatedOn", date);

		return new CompoundTag("About", aboutItems);
	}
}
