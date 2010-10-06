package org.opencraft.server.security;

public class GroupRef extends Group {
	public GroupRef(Policy p, String name) {
		super(name);
		setPolicy(p);
	}

	public boolean isAuthorized(Permission permission) {
		return policy().group(name()).isAuthorized(permission);
	}

	public Permission[] getPermissions() {
		return policy().group(name()).getPermissions();
	}

	public void addPermission(Permission p) {
		policy().group(name()).addPermission(p);
	}
}
