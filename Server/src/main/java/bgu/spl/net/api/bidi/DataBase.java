package bgu.spl.net.api.bidi;

import bgu.spl.net.api.User;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DataBase {

    private LinkedList<User> users;
    private ConcurrentHashMap<User, ConcurrentLinkedQueue<String>> missedMes;
    private ConcurrentHashMap<User, ConcurrentLinkedQueue<User>> followingList;
    private ConcurrentHashMap<User, ConcurrentLinkedQueue<User>> usersIBlocked;
    private ConcurrentHashMap<User, ConcurrentLinkedQueue<String>> posts;
    private ConcurrentHashMap<User, ConcurrentLinkedQueue<String>> pms; //sender + content
    private LinkedList<String> forbiddenWords;

    public static DataBase dataBase = null;

    private DataBase(){
        users = new LinkedList<>();
        missedMes = new ConcurrentHashMap<>();
        followingList = new ConcurrentHashMap<>();
        usersIBlocked = new ConcurrentHashMap<>();
        posts = new ConcurrentHashMap<>();
        pms = new ConcurrentHashMap<>();

        //Words to filter
        forbiddenWords = new LinkedList<>();
        forbiddenWords.add("Adi");
        forbiddenWords.add("Hagar");
        forbiddenWords.add("best");
        forbiddenWords.add("bohen");
        forbiddenWords.add("frontalchecks");
    }

    public static DataBase getInstance(){
        if (dataBase == null)
            dataBase = new DataBase();
        return dataBase;
    }

    public void addUser(User user){
        synchronized (followingList) {
            synchronized (users) {
                users.add(user);
                ConcurrentLinkedQueue<User> queue = new ConcurrentLinkedQueue<>();
                followingList.put(user, queue);
            }
        }
    }

    public boolean isUsernameReg(String name){
        for (User u : users) {
            if (u.username.equals(name))
                return true;
        }
        return false;
    }

    public User getUserByName(String name){
        for (User u : users) {
            if (u.username.equals(name))
                return u;
        }
        return null;
    }

    public void addMissedMes(User user, String message){
        synchronized (missedMes) {
            if (!missedMes.containsKey(user)) {
                ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<String>();
                missedMes.put(user, queue);
            }
            missedMes.get(user).add(message);
        }
    }

    public boolean isAFollowingB(User a, String bName){
        synchronized (followingList) {
            if (followingList.containsKey(a)) {
                for (User u : followingList.get(a)) {
                    if (u.username.equals(bName))
                        return true;
                }
            }
            return false;
        }
    }

    public boolean hasABlockedB(User a, String bName){
        synchronized (usersIBlocked) {
            if (usersIBlocked.containsKey(a)) {
                for (User u : usersIBlocked.get(a)) {
                    if (u.username.equals(bName))
                        return true;
                }
            }
            return false;
        }
    }


    public void aFollowB(User me, String name) {
        synchronized (followingList) {
            followingList.get(me).add(getUserByName(name));
        }
    }

    public void aUnFollowB(User me, String name) {
        synchronized (followingList) {
            followingList.get(me).remove(getUserByName(name));
        }
    }

    public LinkedList<User> whoIsReadingMyShit(User me){
        synchronized (followingList) {
            LinkedList<User> followers = new LinkedList<>();
            for (User someone : followingList.keySet()) {
                if (isAFollowingB(someone, me.username)) {
                    followers.add(someone);
                }
            }
            return followers;
        }
    }

    public void addPost(User sender, String msg){
        synchronized (posts) {
            if (!posts.containsKey(sender)) {
                ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<String>();
                posts.put(sender, queue);
            }
            posts.get(sender).add(msg);
        }
    }

    public void addPm(User sender, String msg){
        synchronized (pms) {
            if (!pms.containsKey(sender)) {
                ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<String>();
                pms.put(sender, queue);
            }
            pms.get(sender).add(msg);
        }
    }

    public String filterPM(String msg){
        for (String toFilter: forbiddenWords){
            if (msg.contains(toFilter+" "))
                msg = msg.replaceAll(toFilter, "<filtered>");
        }
        return msg;
    }

    public int numOfPosts(User u){
        synchronized (posts) {
            if (posts.containsKey(u))
                return posts.get(u).size();
            return 0;
        }
    }

    public int numOfFollowing(User u){
        synchronized (followingList) {
            return followingList.get(u).size();
        }
    }

    public void addBlocked(User blocking, User blocked){
        synchronized (usersIBlocked) {
            if (!usersIBlocked.containsKey(blocking)) {
                ConcurrentLinkedQueue<User> queue = new ConcurrentLinkedQueue<>();
                usersIBlocked.put(blocking, queue);
            }
            usersIBlocked.get(blocking).add(blocked);
        }
        synchronized (followingList) {
            if (isAFollowingB(blocking, blocked.username))
                followingList.get(blocking).remove(blocked);
            if (isAFollowingB(blocked, blocking.username))
                followingList.get(blocked).remove(blocking);
        }
    }

    public LinkedList<User> getUsers() {
        synchronized (users) {
            return users;
        }
    }

    public ConcurrentLinkedQueue<String> getMissedMes(User u){
        synchronized (missedMes){
            if (missedMes.containsKey(u))
                return missedMes.get(u);
            return null;
        }
    }
}
