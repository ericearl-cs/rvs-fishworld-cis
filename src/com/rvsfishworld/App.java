package com.rvsfishworld;

import com.rvsfishworld.dao.CompanyProfileDAO;
import com.rvsfishworld.db.AppBootstrap;
import com.rvsfishworld.model.AppSession;
import com.rvsfishworld.model.CompanyProfile;
import com.rvsfishworld.model.UserAccount;
import com.rvsfishworld.ui.admin.LoginDialog;
import com.rvsfishworld.ui.core.MainMenuFrame;
import java.time.LocalDateTime;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class App {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            AppBootstrap.ensureReady();
            LoginDialog login = new LoginDialog(null);
            login.setVisible(true);
            UserAccount user = login.getLoggedInUser();
            if (user == null) {
                return;
            }
            CompanyProfile profile = new CompanyProfileDAO().load();
            AppSession session = new AppSession(user, profile, LocalDateTime.now());
            AppRuntime.setSession(session);

            MainMenuFrame frame = new MainMenuFrame(session);
            frame.setVisible(true);
        });
    }
}
