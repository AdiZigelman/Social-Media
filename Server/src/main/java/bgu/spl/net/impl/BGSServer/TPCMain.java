package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.MessageEncoderDecoderImpl;
import bgu.spl.net.api.bidi.BidiMessagingProtocolImpl;
import bgu.spl.net.srv.BaseServer;
import bgu.spl.net.srv.Server;

public class TPCMain {
    public static <T> void main(String[] args){
        int port = Integer.parseInt(args[0]);
        try (BaseServer<T> server = (BaseServer<T>) Server.threadPerClient(port,()->new BidiMessagingProtocolImpl<>(),
                ()-> new MessageEncoderDecoderImpl<>());){
            server.serve();
        }
        catch (Exception e){}
    }
}
