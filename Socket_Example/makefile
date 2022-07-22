JCC = javac
FLAG = -g
default: server/server.class server/serverUtil.class client/client.class client/clientUtil.class

server/server.class: server/server.java
	$(JCC) $(FLAG) server/server.java

server/serverUtil.class: server/ServerUtil.java
	$(JCC) $(FLAG) server/ServerUtil.java

client/client.class: client/client.java
	$(JCC) $(FLAG) client/client.java

client/clientUtil.class: client/ClientUtil.java
	$(JCC) $(FLAG) client/ClientUtil.java

clean:
	rm server/*.class
	rm client/*.class