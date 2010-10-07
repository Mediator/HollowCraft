package org.hollowcraft.server.net.packet.handler.impl;
/*
 * HollowCraft License
 *  Copyright (c) 2010 Caleb Champlin.
 *  All rights reserved
 *  This license must be include in all copied, cloned and derived works 
 */


import org.hollowcraft.model.AbsolutePosition;
import org.hollowcraft.model.AbsoluteRotation;
import org.hollowcraft.model.Animation;
import org.hollowcraft.model.BlockActions;
import org.hollowcraft.model.Position;
import org.hollowcraft.server.model.Player;
import org.hollowcraft.server.net.MinecraftSession;
import org.hollowcraft.server.net.packet.Packet;
import org.hollowcraft.server.net.packet.handler.PacketHandler;

/**
 * A packet handler which handles movement packets.
 * @author Caleb Champlin
 */
public class ArmAnimationPacketHandler implements PacketHandler<MinecraftSession> {
	
	public void handlePacket(MinecraftSession session, Packet packet) {
		if (!session.isAuthenticated()) {
			return;
		}
		
		byte position = (byte)packet.getNumericField("position").byteValue();
		Animation armAnimation = session.getPlayer().getAnimation("arm");
		if (armAnimation == null)
		{
			armAnimation = new Animation("arm", 18, position);
			session.getPlayer().addAnimation(armAnimation);
		}
		else
			armAnimation.setPosition(position);
	}
	
}
