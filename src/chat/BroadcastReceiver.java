package chat;

import javax.swing.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by weijiangan on 18/11/2016.
 */
public class BroadcastReceiver extends Thread {
    private MulticastSocket mcSocket;
    private InetAddress multicastGroupAddress;
    private JTextArea taConvo;

    BroadcastReceiver(ChatForm parent) throws IOException {
        this.mcSocket = parent.mcSocket;
        this.multicastGroupAddress = parent.multicastGroupAddress;
        this.taConvo = parent.taConvo;
    }

    public void run() {
        try {
            mcSocket.joinGroup(multicastGroupAddress);
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            while (true) {
                mcSocket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                taConvo.append(received + "\n");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Exception occurred while listening for broadcast: " + e,
                    "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        } finally {
            mcSocket.close();
        }
    }
}
