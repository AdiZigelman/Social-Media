//package bgu.spl.net.api;
//
//import bgu.spl.net.api.bidi.ConnectionsImpl;
//import bgu.spl.net.api.bidi.DataBase;
//import bgu.spl.net.srv.ConnectionHandler;
//
//import java.util.LinkedList;
//import java.util.concurrent.ConcurrentHashMap;
//
//public class MessagingProtocolImpl<T> implements MessagingProtocol<T>{
//
//    private boolean shouldTerminate;
//    private DataBase dataBase;
//    private ConnectionsImpl connections;
//    private ConnectionHandler<T> connectionHandler;
//
//
//    public void MessagingProtocolImpl(){
//        shouldTerminate = false;
//        dataBase = DataBase.getInstance();
//        connections = ConnectionsImpl.getInstance();
//    }
//
//    @Override
//    public T process(T msg) {
//        String mes = (String) msg;
//        String opCode = mes.substring(0,2); //the opcode
//        mes = mes.substring(2, mes.length()-1); //the rest of the mes, without ;
//        String[] data = mes.split("0",-1);
//
//        if (opCode.equals("01")){ //Register
//            String username = data[0];
//            if (dataBase.isUsernameReg(username)) { //Error
//                return (T)"1101";
//            }
//            else { //Ack
//                String password = data[1];
//                String birthday = data[2];
//                User u = new User(username, password, birthday);
//                dataBase.addUser(u);
//                return (T)"1001";
//            }
//        }
//        else if (opCode.equals("02")){ //Login
//            User u = dataBase.getUserByName(data[0]);
//            if (u == null || !u.password.equals(data[1]) || u.id > -1 || data[2].equals("0")){ //Error
//                return (T)"1102";
//            }
//            else { //Ack
//                connections.addCon(u, connectionHandler);
//                return (T)"1002";
//            }
//        }
//        else if (opCode.equals("03")){ //Logout
//            shouldTerminate = true;
//            if (connections.getCon().isEmpty()) //Error
//                return (T)"1103";
//            else //Ack
//                return (T)"1003";
//        }
//        else if (opCode.equals("04")){ //Follow\UnFollow
//            User me = connections.getUserByHandler(connectionHandler);
//            String followORNot = data[0].substring(0,1);
//            String name = data[0].substring(1);
//            if (me == null || (followORNot.equals("0") && dataBase.isAFollowingB(me, name)) ||
//                    (followORNot.equals("1") && !dataBase.isAFollowingB(me, name)) ||
//                    dataBase.hasABlockedB(dataBase.getUserByName(name), me.username)){ //Error
//                return (T)"1104";
//            }
//            else { //Ack
//                if (followORNot.equals("0")) {
//                    if (dataBase.hasABlockedB(me, name))
//                        dataBase.aUnblockB(me, name);
//                    dataBase.aFollowB(me, name);
//                }
//                else{
//                    dataBase.aUnFollowB(me, name);
//                }
//                return (T)("1004"+name+"\0");
//            }
//        }
//        else if (opCode.equals("05")){
//            User me = connections.getUserByHandler(connectionHandler);
//            if (me == null) //Error
//                return (T)"1105";
//            else {
//
//            }
//        }
//
//        return null;
//    }
//
//    @Override
//    public boolean shouldTerminate() {
//        return shouldTerminate;
//    }
//
//    public void setConnectionHandler(ConnectionHandler<T> connectionHandler) {
//        this.connectionHandler = connectionHandler;
//    }
//
//    //returns only tagged users that dont already follow me
//    private LinkedList<User> findTagged(User me, String content){
//        LinkedList<User> tagged = new LinkedList<>();
//        String username;
//        User user;
//        while (content.contains("@")) {
//            content = content.substring(content.indexOf("@"));
//            username = content.substring(0, content.indexOf(" "));
//            user = dataBase.getUserByName(username);
//            if (user != null){
//                //if (data)
//            }
//
//
//        }
//        return tagged;
//    }
//}
