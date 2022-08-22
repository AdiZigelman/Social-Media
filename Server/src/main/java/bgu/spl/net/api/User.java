package bgu.spl.net.api;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class User {

    public String username;
    public String password;
    public String birthday;
    //public int id;
    public int age;
    public boolean loggedIn;
    public int connectionId;

    public User(String username, String password, String birthday){
        this.username = username;
        this.password = password;
        this.birthday = birthday;
        //this.id = -1;
        this.age = calcAge();
        this.loggedIn = false;
        this.connectionId = -1;
    }
    private int calcAge(){
        String[] data = birthday.split("-",-1);
        int birthYear = Integer.parseInt(data[2]);
        int birthMonth = Integer.parseInt(data[1]);
        int birthDay = Integer.parseInt(data[0]);
        String now = java.time.LocalDate.now().toString();
        String[] dataNow = now.split("-",-1);
        int year = Integer.parseInt(dataNow[0]);
        int month = Integer.parseInt(dataNow[1]);
        int day = Integer.parseInt(dataNow[2]);
        if (month > birthMonth || (month == birthMonth & day >= birthDay))
            return year-birthYear;
        return year-birthYear-1;
    }
}
