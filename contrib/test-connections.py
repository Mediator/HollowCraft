#!/usr/bin/env python
import minecraft

class TestBot(minecraft.AlphaConnection):
	def onSelfSpawn(self):
		self.log("Connection success.")
		self.running = False

bot = TestBot("Phong", "", "127.0.0.1", 25565)
bot.run()
