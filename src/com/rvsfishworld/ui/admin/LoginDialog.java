package com.rvsfishworld.ui.admin;

import com.rvsfishworld.dao.AppUserDAO;
import com.rvsfishworld.model.UserAccount;
import com.rvsfishworld.ui.FoxProTheme;
import com.rvsfishworld.ui.chrome.FoxProChildDialog;
import com.rvsfishworld.ui.core.CisDialogs;
import com.rvsfishworld.ui.core.CisScale;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class LoginDialog extends FoxProChildDialog {
    private static final Path SECURITY_BMP = Path.of("C:\\rvs_fish\\PICTURES\\SECURITY.BMP");
    private static final Path CHECK_BMP = Path.of("C:\\rvs_fish\\PICTURES\\CHECK.BMP");
    private static final Path EXIT_BMP = Path.of("C:\\rvs_fish\\PICTURES\\EXIT.BMP");

    private final AppUserDAO userDao = new AppUserDAO();
    private final JTextField usernameField = FoxProTheme.createTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);
    private final JButton loginButton = FoxProTheme.createButton("Accept");
    private UserAccount loggedInUser;

    public LoginDialog(Frame owner) {
        super(owner, "(USER)", CisScale.scale(470), CisScale.scale(235));
        setResizable(false);
        setContentPane(buildContent());
        loadUsers();
        getRootPane().setDefaultButton(loginButton);
    }

    public UserAccount getLoggedInUser() {
        return loggedInUser;
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout(CisScale.scale(10), CisScale.scale(10)));
        root.setBackground(FoxProTheme.PANEL);
        root.setBorder(BorderFactory.createEmptyBorder(
                CisScale.scale(10), CisScale.scale(10), CisScale.scale(10), CisScale.scale(10)));
        root.add(buildBody(), BorderLayout.CENTER);
        root.add(buildActions(), BorderLayout.SOUTH);
        return root;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(CisScale.scale(12), 0));
        body.setBackground(FoxProTheme.PANEL);
        body.add(buildSecurityPanel(), BorderLayout.WEST);
        body.add(buildForm(), BorderLayout.CENTER);
        return body;
    }

    private JPanel buildSecurityPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(CisScale.scale(145), CisScale.scale(160)));
        panel.setBackground(FoxProTheme.PANEL);
        panel.setBorder(BorderFactory.createEtchedBorder());

        JLabel image = new JLabel();
        image.setHorizontalAlignment(JLabel.CENTER);
        ImageIcon icon = loadIcon(SECURITY_BMP);
        if (icon != null) {
            image.setIcon(icon);
        } else {
            image.setText("SECURITY");
            image.setFont(FoxProTheme.FONT_BOLD);
        }
        panel.add(image, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildForm() {
        usernameField.setPreferredSize(new Dimension(CisScale.scale(190), CisScale.scale(24)));
        usernameField.addActionListener(e -> passwordField.requestFocusInWindow());

        passwordField.setFont(FoxProTheme.FONT);
        passwordField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        passwordField.setPreferredSize(new Dimension(CisScale.scale(190), CisScale.scale(24)));
        passwordField.addActionListener(this::onLogin);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(FoxProTheme.PANEL);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(CisScale.scale(10), CisScale.scale(10), CisScale.scale(10), CisScale.scale(10))));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(CisScale.scale(6), CisScale.scale(8), CisScale.scale(6), CisScale.scale(8));
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        form.add(label("User Name         "), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        form.add(label("User Password "), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(passwordField, gbc);

        return form;
    }

    private JPanel buildActions() {
        JButton exitButton = FoxProTheme.createButton("Exit");
        loginButton.setPreferredSize(new Dimension(CisScale.scale(112), CisScale.scale(34)));
        exitButton.setPreferredSize(new Dimension(CisScale.scale(112), CisScale.scale(34)));

        ImageIcon check = loadIcon(CHECK_BMP);
        if (check != null) {
            loginButton.setIcon(check);
        }
        ImageIcon exit = loadIcon(EXIT_BMP);
        if (exit != null) {
            exitButton.setIcon(exit);
        }

        loginButton.addActionListener(this::onLogin);
        exitButton.addActionListener(e -> dispose());

        JPanel actions = new JPanel();
        actions.setBackground(FoxProTheme.PANEL);
        actions.add(loginButton);
        actions.add(exitButton);
        return actions;
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text + ":");
        label.setFont(FoxProTheme.FONT);
        return label;
    }

    private void loadUsers() {
        List<String> users = userDao.listActiveUsernames();
        usernameField.setText("");
        if (!users.isEmpty()) {
            usernameField.requestFocusInWindow();
        }
    }

    private void onLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (username.isBlank()) {
            CisDialogs.showInfo(this, "Enter a user name.");
            return;
        }
        UserAccount user = userDao.validateLogin(username, password);
        if (user == null) {
            CisDialogs.showError(this, "Invalid username or password.");
            passwordField.selectAll();
            passwordField.requestFocusInWindow();
            return;
        }
        loggedInUser = user;
        dispose();
    }

    private ImageIcon loadIcon(Path path) {
        if (Files.exists(path)) {
            return new ImageIcon(path.toString());
        }
        return null;
    }
}
