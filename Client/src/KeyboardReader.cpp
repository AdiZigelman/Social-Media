#include "KeyboardReader.h"
#include <time.h>
using namespace std;
KeyboardReader::KeyboardReader(ConnectionHandler& connectionHandler, mutex &mtx, condition_variable& conditionVariable, bool& shouldTerminate): connectionHandler(connectionHandler), opCodes(), mtx(mtx),conditionVariable(conditionVariable), shouldTerminate(shouldTerminate) {
    opCodes.insert(pair<string , string>("REGISTER", "01"));
    opCodes.insert(pair<string , string>("LOGIN", "02"));
    opCodes.insert(pair<string , string>("LOGOUT", "03"));
    opCodes.insert(pair<string , string>("FOLLOW", "04"));
    opCodes.insert(pair<string , string>("POST", "05"));
    opCodes.insert(pair<string , string>("PM", "06"));
    opCodes.insert(pair<string , string>("LOGSTAT", "07"));
    opCodes.insert(pair<string , string>("STAT", "08"));
    opCodes.insert(pair<string , string>("BLOCK", "12"));
}

void KeyboardReader::operator()() {
    string line;
    while (!shouldTerminate) {
        const short bufsize = 1024;
        char buf[bufsize];
        std::cin.getline(buf, bufsize);
        std::string line(buf);
        vector<string> input;
        stringstream start(line);
        string word;
        while (getline(start, word, ' ')) { // creating a vector of the input from keyboard separated by ' '
            input.push_back(word);
        }
        // translate short to byte array
        //char opCodeBytes[2];
        string opCode = opCodes[input.at(0)];
        const char *op_bytes = opCode.c_str();
        //shortToBytes(opCode,opCodeBytes);
        int begin_index = 0;
        if (opCode == "01") {
            string username = input.at(1);
            string password = input.at(2);
            string birthday = input.at(3);
            const char *username_bytes = username.c_str();
            const char *password_bytes = password.c_str();
            const char *birthday_bytes = birthday.c_str();
            char message[2 + username.length() + 1 + password.length() + 1 + birthday.length() + 1 + 1];
            begin_index = copyBytesArray(message,op_bytes,begin_index,2);
            begin_index = copyBytesArray(message,username_bytes,begin_index,username.length());
            message[begin_index++] = '\0';
            begin_index = copyBytesArray(message, password_bytes, begin_index, password.length());
            message[begin_index++] = '\0';
            begin_index = copyBytesArray(message, birthday_bytes, begin_index, birthday.length());
            message[begin_index++] = '\0';
            message[begin_index++] = ';';
            connectionHandler.sendBytes(message,begin_index);
        }
        else if (opCode == "06"){
            //Need to fix time shit
            string username = input.at(1);
            string content = extractContent(input,2) + getTime().substr(0,10);
            string date = getTime();
            const char *username_bytes = username.c_str();
            const char *content_bytes = content.c_str();
            const char *date_bytes = date.c_str();
            char message[2 + username.length() + 1 + content.length() + 1 + 16 + 1];
            begin_index = copyBytesArray(message,op_bytes,begin_index,2);
            begin_index = copyBytesArray(message,username_bytes,begin_index,username.length());
            message[begin_index++] = '\0';
            begin_index = copyBytesArray(message, content_bytes, begin_index, content.length());
            message[begin_index++] = '\0';
            begin_index = copyBytesArray(message,date_bytes,begin_index, 16);
            message[begin_index++] = '\0';
            message[begin_index++] = ';';
            connectionHandler.sendBytes(message,begin_index);
        }
        else if (opCode == "02"){
            string username = input.at(1);
            string password = input.at(2);
            string captcha = input.at(3);
            const char *username_bytes = username.c_str();
            const char *password_bytes = password.c_str();
            const char *captcha_bytes = captcha.c_str();
            char message[2 + username.length() + 1 + password.length() + 1 + captcha.length()+1];
            begin_index = copyBytesArray(message,op_bytes,begin_index,2);
            begin_index = copyBytesArray(message,username_bytes,begin_index,username.length());
            message[begin_index++] = '\0';
            begin_index = copyBytesArray(message,password_bytes,begin_index, password.length());
            message[begin_index++] = '\0';
            begin_index = copyBytesArray(message,captcha_bytes,begin_index, captcha.length());
            message[begin_index++] = ';';
            connectionHandler.sendBytes(message,begin_index);
        }
        else if (opCode == "03") {
            char message [2+1];
            begin_index = copyBytesArray(message,op_bytes,begin_index,2);
            message[begin_index++] = ';';
            connectionHandler.sendBytes(message,begin_index);
            unique_lock<mutex> lock(mtx);
            conditionVariable.wait(lock); // wait until the SocketReader gets a reply and determine if shouldTerminate
        }
        else if (opCode == "04"){
            string username = input.at(2);
            string follow = input.at(1);
            const char *username_bytes = username.c_str();
            const char *follow_bytes = follow.c_str();
            char message[2 + 1 + username.length()+1];
            begin_index = copyBytesArray(message,op_bytes,begin_index,2);
            begin_index = copyBytesArray(message,follow_bytes,begin_index, 1);
            begin_index = copyBytesArray(message,username_bytes,begin_index,username.length());
            message[begin_index++] = ';';
            connectionHandler.sendBytes(message,begin_index);
        }
        else if (opCode == "05" || opCode == "08" || opCode == "12"){
            string contentOrUsersListOrUsername = "";
            if (opCode == "05")
                contentOrUsersListOrUsername = extractContent(input,1);
            else
                contentOrUsersListOrUsername = input.at(1);
            const char *content_bytes = contentOrUsersListOrUsername.c_str();
            char message[2 + 1 + contentOrUsersListOrUsername.length()+1];
            begin_index = copyBytesArray(message,op_bytes,begin_index,2);
            begin_index = copyBytesArray(message,content_bytes,begin_index, contentOrUsersListOrUsername.length());
            message[begin_index++] = '\0';
            message[begin_index++] = ';';
            connectionHandler.sendBytes(message,begin_index);
        }
        else if (opCode == "07") {
            char message[2+1];
            begin_index = copyBytesArray(message,op_bytes,begin_index,2);
            message[begin_index++] = ';';
            connectionHandler.sendBytes(message,begin_index);
        }
    }
}


// copy all the bytes from "fromArray" to "toArray" starting index begin in "toArray"
int KeyboardReader::copyBytesArray(char *toArray , const char *fromArray, int begin, size_t bytesToCopy) {
    for (size_t i = 0; i < bytesToCopy; i++){
        toArray[begin++] = fromArray[i];
    }
    return begin;
}

void KeyboardReader::shortToBytes( short num, char *bytesArr) {
    bytesArr[0] = ((num >> 8) & 0xFF);
    bytesArr[1] = (num & 0xFF);
}

string KeyboardReader::getTime(){
    time_t theTime = time(NULL);
    struct tm *aTime = localtime((&theTime));
    int day = aTime->tm_mday;
    int month = aTime->tm_mon+1;
    int year = aTime->tm_year+1900;
    int hour = aTime->tm_hour;
    int minutes = aTime->tm_min;
    return padInt(day)+"-"+padInt(month)+"-"+std::to_string(year)+" "+padInt(hour)+":"+padInt(minutes);
}

string KeyboardReader::padInt(int i){
    if (i < 10)
        return "0"+std::to_string(i);
    return std::to_string(i);
}

string KeyboardReader::extractContent(vector<string> input,int index){
    string content = "";
    while (index < int(input.size())) {
        content +=input.at(index) + " ";
        index++;
    }
    return content;
}
