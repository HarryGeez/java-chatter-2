package chat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.Socket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Created by weijiangan on 15/11/2016.
 */
public class LoginForm {
    private AuthenticationInterface login;
    private CardLayout cl;
    private JButton backButton;
    private JButton loginButton;
    private JButton networkSettingsButton;
    private JFrame frame;
    private JPanel mainPanel;
    private JPasswordField pfPassword;
    private JTextField tfServerIp;
    private JTextField tfServerPort;
    private JTextField tfUserName;
    private Socket socket;
    private String serverIp;

    private LoginForm() throws Exception {
        cl = (CardLayout)(mainPanel.getLayout());
        tfUserName.addKeyListener(new loginFieldListener());
        tfUserName.addActionListener(new loginFieldListener());
        pfPassword.addKeyListener(new loginFieldListener());
        pfPassword.addActionListener(new loginFieldListener());
        networkSettingsButton.addActionListener(new loginFieldListener());
        backButton.addActionListener(new loginFieldListener());
        loginButton.addActionListener(new loginFieldListener());
    }

    private class loginFieldListener implements KeyListener, ActionListener {
        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getSource() == tfUserName) {
                String temp = tfUserName.getText();
                if (!temp.equals("") && !temp.equals("username")) {
                    loginButton.setEnabled(true);
                } else {
                    loginButton.setEnabled(false);
                }
            } else if (e.getSource() == pfPassword) {
                String temp = new String(pfPassword.getPassword());
                if (!temp.equals("")) {
                    loginButton.setEnabled(true);
                } else {
                    loginButton.setEnabled(false);
                }
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == loginButton) {
                new Thread(() -> {
                    try {
                        if (login == null) {
                            serverIp = tfServerIp.getText();
                            Registry registry = LocateRegistry.getRegistry(serverIp);
                            login = (AuthenticationInterface) registry.lookup("ObjectForLogin");
                        }
                        resolveLogin(login.authenticate(tfUserName.getText(), new String(pfPassword.getPassword())));
                    } catch (Exception e1) {
                        JOptionPane.showMessageDialog(frame, "Exception occurred while attempting to log in: " + e1,
                                "Login Error", JOptionPane.ERROR_MESSAGE);
                        e1.printStackTrace();
                    }
                }).start();
            } else if (e.getSource() == tfUserName) {
                pfPassword.grabFocus();
            } else if (e.getSource() == pfPassword) {
                loginButton.doClick();
            } else if (e.getSource() == networkSettingsButton) {
                cl.next(mainPanel);
            } else if (e.getSource() == backButton) {
                cl.previous(mainPanel);
            }
        }

        @Override
        public void keyTyped(KeyEvent e) {}
        @Override
        public void keyPressed(KeyEvent e) {}
    }

    private void initGui() {
        frame = new JFrame("Login");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocationByPlatform(true);
        frame.setContentPane(mainPanel);
        frame.pack();
        frame.setMinimumSize(frame.getSize());
        frame.setVisible(true);
    }

    private void resolveLogin(String loginResult) throws Exception {
        String[] retVals = loginResult.split("#");
        switch (Integer.parseInt(retVals[0])) {
            case 0:
                JOptionPane.showMessageDialog(frame, "User not found! Please try again.", "Login Error",
                        JOptionPane.ERROR_MESSAGE);
                break;
            case -1:
                JOptionPane.showMessageDialog(frame, "Invalid password! Please try again.", "Login Error",
                        JOptionPane.ERROR_MESSAGE);
                break;
            case -2:
                JOptionPane.showMessageDialog(frame, "User already logged in! Please log out first.", "Login Error",
                        JOptionPane.ERROR_MESSAGE);
                break;
            case 1:
                frame.dispose();
                socket = new Socket(serverIp, Integer.parseInt(tfServerPort.getText()));
                new Thread(() -> {
                    new ChatForm(socket, retVals[1]).initClient();
                }).start();
                break;
            case 2:
                frame.dispose();
                socket = new Socket(serverIp, Integer.parseInt(tfServerPort.getText()));
                new Thread(() -> {
                    new ChatForm(socket, retVals[1]).initAgent();
                }).start();
                break;
        }
    }

    public static void main(String[] args) {
        try {
            LoginForm loginForm = new LoginForm();
            loginForm.initGui();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error connecting to login server: " + e,
                    "Connect Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
