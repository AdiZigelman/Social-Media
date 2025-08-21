package bgu.spl.net.api.bidi;

import bgu.spl.net.api.User;
import java.util.LinkedList;

public class BidiMessagingProtocolImpl <T> implements BidiMessagingProtocol<T>{

    private boolean shouldTerminate;
    private DataBase dataBase = DataBase.getInstance();
    private ConnectionsImpl<T> connections;
    private User currUser;
    private int connectionId;


    public BidiMessagingProtocolImpl(){
        shouldTerminate = false;
        dataBase = DataBase.getInstance();
    }

    @Override
    public void start(int connectionId, Connections<T> connections) {
        this.connections = (ConnectionsImpl<T>)connections;
        this.connectionId = connectionId;
        this.currUser = null;
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }

    @Override
    public void process(T message) {
        String mes = (String) message;
        String msg = "";
        String opCode = mes.substring(0,2); //the opcode
        if (mes.length() > 2) {
            msg = mes.substring(2, mes.length()); //the rest of the mes, without ;
        }
        String[] data = msg.split("\0",-2);
        if (opCode.equals("01")){ //Register
            String username = data[0];
            if (dataBase.isUsernameReg(username)) { //Error
                connections.send(connectionId, (T)"1101;");
            }
            else { //Ack
                String password = data[1];
                String birthday = data[2];
                User u = new User(username, password, birthday);
                dataBase.addUser(u);
                connections.send(connectionId, (T)"1001;");
            }
        }
        else if (opCode.equals("02")){ //Login
            if (currUser != null || dataBase.getUserByName(data[0]) == null|| !dataBase.getUserByName(data[0]).password.equals(data[1]) || data[2].equals("0")){ //Error
                connections.send(connectionId, (T)"1102;");
            }
            else { //Ack
                currUser = dataBase.getUserByName(data[0]);
                currUser.loggedIn = true;
                currUser.connectionId = connectionId;
                connections.send(connectionId, (T)"1002;");
                //getting all missed messages
                if (dataBase.getMissedMes(currUser) != null){
                    for (String missedMes : dataBase.getMissedMes(currUser))
                        connections.send(connectionId, (T)missedMes);
                    dataBase.getMissedMes(currUser).clear();
                }
            }
        }
        else if (opCode.equals("03")){ //Logout
            if (connections.getIdCon().isEmpty() || currUser == null) //Error
                connections.send(connectionId, (T)"1103;");
            else { //Ack
                connections.send(connectionId, (T) "1003;");
                connections.disconnect(connectionId);
                shouldTerminate = true;
                currUser.loggedIn = false;
                currUser.connectionId = -1;
                currUser = null;
            }
        }
        else if (opCode.equals("04")){ //Follow\UnFollow
            String followORNot = data[0].substring(0,1);
            String name = data[0].substring(1);
            if (currUser == null || (followORNot.equals("0") && dataBase.isAFollowingB(currUser, name)) ||
                    (followORNot.equals("1") && !dataBase.isAFollowingB(currUser, name)) ||
                    dataBase.hasABlockedB(dataBase.getUserByName(name), currUser.username) ||
                    dataBase.hasABlockedB(currUser, name)){ //Error
                connections.send(connectionId, (T)"1104;");
            }
            else { //Ack
                if (followORNot.equals("0")) {
                    dataBase.aFollowB(currUser, name);
                }
                else{
                    dataBase.aUnFollowB(currUser, name);
                }
                connections.send(connectionId,(T)("1004"+name+"\0"+";"));
            }
        }
        else if (opCode.equals("05")){ //Post
            if (currUser == null) //Error
                connections.send(connectionId, (T)"1105;");
            else {
                dataBase.addPost(currUser, data[0]);
                connections.send(connectionId, (T)"1005;"); //Ack
                LinkedList<User> followers = dataBase.whoIsReadingMyShit(currUser);
                LinkedList<User> tagged = findTagged(data[0]);
                followers.addAll(tagged);
                String notification = "091"+currUser.username+"\0"+data[0]+"\0"+";";
                for (User u: followers){
                    if (u.loggedIn) { //user is logged in
                        connections.send(u.connectionId, (T)notification);
                    }
                    else {
                        dataBase.addMissedMes(u, notification);
                    }
                }
            }
        }
        else if (opCode.equals("06")) { //PM
            if (currUser == null || !dataBase.isAFollowingB(currUser, data[0])) //Error
                connections.send(connectionId, (T)"1106;");
            else {
                String filteredMsg = dataBase.filterPM(data[1]);
                dataBase.addPm(currUser, filteredMsg);
                connections.send(connectionId, (T)"1006;"); //Ack
                String notification = "090"+currUser.username+"\0"+filteredMsg+"\0"+";";
                User u = dataBase.getUserByName(data[0]);
                if (u.loggedIn) { //user is logged in
                    connections.send(u.connectionId, (T)notification);
                }
                else {
                    dataBase.addMissedMes(u, notification);
                }
            }
        }
        else if (opCode.equals("07")) { //LOGSTAT
            if (currUser == null) //Error
                connections.send(connectionId, (T)"1107;");
            else {
                LinkedList<User> users = new LinkedList<>();
                for (User u : dataBase.getUsers()){
                    if (currUser != u && u.loggedIn && !dataBase.hasABlockedB(u, currUser.username) && !dataBase.hasABlockedB(currUser, u.username)) {
                        users.add(u);
                    }
                }
                sendInfo(users, "07");
            }
        }
        else if (opCode.equals("08")) { //STAT
            boolean badUser = false;
            if (currUser == null) //Error
                connections.send(connectionId, (T) "1108;");
            else {
                String[] usernames = data[0].split("[|]",-1);
                LinkedList<User> users = new LinkedList<>();
                for (String username : usernames){
                    User u = dataBase.getUserByName(username);
                    if (u != null && !dataBase.hasABlockedB(u, currUser.username) && !dataBase.hasABlockedB(currUser, u.username)) {
                        users.add(u);
                    }
                    else {
                        connections.send(connectionId, (T) "1108;");
                        badUser = true;
                    }
                }
                if (!badUser)
                    sendInfo(users, "08");
            }
        }
        else if (opCode.equals("12")) { //Block
            User toBlock = dataBase.getUserByName(data[0]);
            if (currUser == null || toBlock == null) //Error
                connections.send(connectionId, (T) "1112;");
            else {
                connections.send(connectionId, (T) "1012;");
                dataBase.addBlocked(currUser, toBlock);
            }
        }
    }

    private void sendInfo(LinkedList<User> users, String msType){
        for (User u : users) {
                connections.send(connectionId, (T) ("10" + msType + u.age + '\0' + dataBase.numOfPosts(u) + '\0' +
                        dataBase.whoIsReadingMyShit(u).size() + '\0' + dataBase.numOfFollowing(u) + ";"));
        }
    }

    //returns only tagged users that don't already follow me or is blocked
    private LinkedList<User> findTagged(String content){
        LinkedList<User> tagged = new LinkedList<>();
        String username;
        User user;
        while (content.contains("@")) {
            content = content.substring(content.indexOf("@"));
            username = content.substring(1, content.indexOf(" "));
            user = dataBase.getUserByName(username);
            if (user != null && !dataBase.hasABlockedB(currUser, user.username) &&
                    !dataBase.hasABlockedB(user, currUser.username) && !dataBase.isAFollowingB(user, currUser.username)){
                tagged.add(user);
            }
            content = content.substring(1);
        }
        return tagged;
    }
}
