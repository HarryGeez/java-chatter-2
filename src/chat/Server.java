package chat;

import java.io.*;
import java.net.*;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by weijiangan on 03/10/2016.
 */
public class Server {
    private static final int CLIENT_QUEUE_CAPACITY = 10;
    private static final Vector<Socket> users = new Vector<>();
    private static final Vector<Socket> onlineUsers = new Vector<>();
    private static final Vector<PrintWriter> outs = new Vector<>();
    private static final Vector<String[]> agents = new Vector<>();
    private static ArrayBlockingQueue<User> clientQueue;

    public static void main(String[] args) {
        ServerSocket listener;

        clientQueue = new ArrayBlockingQueue<>(CLIENT_QUEUE_CAPACITY);
        try {
            listener = new ServerSocket(8080);
            System.out.println("Server is up and running");
            LocateRegistry.createRegistry(1099);
            Authenticator loginObj = new Authenticator();
            Naming.rebind("ObjectForLogin", loginObj);
            System.out.println("Authentication server is up and running");
            while (true) new Handler(listener.accept()).start();
        } catch (Exception e) {
            System.out.println("Exception occurred: " + e);
        }
    }

    private static class Handler extends Thread {
        Socket client;
        PrintWriter clientOut;
        String message;

        Handler(Socket client) {
            this.client = client;
        }

        public void run() {
            try {
                users.add(client);
                clientOut = new PrintWriter(client.getOutputStream(), true);
                outs.add(clientOut);
                BufferedReader in =
                        new BufferedReader(new InputStreamReader(client.getInputStream()));
                while (true) {
                    message = in.readLine();
                    if (message == null) break;
                    System.out.println("Received from " + client.getRemoteSocketAddress() + ": " + message);
                    for (PrintWriter out : outs) {
                        out.println(message);
                    }
                }
            } catch (Exception e) {
                System.out.println("Exception occurred: " + e);
            } finally {
                outs.remove(clientOut);
                try {
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}