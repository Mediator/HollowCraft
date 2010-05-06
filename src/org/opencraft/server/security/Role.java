package org.opencraft.server.security;

import java.util.ArrayList;

public class Role extends Permission {
	private String m_name;
	private boolean m_negative;

	private ArrayList<Permission> m_permissions = new ArrayList<Permission>();

	public Role(String name) {
		super(name);
		m_negative = false;
	}

	public Permission[] getPermissions() {
		return m_permissions.toArray(new Permission[m_permissions.size()]);
	}

	public void addPermission(Permission p) {
		m_permissions.add(p);
	}

	public void setNegative(boolean b) {
		m_negative = b;
	}

	public boolean isNegative() {
		return m_negative;
	}

	public boolean implies(Permission p) {
		for(Permission sub : m_permissions) {
			if (sub.implies(p))
				return !m_negative;
		}
		return !m_negative;
	}
}
