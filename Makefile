all:
	javac -cp src src/telomemore/java/Main.java
	cd src; jar cfm ../telomemore.jar Manifest.txt telomemore/java/*.class
gitaddall:
	git add src/telomemore/java/*.java

