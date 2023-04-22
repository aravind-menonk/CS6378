JC = javac
JFLAGS = -g 
CLASSES = \
	server/Server.java \
	client/Client.java
JVM = java
CLASS_SOURCES = $(shell find . -type f -name '*.class')

SERVER = server/Server
CLIENT = client/Client

.SUFFIXES: .java .class

.java.class:
	$(JC) $(JFLAGS) $*.java

default: classes

classes: $(CLASSES:.java=.class)

server: classes
	$(JVM) $(SERVER)

client: classes
	$(JVM) $(CLIENT)

clean:
	$(RM) $(CLASS_SOURCES)
