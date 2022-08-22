package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.MessageEncoderDecoderImpl;
import bgu.spl.net.api.bidi.BidiMessagingProtocolImpl;
import bgu.spl.net.srv.Reactor;
import bgu.spl.net.srv.Server;

public class ReactorMain {
    public static <T> void main(String[] args){
        int port = Integer.parseInt(args[0]);
        int numOfThreads = Integer.parseInt(args[1]);
        try (Reactor<T> server = (Reactor<T>) Server.reactor(numOfThreads,port,()->new BidiMessagingProtocolImpl<>(),
                ()-> new MessageEncoderDecoderImpl<>());){
            server.serve();
        }
        catch (Exception e){}
    }
}
