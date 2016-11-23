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
    private InetAddress multicastAddress;
    private JTextArea textArea;
    private MulticastSocket mcSocket;

    BroadcastReceiver(MulticastSocket mcSocket, InetAddress mcAddress, JTextArea textArea) throws IOException {
        this.mcSocket = mcSocket;
        this.multicastAddress = mcAddress;
        this.textArea = textArea;
    }

    public void run() {
        try {
            mcSocket.joinGroup(multicastAddress);
            byte[] buffer = new byte[512];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            while (true) {
                mcSocket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                textArea.append(received + "\n");
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
