package chat;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by weijiangan on 20/11/2016.
 */
public interface AuthenticationInterface extends Remote {
    int authenticate(String username, String password) throws RemoteException;
}
