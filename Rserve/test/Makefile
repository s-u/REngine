JAVA=java
JAVAC=javac
JFLAGS+=-source 1.4 -target 1.4

all: test

../Rserve.jar: ../../REngine.jar
	$(MAKE) -C .. Rserve.jar

../../REngine.jar:
	$(MAKE) -C ../.. REngine.jar

test: test.class ../Rserve.jar ../../REngine.jar
	$(JAVA) -cp ../Rserve.jar:../../REngine.jar:. test

test.class: test.java ../Rserve.jar ../../REngine.jar
	$(JAVAC) $(JFLAGS) -d . -cp ../Rserve.jar:../../REngine.jar:. test.java

jt: jt.class ../Rserve.jar ../../REngine.jar
	$(JAVA) -cp ../Rserve.jar:../../REngine.jar:. jt

jt.class: jt.java ../Rserve.jar ../../REngine.jar
	$(JAVAC) $(JFLAGS) -d . -cp ../Rserve.jar:../../REngine.jar:. jt.java

clean:
	rm -rf org *~ *.class

.PHONY: test all clean

