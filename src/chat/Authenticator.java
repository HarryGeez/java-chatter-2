package chat;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Created by weijiangan on 03/10/2016.
 */
public class Authenticator extends UnicastRemoteObject implements AuthenticationInterface {
    User[] users = {};

    /* To be implemented later
    Authenticator(String fileName) {

    } */

    Authenticator() throws RemoteException {
        users = new User[] {
                new User("Distributed", "123456", User.Type.CLIENT),
                new User("Client1", "123456", User.Type.CLIENT),
                new User("Client3", "123456", User.Type.CLIENT),
                new User("Agent007", "jamesb", User.Type.AGENT),
                new User("Agent008", "lousy", User.Type.AGENT)
        };
    }

    public int authenticate(String username, String password) {
        int signal = 0;

        for (User user:users) {
            if (username.equalsIgnoreCase(user.getId())) {
                signal = -1;
                if (password.equals(user.getPw())) {
                    if (user.getType() == User.Type.CLIENT) {
                        signal = 1; // Set as client
                    } else {
                        signal = 2; // Set as agent
                    }
                    break;
                }
            }
        }

        return signal;
    }
}
