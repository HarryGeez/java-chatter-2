package chat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Created by weijiangan on 15/11/2016.
 */
public class LoginForm {
    private JTextField usernameTextField;
    private JPanel panel1;
    private JButton loginButton;
    private JPasswordField passwordPasswordField;
    private JDialog dialog;

    LoginForm(Frame parentFrame) throws Exception {
        dialog = new JDialog(parentFrame, "Login", true);
        dialog.setLocationRelativeTo(dialog);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setContentPane(panel1);
        loginButton.setEnabled(false);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });

        usernameTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String temp = usernameTextField.getText();
                if (!temp.equals("") && !temp.equals("username")) {
                    loginButton.setEnabled(true);
                } else {
                    loginButton.setEnabled(false);
                }

            }
        });
        passwordPasswordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String temp = new String(passwordPasswordField.getPassword());
                if (!temp.equals("")) {
                    loginButton.setEnabled(true);
                } else {
                    loginButton.setEnabled(false);
                }
            }
        });
        dialog.pack();
        dialog.setVisible(true);
    }
}
