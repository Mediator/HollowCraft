#!/usr/bin/env python
from minecraft import *

class TestBot(AlphaConnection):
	def __init__(self, user, passwd, host, port):
		super(TestBot, self).__init__(user, passwd, host, port);
		self.blockCount = 0

	def onSelfSpawn(self):
		self.client_setBlock(28, 28, 32, 0, 0);
		self.client_setBlock(28, 28, 32, 1, 0);
	
	def onSetBlock(self, x, y, z, type):
		self.blockCount+=1
		if (self.blockCount == 2):
			self.running = False

bot = TestBot("Phong", "", "127.0.0.1", 25565)
bot.run()
