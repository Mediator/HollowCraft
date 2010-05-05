OpenCraft

For more information about the license, see LICENSE.TXT.

For more information about the project, see http://opencraft.sf.net/ and http://gitorious.org/opencraft-acm

Tools Required:
ant
java
javac or gcj

Compiling
=========
To compile the server with javac, run `ant build`.

To compile the server with gcj, edit build.xml and change the line:
  <target name="build" depends="javac"/>
to
  <target name="build" depends="gcj"/>
then run `ant build`

Running
=======
To run the server, run `ant run`