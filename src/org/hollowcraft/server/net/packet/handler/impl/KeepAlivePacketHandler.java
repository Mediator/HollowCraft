package org.hollowcraft.server.net.packet.handler.impl;
/*
 * HollowCraft License
 *  Copyright (c) 2010 Caleb Champlin.
 *  All rights reserved
 *  This license must be include in all copied, cloned and derived works 
 */


import org.hollowcraft.model.Position;
import org.hollowcraft.model.Rotation;
import org.hollowcraft.server.model.Player;
import org.hollowcraft.server.net.MinecraftSession;
import org.hollowcraft.server.net.actions.impl.AlphaActionSender;
import org.hollowcraft.server.net.packet.Packet;
import org.hollowcraft.server.net.packet.handler.PacketHandler;

/**
 * A packet handler which keep alive packets.
 * @author Caleb Champlin
 */
public class KeepAlivePacketHandler implements PacketHandler<MinecraftSession> {
	
	public void handlePacket(MinecraftSession session, Packet packet) {
		if (!session.isAuthenticated()) {
			return;
		}
		try
		{
			((AlphaActionSender)session.getActionSender()).sendKeepAlive();
		}
		catch (Exception ex)
		{
			
		}
	}
	
}
