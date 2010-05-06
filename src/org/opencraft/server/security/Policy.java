package org.opencraft.server.security;

import java.text.ParseException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;
import org.slf4j.*;

public class Policy {

	private static final Logger logger = LoggerFactory.getLogger(Policy.class);

	public Policy(Reader r) throws ParseException, IOException {
		readFrom(r);
		for(Group group : m_groups.values()) {
			System.out.println("Found group "+group);
			for(Principal p : group.getMembers()) {
				if (p instanceof Group)
					System.out.println("\t@"+p);
				else
					System.out.println("\t"+p);
			}
		}
		for(String role : m_roles.keySet()) {
			System.out.println("Found role "+role);
			for(Permission permission : m_roles.get(role).getPermissions()) {
				if (permission instanceof Role)
					System.out.println("\t@"+permission);
				else
					System.out.println("\t"+permission);
			}
		}
	}

	public void applyPermissions(Principal p) {
		for(Group group : m_userGroups.get(p.getName())) {
			group.addMember(p);
		}
		for(Permission permission : m_userPermissions.get(p.getName())) {
			p.addPermission(permission);
		}
	}

	private HashMap<String, ArrayList<Permission>> m_userPermissions;
	private HashMap<String, Role> m_roles;

	private HashMap<String, Group> m_groups;
	private HashMap<String, ArrayList<Group>> m_userGroups;

	public void readFrom(Reader r) throws ParseException, IOException {
		m_userPermissions = new HashMap<String, ArrayList<Permission>>();
		m_groups = new HashMap<String, Group>();
		m_groups.put("ALL", new Group("ALL") {
			public boolean hasMember(Principal p) {
				return true;
			}
		});
		m_groups.put("NONE", new Group("NONE") {
			public boolean hasMember(Principal p) {
				return false;
			}
		});
		m_userPermissions = new HashMap<String, ArrayList<Permission>>();
		m_userGroups = new HashMap<String, ArrayList<Group>>();
		m_roles = new HashMap<String, Role>();
		StreamTokenizer tokens = new StreamTokenizer(r);
		tokens.slashSlashComments(true);
		tokens.slashStarComments(true);
		tokens.ordinaryChar('{');
		tokens.ordinaryChar('}');
		tokens.wordChars('@', '@');
		HashMap<String, ArrayList<BlockList>> lists = new HashMap<String, ArrayList<BlockList>>();
		lists.put("ROLE", new ArrayList<BlockList>());
		lists.put("GROUP", new ArrayList<BlockList>());

		ArrayList<PermissionBlock> permissions = new ArrayList<PermissionBlock>();
		ArrayList<WorldBlock> worlds = new ArrayList<WorldBlock>();
		double version = 0;
		while(tokens.nextToken() != StreamTokenizer.TT_EOF) {
			String block = tokens.sval.toUpperCase();
			logger.trace("Read new block type {}", tokens.sval);
			if (block.equals("VERSION")) {
				if (tokens.nextToken() != StreamTokenizer.TT_NUMBER)
					throw new ParseException("Version must be a number", tokens.lineno());
				version = tokens.nval;
				logger.debug("Loading policy version {}", version);
			} else if (lists.containsKey(block)) {
				lists.get(block).add(new BlockList(tokens));
			} else if (block.equals("ALLOW")) {
				permissions.add(new AllowBlock(tokens));
			} else if (block.equals("DENY")) {
				permissions.add(new DenyBlock(tokens));
			} else if (block.equals("WORLD")) {
				worlds.add(new WorldBlock(tokens));
			} else {
				throw new ParseException("Unhandled block: "+tokens.sval, tokens.lineno());
			}
		}

		for(BlockList roleBlock : lists.get("ROLE")) {
			String role = roleBlock.getName();
			m_roles.put(role, new Role(role));
		}

		for(BlockList roleBlock : lists.get("ROLE")) {
			String role = roleBlock.getName();
			for(String permission : roleBlock.getMembers()) {
				if (permission.charAt(0)=='@')
					m_roles.get(role).addPermission(m_roles.get(permission.substring(1)));
				else
					m_roles.get(role).addPermission(new Permission(permission));
			}
		}

		for(BlockList groupBlock : lists.get("GROUP")) {
			Group g = new Group(groupBlock.getName());
			m_groups.put(groupBlock.getName(), g);
		}
		
		for(BlockList groupBlock : lists.get("GROUP")) {
			Group g = m_groups.get(groupBlock.getName());
			for(String member : groupBlock.getMembers()) {
				if (member.charAt(0) == '@') {
					g.addMember(m_groups.get(member.substring(1)));
				} else {
					if (!m_userGroups.containsKey(member))
						m_userGroups.put(member, new ArrayList<Group>());
					m_userGroups.get(member).add(g);
				}
			}
		}
	}


	private class WorldBlock {
		private String m_name;
		public WorldBlock(StreamTokenizer tokens) throws ParseException, IOException {
			if (tokens.nextToken() != StreamTokenizer.TT_WORD)
				throw new ParseException("Expected world name", tokens.lineno());
			m_name = tokens.sval;
			logger.trace("Found world {}", m_name);
			if (tokens.nextToken() != '{')
				throw new ParseException("Expected token {", tokens.lineno());
			ArrayList<PermissionBlock> permissions = new ArrayList<PermissionBlock>();
			while(tokens.nextToken() != '}') {
				String block = tokens.sval.toUpperCase();
				if (block.equals("ALLOW")) {
					permissions.add(new AllowBlock(tokens));
				} else if (block.equals("DENY")) {
					permissions.add(new DenyBlock(tokens));
				} else {
					throw new ParseException("Unhandled grant in world: "+block, tokens.lineno());
				}
			}
		}
	}


	private abstract class PermissionBlock {
		private String m_target;
		private String m_permission;

		public PermissionBlock(StreamTokenizer tokens) throws ParseException, IOException {
			logger.debug("Reading in permission block of {}", tokens.sval);
			if (tokens.nextToken() != StreamTokenizer.TT_WORD)
				throw new ParseException("Expected a permission.", tokens.lineno());
			m_permission = tokens.sval;
			logger.debug("Permission: {}", tokens.sval);
			/*if (tokens.nextToken() != StreamTokenizer.TT_WORD || tokens.sval.toLowerCase().equals("to"))
				throw new ParseException("Expected token 'to', got '"+tokens.sval+"'", tokens.lineno());*/
			tokens.nextToken();
			if (tokens.nextToken() != StreamTokenizer.TT_WORD)
				throw new ParseException("Expected role or player name, got "+tokens.sval, tokens.lineno());
			m_target = tokens.sval;
			logger.debug("Target: {}", tokens.sval);
		}
	}

	private class AllowBlock extends PermissionBlock {
		public AllowBlock(StreamTokenizer tokens) throws ParseException, IOException {
			super(tokens);
		}
	}

	private class DenyBlock extends PermissionBlock {
		public DenyBlock(StreamTokenizer tokens) throws ParseException, IOException {
			super(tokens);
		}
	}

	private class BlockList {
		private ArrayList<String> m_members = new ArrayList<String>();

		public ArrayList<String> getMembers() {
			return m_members;
		}

		private String m_name;

		public String getName() {
			return m_name;
		}

		public BlockList(StreamTokenizer tokens) throws ParseException, IOException {
			if (tokens.nextToken() != StreamTokenizer.TT_WORD)
				throw new ParseException("Expected block name", tokens.lineno());
			m_name = tokens.sval;
			logger.trace("Block name: {}", m_name);
			if (tokens.nextToken() != '{')
				throw new ParseException("Expected {, got "+tokens.sval, tokens.lineno());
			while(tokens.nextToken() != '}') {
				m_members.add(tokens.sval);
			}
		}
	}
}
