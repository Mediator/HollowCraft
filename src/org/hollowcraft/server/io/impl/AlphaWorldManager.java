package org.hollowcraft.server.io.impl;
/*
 * HollowCraft License
 *  Copyright (c) 2010 Caleb Champlin.
 *  All rights reserved
 *  This license must be include in all copied, cloned and derived works 
 */


import java.io.IOException;
import java.util.ArrayList;


import java.io.File;
import java.util.Arrays;

import org.hollowcraft.io.*;
import org.hollowcraft.server.Configuration;
import org.hollowcraft.server.io.ChunkGzipper;
import org.hollowcraft.server.io.WorldManager;
import org.hollowcraft.server.model.World;
import org.hollowcraft.server.model.impl.worlds.AlphaWorld;
import org.hollowcraft.server.net.MinecraftSession;
import org.slf4j.*;

/**
 * A Class to handle loading and saving of Worlds
 * @author Caleb Champlin
 */
public final class AlphaWorldManager extends WorldManager {

	private static final Logger logger = LoggerFactory.getLogger(AlphaWorldManager.class);

	
	/**
	 * Default private constructor.
	 * @throws IllegalAccessException 
	 */
	public AlphaWorldManager() throws IllegalAccessException { 	
		logger.info("Initializing instance");
		if (INSTANCE == null)
		INSTANCE = this;
		else
			throw new IllegalAccessException("Cannot instaniate more than one instance of world manager");
	}

	/**
	 * Determines the appropriate file type and loads it.
	 * @param filename The name of the file to unzip
	 * @return The World
	 */
	public World load(String mapName) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		if (!AlphaWorldHandler.levelExists("data/worlds/" + mapName))
		{
			logger.info("Failed to find world");
			return null;
		}
		try {
				return (World)new AlphaWorld(AlphaWorldHandler.loadLevel("data/worlds/" + mapName));
			} catch (IOException e) {
				logger.info("World is of different type than expected.");
			}
			catch (Exception e)
			{
				logger.info("Got world loading exception", e);
			}
			return null;
	}

	/**
	 * Determines the appropriate file type and saves the World.
	 * @param filename The name of the file to unzip
	 * @return The World
	 */
	public void save(World lvl) {
	}
	
	public void gzipWorld(MinecraftSession session)
	{
		ChunkGzipper.getChunkGzipper().gzipChunks(session);
	}
}
