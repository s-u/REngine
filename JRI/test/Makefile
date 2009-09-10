CP=../../REngine.jar:../JRIEngine.jar
JFLAGS=-source 1.4 -target 1.4

## NOTE: if you don't use GNU make uncomment the next line and comment out the subsequent one
##JRI=../../../JRI/src
JRI=$(shell if test -e ../../../JRI/src/JRI.jar; then echo ../../../JRI/src; else echo "cat(system.file('jri',package='rJava'))"|R --slave --no-save; fi)

JRT=-Djava.library.path=${JRI}:.

JAVAC=javac
JAVA=java

all: run

## RJavaClassLoader needs to be included for rJava-related
## tests. Fore real projects the CLASSPATH should be set properly
## to include system.file('java','boot',package='rJava')
RTest.class: RTest.java RJavaClassLoader.java
	$(JAVAC) $(JFLAGS) -classpath $(CP) $^

run: RTest.class
	R CMD $(JAVA) -cp $(CP):. $(JRT) RTest

clean:
	rm -rf org *.class *~

.PHONY: run clean

