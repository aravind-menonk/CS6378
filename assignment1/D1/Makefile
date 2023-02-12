JC = javac
OUTDIR = /home/012/a/ax/axk210275/D1/
JFLAGS = -g -d $(OUTDIR)
SOURCES = $(shell find . -type f -name '*.java')
JVM= java

TXTCLIENT1 = /home/012/a/ax/axk210275/D1/client1/F3.txt
TXTCLIENT2 = /home/012/a/ax/axk210275/D1/client2/F3.txt
TXTSERVER = /home/012/a/ax/axk210275/D1/server/F3.txt

SERVER = Server
CLIENTONE = ClientOne
CLIENTTWO = ClientTwo

.SUFFIXES: .java .class

.java.class:
	$(JC) $(JFLAGS) $*.java

default: classes

classes: $(SOURCES:.java=.class)

server: classes
	$(JVM) $(SERVER)

clientOne: 
	$(JVM) $(CLIENTONE)

clientTwo: 
	$(JVM) $(CLIENTTWO)

clean:
	$(RM) $(OUTDIR)*.class
	$(RM) $(TXTCLIENT1)
	$(RM) $(TXTCLIENT2)
	$(shell :> $(TXTSERVER))
