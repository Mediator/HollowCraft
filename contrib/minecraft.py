#!/usr/bin/env python
from __future__ import division
from BeautifulSoup import BeautifulSoup
import urllib
import urllib2
import socket
import struct
import binascii
import time
import traceback
from mathmodule import *

class Entity(object):
	def __init__(self):
		self.name = ""
		self.x = 0
		self.y = 0
		self.z = 0
		self.type = 0

class ServerConnection(object):
	
	SERVER_PROTOCOL = {
		0: "identify",
		1: "ping",
		2: "initialize",
		3: "chunk",
		4: "finalize",
		6: "setBlock",
		7: "spawnEntity",
		8: "playerMove",
		9: "positionRotationChange",
		10: "positionChange",
		11: "orientationChange",
		12: "despawnEntity",
		13: "message",
		14: "disconnect"
	}

	def getEntity(self, id):
		if (not (id in self.entityMap)):
			self.entityMap[id] = Entity()
			self.entityMap[id].id = id
		return self.entityMap[id]
	
	def player(self):
		return self.getEntity(-3)

	def __init__(self, username, password, host, port):
		self.username = username
		self.password = password
		self.host = host
		self.port = port
		self.running = True
		#urllib2.urlopen("http://minecraft.net/login.jsp", urllib.urlencode({"username":username,"password":password}))
		#page = urllib2.urlopen("http://minecraft.net/play.jsp?ip=%s&port=%s"%(host, port))
		#soup = BeautifulSoup(page)
		#sessionID = soup.find("param", {"name":"sessionid"})["value"]

	def reconnect(self):
		self.log("Connecting...")
		connected = False
		while not connected:
			try:
				sessionID = 0
				self.__sock = socket.socket()
				self.__sock.connect((self.host, self.port))
				self.login(self.username, sessionID)
				self.entityMap = {}
				self.getEntity(0).name = "action"
				self.getEntity(-1).name = "server"
				self.x = 0
				self.y = 0
				self.z = 0
				self.heading = 0
				self.pitch = 0
				self.target = 0
				connected = True
			except socket.error:
				time.sleep(1)
	
	
	def log(self, fmt, *args):
		print fmt % args
	
	def login(self, username, key):
		self.send(struct.pack("!BB64s64sB", 0, 7, username.ljust(64, " "), str(key).ljust(64, " "), 0))
	
	def send(self, data):
		type = struct.unpack("b", data[0])[0]
		self.log("Sending packet ID %s", type)
		self.__sock.send(data)
	
	def sendRaw(self, data):
		self.__sock.send(data)
	
	def read(self, size):
		buf = ''
		while len(buf) < size:
			buf += self.__sock.recv(1)
		return buf
	
	def translatePacket(self, id):
		if (id in self.SERVER_PROTOCOL):
			return self.SERVER_PROTOCOL[type]
		return None
	
	def loop(self):
		type = struct.unpack("b", self.read(1))[0]
		#self.log("Got packet type %s", type)
		handler = self.translatePacket(type)
		if (handler != None):
			handlerName = "server_%s"%(handler)
			if (hasattr(self, handlerName)):
				handler = getattr(self, handlerName)
				handler()
			else:
				self.log("Unhandled packet: %s", handler)
		else:
			self.log("Unknown packet: %s (%s)", type, hex(type))
	
	def onSelfSpawn(self):
		pass
	
	def server_setBlock(self):
		x, y, z, type = self.getData("!hhhb")
		self.log("Block change at %s, %s, %s to %s", x, y, z, type)
		self.onSetBlock(x, y, z, type);
	
	def onSetBlock(self, x, y, z, type):
		pass
	
	def getData(self, fmt):
		return struct.unpack(fmt, self.read(struct.calcsize(fmt)))
	
	def onMessage(self, sender, text):
		pass
	
	def server_message(self):
		player, message = self.getData("b64s")
		name = self.getEntity(player).name
		colors = ('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')
		for c in colors:
			message = message.replace('&%s'%c, '')
		self.log("%s: %s", name, message)
		if (message.startswith('+')):
			pass
		else:
			sender, text = message.split(' ', 1)
			args = text.split(' ')
			if (args[0].startswith('!')):
				cmdName = "command_%s"%args[0][1:]
				if (hasattr(self, cmdName)):
					cmd = getattr(self, cmdName)
					cmd(self.getEntity(player), args[1:])
		self.onMessage(sender, text)
	
	def command_ping(self, sender, *args):
		self.say("Pong")
	
	def command_summon(self, sender, *args):
		self.client_moveTo(sender.x, sender.y, sender.z, 0, 0)
	
	def command_follow(self, sender, *args):
		self.target = sender.id
		self.say("Ok!")
	
	def say(self, text):
		if (len(text) <= 64):
			self.client_message(text)
		else:
			while(len(text)>0):
				self.client_message(text[:64])
				self.say(text[64:])

	
	def client_message(self, text):
		self.send(struct.pack("!BB64s", 13,  255, text.ljust(64, " ")))

	def client_createBlock(self, x, y, z, type):
		self.send(struct.pack("!BhhhBB", 5, x, y, z, 1, type))

	def client_destroyBlock(self, x, y, z, type):
		self.send(struct.pack("!BhhhBB", 5, x, y, z, 0, type))
	
	def server_playerMove(self):
		id, x, y, z, heading, pitch = self.getData("!bhhhbb")
		p = self.getEntity(id)
		p.x = x
		p.y = y
		p.z = z
		p.heading = heading
		p.pitch = pitch
		self.onEntityUpdated(id)
	
	def server_disconnect(self):
		reason = self.getData("64s")
		self.log("Disconnected: %s", reason)
		self.reconnect()
	
	def onEntityUpdated(self, id):
		pass
	
	def server_spawnEntity(self):
		id, name, x, y, z, heading, pitch = self.getData("!B64shhhBB")
		name = name.strip()
		self.log("New player at %s, %s, %s: %s (%s)", x, y, z, name, id)
		p = self.getEntity(id)
		p.name = name
		p.x = x
		p.y = y
		p.z = z
		self.onEntityUpdated(id)
		if (p.name == self.username):
			self.onSelfSpawn(id)

	
	def server_positionRotationChange(self):
		id, x, y, z, heading, pitch = self.getData("!BbbbBB")
		p = self.getEntity(id)
		p.x = x
		p.y = y
		p.z = z
		p.heading = heading
		p.pitch = pitch
		self.onEntityUpdated(id)
	
	def server_positionChange(self):
		id, dx, dy, dz = self.getData("!bbbb")
		p = self.getEntity(id)
		p.x += dx
		p.y += dy
		p.z += dz
		self.onEntityUpdated(id)

	def client_moveTo(self, x, y, z, heading, pitch):
		self.send(struct.pack("!BBhhhBB", 8, 255, x, y, z, heading, pitch))

	def server_ping(self):
		self.log("Ping")
	
	def server_initialize(self):
		self.log("Incoming level data...")

	def server_chunk(self):
		chunkSize = self.getData("!H")[0]
		self.log("Reading %s sized chunk", chunkSize)
		self.log(binascii.hexlify(self.read(1024)))
		pct = self.getData("B")[0]
		self.log("Chunks: %s", pct)
	
	def server_finalize(self):
		width, length, height = self.getData("!hhh")
		self.log("Data complete. Map size: %sx%sx%s", width, length, height)

	def server_identify(self):
		version, name, motd, userType = self.getData("b64s64sb")
		self.log("Server version: %s", version)
		self.log("Server name: %s", name)
		self.log("MOTD: %s", motd)
		if (userType == chr(100)):
			self.log("I am an op!")
	
	def run(self):
		self.reconnect()
		while self.running:
			try:
				self.loop()
			except struct.error, e:
				self.log("Connection or protocol problem. Reconnecting.")
				traceback.print_exc()
				self.reconnect()

class AlphaConnection(ServerConnection):
	ALPHA_PROTOCOL = {
		-1: 'disconnect',
		0: 'keepalive',
		1: 'login',
		2: 'handshake',
		3: 'chat',
		4: 'tick',
		13: 'moveAndLook',
		18: 'armAnimation',
		20: 'playerSpawn',
		21: 'pickupSpawn',
		23: 'unknown',
		24: 'mobSpawn',
		29: 'entityDestroy',
		30: 'entityInit',
		31: 'entityRelativeMove',
		32: 'entityLook',
		33: 'entityRelativeMoveLook',
		50: 'preChunk',
		51: 'mapChunk',
		52: 'multiBlockChange',
		53: 'blockChange',
	}
	def translatePacket(self, id):
		if (id in self.ALPHA_PROTOCOL):
			return self.ALPHA_PROTOCOL[id]
		return None

	def __init__(self, username, password, host, port):
		super(AlphaConnection, self).__init__(username, password, host, port)

	def sendString(self, str):
		self.sendRaw(struct.pack("!h", len(str)))
		self.sendRaw(str)
	
	def getString(self):
		length = self.getData("!h")[0]
		return self.read(length)

	def login(self, username, key):
		self.send(struct.pack("!Bi", 1, 1))
		self.sendString(username)
		self.sendString("Password")
		#self.send(struct.pack("!B", 2))
		#self.sendString(username)
	
	def server_handshake(self):
		serverID = self.getString()
		if (serverID == "-"):
			self.log("No authentication required.")
		else:
			self.log("Authentication required. Logging in to minecraft.net")
			versionPage = urllib2.urlopen("http://minecraft.net/game/getversion.jsp?user=%s&password=%s&version=99"%(self.username,self.password))
			versionData = versionPage.read()
			self.log("got response: %s", versionData)
			version, ticket, username, session = versionData.split(':')[0:4]
			self.sessionID = session
			self.log("Got server ID %s. Authenticating.", serverID)
			urllib2.urlopen("http://www.minecraft.net/game/joinserver.jsp?user=%s&serverId=%s&sessionId=%s"%(self.username, serverID, self.sessionID)).read()

	def server_disconnect(self):
		reason = self.getString()
		self.log("Disconnected: %s", reason)

	def server_login(self):
		id = self.getData('!i')[0]
		serverName = self.getString()
		motd = self.getString()
		self.log("Joined %s: %s", serverName, motd)
	
	def server_chat(self):
		text = self.getString()
		self.log("Chat: %s", text)
		self.onMessage(None, text)
	
	def server_preChunk(self):
		x, y, mode = self.getData("!ii?")
		self.log("Readying chunk at %i,%i: %s", x, y, mode)

	def server_moveAndLook(self):
		x, y, stance, z, rotation, pitch, gravity = self.getData("!ddddff?")
		self.log("Moved myself to %s,%s,%s", x, y, z)
		ent = self.player()
		ent.x = x
		ent.y = y
		ent.z = z
		self.onSelfSpawn()
	
	def server_tick(self):
		curTime = self.getData("!q")[0]
		self.log("Server time: %s", curTime)
	
	def server_multiBlockChange(self):
		x, y, size = self.getData("!iih")
		self.log("Changed blocks at x,y", x, y)
		self.read(size*2)
		self.read(size)
		self.read(size)
	
	def server_pickupSpawn(self):
		id, item, unk, x, y, z, rotation, pitch, unk2 = self.getData("!ihBiiiBBB")
		self.log("Spawned %s at %s,%s,%s", item, x, y, z)
	
	def server_mobSpawn(self):
		id, type, x, y, z, rotation, pitch = self.getData("!iBiiiBB")
		self.log("Spawned mob %s at %s,%s,%s", type, x, y, z)
		ent = self.getEntity(id)
		ent.x = x
		ent.y = y
		ent.z = z
		ent.type = type
		self.onEntityUpdated(id)
	
	def server_unknown(self):
		self.getData("!iBiii")
	
	def server_entityInit(self):
		id = self.getData("!i")[0]
		self.getEntity(id)
	
	def server_entityRelativeMove(self):
		id, dx, dy, dz = self.getData("!iBBB")
		ent = self.getEntity(id)
		ent.x += dx
		ent.y += dy
		ent.z += dz
		self.onEntityUpdated(id)
	
	def server_entityRelativeMoveLook(self):
		id, dx, dy, dz, rotation, pitch = self.getData("!iBBBBB")
		ent = self.getEntity(id)
		ent.x += dx
		ent.y += dy
		ent.z += dz
		self.onEntityUpdated(id)
	
	def server_entityLook(self):
		id, rotation, pitch = self.getData("!iBB")
		self.onEntityUpdated(id)
	
	def server_entityDestroy(self):
		self.getData("!i")
	
	def server_playerSpawn(self):
		id = self.getData("!i")
		name = self.getString()
		x, y, z, rotation, pitch, item = self.getData("!iiiBBh")
		ent = self.getEntity(id)
		ent.x = x
		ent.y = y
		ent.z = z
		ent.name = name
		self.log("Player %s spawned at %s,%s,%s", name, x, y, z)
	
	def server_armAnimation(self):
		self.getData("!iB")
	
	def server_keepalive(self):
		self.send(struct.pack("!B", 0))
	
	def server_blockChange(self):
		x, y, z, type, meta = self.getData("!iBiBB")
		self.onSetBlock(x, y, z, type)

	def server_mapChunk(self):
		x, y, z, sx, sy, sz, size = self.getData("!ihiBBBi")
		self.log("Recieving chunk for %s,%s,%s", x, y, z)
		self.read(size)
	
	def client_setBlock(self, x, y, z, type, meta):
		self.send(struct.pack("!BiBiBB", 53, x, y, z, type, meta))
