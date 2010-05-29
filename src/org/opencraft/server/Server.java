package org.opencraft.server;

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
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Collection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;
import java.lang.ref.SoftReference;

import java.io.File;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.opencraft.server.model.World;
import org.opencraft.server.model.Level;
import org.opencraft.server.net.SessionHandler;
import org.opencraft.server.task.TaskQueue;
import org.opencraft.server.task.impl.HeartbeatTask;
import org.opencraft.server.task.impl.UpdateTask;
import org.opencraft.server.task.impl.FListHeartbeatTask;
import org.opencraft.server.heartbeat.HeartbeatManager;
import org.opencraft.server.util.SetManager;
import org.opencraft.server.net.MinecraftSession;
import org.opencraft.server.util.PlayerList;
import org.opencraft.server.model.Player;
import org.opencraft.server.persistence.SavedGameManager;
import org.opencraft.server.persistence.SavePersistenceRequest;
import org.opencraft.server.security.Group;
import org.opencraft.server.security.Permission;
import org.opencraft.server.security.Principal;
import org.opencraft.server.security.Policy;
import org.opencraft.server.io.LevelManager;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.util.ArrayList;
import java.text.ParseException;
import org.slf4j.*;

import java.util.logging.LogManager;
import java.io.FileInputStream;


/**
 * The core class of the OpenCraft server.
 * @author Graham Edgecombe
 */
public final class Server {
	
	/**
	 * Logger instance.
	 */
	private static final Logger logger = LoggerFactory.getLogger(Server.class);
	private static final Logger m_loginLogger = LoggerFactory.getLogger(Server.class.getName()+".Logins");

	private final PlayerList m_players;


	public static boolean bootstrap() {
		File dataDir = new File("data/");
		if (!dataDir.exists()) {
			logger.info("Extracting resources from jar file.");
			InputStream bootstrapZip = Server.class.getResourceAsStream("/bootstrap.zip");
			if (bootstrapZip != null) {
				dataDir.mkdir();

				ZipInputStream bootstrap = new ZipInputStream(bootstrapZip);

				ZipEntry current;
				try {
					while((current = bootstrap.getNextEntry()) != null) {
						logger.info("Extracting data/{}", current.getName());
						File dest = new File("data/"+current.getName());
						if (current.isDirectory()) {
							dest.mkdirs();
							continue;
						}
						File dir = dest.getParentFile();
						if (dir != null)
							dir.mkdirs();
						FileOutputStream out = new FileOutputStream(dest);
						for(int c = bootstrap.read(); c != -1;c = bootstrap.read())
							out.write(c);
						bootstrap.closeEntry();
						out.close();
					}
					bootstrap.close();
				} catch (IOException e) {
					logger.error("Bootstrap.zip is corrupt.", e);
					return false;
				}
			} else {
				logger.error("No bootstrap.zip found within the jar. Make sure you downloaded the right version!");
				return false;
			}
		}
		File logDir = new File("log/");
		logDir.mkdir();
		logger.info("Bootstrap complete. To configure the server, hit ctrl+c to kill it and edit the files in data/");
		return true;
	}
	
	/**
	 * The entry point of the server application.
	 * @param args
	 */
	public static void main(String[] args) {
		if (bootstrap()) {
			try {
				//Done just in case the user is one of those new people who don't
				//know about SLF4J, but still wants to configure the logging output.
				File logConfig = new File("data/logging.properties");
				LogManager.getLogManager().readConfiguration(new FileInputStream(logConfig));
			} catch (Throwable t) {
				logger.warn("Error loading the java.util.logging configuration.", t);
			}
			try {
				INSTANCE = new Server();
				INSTANCE.start();
			} catch (Throwable t) {
				logger.error("An error occurred whilst loading the server.", t);
			}
		} else {
			logger.error("Bootstrap failed.");
		}
	}

	private HashMap<String,SoftReference<World>> m_worlds;

	private static Server INSTANCE;

	public static Server getServer() {
		return INSTANCE;
	}

	public World[] getWorlds() {
		World[] ret = new World[m_worlds.size()];
		SoftReference[] refs = new SoftReference[m_worlds.size()];
		refs = m_worlds.values().toArray(refs);
		for(int i = 0;i < m_worlds.size();i++) {
			ret[i] = (World)refs[i].get();
		}
		return ret;
	}

	public String[] getLoadedWorldNames() {
		String[] ret = new String[m_worlds.size()];
		SoftReference[] refs = new SoftReference[m_worlds.size()];
		refs = m_worlds.values().toArray(refs);
		for(int i = 0;i < m_worlds.size();i++) {
			ret[i] = ((World)refs[i].get()).getName();
		}
		return ret;
	}

	/**
	 * The socket acceptor.
	 */
	private final IoAcceptor acceptor = new NioSocketAcceptor();
	
	/**
	 * Creates the server.
	 * @throws IOException if an I/O error occurs.
	 * @throws FileNotFoundException if the configuration file is not found.
	 */
	public Server() throws IOException { 
		logger.info("Starting OpenCraft server...");
		logger.info("Configuring...");
		Configuration.readConfiguration();
		SetManager.getSetManager().reloadSets();
		m_players = new PlayerList();
		
		acceptor.setHandler(new SessionHandler());
		logger.info("Initializing games...");
		m_worlds = new HashMap<String,SoftReference<World>>();
		if (!loadWorld(Configuration.getConfiguration().getDefaultMap())) {
			logger.info("Generating default 256x256x64 world.");
			Level lvl = new Level();
			lvl.generateLevel();
			lvl.setName(Configuration.getConfiguration().getDefaultMap());
			LevelManager.save(lvl);
			boolean loaded = loadWorld(Configuration.getConfiguration().getDefaultMap());
			assert(loaded);
		}
	}

	public boolean loadWorld(String name) {
		if (m_worlds.containsKey(name)) {
			logger.trace("World {} was already loaded some time ago.", name);
			if (m_worlds.get(name).get() == null) {
				logger.debug("Found expired world {}. Removing.", name);
				m_worlds.remove(name);
			} else {
				logger.trace("Found cached world {}.", name);
				return true;
			}
		}
		try {
			logger.info("Loading level \""+name+"\"");
			Level lvl = LevelManager.load(name);
			if (lvl == null)
				return false;
			World w = new World(lvl);
			logger.info("Loading policy for {}...", w);
			try {
				w.setPolicy(new Policy(w.getName(), new BufferedReader(new FileReader("data/opencraft.permissions"))));
			} catch (ParseException e) {
				logger.warn("Error parsing policy, line "+e.getErrorOffset(), e);
			} catch (IOException e) {
				logger.warn("Error reading policy", e);
			}
			m_worlds.put(name, new SoftReference<World>(w));
			return true;
		} catch (InstantiationException e) {
			logger.error("Error loading world.", e);
		} catch (IllegalAccessException e) {
			logger.error("Error loading world.", e);
		} catch (ClassNotFoundException e) {
			logger.error("Error loading world.", e);
		} catch (IOException e) {
			logger.error("Error loading world.", e);
		}
		return false;
	}

	public boolean unloadLevel(String name) {
		logger.info("Unloading level \""+name+"\"");
		final Configuration c = Configuration.getConfiguration();
		if (m_worlds.containsKey(name) && !name.equalsIgnoreCase(c.getDefaultMap())) {
			World w = m_worlds.get(name).get();
			w.finalize();

			Collection<Player> players = w.getPlayerList().getPlayers();
			for (Player p : players) {
				p.moveToWorld(getWorld(c.getDefaultMap()));
			}

			m_worlds.remove(name);
			return true;
		} else {
			logger.trace("World {} is not loaded.", name);
		}
		return false;
	}

	public World getWorld(String name) {
		loadWorld(name);
		World w = m_worlds.get(name).get();
		return w;
	}

	public boolean hasWorld(String name) {
		try {
			if (m_worlds.get(name).get() != null) {
				return true;
			}
		} catch (Exception e) { }
		return false;
	}

	/**
	 * Starts the server.
	 * @throws IOException if an I/O error occurs.
	 */
	public void start() throws IOException {
		logger.info("Initializing server...");	
		logger.info("Starting tasks");
		TaskQueue.getTaskQueue().schedule(new UpdateTask());
		TaskQueue.getTaskQueue().schedule(new HeartbeatTask());
		if (Configuration.getConfiguration().getUseFList())
			TaskQueue.getTaskQueue().schedule(new FListHeartbeatTask());
		logger.info("Binding to port " + Configuration.getConfiguration().getPort() + "...");
		acceptor.bind(new InetSocketAddress(Configuration.getConfiguration().getPort()));
		logger.info("Ready for connections.");
	}

	/**
	 * Registers a session.
	 * @param session The session.
	 * @param username The username.
	 * @param verificationKey The verification key.
	 */
	public void register(MinecraftSession session, String username, String verificationKey) {
		// check if the player is banned
		try {
			File banned = new File("data/banned.txt");
			Scanner fread = new Scanner(banned);
			while (fread.hasNextLine()) {
				if (username.equalsIgnoreCase(fread.nextLine())) {
					session.getActionSender().sendLoginFailure("Banned.");
					logger.info("Refused {}: {} is banned.", session, username);
					break;
				}
			}
			fread.close();
		} catch (IOException e) { }

		// verify name
		if (Configuration.getConfiguration().isVerifyingNames()) {
			if (verificationKey.equals("--")) {
				session.getActionSender().sendLoginFailure("Cannot verify names with ip= based URLs");
			}
			long salt = HeartbeatManager.getHeartbeatManager().getSalt();
			String hash = new StringBuilder().append(String.valueOf(salt)).append(username).toString();
			MessageDigest digest;
			try {
				digest = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException("No MD5 algorithm!");
			}
			digest.update(hash.getBytes());
			String test = new BigInteger(1, digest.digest()).toString(16);
			if (!verificationKey.equals(test)) {
				session.getActionSender().sendLoginFailure("Illegal name.");
				return;
			}
		}
		// check if name is valid
		char[] nameChars = username.toCharArray();
		for (char nameChar : nameChars) {
			if (nameChar < ' ' || nameChar > '\177') {
				session.getActionSender().sendLoginFailure("Invalid name!");
				logger.info("Refused {}, invalid login name.", username);
				return;
			}
		}
		// disconnect any existing players with the same name
		for (Player p : m_players.getPlayers()) {
			if (p.getName().equalsIgnoreCase(username)) {
				// Should it not be the person attempting to connect who gets dropped?
				// FIXME
				p.getSession().getActionSender().sendLoginFailure("Logged in from another computer.");
				logger.info("Kicked {}, logged in from another computer.", p);
				break;
			}
		}
		// attempt to add the player
		final Player player = new Player(session, username);
		if (!m_players.add(player)) {
			player.getSession().getActionSender().sendLoginFailure("Too many players online!");
			logger.warn("Too many players online!");
			return;
		}
		// final setup
		m_loginLogger.info("LOGIN "+player.getName()+" "+session.getAddress().toString());

		session.setPlayer(player);
		final Configuration c = Configuration.getConfiguration();
		session.getActionSender().sendLoginResponse(Constants.PROTOCOL_VERSION, c.getName(), c.getMessage(), false);
		assert(getWorld(c.getDefaultMap()) != null);
		assert(session.getPlayer() == player);
		logger.debug("Moving player to default world");
		player.moveToWorld(getWorld(c.getDefaultMap()));
		m_loginLogger.info("JOIN "+player.getName()+" "+player.getWorld().getLevel().getName());
	}

	public PlayerList getPlayerList() {
		return m_players;
	}

	/**
	 * Unregisters a session.
	 * @param session The session.
	 */
	public void unregister(MinecraftSession session) {
		if (session.isAuthenticated()) {
			logger.trace("Unregistering session.");
			World w = session.getPlayer().getWorld();
			w.removePlayer(session.getPlayer());
			m_players.remove(session.getPlayer());
			SavedGameManager.getSavedGameManager().queuePersistenceRequest(new SavePersistenceRequest(session.getPlayer()));
			session.setPlayer(null);
		}
	}
}
