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
    private InetAddress ipAddress;
    private AudioFormat format;
    private int port;

    VoiceTransmitter(String address, int port) throws Exception {
        ipAddress = InetAddress.getByName(address);
        format = new AudioFormat(22050, 16, 2, true, true);
        this.port = port;
    }

    public void run() {
        try {
            DatagramSocket serverSocket = new DatagramSocket();
            DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, format);
            TargetDataLine targetLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
            targetLine.open(format, 1024);
            targetLine.start();

            int numBytesRead;
            byte[] buffer = new byte[targetLine.getBufferSize() / 4];

            while (true) {
                numBytesRead = targetLine.read(buffer, 0, buffer.length);
                serverSocket.send(new DatagramPacket(buffer, buffer.length, ipAddress, port));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Exception occurred making call: " + e,
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
