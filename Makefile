RENG_SRC=$(wildcard *.java)
RSRV_SRC=$(wildcard Rserve/*.java) $(wildcard Rserve/protocol/*.java)

TARGETS=REngine.jar Rserve.jar

all: $(TARGETS)

JAVAC=javac
JFLAGS+=-source 1.4 -target 1.4

REngine.jar: $(RENG_SRC)
	@rm -rf org
	$(JAVAC) -d . $(JFLAGS) $(RENG_SRC)
	jar fc $@ org
	rm -rf org

Rserve.jar: $(RSRV_SRC) REngine.jar
	@rm -rf org
	$(JAVAC) -d . -cp REngine.jar $(RSRV_SRC)
	jar fc $@ org
	rm -rf org

clean:
	rm -rf org *~ $(TARGETS)
	make -C Rserve clean

test:
	make -C Rserve test

rc:	Rserve.jar Rserve/test/jt.java
	make -C Rserve/test jt

.PHONY: clean all test
