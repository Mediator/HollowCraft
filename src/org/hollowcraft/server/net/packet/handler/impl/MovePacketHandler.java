package org.hollowcraft.server.net.packet.handler.impl;
/*
 * HollowCraft License
 *  Copyright (c) 2010 Caleb Champlin.
 *  All rights reserved
 *  This license must be include in all copied, cloned and derived works 
 */

import org.hollowcraft.model.AbsolutePosition;
import org.hollowcraft.model.AbsoluteRotation;
import org.hollowcraft.model.Position;
import org.hollowcraft.model.Rotation;
import org.hollowcraft.server.io.WorldManager;
import org.hollowcraft.server.model.Player;
import org.hollowcraft.server.net.MinecraftSession;
import org.hollowcraft.server.net.packet.Packet;
import org.hollowcraft.server.net.packet.handler.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A packet handler which handles move packets.
 * @author Caleb Champlin
 */
public class MovePacketHandler implements PacketHandler<MinecraftSession> {
	
	private static final Logger logger = LoggerFactory.getLogger(MovePacketHandler.class);
	
	
	public void handlePacket(MinecraftSession session, Packet packet) {
		if (!session.isAuthenticated()) {
			return;
		}
		logger.debug("Player Position ( x = " + packet.getNumericField("x").doubleValue() + ", y = " + packet.getNumericField("y").doubleValue() +", z = " + packet.getNumericField("z").doubleValue() + ", stance = " + packet.getNumericField("stance").doubleValue() + ", flying = " + packet.getNumericField("flying").byteValue() + " )" );
		AbsolutePosition newPos = new AbsolutePosition(packet.getNumericField("x").doubleValue(),packet.getNumericField("y").doubleValue(),packet.getNumericField("z").doubleValue(), packet.getNumericField("stance").doubleValue());
		session.getPlayer().setPosition(newPos);
		try
		{
			WorldManager.getInstance().gzipWorld(session);
		}
		catch (Exception ex)
		{
			//TODO DEAL WITH THIS
		}
	}
	
}
