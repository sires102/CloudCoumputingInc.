JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	ServerApp.java \
	GetFileMessage.java \
	PutFileMessage.java 
	
default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class