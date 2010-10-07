package org.hollowcraft.server.security;

public class RoleRef extends Role {
	private Policy m_policy;

	public RoleRef(Policy policy, String name) {
		super(name);
		m_policy = policy;
	}

	public Permission[] getPermissions() {
		return m_policy.role(name()).getPermissions();
	}

	public void addPermission(Permission p) {
		m_policy.role(name()).addPermission(p);
	}

	public boolean implies(Permission p) {
		assert (m_policy != null);
		assert (name() != null);
		assert(p != null);
		return m_policy.role(name()).implies(p);
	}

}
