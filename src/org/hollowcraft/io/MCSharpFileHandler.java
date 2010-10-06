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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.DataInputStream;
import java.util.zip.GZIPInputStream;


import org.hollowcraft.model.BlockManager;
import org.hollowcraft.model.ClassicEnvironment;
import org.hollowcraft.model.ClassicLevel;
import org.hollowcraft.model.Position;
import org.hollowcraft.model.Rotation;
import org.slf4j.*;

public final class MCSharpFileHandler {
	private MCSharpFileHandler() {}

	private final static Logger logger = LoggerFactory.getLogger(MCSharpFileHandler.class);

	private static int convert(int s) {
		return (int)(((s>>8)&0xff)+((s << 8)&0xff00));
	}

	public static ClassicLevel load(String filename) throws IOException {
		logger.trace("Loading {}", filename);
		ClassicLevel lvl = new ClassicLevel();
		FileInputStream in = new FileInputStream(filename);
		GZIPInputStream decompressor = new GZIPInputStream(in);

		DataInputStream data = new DataInputStream(decompressor);

		int magic = convert(data.readShort());
		logger.trace("Magic number: {}", magic);
		if (magic != 1874)
			throw new IOException("Only version 1 MCSharp levels supported (magic number was "+magic+")");

		int width = convert(data.readShort());
		int height = convert(data.readShort());
		int depth = convert(data.readShort());
		logger.trace("Width: {}", width);
		logger.trace("Depth: {}", depth);
		logger.trace("Height: {}", height);

		int spawnX = convert(data.readShort());
		int spawnY = convert(data.readShort());
		int spawnZ = convert(data.readShort());

		int spawnRotation = data.readUnsignedByte();
		int spawnPitch = data.readUnsignedByte();

		/*int visitRanks =*/ data.readUnsignedByte();
		/*int buildRanks =*/ data.readUnsignedByte();

		byte[][][] blocks = new byte[width][height][depth];
		for(int z = 0;z<depth;z++) {
			for(int y = 0;y<height;y++) {
				byte[] row = new byte[height];
				data.readFully(row);
				for(int x = 0;x<width;x++) {
					blocks[x][y][z] = translateBlock(row[x]);
					blocks[x][y][z] = (byte)translateBlock(row[x]);
				}
			}
		}

		lvl.setBlocks(blocks, new byte[width][height][depth], width, height, depth);
		lvl.setSpawnPosition(new Position(spawnX, spawnY, spawnZ));
		lvl.setSpawnRotation(new Rotation(spawnRotation, spawnPitch));
		lvl.setEnvironment(new ClassicEnvironment());

		return lvl;
	}

	public static byte translateBlock(byte b) {
		if (b <= 49)
			return b;
		if (b == 111)
			return BlockConstants.TREE_TRUNK;
		return BlockConstants.AIR;
	}
}
