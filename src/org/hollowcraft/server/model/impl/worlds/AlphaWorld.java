package org.hollowcraft.server.model.impl.worlds;
/*
 * HollowCraft License
 *  Copyright (c) 2010 Caleb Champlin.
 *  All rights reserved
 *  This license must be include in all copied, cloned and derived works 
 */
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


import java.util.*;
import java.io.IOException;

import org.hollowcraft.model.AlphaEnvironment;
import org.hollowcraft.model.AlphaLevel;
import org.hollowcraft.model.BlockDefinition;
import org.hollowcraft.model.BlockManager;
import org.hollowcraft.model.ClassicLevel;
import org.hollowcraft.model.Entity;
import org.hollowcraft.model.Position;
import org.hollowcraft.model.Rotation;
import org.hollowcraft.server.Configuration;
import org.hollowcraft.server.game.GameMode;
import org.hollowcraft.server.game.impl.SandboxGameMode;
import org.hollowcraft.server.io.WorldManager;
import org.hollowcraft.server.model.Chunk;
import org.hollowcraft.server.model.NPEntity;
import org.hollowcraft.server.model.OnBlockChangeHandler;
import org.hollowcraft.server.model.Player;
import org.hollowcraft.server.model.World;
import org.hollowcraft.server.model.impl.builders.*;
import org.hollowcraft.server.net.MinecraftSession;
import org.hollowcraft.server.security.Policy;
import org.hollowcraft.server.task.TaskQueue;
import org.hollowcraft.server.task.impl.SaveWorldTask;
import org.hollowcraft.server.util.NPEList;
import org.hollowcraft.server.util.PlayerList;
import org.slf4j.*;

/**
 * Manages the in-game world.
 * @author Graham Edgecombe
 * @author Caleb Champlin
 */
public final class AlphaWorld extends AlphaLevel implements World {
	
	/**
	 * Logger instance.
	 */
	private static final Logger logger = LoggerFactory.getLogger(AlphaWorld.class);



	private Policy m_policy;

	public Policy getPolicy() {
		return m_policy;
	}

	public void setPolicy(Policy p) {
		m_policy = p;
	}
	
	
	/**
	 * The player list.
	 */
	private PlayerList playerList = new PlayerList();
	
	/**
	 * The game mode.
	 */
	private GameMode<Player> gameMode;


	public AlphaWorld(AlphaLevel other) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		super(other);
		init();
	}

	public AlphaWorld() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		super();
		init();
	}

	private void init() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		m_policy = new Policy();
		gameMode = (GameMode) Class.forName(Configuration.getConfiguration().getGameMode()).newInstance();
		
		
		setOnBlockChangeHandler(new OnBlockChangeHandler() {
			public void onBlockChange(Position pos) {
				for (Player player : getPlayerList().getPlayers()) {
					try {
						player.getSession().getActionSender().sendBlock(pos, getBlock(pos),getBlockMeta(pos));
					} catch (Exception e) {
						logger.warn("Cannot send block change");
					}
				}
			}
		});
		
		
		TaskQueue.getTaskQueue().schedule(new SaveWorldTask(this));
		logger.info("Active game mode : " + gameMode.getClass().getName() + ".");
	}

	public void setBlock(Position pos, int type)
	{
		super.setBlock(pos, type);
		if (m_handler != null)
			m_handler.onBlockChange(pos);
	}
	
	
	public void finalize() {
		logger.info("Finalizing world, saving level.");
		try {
			WorldManager.getInstance().save(this);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	 * @throws Exception 
	 */
	public void completeRegistration(MinecraftSession session) {
		if (!session.isAuthenticated()) {
			logger.debug("Player not authenticated!!!");
			session.close();
			return;
		}
		try {
			session.getActionSender().sendChatMessage("Welcome to HallowCraft - Marmalade!");
		} catch (Exception e) {
			logger.warn("Unable to send chat message");
		}
		// Notify game mode
		logger.debug("Notify the game mode!!!");
		getGameMode().playerConnected(session.getPlayer());
		logger.debug("Completed Registration!!!");
	}
	
	/**
	 * Broadcasts a chat message.
	 * @param player The source player.
	 * @param message The message.
	 */
	public void broadcast(Player player, String message) {
		for (Player otherPlayer : playerList.getPlayers()) {
			try {
				otherPlayer.getSession().getActionSender().sendChatMessage(player.getId(), message);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.warn("Unable to send chat message");
			}
		}
	}
	
	/**
	 * Broadcasts a server message.
	 * @param message The message.
	 */
	public void broadcast(String message) {
		for (Player player : playerList.getPlayers()) {
			try {
				player.getSession().getActionSender().sendChatMessage(message);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.warn("Unable to send chat message");
			}
		}
	}

	@Override
	public void addEntity(NPEntity ent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public NPEList getEntityList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeEntity(NPEntity ent) {
		// TODO Auto-generated method stub
		
	}


}
