#!/usr/bin/env python
import minecraft

class TestBot(minecraft.ServerConnection):
	def __init__(self, user, passwd, host, port):
		super(TestBot, self).__init__(user, passwd, host, port);
		self.blockCount = 0
		self.ready = False
	
	def onSelfSpawn(self, pid):
		self.ready = True

	def loop(self):
		super(TestBot, self).loop()
		if (self.ready):
			self.client_message(raw_input("> "))

	def onMessage(self, sender, text):
		print "<%s> %s"%(sender, text)


bot = TestBot("Phong", "", "127.0.0.1", 25565)
bot.run()
