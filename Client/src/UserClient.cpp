#include <stdlib.h>
#include <connectionHandler.h>
#include <thread>
#include <mutex>
#include <condition_variable>
#include "KeyboardReader.h"
#include "SocketReader.h"

using namespace std;

int main (int argc, char *argv[]) {
    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }

    std::string host = argv[1];
    short port = atoi(argv[2]);

    ConnectionHandler connectionHandler(host, port);
    if (!connectionHandler.connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }
    std::mutex mutex;
    std::condition_variable conditionVariable;
    bool shouldTerminate = false;

    KeyboardReader keyboardReader(connectionHandler,mutex,conditionVariable,shouldTerminate);
    SocketReader socketReader(connectionHandler,mutex,conditionVariable,shouldTerminate);

    std::thread keyboardThread(std::ref(keyboardReader));
    std::thread socketThread(std::ref(socketReader));

    socketThread.join();
    keyboardThread.join();
}