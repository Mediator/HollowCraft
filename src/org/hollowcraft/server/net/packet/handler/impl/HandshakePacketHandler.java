package org.hollowcraft.server.net.packet.handler.impl;
/*
 * HollowCraft License
 *  Copyright (c) 2010 Caleb Champlin.
 *  All rights reserved
 *  This license must be include in all copied, cloned and derived works 
 */


import org.hollowcraft.server.Constants;
import org.hollowcraft.server.Server;
import org.hollowcraft.server.heartbeat.HeartbeatManager;
import org.hollowcraft.server.net.MinecraftSession;
import org.hollowcraft.server.net.actions.impl.AlphaActionSender;
import org.hollowcraft.server.net.packet.Packet;
import org.hollowcraft.server.net.packet.handler.PacketHandler;
import org.slf4j.*;


/**
 * Handles the incoming handshake packet.
 * @author Caleb Champlin
 */
public final class HandshakePacketHandler implements PacketHandler<MinecraftSession> {
	
	/**
	 * Logger instance.
	 */
	private static final Logger logger = LoggerFactory.getLogger(HandshakePacketHandler.class);
	
	public void handlePacket(MinecraftSession session, Packet packet) {
		logger.info("MERDOM");
		if (session.isAuthenticated()) {
			return;
		}
		
		String username = packet.getStringField("username");
		logger.info("Received initial handshack packet : username=" + username);
		assert (session.getActionSender() != null);
		assert (HeartbeatManager.getHeartbeatManager() != null);
		((AlphaActionSender)session.getActionSender()).sendHandshakeReponse(HeartbeatManager.getHeartbeatManager().getConnectHash());
	}
}
