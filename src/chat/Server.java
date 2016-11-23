package chat;

import java.io.*;
import java.net.*;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Hashtable;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by weijiangan on 03/10/2016.
 */
public class Server {
    private final static int REGISTRY_PORT = 1099;
    private static ArrayBlockingQueue<PrintWriter> clientUserQueue;
    private static Authenticator loginObj;
    private static final Hashtable<UUID,PrintWriter> pairing = new Hashtable<>();
    private static final int CLIENT_QUEUE_CAPACITY = 10;
    private static mcAddressGenerator mcAddressGenerator;

    public static void main(String[] args) {
        if (args[0] == null) System.out.println("Server port not specified. Please supply port number as args[0].");
        ServerSocket listener;
        clientUserQueue = new ArrayBlockingQueue<>(CLIENT_QUEUE_CAPACITY);
        chat.Server.mcAddressGenerator = new mcAddressGenerator();

        try {
            listener = new ServerSocket(Integer.parseInt(args[0]));
            System.out.println("Server is up and listening at port " + args[0]);
            LocateRegistry.createRegistry(REGISTRY_PORT);
            loginObj = new Authenticator();
            Naming.rebind("ObjectForLogin", loginObj);
            System.out.println("Authentication server is up and running on port " + REGISTRY_PORT);
            while (true) new Handler(listener.accept()).start();
        } catch (Exception e) {
            System.out.println("Exception occurred: " + e);
        }
    }

    private static class Handler extends Thread {
        ClientUser currentUser;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mma");
        PrintWriter clientOut, targetOut;
        Socket client;
        String message;

        Handler(Socket client) {
            this.client = client;
        }

        public void run() {
            InetAddress mcAddress = null;
            try {
                BufferedReader in =
                        new BufferedReader(new InputStreamReader(client.getInputStream()));
                currentUser = loginObj.getUser(in.readLine());
                clientOut = new PrintWriter(client.getOutputStream(), true);


                if (currentUser instanceof Agent) {
                    // Check the number of clients the agent is serving
                    if (((Agent) currentUser).availSlot() < 0) {
                        clientOut.println("You are only allowed to serve maximum 2 customers at once. " +
                                "You won't be assigned more customers.");
                        client.close();
                        return;
                    }
                    // Generate and set multicast address in agent
                    if (((Agent) currentUser).getMcGroupAddress() == null) {
                        mcAddress = mcAddressGenerator.nextAddress();
                        ((Agent) currentUser).setMcGroupAddress(mcAddress);
                    } else {
                        mcAddress = ((Agent) currentUser).getMcGroupAddress();
                    }
                    clientOut.println(mcAddress + "#" + currentUser.getId());
                    // Take client out of queue
                    targetOut = clientUserQueue.take();
                    // Generate pairings
                    UUID pairId = UUID.randomUUID();
                    pairing.put(pairId,clientOut);
                    // Send init information to client
                    targetOut.println(mcAddress + "#" + currentUser.getId() + "#" + pairId);

                } else {
                    clientUserQueue.put(clientOut);
                    targetOut = pairing.remove(UUID.fromString(in.readLine()));
                }

                while (true) {
                    message = in.readLine();
                    if (message == null) break;
                    message = LocalTime.now().format(formatter) + "  " + currentUser.getId() + ": " + message;
                    clientOut.println(message);
                    targetOut.println(message);
                }

            } catch (Exception e) {
                System.out.println("Exception occurred: " + e);
            } finally {
                try {
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class mcAddressGenerator {
        private static final int RANGE_BEGIN = 224;
        private static final int RANGE_END = 239;
        private static final int PART_LIMIT = 255;
        private int[] part = new int[4];

        mcAddressGenerator() {
            part[0] = RANGE_BEGIN;
            part[1] = 0;
            part[2] = 0;
            part[3] = 0;
        }

        public InetAddress nextAddress() {
            String tmp = "";
            InetAddress address = null;
            for (int i = 3; i >= 0; i--) {
                part[i]++;
                if (part[i] <= PART_LIMIT) {
                    break;
                } else {
                    part[i] = 0;
                }
            }
            for (int i = 0; i < 4; i++) {
                tmp = tmp.concat(Integer.toString(part[i]) + ((i == 3) ? "" : "."));
            }
            try {
                address = InetAddress.getByName(tmp);
            } catch (UnknownHostException e) {
                System.out.println("Failed to generate multicast address.");
            }
            return address;
        }
    }
}
