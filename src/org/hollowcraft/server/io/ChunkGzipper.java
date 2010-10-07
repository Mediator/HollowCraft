package org.hollowcraft.server.io;
/*
 * HollowCraft License
 *  Copyright (c) 2010 Caleb Champlin.
 *  All rights reserved
 *  This license must be include in all copied, cloned and derived works 
 */


import java.io.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.mina.core.buffer.IoBuffer;
import org.hollowcraft.model.AbsolutePosition;
import org.hollowcraft.model.AbsoluteRotation;
import org.hollowcraft.server.model.Chunk;
import org.hollowcraft.server.model.Player;
import org.hollowcraft.server.model.impl.worlds.AlphaWorld;
import org.hollowcraft.server.net.MinecraftSession;
import org.hollowcraft.server.net.actions.impl.AlphaActionSender;
import org.hollowcraft.server.persistence.LoadPersistenceRequest;
import org.hollowcraft.server.persistence.SavedGameManager;
import org.slf4j.*;

/**
 * A utility class for gzipping chunks.
 * @author Caleb Champlin
 */
public final class ChunkGzipper {
	
	/**
	 * The singleton instance.
	 */
	private static final ChunkGzipper INSTANCE = new ChunkGzipper();
	
	/**
	 * Gets the level gzipper.
	 * @return The level gzipper.
	 */
	public static ChunkGzipper getChunkGzipper() {
		return INSTANCE;
	}
	
	/**
	 * The executor service.
	 */
	private ExecutorService service = Executors.newCachedThreadPool();
	
	/**
	 * Default private constructor.
	 */
	private ChunkGzipper() {
		/* empty */
	}

	private static final Logger logger = LoggerFactory.getLogger(ChunkGzipper.class);


	/**
	 * Gzips and sends the chunk for the specified session.
	 * @param session The session.
	 */
	public void gzipChunks(final MinecraftSession session) {
		final Queue<Chunk> chunksToSend = new LinkedList<Chunk>();
		logger.debug("Gzipping chunks to {}", session);
		assert(session!=null);
		assert(session.getPlayer()!=null);
		assert(session.getPlayer().getWorld() != null);
		final Player player = session.getPlayer();
		final AlphaWorld level = (AlphaWorld)player.getWorld();
		final boolean setPosition = !player.getChunksLoaded();
		if (setPosition)
		{
			logger.debug("Setting player position to: x=" + level.getEnvironment().getSpawnX() + " y=" + level.getEnvironment().getSpawnY() + " z=" +level.getEnvironment().getSpawnZ());
			player.setPosition(new AbsolutePosition(level.getEnvironment().getSpawnX(),level.getEnvironment().getSpawnY(),level.getEnvironment().getSpawnZ()));
			//session.getPlayer().setPosition(new AbsolutePosition(0,level.getEnvironment().getSpawnY(),0));
			player.setRotation(new AbsoluteRotation(0,0));
			//((AlphaActionSender)session.getActionSender()).sendWorldSpawn((int)session.getPlayer().getPosition().getX(),(int)session.getPlayer().getPosition().getY(),(int)session.getPlayer().getPosition().getZ());
			//((AlphaActionSender)session.getActionSender()).sendMoveAndLook(session.getPlayer());
		}
		final int ChunkRange = 16;
		final int xChunksNeeded = (int)((int)player.getPosition().getX() / 16) - (ChunkRange / 2);
		final int zChunksNeeded = (int)((int)player.getPosition().getZ() / 16) - (ChunkRange / 2);
		logger.debug("Chunks needed : x=" + xChunksNeeded + " z=" + zChunksNeeded);
		final int xLoadedChunks = player.getXLoadedChunks();
		final int zLoadedChunks = player.getZLoadedChunks();
		final boolean chunksLoaded = player.getChunksLoaded();

		service.submit(new Runnable() {
			public void run() {
				try {
					
					

					if (xChunksNeeded == xLoadedChunks && zChunksNeeded == zLoadedChunks)
						return;
					
					for (int xChunk = xChunksNeeded; xChunk < (xChunksNeeded + ChunkRange); xChunk++) {
						for (int zChunk = zChunksNeeded; zChunk < (zChunksNeeded + ChunkRange); zChunk++) {
							if ((xChunk >= (xLoadedChunks + ChunkRange) || xChunk < (xLoadedChunks) || 
								zChunk >= (zLoadedChunks + ChunkRange) || zChunk < (zLoadedChunks))	|| 
								(!chunksLoaded)) {
								
								Chunk chunkToSend = level.getChunkAt(xChunk, zChunk);
								if (chunkToSend == null) {
									logger.debug("Failure retrieving chunk");
									//TODO KICK CLIENT
									return;
								}
								chunkToSend.addPlayerToChunk(player);
								//if (!chunkToSend.terrainPopulated)
								//	chunkToSend.CalculateLighting();
								((AlphaActionSender)session.getActionSender()).sendWorldPreChunk(chunkToSend, true);
								chunksToSend.add(chunkToSend);
							}
						}
					}

					// Unload old chunks (if they have old chunks)
					if (player.getChunksLoaded()) {
						for (int xChunk = xLoadedChunks; xChunk < (xLoadedChunks + ChunkRange); xChunk++) {
							for (int zChunk = zLoadedChunks; zChunk < (zLoadedChunks + ChunkRange); zChunk++) {
								if ((xChunk >= (xChunksNeeded + ChunkRange) || xChunk < (xChunksNeeded) || 
									zChunk >= (zChunksNeeded + ChunkRange) || zChunk < (zChunksNeeded)) 
									&& chunksLoaded) {
									Chunk chunkToRemove = level.getChunkAt(xChunk, zChunk);
									if (chunkToRemove != null)
									{
										((AlphaActionSender)session.getActionSender()).sendWorldPreChunk(chunkToRemove, false);
										chunkToRemove.removePlayerFromChunk(player);
										if (!chunkToRemove.hasPlayers())
											level.unloadChunk(chunkToRemove);
									}
								}
							}
						}
					} else {
						player.setChunksLoaded(true);
					}

					// Set new loaded chunk range
					player.setXLoadedChunks(xChunksNeeded);
					player.setZLoadedChunks(zChunksNeeded);
					
					
					
					while (!chunksToSend.isEmpty())
					{
						Chunk chunk = chunksToSend.poll();
						int[] chunkBlocks = chunk.getChunkBlocks();
						
						
						
						byte[] outBlocks = new byte[chunkBlocks.length];
						byte[] outMetadata = new byte[chunkBlocks.length / 2];
						byte[] outBlockLight = new byte[chunkBlocks.length / 2];
						byte[] outSkyLight = new byte[chunkBlocks.length / 2];
						byte type;
						byte meta, meta2;
						byte blockLight, blockLight2;
						byte skyLight, skyLight2;
						for (int x = 0; x < chunkBlocks.length; x++)
						{
							type=(byte)((chunkBlocks[x] >> 24) & 0xFF);
							meta=(byte)((chunkBlocks[x] >> 16) & 0xFF);
							blockLight=(byte)((chunkBlocks[x] >> 8)& 0xFF);
							skyLight=(byte)((chunkBlocks[x]) & 0xFF);
							outBlocks[x] = type;
							
							
							if (x % 2 == 0)
							{
								meta2 = (byte)((chunkBlocks[x+1] >> 16) & 0xFF);
								blockLight2=(byte)((chunkBlocks[x+1] >> 8) & 0xFF);
								skyLight2=(byte)((chunkBlocks[x+1]) & 0xFF);
								outMetadata[x/2] = (byte)(((meta2 & 0x0F) << 4) | (meta & 0x0F));
								outSkyLight[x/2] = (byte)(((skyLight2 & 0x0F) << 4) | (skyLight & 0x0F));
								outBlockLight[x/2] = (byte)(((blockLight2 & 0x0F) << 4) | (blockLight & 0x0F));
								
							}
						}
						logger.debug("Compressing Chunk for x=" + chunk.getX() + " z=" + chunk.getZ() + " blocks=" + chunkBlocks.length + " size=" + (outBlocks.length + outMetadata.length + outSkyLight.length + outBlockLight.length));
						byte[] compressedChunk = null;
						ByteArrayOutputStream out = new ByteArrayOutputStream();
						DataOutputStream os = new DataOutputStream(new DeflaterOutputStream(out));
						os.write(outBlocks);
						os.write(outMetadata);
						os.write(outBlockLight);
						os.write(outSkyLight);
						os.close();
						compressedChunk = out.toByteArray();
						
						logger.trace("Compressed chunk size: " + compressedChunk.length);
						IoBuffer buf = IoBuffer.allocate(compressedChunk.length);
						buf.put(compressedChunk);
						buf.flip();

						byte[] outBound = new byte[buf.remaining()];
						buf.get(outBound);
						((AlphaActionSender)session.getActionSender()).sendWorldChunk(chunk, outBound);
						logger.trace("Chunk sent! size=" + outBound.length);
					}
					if (setPosition)
					{
						SavedGameManager.getSavedGameManager().queuePersistenceRequest(new LoadPersistenceRequest(player));
						((AlphaActionSender)session.getActionSender()).sendFullInventory();
						((AlphaActionSender)session.getActionSender()).sendMoveAndLook(player);
						((AlphaActionSender)session.getActionSender()).sendTime(level.getEnvironment().getTimeOfDay());
						player.setHasLoadedWorld(true);
					}
					
				} catch(IOException ex) {
					try {
						session.getActionSender().sendLoginFailure("Failed to gzip level. Please try again.");
					} catch (Exception e) {
						logger.warn("Failed to send login failure");
					}
				}
			}
		});
	}
}
