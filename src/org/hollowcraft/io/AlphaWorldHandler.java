package org.hollowcraft.io;

/*
 * HollowCraft License
 *  Copyright (c) 2010 Caleb Champlin.
 *  All rights reserved
 *  This license must be include in all copied, cloned and derived works 
 */

import org.hollowcraft.model.AlphaEnvironment;
import org.hollowcraft.model.AlphaLevel;
import org.hollowcraft.model.Point;
import org.hollowcraft.server.model.Chunk;
import org.jnbt.*;

import java.io.*;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * World handler for disk saved alpha levels
 * @author Caleb champlin
 */
public class AlphaWorldHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(AlphaNBTFileHandler.class);
	
	public AlphaWorldHandler()
	{
		
	}
	public static AlphaLevel loadLevel(String path) throws IOException
	{
		AlphaLevel level = new AlphaLevel();
		String levelInfo = path + "/" + "level.dat";
		Map<String, Tag> nbtInfo = AlphaNBTFileHandler.load(levelInfo);
		level.setName("Default");
		level.setWorldPath(path);
		unpackEnvironment(level, nbtInfo);
		return level;
	}
	public static boolean levelExists(String path)
	{
		File levelFile = new File(path + "/" + "level.dat");
		if (levelFile.exists())
			return true;
		else
			return false;
	}
	public static boolean saveLevel(AlphaLevel level)
	{
		return false;
	}
	public static Chunk loadChunk(String path, Point p) throws IOException
	{
		Chunk chunk = new Chunk(p.getX(), p.getY());
		String chunkInfo = path + "/" + chunkPathname(p.getX(), p.getY()) + "/" + chunkFilename(p.getX(), p.getY());
		Map<String, Tag> nbtInfo = AlphaNBTFileHandler.load(chunkInfo);
		unpackChunk(chunk, nbtInfo);
		return chunk;
	}
	public static boolean chunkExists(String path, Point p)
	{
		
		String chunkInfo = path + "/" + chunkPathname(p.getX(), p.getY()) + "/" + chunkFilename(p.getX(), p.getY());
		logger.debug("Checking for existance of chunk at: " + chunkInfo);
		File chunkFile = new File(chunkInfo);
		if (chunkFile.exists())
			return true;
		else
			return false;
	}
	public static boolean saveChunk(String path, Chunk chunk)
	{
		return false;
	}
	private static void unpackEnvironment(AlphaLevel lvl, Map<String, Tag> items) {
		CompoundTag etag = (CompoundTag)(items.get("Data"));
		Map<String, Tag> eItems = etag.getValue();
		AlphaEnvironment env = new AlphaEnvironment();

		// A single missing property should not stop any other property from being loaded. All vars already have a default variable
		try { env.setSpawnX(((IntTag)(eItems.get("SpawnX"))).getValue());	} catch (NullPointerException e) { }
		try { env.setSpawnY(((IntTag)(eItems.get("SpawnY"))).getValue());	} catch (NullPointerException e) { }
		try { env.setSpawnZ(((IntTag)(eItems.get("SpawnZ"))).getValue());	} catch (NullPointerException e) { }
		try { env.setSnowCovered(((ByteTag)(eItems.get("SnowCovered"))).getValue());		} catch (NullPointerException e) { }
		try { env.setTimeOfDay(((LongTag)(eItems.get("Time"))).getValue());				} catch (NullPointerException e) { }
		try { env.setRandomSeed(((LongTag)(eItems.get("RandomSeed"))).getValue());				} catch (NullPointerException e) { }
		try { env.setLastPlayed(((LongTag)(eItems.get("LastPlayed"))).getValue());				} catch (NullPointerException e) { }
		try { env.setSizeOnDisk(((LongTag)(eItems.get("SizeOnDisk"))).getValue());					} catch (NullPointerException e) { }

		lvl.setEnvironment(env);
	}

	private static void unpackChunk(Chunk chunk, Map<String, Tag> items) throws IOException {
		CompoundTag etag = (CompoundTag)(items.get("Level"));
		Map<String, Tag> eItems = etag.getValue();
		//Sanity check
		if (((IntTag)(eItems.get("xPos"))).getValue() != chunk.getX())
			throw new IOException("Invalid x coordinate");
		if (((IntTag)(eItems.get("zPos"))).getValue() != chunk.getZ())
			throw new IOException("Invalid z coordinate");
		byte[] heightMap   = ((ByteArrayTag)(eItems.get("HeightMap"))).getValue();
		
		byte[] blocks   = ((ByteArrayTag)(eItems.get("Blocks"))).getValue();
		byte[] blockLight   = ((ByteArrayTag)(eItems.get("BlockLight"))).getValue();
		byte[] skyLight   = ((ByteArrayTag)(eItems.get("SkyLight"))).getValue();
		byte[] data     = ((ByteArrayTag)(eItems.get("Data"))).getValue();
		for (int x = 0; x < blocks.length; x++)
		{
			int metadata;
			int blocklighting;
			int blockskylighting;
			
			
			chunk.setChunkBlockType(x, blocks[x]);
			
			
			if (x % 2 == 0)
			{
			
				
				chunk.setChunkBlockMeta(x/2, data[x/2]);
				chunk.setChunkBlockBlockLight(x/2, blockLight[x/2]);
				chunk.setChunkBlockSkyLight(x/2, skyLight[x/2]);
				byte signedMeta = data[x/2];
				byte signedBlockLighting = blockLight[x/2];
				byte signedSkyLighting = skyLight[x/2];
				metadata = (signedMeta & 0x0F);
				blocklighting = (signedBlockLighting & 0x0F);
				blockskylighting = (signedSkyLighting & 0x0F);
				
				
			}
			else
			{
				byte signedMeta = data[x/2];
				byte signedBlockLighting = blockLight[x/2];
				byte signedSkyLighting = skyLight[x/2];
				metadata = ((signedMeta >> 4) & 0x0F);
				blocklighting = ((signedBlockLighting >> 4) & 0x0F);
				blockskylighting = ((signedSkyLighting >> 4) & 0x0F);

			}
			chunk.setChunkBlock(x, blocks[x], (byte)metadata, (byte)blocklighting, (byte)blockskylighting);
		}
		chunk.setHeightMap(heightMap);
		chunk.setTerrainPopulated(((ByteTag)(eItems.get("TerrainPopulated"))).getValue());
	}
	
	private static String chunkPathname(int x, int z)
	{
		x = (x >= 0) ? x % 64: (x + 64); 
		z = (z >= 0) ? z % 64 : (z + 64); 
		return base36Encode(x) + "/" + base36Encode(z);
	}
	private static short toUnsigned(byte b) { 
	    return (short)(b & 0xff);
	}
	private static String base36Encode(int value)
	{
		if (value == 0)
			return "0";
		StringBuffer sb = new StringBuffer();
		String charSet = "0123456789abcdefghijklmnopqrstuvwxyz";
		int mod = Math.abs(value); 
		while (mod > 0)
		{
			sb.append(charSet.charAt(mod % 36));
			mod /= 36;
		}

		if (value < 0)
		{
			sb.append("-");
		}
		return sb.reverse().toString();
	}
	 private static String chunkFilename(int x, int z) 
	 {
		 return "c." + base36Encode(x) + "." + base36Encode(z) + ".dat";
	}

}
