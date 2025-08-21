# Social-Media
This is a social media platform built with a client-server architecture where users can register, login, and interact through features like posting messages, following other users, sending private messages, and viewing statistics. The server is implemented in Java using both Reactor and Thread-Per-Client (TPC) patterns, while the client is written in C++ with a command-line interface that supports various social media commands like REGISTER, LOGIN, POST, FOLLOW, and PM. The system includes content filtering capabilities and provides user statistics and status tracking functionality.

Folder Server:
1) Reactor:
- mvn clean
- mvn compile
- mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.BGSServer.ReactorMain" -Dexec.args="7777 5"

2) TPC:
- mvn clean
- mvn compile
- mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.BGSServer.TPCMain" -Dexec.args=7777

Folder Client:
- make
- cd bin
- ./BGRSclient 127.0.0.1 7777



Messages example:
- REGISTER Adi a123 08-09-1998
- LOGIN Adi a123 1
- LOGOUT
- FOLLOW 0 Adi
- POST this is the best project!!!
- PM Adi i think so too
- LOGSTAT
- STAT Adi|Hagar
- BLOCK Adi



Filtered words: api.bidi.DataBase (in the constructor)
