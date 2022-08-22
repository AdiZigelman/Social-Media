# Social-Media

How to compile:
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
