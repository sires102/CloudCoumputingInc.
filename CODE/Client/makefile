JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	ClientApp.java \
	GetFileMesssage.java \
	PutFileMessage.java 
        

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class