package bgu.spl.net.api;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class MessageEncoderDecoderImpl<T> implements MessageEncoderDecoder<T>{

    private byte[] bytes;
    private int len;

    public MessageEncoderDecoderImpl(){
        bytes = new byte[1 << 16];
        len = 0;
    }

    @Override
    public T decodeNextByte(byte nextByte) {
        String finalAns = "";
//        if (nextByte == '\0'){
//            finalAns += popString() + "\0";
//        }
        if (nextByte == ';') {
            //return (T)finalAns;
            return (T)popString();
        }
        else {
            if (len >= bytes.length)
                bytes = Arrays.copyOf(bytes, bytes.length*2);
            bytes[len] = nextByte;
            len++;
            return null;
        }
    }

    @Override
    public byte[] encode(T message) {
        String mes = (String)message;
        return mes.getBytes();
    }

    private String popString(){
        String result = new String(bytes, 0, len, StandardCharsets.UTF_8);
        len = 0;
        return result;
    }
}
