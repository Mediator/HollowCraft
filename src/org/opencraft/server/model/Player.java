package org.opencraft.server.model;

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

import org.opencraft.server.net.ActionSender;
import org.opencraft.server.net.MinecraftSession;
import org.opencraft.server.io.LevelGzipper;
import org.opencraft.server.security.Group;
import org.opencraft.server.security.Principal;
import org.opencraft.server.security.Permission;
import java.util.ArrayList;
import org.slf4j.*;

/**
 * Represents a connected player.
 * @author Graham Edgecombe
 */
public class Player extends Entity implements Principal {
	
	/**
	 * The player's session.
	 */
	private final MinecraftSession session;

	private static final Logger logger = LoggerFactory.getLogger(Player.class);
	
	/**
	 * The player's name.
	 */
	private final String name;
	
	/**
	 * A map of attributes that can be attached to this player.
	 */
	private final Map<String, Object> attributes = new HashMap<String, Object>();
	
	/**
	 * Creates the player.
	 * @param name The player's name.
	 */
	public Player(MinecraftSession session, String name) {
		this.session = session;
		this.name = name;
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

	public boolean equals(Object another) {
		if (another instanceof Player) {
			Player p = (Player) another;
			return getName().equals(p.getName());
		}
		return false;
	}
	
	@Override
	public String getName() {
		return name;
	}

	private ArrayList<Permission> m_permissions = new ArrayList<Permission>();

	public Permission[] getPermissions() {
		return m_permissions.toArray(new Permission[m_permissions.size()]);
	}

	public void addPermission(Permission p) {
		m_permissions.add(p);
	}

	public void clearPolicy() {
		m_permissions = new ArrayList<Permission>();
	}

	public boolean isAuthorized(Permission perm) {
		for(Permission p : m_permissions) {
			logger.trace("Testing {} against {}", perm, p);
			if (p.implies(perm))
				return true;
		}
		for(Group group : getWorld().getPolicy().getGroups()) {
			logger.trace("Testing {} against {}", group, perm);
			if (group.hasMember(this) && group.isAuthorized(perm))
				return true;
		}
		return false;
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

	public void teleport(Position position, Rotation rotation) {
		setPosition(position);
		setRotation(rotation);
		session.getActionSender().sendTeleport(position, rotation);
	}

	private World m_world;

	public World getWorld() {
		return m_world;
	}

	public void setWorld(World world) {
		m_world = world;
	}

	public void moveToWorld(World world) {
		logger.debug("Moving player {} to world {}", this, world);
		if (m_world != null)
			m_world.removePlayer(this);
		assert(world != null);
		setWorld(world);
		m_world.addPlayer(this);
		m_world.getPolicy().apply(this);
		LevelGzipper.getLevelGzipper().gzipLevel(session);
	}
}
