#include "SocketReader.h"
using namespace std;
SocketReader::SocketReader(ConnectionHandler& connectionHandler, mutex &mtx, condition_variable& conditionVariable, bool& shouldTerminate) : connectionHandler(connectionHandler), mtx(mtx),conditionVariable(conditionVariable), shouldTerminate(shouldTerminate) {}

void SocketReader::operator()()  {
    while (!shouldTerminate) {
        char opCodeBytes[5];
        string opCode= "";
        string command = "";
        string commandStatus;
        string leftovers = "";
        string line;
        if (connectionHandler.getBytes(opCodeBytes,5)) {
            opCode = bytesToShort(opCodeBytes,0,1);
            if (opCode == "10" || opCode == "11"){
                command = bytesToShort(opCodeBytes,2,3);
                leftovers = bytesToShort(opCodeBytes,4,4);
            }
            else {
                command = bytesToShort(opCodeBytes,2,2);
                leftovers = bytesToShort(opCodeBytes,3,4);
            }
        }
        if (opCode == "09") { // Notification
            commandStatus = "NOTIFICATION ";
            string notificationType;
            if (command == "0") {
                notificationType = "PM ";
            }
            else
                notificationType = "Public ";
            connectionHandler.getLine(line);
            line = leftovers + line;
            cout << commandStatus << notificationType << line << endl;
        }
        else {
             if (opCode == "10") { // Ack
                 commandStatus = "ACK ";
                if (command == "03") {// received ACK for Logout Command
                    unique_lock<mutex> lock(mtx);
                    shouldTerminate = true;
                    conditionVariable.notify_one(); // wakeup the keyboard reader thread
                }
                if (command == "04" || command == "07" || command == "08"){
                    connectionHandler.getLine(line);
                    line = leftovers + line;
                }
            }
            else if (opCode == "11") { // Error
                commandStatus = "ERROR ";
                if (command == "03")
                    conditionVariable.notify_one(); // wakeup the keyboard reader thread
            }
            if (command.at(0)=='0')
                command = command.substr(1,1);
            cout << commandStatus << command + " " << line << endl;
        }
    }
}

std::string SocketReader::bytesToShort(char *bytesArr, int start, int end) {
    string result = "";
    while (start <= end) {
        char ch = char(bytesArr[start]);
        start++;
        result +=ch;
    }
    return result;
}