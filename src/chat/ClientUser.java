package chat;

import java.io.*;
import java.net.InetAddress;

/**
 * Created by weijiangan on 19/09/2016.
 */
public class ClientUser implements Serializable {
    String id;
    String pw;

    public ClientUser() {};

    public ClientUser(String id, String pw) {
        this.id = id;
        this.pw = pw;
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
}

class Agent extends ClientUser {
    private PrintWriter[] clientUsers;
    private InetAddress mcGroupAddress;

    public Agent(String id, String pw) {
        this.id = id;
        this.pw = pw;
        this.clientUsers = new PrintWriter[2];
    }

    public InetAddress getMcGroupAddress() {
        return mcGroupAddress;
    }

    public void setMcGroupAddress(InetAddress mcGroupAddress) {
        this.mcGroupAddress = mcGroupAddress;
    }

    public PrintWriter[] getClientUsers() {
        return clientUsers;
    }

    public int putClientUsers(PrintWriter clientUser) {
        int i;
        for (i = 0; i < clientUsers.length; i++) {
            if (clientUsers[i] == null) {
                clientUsers[i] = clientUser;
            }
        }
        return i;
    }

    public int availSlot() {
        int slots = 0;
        for (int i = 0; i < clientUsers.length; i++) {
            if (clientUsers[i] == null) {
                slots++;
            }
        }
        return slots;
    }

}