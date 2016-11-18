package chat;

import javax.sound.sampled.*;
import javax.swing.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Created by weijiangan on 17/11/2016.
 */
public class VoiceReceiver extends Thread {
    private AudioFormat format;
    private DatagramSocket clientSocket;

    VoiceReceiver(int port) throws SocketException {
        format = new AudioFormat(22050, 16, 2, true, true);
        clientSocket = new DatagramSocket(port);
    }

    public void run() {
        try {
            DataLine.Info sourceInfo = new DataLine.Info(SourceDataLine.class, format);
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            SourceDataLine sourceLine = (SourceDataLine) AudioSystem.getLine(sourceInfo);
            sourceLine.open(format, 1024);
            sourceLine.start();

            while (true) {
                clientSocket.receive(packet);
                sourceLine.write(packet.getData(), 0, packet.getLength());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Exception occurred receiving call: " + e,
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
