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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by weijiangan on 14/10/2016.
 */
public class ChatForm {
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mma");
    private InetAddress multicastAddress;
    private JButton broadcastButton;
    private JButton callButton;
    private JButton sendButton;
    private JFileChooser fileChooser;
    private JFrame frame;
    private JLabel userLabel;
    private JPanel chatPanel;
    private JScrollPane spConvo;
    private JTextArea taConvo;
    private JTextArea taMessage;
    private MulticastSocket mcSocket;
    private PrintWriter out;
    private Socket socket;
    private String sessionId;
    private VoiceReceiver receiver;
    private VoiceTransmitter caller;
    private boolean isClient;
    private static String USR_NAME;
    public static final int MC_PORT = 9090;
    public static final int CA_PORT = 9877;
    public static final int RC_PORT = 9876;
    public static final String CA_IP = "127.0.0.1";

    ChatForm(Socket socket, String sessionId) {
        this.socket = socket;
        this.sessionId = sessionId;
        isClient = false;

    }

    private class sendMessageListeners implements ActionListener, KeyListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String text = taMessage.getText();
                if (e.getSource() == sendButton) {
                    out.println(text);
                } else if (e.getSource() == broadcastButton) {
                    text = LocalTime.now().format(formatter) + "  " + USR_NAME + ": " + text;
                    mcSocket.send(new DatagramPacket(text.getBytes(), text.length(), multicastAddress, MC_PORT));
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
            out.println("/INITCALL " + RC_PORT);
            /*if (callButton.getText().equals("Call")) {
                try {
                    caller = new VoiceTransmitter(CA_IP, CA_PORT);
                    receiver = new VoiceReceiver(RC_PORT, callButton);
                    caller.start();
                    receiver.start();
                } catch (BindException e1) {
                    JOptionPane.showMessageDialog(frame, "Port taken, please use another port", "Error",
                            JOptionPane.ERROR_MESSAGE);
                } catch (Exception e1) {
                    JOptionPane.showMessageDialog(frame, "Failed to make call: " + e1, "Error",
                            JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }
                callButton.setText("End Call");
            } else {
                callButton.setEnabled(false);
                caller.kill();
                receiver.kill();
                callButton.setText("Call");
            }*/
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
        JMenuItem fileNew = new JMenuItem("New Conversation");
        JMenuItem fileSave = new JMenuItem("Save Conversation...");
        JMenuItem fileExit = new JMenuItem("Exit");

        class MenuListener implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == fileNew) {
                    try {
                        Socket newSocket = new Socket(socket.getInetAddress(), socket.getPort());
                        new Thread(() -> {
                            new ChatForm(newSocket, sessionId).initAgent();
                        }).start();
                    } catch (Exception e1) {
                        JOptionPane.showMessageDialog(frame, "Error creating new conversation: " + e,
                                "New Conversation", JOptionPane.ERROR_MESSAGE);
                    }
                } else if (e.getSource() == fileSave) {
                    fileChooser = new JFileChooser();
                    int returnVal = fileChooser.showSaveDialog(frame);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        try {
                            FileWriter fw = new FileWriter(fileChooser.getSelectedFile() + ".txt");
                            fw.write(taConvo.getText());
                            fw.close();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                } else if (e.getSource() == fileExit) {
                    System.exit(0);
                }
            }
        }

        fileNew.addActionListener(new MenuListener());
        fileSave.addActionListener(new MenuListener());
        fileExit.setMnemonic(KeyEvent.VK_E);
        fileExit.addActionListener(new MenuListener());

        file.add(fileNew);
        file.add(fileSave);
        file.add(fileExit);
        menuBar.add(file);
        frame.setJMenuBar(menuBar);
    }

    void initClient() {
        initGui();
        isClient = true;
        userLabel.setText("Waiting for agent... Please be patient\n");
        sendButton.setEnabled(true);
        run();
    }

    void initAgent() {
        initGui();
        sendButton.setEnabled(true);
        broadcastButton.setEnabled(true);
        createMenuBar();
        frame.pack();
        frame.setMinimumSize(frame.getSize());
        userLabel.setText("Finding customer in queue...\n");
        run();
    }

    private void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println(sessionId);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String[] tokens = in.readLine().split("#");
            System.out.println(tokens[0].substring(1));
            multicastAddress = InetAddress.getByName(tokens[0].substring(1));
            mcSocket = new MulticastSocket(9090);
            USR_NAME = tokens[1];
            if (isClient) {
                out.println(tokens[2]);
            }
            // Listen for broadcast
            new BroadcastReceiver(mcSocket, multicastAddress, taConvo).start();

            while (true) {
                String message = in.readLine();
                if (message == null) break;
                taConvo.append(message + "\n");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Exception occurred: " + e, "Oops",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }
    }
}
