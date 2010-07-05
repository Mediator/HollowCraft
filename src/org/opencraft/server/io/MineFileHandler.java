package org.opencraft.server.io;

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

import java.io.IOException;
import java.io.ObjectStreamClass;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.util.zip.GZIPInputStream;
import org.opencraft.model.Level;
import org.opencraft.model.Environment;
import org.opencraft.model.Rotation;
import org.opencraft.model.Position;

/**
 * A level loader that loads up serialized levels.
 * @author Adam Liszka
 */
public final class MineFileHandler {

	/**
	 * Default private constructor.
	 */
	private MineFileHandler() { /* empty */ }
	
	/**
	 * Uses the magic of java introspection to load a level
	 * @param filename The name of the file to unzip
	 * @return The uncompressed Level
	 */
	public static Level load(String filename) throws IOException {
		Level lvl = new Level();
		FileInputStream in = new FileInputStream(filename);
		GZIPInputStream decompressor = new GZIPInputStream(in);
		DataInputStream data = new DataInputStream(decompressor);
		int magic = data.readInt();
		byte version = data.readByte();
		ObjectInputStream stream = new LevelDeserializer(decompressor);
		DeserializedLevel level;
		try {
			level = (DeserializedLevel)stream.readObject();
		} catch (ClassNotFoundException e) {
			throw new IOException(e.getMessage());
		}

		if (level != null) {
			Environment env = new Environment();

			int width  = level.width;
			int height = level.height;
			int depth  = level.depth;
			byte[] fblocks = level.blocks;
			byte[][][] blocks = new byte[width][height][depth];

			int i = 0;
			for (int z = 0; z < depth; z++) {
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						blocks[x][y][z] = fblocks[i];
						i += 1;
					}
				}
			}

			lvl.setEnvironment(env);

			lvl.setBlocks(blocks, new byte[width][height][depth], width, height, depth);

			lvl.setSpawnPosition(new Position(level.xSpawn, level.ySpawn, level.zSpawn));
			lvl.setSpawnRotation(new Rotation((int)level.rotSpawn, 0));

			lvl.setName(level.name);
			lvl.setAuthor(level.creator);
			lvl.setCreationDate(level.createTime);
		} else {
			throw new IOException("Failed to load mine file");
		}
		

		return lvl;
	}

	public static void save(Level lvl, String filename) {
		//We'd need to basically steal code from minecraft to do this, since
		//there doesn't seem to be a way to make a ObjectStreamClass from
		//scratch with a custom class name.
		throw new UnsupportedOperationException();
	}
}
class DeserializedLevel implements Serializable {
	static final long serialVersionUID = 0L;
	public int cloudColor;
	public long createTime;
	public boolean creativeMode;
	public int depth;
	public int fogColor;
	public boolean growTrees;
	public int height;
	public boolean networkMode;
	public float rotSpawn;
	public int skyColor;
	public int tickCount;
	public int unprocessed;
	public int waterLevel;
	public int width;
	public int xSpawn;
	public int ySpawn;
	public int zSpawn;
	public Object blockMap;
	public byte[] blocks;
	public String creator;
	public String name;
	public Object player;
	public DeserializedLevel() {
	}

}
class LevelDeserializer extends ObjectInputStream {
	public LevelDeserializer(InputStream in) throws IOException {
		super(in);
	}

	protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
		ObjectStreamClass desc = super.readClassDescriptor();
		if (desc.getName().equals("com.mojang.minecraft.level.Level")) {
			return ObjectStreamClass.lookup(DeserializedLevel.class);
		}
		return desc;
	}
}

