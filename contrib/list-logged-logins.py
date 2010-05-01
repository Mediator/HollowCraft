#!/usr/bin/env python
import xml.sax
import sys

class LogRecordHandler(xml.sax.ContentHandler):
	def __init__(self):
		self.inRecord = False
		self.loginRecord = False
		self.curTag = ""
		self.stamp = ""
		self.message = ""

	def startElement(self, name, attrs):
		if (self.inRecord):
			self.curTag = name
		else:
			self.curTag = ""
		if (name == 'record'):
			self.inRecord = True
	
	def characters(self, chars):
		chars = chars.strip()
		if (chars == ""):
			return
		if (self.curTag == "message"):
			self.message = chars

		if (self.curTag == "date"):
			self.stamp = chars

		if (self.curTag == "logger"):
			if (chars == "org.opencraft.server.Server.Logins"):
				self.loginRecord = True
			else:
				self.loginRecord = False
				self.message = ""
				self.stamp = ""

		if (self.loginRecord and self.message != "" and self.stamp != ""):
			addLog(self.stamp, self.message)
			self.loginRecord = False
			self.message = ""
			self.stamp = ""

	def endElement(self, name):
		if (name == 'record'):
			self.inRecord = False
			self.loginRecord = False
			self.message = ""
			self.stamp = ""

log = []
def addLog(stamp, message):
	log.append((stamp, message))

parser = xml.sax.make_parser()
parser.setContentHandler(LogRecordHandler())
parser.setFeature(xml.sax.handler.feature_external_ges, 0)
parser.parse(open(sys.argv[1]))

for stamp,record in log:
	(type, args) = record.split(" ", 1)
	args = args.split(" ")
	if (type == "LOGIN"):
		print "%s: Login: %s from %s"%(stamp, args[0], args[1])
	if (type == "OP"):
		print "%s: Opped: %s"%(stamp, args[0])
	if (type == "JOIN"):
		print "%s: %s went to %s"%(stamp, args[0], args[1])
