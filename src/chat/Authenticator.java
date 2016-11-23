package chat;

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.SecureRandom;
import java.util.Hashtable;

/**
 * Created by weijiangan on 03/10/2016.
 */
public class Authenticator extends UnicastRemoteObject implements AuthenticationInterface {
    private ClientUser[] clientUsers;
    private SessionIdentifierGenerator sIG;
    private Hashtable<String,ClientUser> sessions;

    /* To be implemented later
    Authenticator(String fileName) {

    } */

    Authenticator() throws RemoteException {
        clientUsers = new ClientUser[] {
                new ClientUser("Distributed", "123456"),
                new ClientUser("Client1", "123456"),
                new ClientUser("Client3", "123456"),
                new Agent("Agent007", "jamesb"),
                new Agent("Agent008", "lousy")
        };
        sIG = new SessionIdentifierGenerator();
        sessions = new Hashtable<>();
    }

    public String authenticate(String username, String password) {
        int signal = 0;
        String sessionId = sIG.nextSessionId();

        for (ClientUser clientUser : clientUsers) {
            if (username.equalsIgnoreCase(clientUser.getId())) {
                signal = -1;
                if (password.equals(clientUser.getPw())) {
                    sessions.put(sessionId, clientUser);
                    if (!(clientUser instanceof Agent)) {
                        signal = 1; // Set as client
                    } else {
                        signal = 2; // Set as agent
                    }
                    break;
                }
            }
        }
        return (signal + "#" + sessionId);
    }

    ClientUser getUser(String sessionId) {
        return sessions.get(sessionId);
    }

    private class SessionIdentifierGenerator {
        private SecureRandom random = new SecureRandom();

        public String nextSessionId() {
            return new BigInteger(130, random).toString(32);
        }
    }
}
