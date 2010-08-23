package org.opencraft.server.security;

import org.slf4j.*;
import java.util.ArrayList;

public class Group implements Principal {
	private String m_name;

	private final static Logger logger = LoggerFactory.getLogger(Group.class);

	private Policy m_policy;
	private ArrayList<Principal> m_members = new ArrayList<Principal>();

	public Group(String name) {
		m_name = name;
	}

	public boolean equals(Object o) {
		if (o instanceof Group) {
			Group other = (Group)o;
			return true;
		}

		return false;
	}

	public void addPermission(Permission perm) {
		m_policy.grant(this, perm);
	}

	public void setPolicy(Policy p) {
		m_policy = p;
	}

	public boolean isAuthorized(Permission perm) {
		return m_policy.isAuthorized(perm, this);
	}

	public Principal[] getMembers() {
		return m_members.toArray(new Principal[m_members.size()]);
	}

	public void addMember(Principal p) {
		m_members.add(p);
	}

	public Permission[] getPermissions() {
		return m_policy.getPermissions(this);
	}

	public String name() {
		return m_name;
	}

	public Policy policy() {
		return m_policy;
	}

	public void grant(Permission p) {
		m_policy.grant(this, p);
	}

	public String toString() {
		return "@"+m_name;
	}
}
