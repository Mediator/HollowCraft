#!/usr/bin/env python
import minecraft

class TestBot(ServerConnection):
	def __init__(self, user, passwd, host, port):
		super(TestBot, self).__init__(user, passwd, host, port);
		self.blockCount = 0

	def onSelfSpawn(self, pid):
		self.client_destroyBlock(128, 128, 32, 1);
		self.client_createBlock(128, 128, 32, 1);
	
	def onSetBlock(self, x, y, z, type):
		self.blockCount+=1
		if (self.blockCount == 2):
			self.running = False

bot = TestBot("Phong", "", "127.0.0.1", 25565)
bot.run()
