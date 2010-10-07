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

import org.hollowcraft.model.AlphaLevel;
import org.hollowcraft.model.BlockDefinition;
import org.hollowcraft.model.BlockManager;
import org.hollowcraft.model.ClassicEnvironment;
import org.hollowcraft.model.ClassicLevel;
import org.hollowcraft.model.Entity;
import org.hollowcraft.model.Position;
import org.hollowcraft.model.Rotation;
import org.hollowcraft.server.Configuration;
import org.hollowcraft.server.game.GameMode;
import org.hollowcraft.server.game.impl.SandboxGameMode;
import org.hollowcraft.server.io.WorldManager;
import org.hollowcraft.server.model.Builder;
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
 * Manages the in-game classic world.
 * @author Graham Edgecombe
 * @author Caleb Champlin
 */
public final class ClassicWorld extends ClassicLevel implements World {
	
	/**
	 * Logger instance.
	 */
	private static final Logger logger = LoggerFactory.getLogger(World.class);

	/** The active "thinking" blocks on the map. */
	protected Map<Integer, ArrayDeque<Position>> m_activeBlocks = new HashMap<Integer, ArrayDeque<Position>>();
	/** The timers for the active "thinking" blocks on the map. */
	protected Map<Integer, Long> m_activeTimers = new HashMap<Integer, Long>();
	/** A queue of positions to update at the next tick. */
	protected Queue<Position> m_updateQueue = new ArrayDeque<Position>();

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

	private short[][] m_lightDepths;

	public ClassicWorld(ClassicLevel other) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		super(other);
		assert(other.getDepth() > 0);
		assert(getDepth() == other.getDepth());
		init();
	}

	public ClassicWorld() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		super();
		init();
	}

	private void init() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		for (int i = 0; i < 256; i++) {
			BlockDefinition b = BlockManager.getBlockManager().getBlock((short)i);
			if (b != null && b.doesThink()) {
				m_activeBlocks.put(i, new ArrayDeque<Position>());
				m_activeTimers.put(i, System.currentTimeMillis());
			}
		}
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
		m_lightDepths = new short[m_width][m_height];
		recalculateAllLightDepths();
	}

	public void generateWorld() {
		generateWorld(256, 256, 64);
	}

	public void generateWorld(int width, int height, int depth) {
		generateWorld(new ClassicLandscapeBuilder(this), width, height, depth, new ClassicEnvironment(), "Generated World", "ACM OpenCraft Crew");
	}

	public void generateWorld(Builder b, int width, int height, int depth, ClassicEnvironment env, String title, String author) {
		m_title = title;
		m_author = author;
		m_fileType = "mclevel";
		m_created = (new java.util.Date()).getTime();
		m_env = env;
		m_width = width;
		m_height = height;
		m_depth = depth;
		m_blocks = new byte[m_width][m_height][m_depth];
		m_data = new byte[m_width][m_height][m_depth];
		m_lightDepths = new short[m_width][m_height];
		m_spawnPosition = new Position(m_width*16, m_height*16, m_depth*32);
		m_spawnRotation = new Rotation(0, 0);

		b.setLevel(this);
		b.generate();
		m_blocks = b.getBlocks();
		activateOcean();
	}

	public void activateOcean() {
		for (int x = 0;x < m_width; x++) {
			m_logger.debug("Activating ocean: " + (x * 2) + "/" + (m_width * 2 + m_height * 2));
			queueTileUpdate(new Position(x, 0, m_depth/2 - 1));
			queueTileUpdate(new Position(x, m_height - 1, m_depth/2 - 1));
		}

		for (int y = 0;y < m_height; y++) {
			m_logger.debug("Activating ocean: " + (y * 2 + m_width * 2) + "/" + (m_width * 2 + m_height * 2));
			queueTileUpdate(new Position(0, y, m_depth/2 - 1));
			queueTileUpdate(new Position(m_width - 1, y, m_depth/2 - 1));
		}

		recalculateAllLightDepths();
	}
	
	/**
	 * Recalculates all light depths. WARNING: this is a costly function and
	 * should only be used when it really is necessary.
	 */
	public void recalculateAllLightDepths() {
		for (int x = 0; x < m_width; x++) {
			for (int y = 0; y < m_height; y++) {
				recalculateLightDepth(x, y);
			}
		}
	}
	
	/**
	 * Recalculates the light depth of the specified coordinates.
	 * @param x The x coordinates.
	 * @param y The y coordinates.
	 */
	public void recalculateLightDepth(int x, int y) {
		for (int z = m_depth - 1; z >= 0; z--) {
			if (BlockManager.getBlockManager().getBlock(m_blocks[x][y][z]).doesBlockLight()) {
				m_lightDepths[x][y] = (short) z;
				return;
			}
		}
		m_lightDepths[x][y] = (short) -1;
	}
	
	/**
	 * Manually assign a light depth to a given Cartesian coordinate.
	 * @param x The X coordinate.
	 * @param y The Y coordinate.
	 * @param depth The lowest-lit block.
	 */
	public void assignLightDepth(int x, int y, int depth) {
		if (depth > m_height)
			return;
		m_lightDepths[x][y] = (short) depth;
	}
	
	/**
	 * Gets the light depth at the specific coordinate.
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 * @return The light depth.
	 */
	public int getLightDepth(int x, int y) {
		return m_lightDepths[x][y];
	}
	
	/**
	 * Performs physics updates on queued blocks.
	 */
	public void applyBlockBehaviour() {
		Queue<Position> currentQueue;
		synchronized (m_updateQueue) {
			currentQueue = new ArrayDeque<Position>(m_updateQueue);
			m_updateQueue.clear();
		}
		for (Position pos : currentQueue) {
			if (BlockManager.getBlockManager().getBlock(this.getBlock(pos)).hasGravity()) {
				if (!blockIsStable(pos)) {
					setBlock(new Position(pos.getX(), pos.getY(), pos.getZ() - 1), getBlock(pos));
					setBlock(pos, BlockManager.getBlockManager().getBlock("AIR").getId());
				}
			}
			BlockManager.getBlockManager().getBlock(this.getBlock(pos)).behavePassive(this, pos);
		}
		// we only process up to 20 of each type of thinking block every tick,
		// or we'd probably be here all day.
		for (int type = 0; type < 256; type++) {
			if (m_activeBlocks.containsKey(type)) {
				if (System.currentTimeMillis() - m_activeTimers.get(type) > BlockManager.getBlockManager().getBlock((short)type).getTimer()) {
					int cyclesThisTick = (m_activeBlocks.get(type).size() > 600 ? 600 : m_activeBlocks.get(type).size());
					for (int i = 0; i < cyclesThisTick; i++) {
						Position pos = m_activeBlocks.get(type).poll();
						if (pos == null)
							break;
						// the block that occupies this space might have
						// changed.
						if (getBlock(pos) == type) {
							// World.getWorld().broadcast("Processing thinker at ("+pos.getX()+","+pos.getY()+","+pos.getZ()+")");
							BlockManager.getBlockManager().getBlock((short)type).behaveSchedule(this, pos);
						}
					}
					m_activeTimers.put(type, System.currentTimeMillis());
				}
			}
		}
	}

	public void setBlock(Position pos, short type) {
		setBlock(pos, type, true);
	}
	
	/**
	 * Sets a block.
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 * @param z The z coordinate.
	 * @param type The type id.
	 * @param updateSelf Update self flag.
	 */
	public void setBlock(Position pos, short type, boolean updateSelf) {
		if (pos.getX() < 0 || pos.getY() < 0 || pos.getZ() < 0 || pos.getX() >= m_width || pos.getY() >= m_height || pos.getZ() >= m_depth) {
			return;
		}
		byte formerBlock = getBlock(pos);
		super.setBlock(pos, type);
		if (m_handler != null)
			m_handler.onBlockChange(pos);
		if (updateSelf) {
			queueTileUpdate(pos);
		}
		if (type == 0) {
			BlockManager.getBlockManager().getBlock(formerBlock).behaveDestruct(this, pos);
			updateNeighboursAt(pos);
			if (getLightDepth(pos.getX(), pos.getY()) == pos.getZ()) {
				recalculateLightDepth(pos.getX(), pos.getY());
				scheduleZPlantThink(pos);
			}
		}
		if (BlockManager.getBlockManager().getBlock(type).doesThink()) {
			assert(m_activeBlocks!=null);
			assert(m_activeBlocks.get(type) != null);
			m_activeBlocks.get(type).add(pos);
		}
		if (BlockManager.getBlockManager().getBlock(type).doesBlockLight()) {
			assignLightDepth(pos.getX(), pos.getY(), pos.getZ());
			scheduleZPlantThink(pos);
		}
		
	}

	/**
	 * Schedules plants to think in a Z coordinate if a block above them
	 * changed.
	 * @param x X coordinate.
	 * @param y Y coordinate.
	 * @param z Z coordinate.
	 */
	public void scheduleZPlantThink(Position pos) {
		for (int i = pos.getZ() - 1; i > 0; i--) {
			if (BlockManager.getBlockManager().getBlock(this.getBlock(new Position(pos.getX(), pos.getY(), i))).isPlant()) {
				queueActiveBlockUpdate(new Position(pos.getX(), pos.getY(), i));
			}
			if (BlockManager.getBlockManager().getBlock(this.getBlock(new Position(pos.getX(), pos.getY(), i))).doesBlockLight()) {
				return;
			}
		}
	}
	
	/**
	 * Updates neighbours at the specified coordinate.
	 * @param x X coordinate.
	 * @param y Y coordinate.
	 * @param z Z coordinate.
	 */
	private void updateNeighboursAt(Position pos) {
		queueTileUpdate(new Position(pos.getX() - 1, pos.getY(), pos.getZ()));
		queueTileUpdate(new Position(pos.getX(), pos.getY() - 1, pos.getZ()));
		queueTileUpdate(new Position(pos.getX() + 1, pos.getY(), pos.getZ()));
		queueTileUpdate(new Position(pos.getX(), pos.getY() + 1, pos.getZ()));
		queueTileUpdate(new Position(pos.getX(), pos.getY(), pos.getZ() - 1));
		queueTileUpdate(new Position(pos.getX(), pos.getY(), pos.getZ() + 1));
		recalculateLightDepth(pos.getX(), pos.getY());
	}
	
	/**
	 * Queues a tile update.
	 * @param x X coordinate.
	 * @param y Y coordinate.
	 * @param z Z coordinate.
	 */
	private void queueTileUpdate(Position pos) {
		if (pos.getX() >= 0 && pos.getY() >= 0 && pos.getZ() >= 0 && pos.getX() < m_width && pos.getY() < m_height && pos.getZ() < m_depth) {
			if (!m_updateQueue.contains(pos)) {
				m_updateQueue.add(pos);
			}
		}
	}
	
	/**
	 * Forces a tile update to be queued. Use with caution.
	 * @param x X coordinate.
	 * @param y Y coordinate.
	 * @param z Z coordinate.
	 */
	public void queueActiveBlockUpdate(Position pos) {
		if (pos.getX() >= 0 && pos.getY() >= 0 && pos.getZ() >= 0 && pos.getX() < m_width && pos.getY() < m_height && pos.getZ() < m_depth) {
			short blockAt = getBlock(pos);
			if (BlockManager.getBlockManager().getBlock(blockAt).doesThink()) {
				m_activeBlocks.get(blockAt).add(pos);
			}
		}
	}

	public void setBlocks(byte[][][] blocks, byte[][][] data, int width, int height, int depth) {
		super.setBlocks(blocks, data, width, height, depth);
		m_lightDepths = new short[m_width][m_height];
		recalculateAllLightDepths();
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
	 */
	public void completeRegistration(MinecraftSession session) {
		if (!session.isAuthenticated()) {
			session.close();
			return;
		}
		try {
			session.getActionSender().sendChatMessage("Welcome to HollowCraft!");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.warn("Unable to send chat message");
		}
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
			try {
				otherPlayer.getSession().getActionSender().sendChatMessage(player.getId(), message);
			} catch (Exception e) {
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
