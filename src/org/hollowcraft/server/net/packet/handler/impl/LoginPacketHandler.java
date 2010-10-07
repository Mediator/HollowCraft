package org.hollowcraft.server.net.packet.handler.impl;
/*
 * HollowCraft License
 *  Copyright (c) 2010 Caleb Champlin.
 *  All rights reserved
 *  This license must be include in all copied, cloned and derived works 
 */

import org.hollowcraft.server.Constants;
import org.hollowcraft.server.Server;
import org.hollowcraft.server.net.MinecraftSession;
import org.hollowcraft.server.net.packet.Packet;
import org.hollowcraft.server.net.packet.handler.PacketHandler;
import org.slf4j.*;


/**
 * Handles the incoming login packet.
 * @author Caleb Champlin
 * 
 */
public final class LoginPacketHandler implements PacketHandler<MinecraftSession> {
	
	/**
	 * Logger instance.
	 */
	private static final Logger logger = LoggerFactory.getLogger(LoginPacketHandler.class);
	
	public void handlePacket(MinecraftSession session, Packet packet) {
		logger.info("MER");
		if (session.isAuthenticated()) {
			return;
		}
		String username = packet.getStringField("username");
		String password = packet.getStringField("password");
		// TODO IMPLEMENT PASSWORD CHECKING
		// TODO IMPLEMENT AUTHENTICATION VERIFICATION
		int protocolVersion = packet.getNumericField("protocol_version").intValue();
		logger.info("Received authentication packet : username=" + username + ", password=" + password + ", protocolVersion=" + protocolVersion + ".");
		
		if (protocolVersion != Constants.PROTOCOL_VERSION) {
			logger.info("Protocol version mismatch expected=" + Constants.PROTOCOL_VERSION + " received=" + protocolVersion);
			try
			{
			session.getActionSender().sendLoginFailure("Incorrect protocol version.");
			}
			catch (Exception ex)
			{
				logger.warn("Failed to send login failure");
			}
		} else {
			Server.getServer().register(session, username);
		}
	}
}
