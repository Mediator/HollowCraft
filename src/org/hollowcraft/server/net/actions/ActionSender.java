package org.hollowcraft.server.net.actions;
/*
 * HollowCraft License
 *  Copyright (c) 2010 Caleb Champlin.
 *  All rights reserved
 *  This license must be include in all copied, cloned and derived works 
 */
import org.hollowcraft.model.Entity;
import org.hollowcraft.model.Position;
import org.hollowcraft.model.Rotation;
import org.hollowcraft.server.model.impl.worlds.ClassicWorld;
import org.hollowcraft.server.net.MinecraftSession;
import org.hollowcraft.server.net.packet.PacketBuilder;
import org.hollowcraft.server.persistence.LoadPersistenceRequest;
import org.hollowcraft.server.persistence.SavedGameManager;
import org.hollowcraft.server.task.Task;
import org.hollowcraft.server.task.TaskQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ActionSender {
	protected MinecraftSession session;


	/**
	 * Creates the action sender.
	 * @param session The session.
	 */
	public ActionSender(MinecraftSession session) {
		this.session = session;
	}
	
		
	
	public void sendLoginResponse(int protocolVersion, String name, String message, boolean op) throws Exception
	{
		throw new Exception("Not Implemented");
	}

	public void sendKeepAlive() throws Exception
	{
		throw new Exception("Not Implemented");
	}
	/**
	 * Sends a login failure.
	 * @param message The message to send to the client.
	 * @throws Exception 
	 */
	public void sendLoginFailure(String message) throws Exception
	{
		throw new Exception("Not Implemented");
	}
	
	/**
	 * Sends the level init packet.
	 * @throws Exception 
	 */
	public void sendWorldInit() throws Exception
	{
		throw new Exception("Not Implemented");	
	}
	
	/**
	 * Sends a level block/chunk.
	 * @param len The length of the chunk.
	 * @param chunk The chunk data.
	 * @param percent The percentage.
	 * @throws Exception 
	 */
	public  void sendWorldBlock(int len, byte[] chunk, int percent) throws Exception	
	{
		throw new Exception("Not Implemented");
	}
	
	/**
	 * Sends the level finish packet.
	 * @throws Exception 
	 */
	public void sendWorldFinish() throws Exception
	{
		throw new Exception("Not Implemented");
	}
	/**
	 * Sends a teleport.
	 * @param position The new position.
	 * @param rotation The new rotation.
	 * @throws Exception 
	 */
	public void sendTeleport(Position position, Rotation rotation) throws Exception
	{
		throw new Exception("Not Implemented");
	}
	
	public void sendAddEntity(Entity entity, int id) throws Exception
	{
		throw new Exception("Not Implemented");
	}
	
	/**
	 * Sends the add entity packet.
	 * @param entity The entity being added.
	 * @throws Exception 
	 */
	public void sendAddEntity(Entity entity) throws Exception
	{
		throw new Exception("Not Implemented");
	}
	/**
	 * Sends the update entity packet.
	 * @param entity The entity being updated.
	 * @throws Exception 
	 */
	public void sendUpdateEntity(Entity entity) throws Exception
	{
		throw new Exception("Not Implemented");
	}
	/**
	 * Sends the remove entity packet.
	 * @param entity The entity being removed.
	 * @throws Exception 
	 */
	public void sendRemoveEntity(Entity entity) throws Exception
	{
		throw new Exception("Not Implemented");
	}
	
	/**
	 * Sends a block.
	 * @param x X coordinate.
	 * @param y Y coordinate.
	 * @param z Z coordinate.
	 * @param type BlockDefinition type.
	 * @throws Exception 
	 */
	public void sendBlock(Position pos, byte type, byte meta) throws Exception	{
		throw new Exception("Not Implemented");
	}
	/**
	 * Sends a chat message.
	 * @param message The message.
	 * @throws Exception 
	 */
	public void sendChatMessage(String message) throws Exception
	{
		throw new Exception("Not Implemented");
	}

	/**
	 * Sends a chat message.
	 * @param id The source player id.
	 * @param message The message.
	 * @throws Exception 
	 */
	public void sendChatMessage(int id, String message) throws Exception
	{
		throw new Exception("Not Implemented");
	}
	
	
	
	
	
	
}
