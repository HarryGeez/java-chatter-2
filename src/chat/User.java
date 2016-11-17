package chat;

import java.io.*;
import java.net.*;

/**
 * Created by weijiangan on 19/09/2016.
 */
public class User implements Serializable {
    public enum Type {
        CLIENT, AGENT
    }

    private String id;
    private String pw;
    private Type type;

    public User() {};

    public User(String id, String pw) {
        this.id = id;
        this.pw = pw;
        this.type = null;
    }

    public User(String id, String pw, Type type) {
        this.id = id;
        this.pw = pw;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPw() {
        return pw;
    }

    public void setPw(String pw) {
        this.pw = pw;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
