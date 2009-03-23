CP=../../REngine.jar:../JRIEngine.jar
JFLAGS=-source 1.4 -target 1.4
JRT=-Djava.library.path=../../../JRI/src:.

JAVAC=javac
JAVA=java

all: run

RTest.class: RTest.java
	$(JAVAC) $(JFLAGS) -classpath $(CP) $^

run: RTest.class
	R CMD $(JAVA) -cp $(CP):. $(JRT) RTest

clean:
	rm -rf org *.class *~

.PHONY: run clean

