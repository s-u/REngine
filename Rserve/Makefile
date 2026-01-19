RENG_SRC=$(wildcard ../*.java)
RSRV_SRC=$(wildcard *.java) $(wildcard protocol/*.java)

TARGETS=Rserve.jar

all: $(TARGETS)

JAVAC=javac
ifeq ($(JTARGET),)
JTARGET:=$(shell for i in 1.6 8 11 17; do if javac -source $$i -target $$i ../package-info.java >/dev/null 2>&1; then echo $$i; break; fi; done)
endif
JFLAGS=-encoding utf8 -source $(JTARGET) -target $(JTARGET)

../REngine.jar: $(RENG_SRC)
	make -C .. REngine.jar

Rserve.jar: $(RSRV_SRC) ../REngine.jar
	@rm -rf org
	$(JAVAC) $(JFLAGS) -d . -cp ../REngine.jar $(RSRV_SRC)
	jar fc $@ org
	rm -rf org

clean:
	rm -rf org *~ protocol/*~ $(TARGETS)
	make -C test clean

test:
	make -C test test

.PHONY: clean all test
