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
		String[] otherName = permission.m_name.split(" ");
		String[] name = m_name.split(" ");
		int segment = 0;
		while(segment < otherName.length && segment < name.length) {
			logger.trace("Testing {} against {}", name[segment], otherName[segment]);
			if (name[segment].equals('*'))
				return true;
			if (!name[segment].equals(otherName[segment]))
				return false;
		}
		return true;
	}

	public String toString() {
		return m_name;
	}
}
