package org.hollowcraft.server.net.packet.handler.impl;
/*
 * HollowCraft License
 *  Copyright (c) 2010 Caleb Champlin.
 *  All rights reserved
 *  This license must be include in all copied, cloned and derived works 
 */


import org.hollowcraft.model.AbsolutePosition;
import org.hollowcraft.model.AbsoluteRotation;
import org.hollowcraft.model.BlockActions;
import org.hollowcraft.model.BlockManager;
import org.hollowcraft.model.Position;
import org.hollowcraft.server.model.Player;
import org.hollowcraft.server.net.MinecraftSession;
import org.hollowcraft.server.net.packet.Packet;
import org.hollowcraft.server.net.packet.handler.PacketHandler;

/**
 * A packet handler which handles movement packets.
 * @author Caleb Champlin
 */
public class DigPacketHandler implements PacketHandler<MinecraftSession> {
	
	public void handlePacket(MinecraftSession session, Packet packet) {
		if (!session.isAuthenticated()) {
			return;
		}
		
		int status = (int)packet.getNumericField("status").byteValue();
		int blockX = packet.getNumericField("x").intValue();
		int blockY = (int)packet.getNumericField("y").shortValue();
		int blockZ = packet.getNumericField("z").intValue();
		Position pos = new Position(blockX, blockY, blockZ);
		int face = (int)packet.getNumericField("status").byteValue();
		if (status == 3)
			session.getPlayer().getWorld().getGameMode().setBlock(session.getPlayer(), session.getPlayer().getWorld(), pos, BlockActions.DESTROY, BlockManager.getBlockManager().getBlock("AIR").getId());
	}
	
}
