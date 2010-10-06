package org.opencraft.server.security;

public class Grant {
	private Permission m_perm;
	private Principal m_target;
	public Grant(Principal target, Permission perm) {
		m_perm = perm;
		m_target = target;
	}

	public Permission permission() {
		return m_perm;
	}

	public Principal principal() {
		return m_target;
	}

	public boolean equals(Object o) {
		if (o instanceof Grant) {
			Grant other = (Grant)o;
			return m_target.equals(other.m_target) && m_perm.implies(other.m_perm);
		}
		return false;
	}

	public String toString() {
		return "("+m_target+","+m_perm+")";
	}
}
