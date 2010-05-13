package org.opencraft.server.model;

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

import org.opencraft.server.Configuration;
import org.opencraft.server.task.impl.SaveLevelTask;
import org.opencraft.server.task.TaskQueue;
import org.opencraft.server.game.GameMode;
import org.opencraft.server.net.MinecraftSession;
import org.opencraft.server.util.PlayerList;
import org.opencraft.server.io.LevelManager;
import org.opencraft.server.security.Policy;
import java.io.IOException;
import org.slf4j.*;

/**
 * Manages the in-game world.
 * @author Graham Edgecombe
 */
public final class World {
	
	/**
	 * Logger instance.
	 */
	private static final Logger logger = LoggerFactory.getLogger(World.class);

	private Policy m_policy;

	public Policy getPolicy() {
		return m_policy;
	}

	public void setPolicy(Policy p) {
		m_policy = p;
	}
	
	
	/**
	 * The level.
	 */
	private Level level;
	
	/**
	 * The player list.
	 */
	private PlayerList playerList = new PlayerList();
	
	/**
	 * The game mode.
	 */
	private GameMode<Player> gameMode;

	public String getName() {
		return level.getName();
	}
	
	/**
	 * Default private constructor.
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */

	public World(Level lvl) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		m_policy = new Policy();
		gameMode = (GameMode) Class.forName(Configuration.getConfiguration().getGameMode()).newInstance();
		level = lvl;
		level.setOnBlockChangeHandler(new OnBlockChangeHandler() {
			public void onBlockChange(int x, int y, int z) {
				for (Player player : getPlayerList().getPlayers()) {
					player.getSession().getActionSender().sendBlock(x, y, z, level.getBlock(x, y, z));
				}
			}
		});
		TaskQueue.getTaskQueue().schedule(new SaveLevelTask(level));
		logger.info("Active game mode : " + gameMode.getClass().getName() + ".");
	}

	public void finalize() {
		logger.info("Finalizing world, saving level.");
		LevelManager.save(level);
	}
	
	/**
	 * Gets the current game mode.
	 * @return The current game mode.
	 */
	public GameMode<Player> getGameMode() {
		return gameMode;
	}

	/**
	 * Gets the player list.
	 * @return The player list.
	 */
	public PlayerList getPlayerList() {
		return playerList;
	}
	
	/**
	 * Gets the level.
	 * @return The level.
	 */
	public Level getLevel() {
		return level;
	}

	public void removePlayer(Player p) {
		logger.trace("Removing player");
		playerList.remove(p);
		getGameMode().playerDisconnected(p);
	}

	public void addPlayer(Player p) {
		playerList.add(p);
		//getGameMode().playerConnected(p);
	}
	
	/**
	 * Completes registration of a session.
	 * @param session The session.
	 */
	public void completeRegistration(MinecraftSession session) {
		if (!session.isAuthenticated()) {
			session.close();
			return;
		}
		session.getActionSender().sendChatMessage("Welcome to OpenCraft!");
		// Notify game mode
		getGameMode().playerConnected(session.getPlayer());
	}
	
	/**
	 * Broadcasts a chat message.
	 * @param player The source player.
	 * @param message The message.
	 */
	public void broadcast(Player player, String message) {
		for (Player otherPlayer : playerList.getPlayers()) {
			otherPlayer.getSession().getActionSender().sendChatMessage(player.getId(), message);
		}
	}
	
	/**
	 * Broadcasts a server message.
	 * @param message The message.
	 */
	public void broadcast(String message) {
		for (Player player : playerList.getPlayers()) {
			player.getSession().getActionSender().sendChatMessage(message);
		}
	}
	
}
