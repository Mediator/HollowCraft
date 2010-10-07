package org.hollowcraft.server.net.actions.impl;
/*
 * HollowCraft License
 *  Copyright (c) 2010 Caleb Champlin.
 *  All rights reserved
 *  This license must be include in all copied, cloned and derived works 
 */

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.mina.core.buffer.IoBuffer;
import org.hollowcraft.model.AbsolutePosition;
import org.hollowcraft.model.AbsoluteRotation;
import org.hollowcraft.model.Animation;
import org.hollowcraft.model.Entity;
import org.hollowcraft.model.Position;
import org.hollowcraft.model.Rotation;
import org.hollowcraft.server.model.Chunk;
import org.hollowcraft.server.model.Player;
import org.hollowcraft.server.model.impl.worlds.AlphaWorld;
import org.hollowcraft.server.net.MinecraftSession;
import org.hollowcraft.server.net.actions.ActionSender;
import org.hollowcraft.server.net.packet.PacketBuilder;
import org.hollowcraft.server.persistence.LoadPersistenceRequest;
import org.hollowcraft.server.persistence.SavedGameManager;
import org.hollowcraft.server.task.Task;
import org.hollowcraft.server.task.TaskQueue;

import org.slf4j.*;


/**
 * A utility class for sending alpha packets.
 * @author Caleb Champlin
 */
public class AlphaActionSender extends ActionSender {
	
	private static final Logger logger = LoggerFactory.getLogger(AlphaActionSender.class);
	
	/**
	 * Creates the action sender.
	 * @param session The session.
	 */
	public AlphaActionSender(MinecraftSession session) {
		super(session);
	}
	/**
	 * Sends a handshake.
	 * @param serverHash The server hash for the server.
	 */
	public void sendHandshakeReponse(String serverHash) {
		PacketBuilder bldr = new PacketBuilder(session.protocol().packets().getOutgoingPacket(2));
		logger.info("Sending handshake response: server_hash=" + serverHash);
		bldr.putString("server_hash", serverHash);
		session.send(bldr.toPacket());
	}
	
	/**
	 * Sends a login response.
	 * @param protocolVersion The protocol version.
	 * @param name The server name.
	 * @param message The server message of the day.
	 * @param op Operator flag.
	 */
	public void sendLoginResponse(int protocolVersion, String name, String message, boolean op) {
		PacketBuilder bldr = new PacketBuilder(session.protocol().packets().getOutgoingPacket(1));
		bldr.putInt("protocol_version", protocolVersion);
		bldr.putString("server_name", "a");
		bldr.putString("server_message", "b");
		logger.info("Sending login response");
		session.send(bldr.toPacket());
	}
	
	/**
	 * Sends a login failure.
	 * @param message The message to send to the client.
	 */
	public void sendLoginFailure(String message) {
		logger.info("Login failure: {}", message);
		 sendKick(message);
	}
	
	/**
	 * Sends a login failure.
	 * @param message The message to send to the client.
	 */
	public void sendKick(String message) {
		logger.info("Kicking client: {}", message);
		PacketBuilder bldr = new PacketBuilder(session.protocol().packets().getOutgoingPacket(255));
		bldr.putString("message", message);
		session.send(bldr.toPacket());
		session.close();
	}
	
	public void sendWorldPreChunk(Chunk chunk, boolean load) {
		PacketBuilder bldr = new PacketBuilder(session.protocol().packets().getOutgoingPacket(50));
		logger.debug("Sending Pre-Chunk for x=" + chunk.getX() + " z=" + chunk.getZ() + " loading=" + load);
		bldr.putInt("x", chunk.getX());
		bldr.putInt("z", chunk.getZ());
		bldr.putByte("mode", (byte)(load ? 1 : 0));
		session.send(bldr.toPacket());
	}
	public void sendFullInventory() {
		try
		{
		PacketBuilder bldr = new PacketBuilder(session.protocol().packets().getOutgoingPacket(5));
		
		bldr.putInt("type", -1);
		bldr.putShort("count", 36);
		IoBuffer buf = IoBuffer.allocate(36 * 2);
		for (int x = 0; x < 36;x++)
			buf.putShort((short)-1);
		buf.flip();
		byte[] outBound = new byte[buf.remaining()];
		logger.info("Inventory payload length: " + outBound.length);
		buf.get(outBound);
		bldr.putInventory("payload", outBound);
		session.send(bldr.toPacket());
		
		bldr = new PacketBuilder(session.protocol().packets().getOutgoingPacket(5));
		
		bldr.putInt("type", -2);
		bldr.putShort("count", 4);
		buf = IoBuffer.allocate(4 * 2);
		for (int x = 0; x < 4;x++)
			buf.putShort((short)-1);
		
		
		buf.flip();
		outBound = new byte[buf.remaining()];
		buf.get(outBound);
		bldr.putInventory("payload", outBound);
		session.send(bldr.toPacket());
		
		bldr = new PacketBuilder(session.protocol().packets().getOutgoingPacket(5));
		bldr.putInt("type", -3);
		bldr.putShort("count", 4);
		buf = IoBuffer.allocate(4 * 2);
		for (int x = 0; x < 4;x++)
			buf.putShort((short)-1);
		buf.flip();
		outBound = new byte[buf.remaining()];
		buf.get(outBound);
		bldr.putInventory("payload", outBound);
		session.send(bldr.toPacket());
		}
		catch (Exception ex)
		{
			logger.warn("Exception sending inventory", ex);
		}
	}
	public void sendWorldChunk(Chunk chunk, byte[] compressedChunk)
	{
			PacketBuilder bldr = new PacketBuilder(session.protocol().packets().getOutgoingPacket(51));
			bldr.putInt("x", chunk.getX() * 16);
			bldr.putShort("y", (short)0);
			bldr.putInt("z", chunk.getZ() * 16);
			bldr.putByte("size_x", 15);
			bldr.putByte("size_y", 127);
			bldr.putByte("size_z", 15);
			logger.debug("Sending chunk");
			bldr.putByteArray("chunk", compressedChunk);
			session.send(bldr.toPacket());
	}
	public void sendKeepAlive()
	{
		PacketBuilder bldr = new PacketBuilder(session.protocol().packets().getOutgoingPacket(0));
		session.send(bldr.toPacket());
	}
	
	public void sendMoveAndLook(Entity ent)
	{
		PacketBuilder bldr = new PacketBuilder(session.protocol().packets().getOutgoingPacket(13));
		bldr.putDouble("x", ent.getPosition().getX());
		bldr.putDouble("y", ent.getPosition().getY());
		bldr.putDouble("stance", ent.getPosition().getY());
		bldr.putDouble("z", ent.getPosition().getZ());
		bldr.putFloat("rotation", ent.getRotation().getRotation());
		bldr.putFloat("pitch", ent.getRotation().getLook());
		bldr.putByte("flying", (byte)1);
		logger.info("Sending move and look " + ent.getPosition().getX() + " " + ent.getPosition().getY() + " " + ent.getPosition().getZ());
		session.send(bldr.toPacket());
	}
	public void sendWorldSpawn(int x, int y, int z) {
		PacketBuilder bldr = new PacketBuilder(session.protocol().packets().getOutgoingPacket(6));
		logger.info("Sending spawn info");
		bldr.putInt("x",x);
		bldr.putInt("y", y);
		bldr.putInt("z", z);
		session.send(bldr.toPacket());
	}
	public void sendTime(long time)
	{
		PacketBuilder bldr = new PacketBuilder(session.protocol().packets().getOutgoingPacket(4));
		bldr.putLong("time", time);
		logger.info("Sending time");
		session.send(bldr.toPacket());
	}
	
	public void sendBlock(Position pos, byte type, byte meta)
	{
		PacketBuilder bldr = new PacketBuilder(session.protocol().packets().getOutgoingPacket(53));
		bldr.putInt("x", pos.getX());
		bldr.putByte("y", pos.getY());
		bldr.putInt("z", pos.getZ());
		bldr.putByte("type", type);
		bldr.putByte("metadata", meta);
		session.send(bldr.toPacket());
	}
	
	/**
	 * Sends the update entity packet.
	 * @param entity The entity being updated.
	 */
	public void sendUpdateEntity(Entity entity) {
		final AbsolutePosition oldPosition = entity.getOldPosition();
		final AbsolutePosition position = entity.getPosition();
		
		final AbsoluteRotation oldRotation = entity.getOldRotation();
		final AbsoluteRotation rotation = entity.getRotation();
		
		final double deltaX = (-oldPosition.getX() - position.getX());
		final double deltaY = (-oldPosition.getY() - position.getY());
		final double deltaZ = (-oldPosition.getZ() - position.getZ());
		
		final int deltaRotation = (int)(-oldRotation.getRotation() - rotation.getRotation());
		final int deltaLook = (int)(-oldRotation.getLook() - rotation.getLook());
		logger.trace("Sending update entity");
		
		if (Math.abs(deltaX) > 4 || Math.abs(deltaY) > 4 || Math.abs(deltaZ) > 4) {
			// teleport
			PacketBuilder bldr = new PacketBuilder(session.protocol().packets().getOutgoingPacket(34));
			bldr.putInt("entity", entity.getId());
			bldr.putInt("x", (int)(position.getX() * 32));
			bldr.putInt("y", (int)(position.getY() * 32));
			bldr.putInt("z", (int)(position.getZ() * 32));
			bldr.putByte("rotation", (byte)(rotation.getRotation() * 255 / 360));
			bldr.putByte("pitch", (byte)(rotation.getLook() * 255 / 360));
			session.send(bldr.toPacket());
		} else {
			// send move and rotate packet
			PacketBuilder bldr = new PacketBuilder(session.protocol().packets().getOutgoingPacket(33));
			bldr.putInt("entity", entity.getId());
			bldr.putByte("x", (byte)(deltaX * 32));
			bldr.putByte("y", (byte)(deltaY * 32));
			bldr.putByte("z", (byte)(deltaZ * 32));
			bldr.putByte("rotation", (byte)(deltaRotation * 255 / 360 ));
			bldr.putByte("pitch", (byte)(deltaLook * 255 / 360 ));
			session.send(bldr.toPacket());
		}
		if (entity instanceof Player)
		{
			Player player = (Player)entity;
			Animation[] animations = player.getAnimations();
			for (Animation animation : animations)
			{
				if (animation.getPosition() > 0)
				{
					PacketBuilder bldr = new PacketBuilder(session.protocol().packets().getOutgoingPacket(animation.getAnimationID()));
					bldr.putInt("entity", entity.getId());
					bldr.putByte("position", animation.getPosition());
					session.send(bldr.toPacket());
				}
			}
		
			final short holdingID = player.getHoldingID();
			final short oldHoldingID = player.getOldHoldingID();
			if (holdingID != oldHoldingID)
			{
				PacketBuilder bldr = new PacketBuilder(session.protocol().packets().getOutgoingPacket(16));
				bldr.putInt("entity", entity.getId());
				bldr.putShort("item", holdingID);
				session.send(bldr.toPacket());
			}
		}
	}
	
		
}
