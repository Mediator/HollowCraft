package org.hollowcraft.server.net.actions.impl;
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
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */



import org.hollowcraft.model.AbsolutePosition;
import org.hollowcraft.model.AbsoluteRotation;
import org.hollowcraft.model.Entity;
import org.hollowcraft.model.Position;
import org.hollowcraft.model.Rotation;
import org.hollowcraft.server.model.World;
import org.hollowcraft.server.model.impl.worlds.ClassicWorld;
import org.hollowcraft.server.net.MinecraftSession;
import org.hollowcraft.server.net.actions.ActionSender;
import org.hollowcraft.server.net.packet.PacketBuilder;
import org.hollowcraft.server.persistence.LoadPersistenceRequest;
import org.hollowcraft.server.persistence.SavedGameManager;
import org.hollowcraft.server.task.Task;
import org.hollowcraft.server.task.TaskQueue;
import org.slf4j.*;


/**
 * A utility class for sending packets.
 * @author Graham Edgecombe
 * @author Caleb Champlin
 */
public class ClassicActionSender extends ActionSender {
	
	/**
	 * The session.
	 */

	private static final Logger logger = LoggerFactory.getLogger(ActionSender.class);
	
	/**
	 * Creates the action sender.
	 * @param session The session.
	 */
	public ClassicActionSender(MinecraftSession session) {
		super(session);
	}
	
	/**
	 * Sends a login response.
	 * @param protocolVersion The protocol version.
	 * @param name The server name.
	 * @param message The server message of the day.
	 * @param op Operator flag.
	 */
	public void sendLoginResponse(int protocolVersion, String name, String message, boolean op) {
		PacketBuilder bldr = new PacketBuilder(session.protocol().packets().getOutgoingPacket(0));
		bldr.putByte("protocol_version", protocolVersion);
		bldr.putString("server_name", name);
		bldr.putString("server_message", message);
		bldr.putByte("user_type", op ? 100 : 0);
		logger.trace("Sending login response");
		session.send(bldr.toPacket());
	}
	
	/**
	 * Sends a login failure.
	 * @param message The message to send to the client.
	 */
	public void sendLoginFailure(String message) {
		logger.info("Login failure: {}", message);
		PacketBuilder bldr = new PacketBuilder(session.protocol().packets().getOutgoingPacket(14));
		bldr.putString("reason", message);
		logger.trace("Sending login failure");
		session.send(bldr.toPacket());
		session.close();
	}
	
	/**
	 * Sends the level init packet.
	 */
	public void sendWorldInit() {
		session.setAuthenticated();
		PacketBuilder bldr = new PacketBuilder(session.protocol().packets().getOutgoingPacket(2));
		logger.trace("Sending level init");
		session.send(bldr.toPacket());
	}
	
	/**
	 * Sends a level block/chunk.
	 * @param len The length of the chunk.
	 * @param chunk The chunk data.
	 * @param percent The percentage.
	 */
	public void sendWorldBlock(int len, byte[] chunk, int percent) {
		PacketBuilder bldr = new PacketBuilder(session.protocol().packets().getOutgoingPacket(3));
		bldr.putShort("chunk_length", len);
		bldr.putByteArray("chunk_data", chunk);
		bldr.putByte("percent", percent);
		logger.trace("Sending block");
		session.send(bldr.toPacket());
	}
	
	/**
	 * Sends the level finish packet.
	 */
	public void sendWorldFinish() {
		TaskQueue.getTaskQueue().push(new Task() {
			public void execute() {
				// for thread safety
				ClassicWorld level = (ClassicWorld)session.getPlayer().getWorld();
				PacketBuilder bldr = new PacketBuilder(session.protocol().packets().getOutgoingPacket(4));
				bldr.putShort("width", level.getWidth());
				bldr.putShort("height", level.getHeight());
				bldr.putShort("depth", level.getDepth());
				logger.trace("Sending level finish");
				session.send(bldr.toPacket());
				sendAddEntity(session.getPlayer(), -1);
				sendTeleport(level.getSpawnPosition(), level.getSpawnRotation());
				// now load the player's game (TODO in the future do this in parallel with loading the level)
				// TODO: We should use this to save what world a player was last
				// on
				SavedGameManager.getSavedGameManager().queuePersistenceRequest(new LoadPersistenceRequest(session.getPlayer()));
			}
		});
	}
	
	/**
	 * Sends a teleport.
	 * @param position The new position.
	 * @param rotation The new rotation.
	 */
	public void sendTeleport(Position position, Rotation rotation) {
		PacketBuilder bldr = new PacketBuilder(session.protocol().packets().getOutgoingPacket(8));
		bldr.putByte("id", -1);
		bldr.putShort("x", position.getX());
		bldr.putShort("y", position.getY());
		bldr.putShort("z", position.getZ());
		bldr.putByte("rotation", rotation.getRotation());
		bldr.putByte("look", rotation.getLook());
		logger.trace("Sending teleport");
		session.send(bldr.toPacket());
	}

	public void sendAddEntity(Entity entity, int id) {
		PacketBuilder bldr = new PacketBuilder(session.protocol().packets().getOutgoingPacket(7));
		bldr.putByte("id", id);
		bldr.putString("name", entity.getName());
		bldr.putShort("x", (int)entity.getPosition().getX());
		bldr.putShort("y", (int)entity.getPosition().getY());
		bldr.putShort("z", (int)entity.getPosition().getZ());
		bldr.putByte("rotation", (int)entity.getRotation().getRotation());
		bldr.putByte("look", (int)entity.getRotation().getLook());
		logger.trace("Sending add entity");
		session.send(bldr.toPacket());
	}
	
	/**
	 * Sends the add entity packet.
	 * @param entity The entity being added.
	 */
	public void sendAddEntity(Entity entity) {
		sendAddEntity(entity, entity.getId());
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
		
		final int deltaX = (int)(-oldPosition.getX() - position.getX());
		final int deltaY = (int)(-oldPosition.getY() - position.getY());
		final int deltaZ = (int)(-oldPosition.getZ() - position.getZ());
		
		final int deltaRotation = (int)(-oldRotation.getRotation() - rotation.getRotation());
		final int deltaLook = (int)(-oldRotation.getLook() - rotation.getLook());
		logger.trace("Sending update entity");
		
		if (deltaX > Byte.MAX_VALUE || deltaX < Byte.MIN_VALUE || deltaY > Byte.MAX_VALUE || deltaY < Byte.MIN_VALUE || deltaZ > Byte.MAX_VALUE || deltaZ < Byte.MIN_VALUE || deltaRotation > Byte.MAX_VALUE || deltaRotation < Byte.MIN_VALUE || deltaLook > Byte.MAX_VALUE || deltaLook < Byte.MIN_VALUE) {
			// teleport
			PacketBuilder bldr = new PacketBuilder(session.protocol().packets().getOutgoingPacket(8));
			bldr.putByte("id", entity.getId());
			bldr.putShort("x", (int)position.getX());
			bldr.putShort("y", (int)position.getY());
			bldr.putShort("z", (int)position.getZ());
			bldr.putByte("rotation", (int)rotation.getRotation());
			bldr.putByte("look", (int)rotation.getLook());
			session.send(bldr.toPacket());
		} else {
			// send move and rotate packet
			PacketBuilder bldr = new PacketBuilder(session.protocol().packets().getOutgoingPacket(9));
			bldr.putByte("id", entity.getId());
			bldr.putByte("delta_x", deltaX);
			bldr.putByte("delta_y", deltaY);
			bldr.putByte("delta_z", deltaZ);
			bldr.putByte("delta_rotation", deltaRotation);
			bldr.putByte("delta_look", deltaLook);
			session.send(bldr.toPacket());
		}
	}
	
	/**
	 * Sends the remove entity packet.
	 * @param entity The entity being removed.
	 */
	public void sendRemoveEntity(Entity entity) {
		PacketBuilder bldr = new PacketBuilder(session.protocol().packets().getOutgoingPacket(12));
		bldr.putByte("id", entity.getOldId());
		logger.trace("Sending remove entity");
		session.send(bldr.toPacket());
	}
	
	/**
	 * Sends a block.
	 * @param x X coordinate.
	 * @param y Y coordinate.
	 * @param z Z coordinate.
	 * @param type BlockDefinition type.
	 */
	public void sendBlock(Position pos, byte type, byte meta) {
		PacketBuilder bldr = new PacketBuilder(session.protocol().packets().getOutgoingPacket(6));
		bldr.putShort("x", pos.getX());
		bldr.putShort("y", pos.getY());
		bldr.putShort("z", pos.getZ());
		bldr.putByte("type", type);
		logger.trace("Sending block");
		session.send(bldr.toPacket());
	}
	
	/**
	 * Sends a chat message.
	 * @param message The message.
	 */
	public void sendChatMessage(String message) {
			sendChatMessage(-1, message);
	}

	/**
	 * Sends a chat message.
	 * @param id The source player id.
	 * @param message The message.
	 */
	public void sendChatMessage(int id, String message) {
		PacketBuilder bldr = new PacketBuilder(session.protocol().packets().getOutgoingPacket(13));
		bldr.putByte("id", id);
		bldr.putString("message", message);
		logger.trace("Sending chat message: {}", message);
		session.send(bldr.toPacket());
	}
	
}
