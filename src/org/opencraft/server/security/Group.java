package org.opencraft.server.security;

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

import java.util.ArrayList;
import org.slf4j.*;

public class Group implements Principal {
	private String m_name;

	private final static Logger logger = LoggerFactory.getLogger(Group.class);
	
	private ArrayList<Principal> m_members;

	public Group(String name) {
		m_name = name;
		m_members = new ArrayList<Principal>();
	}

	public boolean equals(Object another) {
		if (another instanceof Group) {
			Group grp = (Group)another;
			return m_name.equals(grp.m_name);
		}
		return false;
	}

	public String getName() {
		return m_name;
	}

	public int hashCode() {
		return m_name.hashCode();
	}

	public String toString() {
		return m_name;
	}

	public boolean isAuthorized(Permission perm) {
		for(Permission p : m_permissions) {
			logger.trace("Testing permission {} on {}", perm, p);
			if (p.implies(perm))
				return true;
		}
		return false;
	}

	private ArrayList<Permission> m_permissions = new ArrayList<Permission>();

	public Permission[] getPermissions() {
		return m_permissions.toArray(new Permission[m_permissions.size()]);
	}

	public void addPermission(Permission p) {
		m_permissions.add(p);
	}

	public boolean hasMember(Principal p) {
		return m_members.contains(p);
	}

	public void addMember(Principal p) {
		m_members.add(p);
	}

	public void clearPolicy() {
		m_members = new ArrayList<Principal>();
		m_permissions = new ArrayList<Permission>();
	}

	public void removeMember(Principal p) {
		m_members.remove(p);
	}

	public Principal[] getMembers() {
		return m_members.toArray(new Principal[m_members.size()]);
	}
}
