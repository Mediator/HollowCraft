package org.opencraft.server.security;

import java.util.ArrayList;

public class Role extends Permission {
	private String m_name;

	private ArrayList<Permission> m_permissions = new ArrayList<Permission>();

	public Role(String name) {
		super(name);
	}

	public String toString() {
		return "@"+super.toString();
	}

	public Permission[] getPermissions() {
		return m_permissions.toArray(new Permission[m_permissions.size()]);
	}

	public void addPermission(Permission p) {
		m_permissions.add(p);
	}

	public boolean implies(Permission p) {
		for(Permission sub : m_permissions) {
			if (sub.implies(p))
				return true;
		}
		return false;
	}
}
