package org.opencraft.server.security;
import org.slf4j.*;

public class Permission {
	public static final Permission BUILD = new Permission("org.opencraft.server.Build");
	public static final Permission DESTROY = new Permission("org.opencraft.server.Destroy");
	public static final Permission DESTROY_OWN = new Permission("org.opencraft.server.DestroyOwn");

	private String m_name;
	public Permission(String name) {
		m_name = name;
	}

	private static final Logger logger = LoggerFactory.getLogger(Permission.class);

	public boolean implies(Permission permission) {
		logger.trace("Testing {} against {}", this, permission);
		if (permission.m_name.equals(m_name)) {
			logger.trace("Exact match.");
			return true;
		}
		String[] otherName = permission.m_name.split("\\.");
		String[] name = m_name.split("\\.");
		int segment = 0;
		while(segment < otherName.length && segment < name.length) {
			logger.trace("Checking section {} against {}", name[segment], otherName[segment]);
			if (name[segment].equals('*')) {
				logger.trace("Found wildcard.");
				return true;
			}
			if (!name[segment].equals(otherName[segment])) {
				logger.trace("Found discrepency.");
				return false;
			}
			segment++;
		}
		if (name.length != otherName.length) {
			logger.trace("Ran out of tokens.");
			return false;
		}
		logger.trace("Found a match.");
		return true;
	}

	public String name() {
		return m_name;
	}

	public String toString() {
		return m_name;
	}
}
