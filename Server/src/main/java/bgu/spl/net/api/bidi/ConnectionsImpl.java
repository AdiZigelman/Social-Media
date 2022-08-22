package bgu.spl.net.api.bidi;
import bgu.spl.net.api.User;
import bgu.spl.net.srv.ConnectionHandler;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsImpl<T> implements Connections<T>{

    private ConcurrentHashMap<Integer, bgu.spl.net.srv.ConnectionHandler<T>> idCon;
    public static ConnectionsImpl connections = null;
    private DataBase dataBase;

    private ConnectionsImpl(){
        idCon = new ConcurrentHashMap<>();
        dataBase = DataBase.getInstance();
    }

    public static ConnectionsImpl getInstance(){
        if (connections == null)
            connections = new ConnectionsImpl<>();
        return connections;
    }

    @Override
    public synchronized boolean send(int connectionId, T msg) {
        if (idCon.containsKey(connectionId)){
            idCon.get(connectionId).send(msg);
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public synchronized void broadcast(T msg) {
        for(Integer id : idCon.keySet()){
            idCon.get(id).send(msg);
        }
    }

    @Override
    public synchronized void disconnect(int connectionId) {
        idCon.remove(connectionId);
    }

    public synchronized void addCon(Integer connectionID, bgu.spl.net.srv.ConnectionHandler<T> ch){
        idCon.put(connectionID, ch);
    }

    public synchronized ConcurrentHashMap<Integer, ConnectionHandler<T>> getIdCon() {
        return idCon;
    }

}
