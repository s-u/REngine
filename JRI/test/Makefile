CP=../../REngine.jar:../JRIEngine.jar
JFLAGS=-source 1.4 -target 1.4
JRT=-Djava.library.path=../../../JRI/src:.

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

