#ifndef BOOST_ECHO_CLIENT_KEYBOARDREADER_H
#define BOOST_ECHO_CLIENT_KEYBOARDREADER_H
#include <mutex>
#include <condition_variable>
#include "connectionHandler.h"
#include <unordered_map>
using namespace std;

class KeyboardReader {
private:
    ConnectionHandler& connectionHandler;
    unordered_map<string, string> opCodes;
    mutex& mtx;
    condition_variable& conditionVariable;
    bool& shouldTerminate;
    void shortToBytes(short num, char* bytesArr);
    int copyBytesArray(char *toArray , const char *fromArray, int to_begin, size_t from_length);

public:
    KeyboardReader(ConnectionHandler&, mutex& mtx, condition_variable&,bool&);
    ~KeyboardReader()= default;
    KeyboardReader(const KeyboardReader&)= default;
    KeyboardReader &operator=(const KeyboardReader&)= default;
    void operator()();
    string getTime();
    string padInt(int i);

    string extractContent(vector<string> input, int index);
};


#endif
