package org.opencraft.server;

/*
 * OpenCraft License
 * 
 * Copyright (c) 2009 Graham Edgecombe, S�ren Enevoldsen and Brett Russell.
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;

import java.io.File;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.opencraft.server.model.World;
import org.opencraft.server.net.SessionHandler;
import org.opencraft.server.task.TaskQueue;
import org.opencraft.server.task.impl.HeartbeatTask;
import org.opencraft.server.task.impl.UpdateTask;
import org.opencraft.server.heartbeat.HeartbeatManager;
import org.opencraft.server.util.SetManager;
import org.opencraft.server.net.MinecraftSession;
import org.opencraft.server.util.PlayerList;
import org.opencraft.server.model.Player;
import org.opencraft.server.persistence.SavedGameManager;
import org.opencraft.server.persistence.SavePersistenceRequest;
import org.slf4j.*;


/**
 * The core class of the OpenCraft server.
 * @author Graham Edgecombe
 */
public final class Server {
	
	/**
	 * Logger instance.
	 */
	private static final Logger logger = LoggerFactory.getLogger(Server.class);

	private final PlayerList m_players;
	
	/**
	 * The entry point of the server application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			INSTANCE = new Server();
			INSTANCE.start();
		} catch (Throwable t) {
			logger.error("An error occurred whilst loading the server.", t);
		}
	}

	private HashMap<String,World> m_worlds;

	private static Server INSTANCE;

	public static Server getServer() {
		return INSTANCE;
	}

	public World[] getWorlds() {
		World[] ret = new World[m_worlds.size()];
		return m_worlds.values().toArray(ret);
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
		m_worlds = new HashMap<String,World>();
		loadLevel("default");
		TaskQueue.getTaskQueue().schedule(new UpdateTask());
		TaskQueue.getTaskQueue().schedule(new HeartbeatTask());
	}

	public void loadLevel(String name) {
		logger.info("Loading level \""+name+"\"");
		try {
			m_worlds.put(name, new World(name));
		} catch (InstantiationException e) {
			logger.error("Error loading world.");
		} catch (IllegalAccessException e) {
			logger.error("Error loading world.");
		} catch (ClassNotFoundException e) {
			logger.error("Error loading world.");
		}
	}

	public World getWorld(String name) {
		if (!m_worlds.containsKey(name)) {
			loadLevel(name);
		}
		assert(m_worlds.get(name) != null);
		return m_worlds.get(name);
	}
	
	/**
	 * Starts the server.
	 * @throws IOException if an I/O error occurs.
	 */
	public void start() throws IOException {
		logger.debug("Debug");
		logger.info("Initializing server...");
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
					break;
				}
			}
			fread.close();
		} catch (IOException e) { }

		// verify name
		if (Configuration.getConfiguration().isVerifyingNames()) {
			long salt = HeartbeatManager.getHeartbeatManager().getSalt();
			String hash = new StringBuilder().append(String.valueOf(salt)).append(username).toString();
			MessageDigest digest;
			try {
				digest = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException("No MD5 algorithm!");
			}
			digest.update(hash.getBytes());
			if (!verificationKey.equals(new BigInteger(1, digest.digest()).toString(16))) {
				session.getActionSender().sendLoginFailure("Illegal name.");
				return;
			}
		}
		// check if name is valid
		char[] nameChars = username.toCharArray();
		for (char nameChar : nameChars) {
			if (nameChar < ' ' || nameChar > '\177') {
				session.getActionSender().sendLoginFailure("Invalid name!");
				return;
			}
		}
		// disconnect any existing players with the same name
		for (Player p : m_players.getPlayers()) {
			if (p.getName().equalsIgnoreCase(username)) {
				// Should it not be the person attempting to connect who gets dropped?
				// FIXME
				p.getSession().getActionSender().sendLoginFailure("Logged in from another computer.");
				break;
			}
		}
		// attempt to add the player
		final Player player = new Player(session, username);
		if (!m_players.add(player)) {
			player.getSession().getActionSender().sendLoginFailure("Too many players online!");
			return;
		}
		// final setup

		// set op rights
		try {
			File ops = new File("data/ops.txt");
			Scanner fread = new Scanner(ops);
			while (fread.hasNextLine()) {
				if (username.equalsIgnoreCase(fread.nextLine())) {
					player.setAttribute("IsOperator","true");
					break;
				}
			}
			fread.close();
		} catch (IOException e) { }

		session.setPlayer(player);
		final Configuration c = Configuration.getConfiguration();
		session.getActionSender().sendLoginResponse(Constants.PROTOCOL_VERSION, c.getName(), c.getMessage(), false);
		//FIXME: Make the default configurable.
		assert(getWorld("default") != null);
		assert(session.getPlayer() == player);
		player.moveToWorld(getWorld("default"));
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
