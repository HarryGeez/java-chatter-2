package chat;

import javax.sound.sampled.*;
import javax.swing.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by weijiangan on 17/11/2016.
 */
public class VoiceTransmitter extends Thread {
    private AudioFormat format;
    private DatagramSocket serverSocket;
    private InetAddress ipAddress;
    private TargetDataLine targetLine;
    private int port;
    private static boolean KILLING = false;

    VoiceTransmitter(String address, int port) throws Exception {
        ipAddress = InetAddress.getByName(address);
        format = new AudioFormat(22050, 16, 2, true, true);
        this.port = port;
    }

    public void run() {
        try {
            serverSocket = new DatagramSocket();
            DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, format);
            targetLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
            targetLine.open(format, 1024);
            targetLine.start();
            byte[] buffer = new byte[targetLine.getBufferSize() / 4];

            while (true) {
                targetLine.read(buffer, 0, buffer.length);
                serverSocket.send(new DatagramPacket(buffer, buffer.length, ipAddress, port));
            }

        } catch (Exception e) {
            if (!KILLING) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error making call", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } finally {
            if (serverSocket != null)
                serverSocket.close();
        }
    }

    public void kill() {
        KILLING = true;
        targetLine.drain();
        targetLine.stop();
        serverSocket.close();
    }
}
