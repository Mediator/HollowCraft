package org.hollowcraft.server.security;

import org.slf4j.*;
import java.util.ArrayList;
import java.util.HashMap;
import org.antlr.runtime.tree.Tree;
import org.antlr.runtime.tree.BaseTree;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.ANTLRReaderStream;

import java.text.ParseException;
import java.io.IOException;
import java.io.Reader;

public class Policy {
	private static final Logger logger = LoggerFactory.getLogger(Policy.class);

	private HashMap<String, Group> m_groups = new HashMap<String, Group>();
	private ArrayList<Grant> m_grants = new ArrayList<Grant>();
	private HashMap<String, Role> m_roles = new HashMap<String, Role>();
	private HashMap<String, Policy> m_worlds = new HashMap<String, Policy>();
	private int m_version;

	public Policy() {

	}

	public Policy(Reader r) throws IOException, ParseException {
		readFrom(r);
	}

	public String toString() {
		return toPolicyFile();
	}

	public String toPolicyFile() {
		StringBuilder sb = new StringBuilder();
		sb.append("VERSION ");
		sb.append(m_version);
		sb.append("\n");
		for(Role r : m_roles.values()) {
			sb.append("ROLE "+r.name()+" {\n");
			for(Permission p : r.getPermissions()) {
				sb.append("    "+p+"\n");
			}
			sb.append("}\n\n");
		}
		for(Group g : m_groups.values()) {
			sb.append("GROUP "+g.name()+" {\n");
			for(Principal p : g.getMembers()) {
				sb.append("    "+p+"\n");
			}
			sb.append("}\n\n");
		}

		for(Grant g : m_grants) {
			sb.append("ALLOW "+g.permission()+" to "+g.principal()+"\n");
		}
		return sb.toString();
	}

	public Permission[] getPermissions(Principal target) {
		ArrayList<Permission> permissions = new ArrayList<Permission>();
		for(Grant g : m_grants) {
			Grant test = new Grant(target, g.permission());
			if (g.equals(test)) {
				permissions.add(g.permission());
			}
		}
		Permission[] ret = new Permission[permissions.size()];
		return permissions.toArray(ret);
	}

	public boolean isAuthorized(Permission permission, Principal target) {
		logger.info("Checking {} for {} permission", target, permission);
		Grant test = new Grant(target, permission);
		for(Grant g : m_grants) {
			logger.info("Comparing {} against {}" + g.equals(test), test, g);
			if (g.equals(test))
				return true;
		}
		for (Policy p : m_worlds.values())
		{
			if (p.isAuthorized(permission, target))
				return true;
		}
		return false;
	}

	public void readFrom(Reader r) throws IOException, ParseException {
		logger.trace("Lexer");
		SecurityPolicyLexer lex = new SecurityPolicyLexer(new ANTLRReaderStream(r));
		logger.trace("Token stream");
		CommonTokenStream tokens = new CommonTokenStream(lex);
		logger.trace("Parser");
		SecurityPolicyParser parser = new SecurityPolicyParser(tokens);
		BaseTree policy = null;
		try {
			logger.trace("Parsing!");
			policy = (BaseTree)parser.policy().getTree();
		} catch (RecognitionException e) {
			throw new ParseException(e.getMessage(), e.line);
		}
		Tree versionNode = policy.getFirstChildWithType(SecurityPolicyLexer.VERSION);
		m_version = Integer.parseInt(versionNode.getChild(0).getText());
		logger.trace("Loading policy version {}", m_version);
		logger.trace("Found {} blocks", policy.getChildCount());
		for(int i = 0;i<policy.getChildCount();i++) {
			Tree child = policy.getChild(i);
			parse(child);
		}
	}

	public void parse(Tree node) {
		switch(node.getType()) {
			case SecurityPolicyLexer.WORLD:
				parseWorld(node);
				break;
			case SecurityPolicyLexer.GROUP:
				parseGroup(node);
				break;
			case SecurityPolicyLexer.ROLE:
				parseRole(node);
				break;
			case SecurityPolicyLexer.ALLOW:
				parseAllow(node);
				break;
		}
	}

	public void parseWorld(Tree node) {
		Policy subPolicy = new Policy();
		subPolicy.m_version = m_version;
		String name = node.getChild(0).getText();
		for(int i = 1;i<node.getChildCount();i++) {
			subPolicy.parse(node.getChild(i));
		}
		m_worlds.put(name, subPolicy);
	}

	public void parseGroup(Tree node) {
		String name = node.getChild(0).getText();
		Group group = new Group(name);
		m_groups.put(name, group);
		Principal member = null;
		for(int i = 1;i<node.getChildCount();i++) {
			switch(node.getChild(i).getType()) {
				case SecurityPolicyLexer.ID:
					try
					{
					member = new Player(node.getChild(i).getText());
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
					break;
				case SecurityPolicyLexer.REFERENCE:
					member = new GroupRef(this, node.getChild(i).getText());
					break;
			}
		}
		if (group != null) //Might be a comment
			group.addMember(member);
	}

	public Group group(String name) {
		return m_groups.get(name);
	}

	public Role role(String name) {
		return m_roles.get(name);
	}

	public void parseRole(Tree node) {
		String name = node.getChild(0).getText();
		Role role = new Role(name);
		m_roles.put(name, role);
		Permission perm = null;
		for(int i = 1;i<node.getChildCount();i++) {
			switch(node.getChild(i).getType()) {
				case SecurityPolicyLexer.REFERENCE:
					perm = new RoleRef(this, node.getChild(i).getChild(0).getText());
					break;
				case SecurityPolicyLexer.FQN:
					String result = node.getChild(i).getChild(0).getText();
					for (int x = 1; x < node.getChild(i).getChildCount();x++)
						result += "." + node.getChild(i).getChild(x).getText();
					logger.debug("FQN" + result);
					perm = new Permission(result);
					break;
			}
			
		if (perm != null)
			role.addPermission(perm);
		perm = null;
		}
	}

	public void grant(Principal target, Permission perm) {
		m_grants.add(new Grant(target, perm));
	}

	public void parseAllow(Tree node) {
		Permission perm = null;
		switch(node.getChild(0).getType()) {
			case SecurityPolicyLexer.FQN:
				perm = new Permission(node.getChild(0).getChild(0).getText());
				break;
			case SecurityPolicyLexer.REFERENCE:
				perm = new RoleRef(this, node.getChild(0).getChild(0).getText());
				break;
		}
		Principal target = null;
		switch(node.getChild(1).getType()) {
			case SecurityPolicyLexer.REFERENCE:
				target = new GroupRef(this, node.getChild(1).getChild(0).getText());
				break;
			case SecurityPolicyLexer.STRING:
			case SecurityPolicyLexer.ID:
				try
				{
				target = new Player(node.getChild(1).getChild(0).getText());
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
				target.setPolicy(this);
				break;
		}

		grant(target, perm);
	}
}
