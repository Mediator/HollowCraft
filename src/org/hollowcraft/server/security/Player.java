package org.hollowcraft.server.security;

public class Player implements Principal {
	private String m_name;
	private Policy m_policy;

	public Player(String name) {
		m_name = name;
	}

	public String toString() {
		return m_name;
	}

	public boolean isAuthorized(Permission permission) {
		return m_policy.isAuthorized(permission, this);
	}

	public void setPolicy(Policy p) {
		m_policy = p;
	}

	public Permission[] getPermissions() {
		return m_policy.getPermissions(this);
	}

	public void grant(Permission p) {
		m_policy.grant(this, p);
	}

	public String name() {
		return m_name;
	}

	public Policy policy() {
		return m_policy;
	}
}
