package org.hollowcraft.server.model;
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

import java.util.HashMap;
import java.util.Map;


import java.util.ArrayList;

import org.hollowcraft.model.AbsolutePosition;
import org.hollowcraft.model.AbsoluteRotation;
import org.hollowcraft.model.Animation;
import org.hollowcraft.model.Entity;
import org.hollowcraft.server.io.WorldManager;
import org.hollowcraft.server.model.World;
import org.hollowcraft.server.net.MinecraftSession;
import org.hollowcraft.server.net.actions.ActionSender;
import org.hollowcraft.server.security.Permission;
import org.hollowcraft.server.security.Policy;
import org.hollowcraft.server.security.Principal;
import org.slf4j.*;

/**
 * Represents a connected player.
 * @author Graham Edgecombe
 */
public class Player extends Entity implements Principal {
	
	/**
	 * The player's session.
	 */
	private boolean hasLoadedWorld;
	
	private final MinecraftSession session;

	private static final Logger logger = LoggerFactory.getLogger(Player.class);
	
	private ArrayList<Permission> m_permissions = new ArrayList<Permission>();
	
	/**
	 * A map of attributes that can be attached to this player.
	 */
	private final Map<String, Object> attributes = new HashMap<String, Object>();
	
	
	private final Map<String, Animation> animations = new HashMap<String, Animation>();
	private short holdingID = 0;
	private short oldHoldingID = 0;
	
	
	/**
	 * Creates the player.
	 * @param name The player's name.
	 */
	public Player(MinecraftSession session, String name) {
		oldHoldingID = 0;
		holdingID = 0;
		m_policyRef = new org.hollowcraft.server.security.Player(name);
		this.session = session;
	}

	public String toString() {
		return getName();
	}
	
	/**
	 * Sets an attribute of this player.
	 * @param name The name of the attribute.
	 * @param value The value of the attribute.
	 * @return The old value of the attribute, or <code>null</code> if there was
	 * no previous attribute with that name.
	 */
	public Object setAttribute(String name, Object value) {
		return attributes.put(name, value);
	}
	
	
	/**
	 * Sets an attribute of this player.
	 * @param name The name of the attribute.
	 * @param value The value of the attribute.
	 * @return The old value of the attribute, or <code>null</code> if there was
	 * no previous attribute with that name.
	 */
	public Object setAttribute(String name, Object value, boolean init) {
		
		//TODO Find a better way to do this :)
		if (init)
		{
			if (name.equalsIgnoreCase("holdingid"));
				holdingID = ((Number)value).shortValue();
			if (name.equalsIgnoreCase("onground"));
				super.setOnGround(((Boolean)value).booleanValue());
			if (name.equalsIgnoreCase("position"));
				super.setPosition((AbsolutePosition)value);
			if (name.equalsIgnoreCase("rotation"));
				super.setRotation((AbsoluteRotation)value);
				
		}
		return attributes.put(name, value);
	}
	
	/**
	 * Gets an attribute.
	 * @param name The name of the attribute.
	 * @return The attribute, or <code>null</code> if there is not an attribute
	 * with that name.
	 */
	public Object getAttribute(String name) {
		return attributes.get(name);
	}
	
	/**
	 * Checks if an attribute is set.
	 * @param name The name of the attribute.
	 * @return <code>true</code> if set, <code>false</code> if not.
	 */
	public boolean isAttributeSet(String name) {
		return attributes.containsKey(name);
	}
	
	/**
	 * Removes an attribute.
	 * @param name The name of the attribute.
	 * @return The old value of the attribute, or <code>null</code> if an
	 * attribute with that name did not exist.
	 */
	public Object removeAttribute(String name) {
		return attributes.remove(name);
	}

	
	
	public Animation getAnimation(String name)
	{
		if (animations.containsKey(name))
			return animations.get(name);
		return null;
	}
	public Animation[] getAnimations()
	{
		return (Animation[])animations.values().toArray();
	}
	public void addAnimation(Animation animation)
	{
		
		animations.put(animation.getName(), animation);
	}
	public void removeAnimation(Animation animation)
	{
		animations.remove(animation.getName());
	}
	public void resetOldPositionAndRotation() {
		super.resetOldPositionAndRotation();
		oldHoldingID = holdingID;
	}
	
	
	/**
	 * Gets the item the entity is holding
	 * @return item id
	 */
	public short getHoldingID() {
		return holdingID;
	}
	

	/**
	 * Sets the item the entity is holding
	 */
	public short getOldHoldingID() {
		return oldHoldingID;
	}
	
	
	public boolean equals(Object another) {
		if (another instanceof Player) {
			Player p = (Player) another;
			return getName().equals(p.getName());
		}
		return false;
	}
	
	public String getName() {
		return name();
	}

	public String name() {
		return m_policyRef.name();
	}

	public Permission[] getPermissions() {
		return m_policyRef.getPermissions();
	}

	public void grant(Permission p) {
		m_policyRef.grant(p);
	}

	public boolean isAuthorized(Permission perm) {
		return m_policyRef.isAuthorized(perm);
	}

	private org.hollowcraft.server.security.Player m_policyRef;

	public Policy policy() {
		return m_policyRef.policy();
	}

	public void setPolicy(Policy p) {
		m_policyRef.setPolicy(p);
	}
	
	/**
	 * Gets the player's session.
	 * @return The session.
	 */
	public MinecraftSession getSession() {
		return session;
	}
	
	/**
	 * Gets this player's action sender.
	 * @return The action sender.
	 */
	public ActionSender getActionSender() {
		return session.getActionSender();
	}

	/**
	 * Gets the attributes map.
	 * @return The attributes map.
	 */
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public void teleport(AbsolutePosition position, AbsoluteRotation rotation) {
		setPosition(position);
		setRotation(rotation);
	//	session.getActionSender().sendTeleport(position, rotation);
	}

	private World m_world;

	public World getWorld() {
		return m_world;
	}
	
	private boolean chunksLoaded;
	private int xLoadedChunks = 0;
	private int zLoadedChunks = 0;
	public boolean getChunksLoaded()
	{
		return chunksLoaded;
	}
	
	public void setChunksLoaded(boolean value)
	{
		chunksLoaded = value;
	}
	
	public int getXLoadedChunks()
	{
		return xLoadedChunks;
	}
	
	public void setXLoadedChunks(int value)
	{
		xLoadedChunks = value;
	}
	
	public int getZLoadedChunks()
	{
		return zLoadedChunks;
	}
	
	public void setZLoadedChunks(int value)
	{
		zLoadedChunks = value;
	}

	public void setWorld(World world) {
		m_world = world;
	}
	
	/**
	 * Sets the item the entity is holding
	 */
	public void setHoldingID(short value) {
		holdingID = value;
		setAttribute("holdingID", value);
	}
	
	/**
	 * Sets whether or not an entity is on the ground
	 */
	public void setOnGround(boolean value) {
		super.setOnGround(value);
		setAttribute("onGround", value);
	}
	
	/**
	 * Sets the position.
	 * @param position The position.
	 */
	public void setPosition(AbsolutePosition position) {
		super.setPosition(position);
		setAttribute("position", position);
	}
	
	/**
	 * Sets the rotation.
	 * @param rotation The rotation.
	 */
	public void setRotation(AbsoluteRotation rotation) {
		super.setRotation(rotation);
		setAttribute("rotation",rotation);
	}

	public void moveToWorld(World world) {
		logger.debug("Moving player {} to world {}", this, world);
		if (m_world != null)
			m_world.removePlayer(this);
		assert(world != null);
		setWorld(world);
		m_world.addPlayer(this);
		setPolicy(m_world.getPolicy());
		try
		{
		WorldManager.getInstance().gzipWorld(session);
		}
		catch (Exception ex)
		{
			logger.info("Failed to get world manager session in player move to world", ex);
		}
	}

	public boolean getHasLoadedWorld() {
		return hasLoadedWorld;
	}
	public void setHasLoadedWorld(boolean value) {
		hasLoadedWorld = value;
	}
}
