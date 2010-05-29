#!/usr/bin/env python
from __future__ import division
from BeautifulSoup import BeautifulSoup
import urllib
import urllib2
import socket
import struct
import binascii
import time
from mathmodule import *

class Player(object):
	def __init__(self):
		self.name = ""
		self.x = 0
		self.y = 0
		self.z = 0
		self.heading = 0
		self.pitch = 0

class ServerConnection(object):
	
	SERVER_PROTOCOL = {
		0: "identify",
		1: "ping",
		2: "initialize",
		3: "chunk",
		4: "finalize",
		6: "setBlock",
		7: "spawnPlayer",
		8: "playerMove",
		9: "positionRotationChange",
		10: "positionChange",
		11: "orientationChange",
		12: "despawnPlayer",
		13: "message",
		14: "disconnect"
	}

	def getPlayer(self, id):
		if (not (id in self.playerMap)):
			self.playerMap[id] = Player()
			self.playerMap[id].id = id
		return self.playerMap[id]
	
	def player(self):
		return self.getPlayer(255)

	def __init__(self, username, password, host, port):
		self.username = username
		self.password = password
		self.host = host
		self.port = port
		self.running = True
		connected = False
		self.log("Connecting...")
		while not connected:
			try:
				self.reconnect()
				connected = True
			except socket.error:
				time.sleep(1)
		#urllib2.urlopen("http://minecraft.net/login.jsp", urllib.urlencode({"username":username,"password":password}))
		#page = urllib2.urlopen("http://minecraft.net/play.jsp?ip=%s&port=%s"%(host, port))
		#soup = BeautifulSoup(page)
		#sessionID = soup.find("param", {"name":"sessionid"})["value"]

	def reconnect(self):
		sessionID = 0
		self.__sock = socket.socket()
		self.__sock.connect((self.host, self.port))
		self.login(self.username, sessionID)
		self.playerMap = {}
		self.getPlayer(0).name = "action"
		self.getPlayer(-1).name = "server"
		self.x = 0
		self.y = 0
		self.z = 0
		self.heading = 0
		self.pitch = 0
		self.target = 0
	
	
	def log(self, fmt, *args):
		print fmt % args
	
	def login(self, username, key):
		self.send(struct.pack("!BB64s64sB", 0, 7, username.ljust(64, " "), str(key).ljust(64, " "), 0))
	
	def send(self, data):
		self.__sock.send(data)
	
	def read(self, size):
		return self.__sock.recv(size)
	
	def loop(self):
		type = struct.unpack("b", self.read(1))[0]
		if (type in self.SERVER_PROTOCOL):
			handlerName = "server_%s"%(self.SERVER_PROTOCOL[type])
			if (hasattr(self, handlerName)):
				handler = getattr(self, handlerName)
				handler()
			else:
				self.log("Unhandled packet: %s", self.SERVER_PROTOCOL[type])
		else:
			self.log("Unknown packet: %X", type)
	
	def onSelfSpawn(self, myID):
		pass
	
	def server_setBlock(self):
		x, y, z, type = self.getData("!hhhb")
		self.log("Block change at %s, %s, %s to %s", x, y, z, type)
		self.onSetBlock(x, y, z, type);
	
	def onSetBlock(self, x, y, z, type):
		pass
	
	def getData(self, fmt):
		return struct.unpack(fmt, self.read(struct.calcsize(fmt)))
	
	def server_message(self):
		player, message = self.getData("b64s")
		name = self.getPlayer(player).name
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
					cmd(self.getPlayer(player), args[1:])
	
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
		p = self.getPlayer(id)
		p.x = x
		p.y = y
		p.z = z
		p.heading = heading
		p.pitch = pitch
		self.onPlayerUpdated(id)
	
	def server_disconnect(self):
		reason = self.getData("64s")
		self.log("Disconnected: %s", reason)
		self.reconnect()
	
	def onPlayerUpdated(self, id):
		pass
	
	def server_spawnPlayer(self):
		id, name, x, y, z, heading, pitch = self.getData("!B64shhhBB")
		name = name.strip()
		self.log("New player at %s, %s, %s: %s (%s)", x, y, z, name, id)
		p = self.getPlayer(id)
		p.name = name
		p.x = x
		p.y = y
		p.z = z
		self.onPlayerUpdated(id)
		if (p.name == self.username):
			self.onSelfSpawn(id)

	
	def server_positionRotationChange(self):
		id, x, y, z, heading, pitch = self.getData("!BbbbBB")
		p = self.getPlayer(id)
		p.x = x
		p.y = y
		p.z = z
		p.heading = heading
		p.pitch = pitch
		self.onPlayerUpdated(id)
	
	def server_positionChange(self):
		id, dx, dy, dz = self.getData("!bbbb")
		p = self.getPlayer(id)
		p.x += dx
		p.y += dy
		p.z += dz
		self.onPlayerUpdated(id)

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
		while self.running:
			try:
				self.loop()
			except struct.error:
				self.log("Connection or protocol problem. Reconnecting.")
				self.reconnect()

