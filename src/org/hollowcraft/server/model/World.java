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
import org.opencraft.server.task.impl.SaveWorldTask;
import org.opencraft.server.task.TaskQueue;
import org.opencraft.server.game.GameMode;
import org.opencraft.server.game.impl.SandboxGameMode;
import org.opencraft.server.net.MinecraftSession;
import org.opencraft.server.util.PlayerList;
import org.opencraft.server.io.WorldManager;
import org.opencraft.model.BlockDefinition;
import org.opencraft.server.security.Policy;
import org.opencraft.model.Entity;
import org.opencraft.model.Level;
import org.opencraft.model.Environment;
import org.opencraft.model.Position;
import org.opencraft.model.Rotation;
import org.opencraft.model.BlockManager;
import org.opencraft.model.BlockConstants;
import org.opencraft.server.model.impl.builders.*;
import java.util.*;
import java.io.IOException;
import org.slf4j.*;

/**
 * Manages the in-game world.
 * @author Graham Edgecombe
 */
public final class World extends Level {
	
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

	public World(Level other) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		super(other);
		assert(other.getDepth() > 0);
		assert(getDepth() == other.getDepth());
		init();
	}

	public World() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		super();
		init();
	}

	private void init() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		for (int i = 0; i < 256; i++) {
			BlockDefinition b = BlockManager.getBlockManager().getBlock(i);
			if (b != null && b.doesThink()) {
				m_activeBlocks.put(i, new ArrayDeque<Position>());
				m_activeTimers.put(i, System.currentTimeMillis());
			}
		}
		m_policy = new Policy();
		gameMode = (GameMode) Class.forName(Configuration.getConfiguration().getGameMode()).newInstance();
		setOnBlockChangeHandler(new OnBlockChangeHandler() {
			public void onBlockChange(int x, int y, int z) {
				for (Player player : getPlayerList().getPlayers()) {
					player.getSession().getActionSender().sendBlock(x, y, z, getBlock(x, y, z));
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
		generateWorld(new LandscapeBuilder(this), width, height, depth, new Environment(), "Generated World", "ACM OpenCraft Crew");
	}

	public void generateWorld(Builder b, int width, int height, int depth, Environment env, String title, String author) {
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
			queueTileUpdate(x, 0, m_depth/2 - 1);
			queueTileUpdate(x, m_height - 1, m_depth/2 - 1);
		}

		for (int y = 0;y < m_height; y++) {
			m_logger.debug("Activating ocean: " + (y * 2 + m_width * 2) + "/" + (m_width * 2 + m_height * 2));
			queueTileUpdate(0, y, m_depth/2 - 1);
			queueTileUpdate(m_width - 1, y, m_depth/2 - 1);
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
			if (BlockManager.getBlockManager().getBlock(this.getBlock(pos.getX(), pos.getY(), pos.getZ())).hasGravity()) {
				if (!blockIsStable(pos.getX(), pos.getY(), pos.getZ())) {
					setBlock(pos.getX(), pos.getY(), pos.getZ() - 1, getBlock(pos.getX(), pos.getY(), pos.getZ()));
					setBlock(pos.getX(), pos.getY(), pos.getZ(), BlockConstants.AIR);
				}
			}
			BlockManager.getBlockManager().getBlock(this.getBlock(pos.getX(), pos.getY(), pos.getZ())).behavePassive(this, pos.getX(), pos.getY(), pos.getZ());
		}
		// we only process up to 20 of each type of thinking block every tick,
		// or we'd probably be here all day.
		for (int type = 0; type < 256; type++) {
			if (m_activeBlocks.containsKey(type)) {
				if (System.currentTimeMillis() - m_activeTimers.get(type) > BlockManager.getBlockManager().getBlock(type).getTimer()) {
					int cyclesThisTick = (m_activeBlocks.get(type).size() > 600 ? 600 : m_activeBlocks.get(type).size());
					for (int i = 0; i < cyclesThisTick; i++) {
						Position pos = m_activeBlocks.get(type).poll();
						if (pos == null)
							break;
						// the block that occupies this space might have
						// changed.
						if (getBlock(pos.getX(), pos.getY(), pos.getZ()) == type) {
							// World.getWorld().broadcast("Processing thinker at ("+pos.getX()+","+pos.getY()+","+pos.getZ()+")");
							BlockManager.getBlockManager().getBlock(type).behaveSchedule(this, pos.getX(), pos.getY(), pos.getZ());
						}
					}
					m_activeTimers.put(type, System.currentTimeMillis());
				}
			}
		}
	}

	public void setBlock(int x, int y, int z, int type) {
		setBlock(x, y, z, type, true);
	}
	
	/**
	 * Sets a block.
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 * @param z The z coordinate.
	 * @param type The type id.
	 * @param updateSelf Update self flag.
	 */
	public void setBlock(int x, int y, int z, int type, boolean updateSelf) {
		if (x < 0 || y < 0 || z < 0 || x >= m_width || y >= m_height || z >= m_depth) {
			return;
		}
		byte formerBlock = getBlock(x, y, z);
		super.setBlock(x, y, z, type);
		if (m_handler != null)
			m_handler.onBlockChange(x, y, z);
		if (updateSelf) {
			queueTileUpdate(x, y, z);
		}
		if (type == 0) {
			BlockManager.getBlockManager().getBlock(formerBlock).behaveDestruct(this, x, y, z);
			updateNeighboursAt(x, y, z);
			if (getLightDepth(x, y) == z) {
				recalculateLightDepth(x, y);
				scheduleZPlantThink(x, y, z);
			}
		}
		if (BlockManager.getBlockManager().getBlock(type).doesThink()) {
			assert(m_activeBlocks!=null);
			assert(m_activeBlocks.get(type) != null);
			m_activeBlocks.get(type).add(new Position(x, y, z));
		}
		if (BlockManager.getBlockManager().getBlock(type).doesBlockLight()) {
			assignLightDepth(x, y, z);
			scheduleZPlantThink(x, y, z);
		}
		
	}

	/**
	 * Schedules plants to think in a Z coordinate if a block above them
	 * changed.
	 * @param x X coordinate.
	 * @param y Y coordinate.
	 * @param z Z coordinate.
	 */
	public void scheduleZPlantThink(int x, int y, int z) {
		for (int i = z - 1; i > 0; i--) {
			if (BlockManager.getBlockManager().getBlock(this.getBlock(x, y, i)).isPlant()) {
				queueActiveBlockUpdate(x, y, i);
			}
			if (BlockManager.getBlockManager().getBlock(this.getBlock(x, y, i)).doesBlockLight()) {
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
	private void updateNeighboursAt(int x, int y, int z) {
		queueTileUpdate(x - 1, y, z);
		queueTileUpdate(x, y - 1, z);
		queueTileUpdate(x + 1, y, z);
		queueTileUpdate(x, y + 1, z);
		queueTileUpdate(x, y, z - 1);
		queueTileUpdate(x, y, z + 1);
		recalculateLightDepth(x, y);
	}
	
	/**
	 * Queues a tile update.
	 * @param x X coordinate.
	 * @param y Y coordinate.
	 * @param z Z coordinate.
	 */
	private void queueTileUpdate(int x, int y, int z) {
		if (x >= 0 && y >= 0 && z >= 0 && x < m_width && y < m_height && z < m_depth) {
			Position pos = new Position(x, y, z);
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
	public void queueActiveBlockUpdate(int x, int y, int z) {
		if (x >= 0 && y >= 0 && z >= 0 && x < m_width && y < m_height && z < m_depth) {
			int blockAt = getBlock(x, y, z);
			if (BlockManager.getBlockManager().getBlock(blockAt).doesThink()) {
				m_activeBlocks.get(blockAt).add(new Position(x, y, z));
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
		WorldManager.save(this);
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
