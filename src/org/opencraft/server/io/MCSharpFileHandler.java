package org.opencraft.server.io;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.DataInputStream;
import java.util.zip.GZIPInputStream;
import org.opencraft.server.model.Level;
import org.opencraft.server.model.Environment;
import org.opencraft.server.model.Position;
import org.opencraft.server.model.Rotation;
import org.opencraft.server.model.BlockManager;
import org.opencraft.server.model.BlockConstants;

import org.slf4j.*;

public final class MCSharpFileHandler {
	private MCSharpFileHandler() {}

	private final static Logger logger = LoggerFactory.getLogger(MCSharpFileHandler.class);

	private static int convert(int s) {
		return (int)(((s>>8)&0xff)+((s << 8)&0xff00));
	}

	public static Level load(String filename) throws IOException {
		logger.trace("Loading {}", filename);
		Level lvl = new Level();
		FileInputStream in = new FileInputStream(filename);
		GZIPInputStream decompressor = new GZIPInputStream(in);

		DataInputStream data = new DataInputStream(decompressor);

		int magic = convert(data.readShort());
		logger.trace("Magic number: {}", magic);
		if (magic != 1874)
			throw new IOException("Only version 1 MCSharp levels supported (magic number was "+magic+")");

		int width = convert(data.readShort());
		int heightheight  = convert(data.readShort());
		int depth = convert(data.readShort());
		logger.trace("Width: {}", width);
		logger.trace("Depth: {}", depth);
		logger.trace("Height: {}", height);

		int spawnX = convert(data.readShort());
		int spawnY = convert(data.readShort());
		int spawnZ = convert(data.readShort());

		int spawnRotation = data.readUnsignedByte();
		int spawnPitch = data.readUnsignedByte();

		int visitRanks = data.readUnsignedByte();
		int buildRanks = data.readUnsignedByte();

		byte[][][] blocks = new byte[width][height][depth];
		int i = 0;
		BlockManager manager = BlockManager.getBlockManager();
		for(int z = 0;z<depth;z++) {
			for(int y = 0;y<height;y++) {
				byte[] row = new byte[height];
				data.readFully(row);
				for(int x = 0;x<width;x++) {
					blocks[x][y][z] = translateBlock(row[x]);
				}
			}
		}

		lvl.setBlocks(blocks, new byte[width][height][depth], width, height, depth);
		lvl.setSpawnPosition(new Position(spawnX, spawnY, spawnZ));
		lvl.setSpawnRotation(new Rotation(spawnRotation, spawnPitch));
		lvl.setEnvironment(new Environment());

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
