package org.hollowcraft.server.io;
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
import org.hollowcraft.server.io.impl.AlphaWorldManager;
import org.hollowcraft.server.model.World;
import org.hollowcraft.server.net.MinecraftSession;
import org.slf4j.*;

/**
 * A Class to handle loading and saving of Worlds
 * @author Caleb Champlin
 */
public abstract class WorldManager {


	protected static WorldManager INSTANCE;
	
	public static WorldManager getInstance() throws ClassNotFoundException {
		if (INSTANCE == null)
			throw new ClassNotFoundException("Cannot utilize abstract singleton");
		else
			return INSTANCE;
	}
	
	public abstract World load(String mapName) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException;
	public abstract void save(World lvl);
	public abstract void gzipWorld(MinecraftSession session);
}
