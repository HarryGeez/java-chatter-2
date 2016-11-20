package chat;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.*;

/**
 * Created by weijiangan on 14/10/2016.
 */
public class ChatForm {
    InetAddress multicastGroupAddress;
    MulticastSocket mcSocket;
    private JButton broadcastButton;
    private JButton callButton;
    private JButton sendButton;
    private JFileChooser fileChooser;
    private JFrame frame;
    private JLabel userLabel;
    private JPanel chatPanel;
    private JScrollPane spConvo;
    private JTextArea taMessage;
    private Socket socket;
    private PrintWriter out;
    private VoiceReceiver receiver;
    private VoiceTransmitter caller;
    protected JTextArea taConvo;
    public static volatile boolean CALLING = false;

    public ChatForm(Socket socket) {
        this.socket = socket;
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

    private class callButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (callButton.getText().equals("Call")) {
                CALLING = true;
                try {
                    new VoiceTransmitter("127.0.0.1", 9877).start();
                    new VoiceReceiver(9876, callButton).start();
                } catch (BindException e1) {
                    JOptionPane.showMessageDialog(frame, "Port taken, please use another port", "Call Error",
                            JOptionPane.ERROR_MESSAGE);
                } catch (Exception e1) {
                    JOptionPane.showMessageDialog(frame, "Exception occurred: " + e1, "Call Error",
                            JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }
                callButton.setText("Hang up");

            } else {
                CALLING = false;
                callButton.setEnabled(false);
                callButton.setText("Call");
            }
        }
    }

    private void initGui() {
        frame = new JFrame("Chat");
        frame.setContentPane(chatPanel);
        frame.setLocationByPlatform(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        taMessage.addKeyListener(new sendMessageListeners());
        sendButton.addActionListener(new sendMessageListeners());
        broadcastButton.addActionListener(new sendMessageListeners());
        callButton.addActionListener(new callButtonListener());
        spConvo.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(218, 218, 218)));
        taConvo.setMargin(new Insets(5, 10, 5, 10));
        taMessage.setMargin(new Insets(5, 5, 5, 5));

        // Make conversation scroll to the bottom automatically
        DefaultCaret caret = (DefaultCaret)taConvo.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        frame.pack();
        frame.setMinimumSize(frame.getSize());
        frame.setVisible(true);
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

    void initClient() {
        initGui();
        userLabel.setText("Waiting for agent... Please be patient\n");
        sendButton.setEnabled(true);
        run();
    }

    void initAgent() {
        initGui();
        sendButton.setEnabled(true);
        broadcastButton.setEnabled(true);
        createMenuBar();
        Dimension curSize = frame.getSize();
        curSize.height += 20;
        frame.setSize(curSize);
        userLabel.setText("Finding customer in queue...\n");
        run();
    }

    void run() {
        try {
            multicastGroupAddress = InetAddress.getByName("224.0.1.0");
            mcSocket = new MulticastSocket(9090);
            String message;
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Listen for broadcast
            new BroadcastReceiver(this).start();

            while (true) {
                message = in.readLine();
                taConvo.append(message + "\n");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Exception occurred: " + e, "Login Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}
