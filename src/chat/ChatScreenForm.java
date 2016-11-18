package chat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;

/**
 * Created by weijiangan on 14/10/2016.
 */
public class ChatScreenForm {
    protected JTextArea taConvo;
    private JTextArea taMessage;
    private JButton sendButton;
    private JButton broadcastButton;
    private JPanel chatPanel;
    private JFrame frame;
    private JFileChooser fileChooser;
    private PrintWriter out;
    InetAddress multicastGroupAddress;
    MulticastSocket mcSocket;


    public ChatScreenForm() {
        frame = new JFrame("Chat");
        frame.setContentPane(chatPanel);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        taMessage.addKeyListener(new sendMessageListeners());
        sendButton.addActionListener(new sendMessageListeners());
        broadcastButton.addActionListener(new sendMessageListeners());
        frame.pack();
        frame.setMinimumSize(frame.getSize());
        frame.setVisible(true);
    }

    private class sendMessageListeners implements ActionListener, KeyListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String text = taMessage.getText();
                if (e.getSource() == sendButton) {
                    out.println(text);
                } else if (e.getSource() == broadcastButton) {
                    mcSocket.send(new DatagramPacket(text.getBytes(), text.length(),
                            multicastGroupAddress, 9090));
                }
                taMessage.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Exception occurred: " + ex, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                e.consume();
                sendButton.doClick();
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {}

        @Override
        public void keyTyped(KeyEvent e) {}
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        // File menu
        JMenu file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_F);

        // File menu items
        JMenuItem miSave = new JMenuItem("Save conversation...");
        JMenuItem miExit = new JMenuItem("Exit");

        // Save conversation actions
        miSave.addActionListener((ActionEvent e) -> {
            fileChooser = new JFileChooser();
            int returnVal = fileChooser.showSaveDialog(frame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                    FileWriter fw = new FileWriter(fileChooser.getSelectedFile() + ".txt");
                    fw.write(taConvo.getText());
                    fw.close();
                } catch (Exception ex) {
                    System.out.println("Exception occurred " + ex);
                }
            }
        });

        // Exit actions
        miExit.setMnemonic(KeyEvent.VK_E);
        miExit.addActionListener((ActionEvent e) -> {
            System.exit(0);
        } );

        file.add(miSave);
        file.add(miExit);
        menuBar.add(file);
        frame.setJMenuBar(menuBar);
    }

    private void initClient() throws IOException, ClassNotFoundException {
        taConvo.append("Waiting for agent... Please be patient\n");
        sendButton.setEnabled(true);
    }

    private void initAgent() {
        sendButton.setEnabled(true);
        broadcastButton.setEnabled(true);
        createMenuBar();
        Dimension curSize = frame.getSize();
        curSize.height += 20;
        frame.setSize(curSize);
        taConvo.append("Finding customer in queue...\n");
    }

    private void run() throws Exception {
        final int portNum = 8081;
        String ipAddress = "127.0.0.1";
        Socket socket = new Socket(ipAddress, portNum);
        multicastGroupAddress = InetAddress.getByName("224.0.1.0");
        mcSocket = new MulticastSocket(9090);
        initAgent();
        String message;
        out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Listen for broadcast
        new BroadcastReceiver(this).start();

        VoiceTransmitter caller = new VoiceTransmitter("127.0.0.1", 9876);
        VoiceReceiver reciever = new VoiceReceiver(9877);

        while (true) {
            message = in.readLine();
            taConvo.append(message + "\n");
        }
    }

    public static void main(String[] args) {
        ChatScreenForm chatScreen = new ChatScreenForm();
        try {
            chatScreen.run();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Exception occurred: " + e, "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}
