package org.hollowcraft.server.io.impl;
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

import java.io.IOException;
import java.util.ArrayList;


import java.io.File;
import java.util.Arrays;

import org.hollowcraft.io.*;
import org.hollowcraft.server.Configuration;
import org.hollowcraft.server.io.ChunkGzipper;
import org.hollowcraft.server.io.ClassicWorldGzipper;
import org.hollowcraft.server.io.WorldManager;
import org.hollowcraft.server.model.World;
import org.hollowcraft.server.model.impl.worlds.ClassicWorld;
import org.hollowcraft.server.net.MinecraftSession;
import org.slf4j.*;

/**
 * A Class to handle loading and saving of Classic Worlds
 * @author Adam Liszka
 * @author Caleb Champlin
 */
public final class ClassicWorldManager extends WorldManager {

	private static final Logger logger = LoggerFactory.getLogger(ClassicWorldManager.class);

	
	public static WorldManager getInstance() {
		
		return INSTANCE;
	}
	
	
	/**
	 * Default private constructor.
	 */
	/**
	 * Default private constructor.
	 * @throws IllegalAccessException 
	 */
	public ClassicWorldManager() throws IllegalAccessException { 	
		logger.info("Initializing instance");
		if (INSTANCE == null)
			INSTANCE = this;
		else
			throw new IllegalAccessException("Cannot instaniate more than one instance of world manager");
	}

	private static class Version implements Comparable<Version> {
		public File file;
		public int version;
		public String extension;
		public String name;
		public Version(File f, String mapName) {
			name = mapName;
			file = f;
			String baseName = f.getName().replace(mapName+"-", "");
			String[] tokens = baseName.split("\\.");
			try {
				version = Integer.parseInt(tokens[0]);
			} catch (NumberFormatException e) {
				logger.info("Invalid file name {}.", f);
				version = -1;
			}
			extension = tokens[1];
		}

		public int compareTo(Version other) {
			if (version == other.version)
				return 0;
			return version > other.version ? 1 : -1;
		}
	}

	public static Version[] getAllVersions(String mapName) {
		ArrayList<Version> list = new ArrayList<Version>();
		File base = new File("data/maps/"+mapName+"/");
		if (base.exists()) {
			for(File f : base.listFiles()) {
				list.add(new Version(f, mapName));
			}
			return list.toArray(new Version[list.size()]);
		}
		return new Version[0];
	}

	public static Version getLatestVersion(String mapName) {
		Version highest = null;
		for(Version v : getAllVersions(mapName)) {
			if (highest == null || v.version > highest.version)
				highest = v;
		}
		return highest;
	}
	
	public static String getExtension(String mapName, int ver) {
		for(Version v : getAllVersions(mapName)) {
			if (v.version == ver)
				return v.extension;
		}
		return "mclevel";
	}

	private static File getNextFile(String mapName) {
		File base = new File("data/maps/"+mapName);
		if (!base.exists())
			base.mkdirs();
		Version prev = getLatestVersion(mapName);
		if (prev == null)
			return new File("data/maps/"+mapName+"/"+mapName+"-0.mclevel");
		File next = new File("data/maps/"+mapName+"/"+mapName+"-"+(getLatestVersion(mapName).version+1)+".mclevel");
		if (!prev.file.exists())
			return prev.file;
		long then = prev.file.lastModified();
		long now = System.currentTimeMillis();
		long diff = (now-then)/60000;
		logger.trace("Time delta is {} minutes", diff);

		if (diff > Configuration.getConfiguration().getBackupPeriod())
			return next;
		return prev.file;
	}

	/**
	 * Determines the appropriate file type and loads it.
	 * @param filename The name of the file to unzip
	 * @return The World
	 */
	public World load(String mapName) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		Version latest = getLatestVersion(mapName);
		if (latest != null) {
			File mapFile = getLatestVersion(mapName).file;
			String ext = mapFile.getName().substring(mapFile.getName().lastIndexOf(".") + 1);

			try {
				if (ext.equalsIgnoreCase("mclevel")) {
					return new ClassicWorld(NBTFileHandler.load(mapFile.getPath()));
				} else if (ext.equalsIgnoreCase("dat") || ext.equalsIgnoreCase("mine")) {
					return new ClassicWorld(MineFileHandler.load(mapFile.getPath()));
				} else if (ext.equalsIgnoreCase("lvl")) {
					return new ClassicWorld(MCSharpFileHandler.load(mapFile.getPath()));
				}
				logger.info("Unknown file extension {}. Trying all known formats.", ext);
			} catch (IOException e) {
				logger.info("File is of different type than expected. Trying all known formats.");
			}

			try {
				logger.info("Trying NBT .mclevel");
				return new ClassicWorld(NBTFileHandler.load(mapFile.getPath()));
			} catch (IOException e) { }

			try {
				logger.info("Trying old .mine/.dat");
				return new ClassicWorld(MineFileHandler.load(mapFile.getPath()));
			} catch (IOException e) { }

			try {
				logger.info("Trying MCSharp .lvl");
				return new ClassicWorld(MCSharpFileHandler.load(mapFile.getPath()));
			} catch (IOException e) { }
		}

		/*
		logger.info("Generating level instead of loading.");
		World lvl = new World();
		lvl.generateWorld();
		lvl.setName(mapName);
		return lvl;
		*/
		logger.warn("Cannot load level. No handlers found.");
		return null;
	}

	/**
	 * Determines the appropriate file type and saves the World.
	 * @param filename The name of the file to unzip
	 * @return The World
	 */
	public void save(World lvl) {
		File nextFile = getNextFile(lvl.getName());
		File temp = new File("data/maps/"+lvl.getName()+"/"+lvl.getName()+".tmp");
		NBTFileHandler.save((ClassicWorld)lvl, temp.getPath());
		assert(temp.exists());
		logger.trace("Renaming {} to {}", temp, nextFile);
		if (temp.renameTo(nextFile))
			logger.trace("Save complete");
		else
			logger.warn("Could not save to {}!", nextFile);
		int verCount = Configuration.getConfiguration().getBackupCount();
		logger.debug("Removing old versions to trim to {}", verCount);
		Version[] versions = getAllVersions(lvl.getName());
		if (versions.length <= verCount) {
			logger.debug("Not removing old versions, there's only {}", versions.length);
			return;
		}
		Arrays.sort(versions);
		for(int i = 0;i<versions.length && versions.length-i> verCount;i++) {
			logger.trace("Deleting {}", versions[i].file);
			versions[i].file.delete();
		}
	}
	public void gzipWorld(MinecraftSession session)
	{
		ClassicWorldGzipper.getWorldGzipper().gzipWorld(session);
	}
}
