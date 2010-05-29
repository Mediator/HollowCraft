#!/usr/bin/env python
import minecraft

class TestBot(minecraft.ServerConnection):
	def onSelfSpawn(self, myID):
		self.log("Connection success.")
		self.running = False

bot = TestBot("Phong", "", "127.0.0.1", 25565)
bot.run()
