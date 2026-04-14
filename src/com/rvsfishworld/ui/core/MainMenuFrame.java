package com.rvsfishworld.ui.core;

import com.rvsfishworld.AppRuntime;
import com.rvsfishworld.dao.AuditDAO;
import com.rvsfishworld.dao.CompanyProfileDAO;
import com.rvsfishworld.dao.UtilityService;
import com.rvsfishworld.model.AppSession;
import com.rvsfishworld.model.UserAccount;
import com.rvsfishworld.ui.admin.LoginDialog;
import com.rvsfishworld.ui.chrome.CisDesktopPane;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

@SuppressWarnings("serial")
public class MainMenuFrame extends JFrame {
    private static final DateTimeFormatter PROMPT_DATE = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private final CisDesktopPane desktopPane = new CisDesktopPane();
    private final Map<String, JInternalFrame> openFrames = new HashMap<>();
    private final AuditDAO auditDAO = new AuditDAO();
    private final UtilityService utilityService = new UtilityService();
    private final AppSession session;
    private final UserAccount user;

    public MainMenuFrame(AppSession session) {
        this.session = session;
        this.user = session.getUserAccount();
        CisTheme.applyGlobalFont();

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setSize(1366, 768);
        setLocationRelativeTo(null);
        refreshTitle();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmQuit();
            }
        });

        setContentPane(desktopPane);
        setJMenuBar(createMenuBar());
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setFont(CisTheme.FONT);
        CisTheme.styleMenuBar(menuBar);

        JMenu masterMenu = menu("Master File");
        JMenu transactionMenu = menu("Transaction");
        JMenu reportsMenu = menu("Reports");
        JMenu utilitiesMenu = menu("Utilities");

        if (canAccessCategory("M")) {
            addMenuItem(masterMenu, "Product File", "master_product");
            addMenuItem(masterMenu, "Category File", "master_category");
            addMenuItem(masterMenu, "Supplier File", "master_supplier");
            addMenuItem(masterMenu, "Customer File", "master_customer");
            addMenuItem(masterMenu, "Tran-Shipper", "master_trans_shipper");
            masterMenu.addSeparator();
            addMenuItem(masterMenu, "NET PRICES", "master_net_prices");
            addMenuItem(masterMenu, "STOP OF THE WEEK", "master_stop_week");
            addMenuItem(masterMenu, "CONTRACT PRICE (TRAN-SHIPPER)", "master_contract_trans");
            addMenuItem(masterMenu, "CONTRACT PRICE (SUPPLIER)", "master_contract_supplier");
            addMenuItem(masterMenu, "SPECIAL OF THE WEEK", "master_special_week");
            addMenuItem(masterMenu, "FLAT PRICE (SUPPLIER)", "master_flat_price");
            addMenuItem(masterMenu, "STOP FOREVER", "master_stop_forever");
            addMenuItem(masterMenu, "GROUP FILE", "master_group");
        }
        if (masterMenu.getItemCount() > 0) {
            masterMenu.addSeparator();
        }
        masterMenu.add(menuItem("Quit", this::confirmQuit));

        if (canAccessCategory("T")) {
            addMenuItem(transactionMenu, "RECEIVING OF FISH PURCHASES", "trans_receiving_purchase");
            addMenuItem(transactionMenu, "RECEIVING SUB-STATION", "trans_receiving_sub");
            addMenuItem(transactionMenu, "LOCAL SALES", "trans_local_sales");
            addMenuItem(transactionMenu, "PROFORMA", "trans_proforma");
            addMenuItem(transactionMenu, "SALES INVOICE", "trans_sales_invoice");
            addMenuItem(transactionMenu, "MORTALITY", "trans_mortality");
            addMenuItem(transactionMenu, "STOCK REPORT", "trans_stock_report");
        }

        if (canAccessCategory("R")) {
            addMenuItem(reportsMenu, "RECEIVING REPORT", "report_receiving");
            addMenuItem(reportsMenu, "RECEIVING SUB-STATION", "report_receiving_sub");
            addMenuItem(reportsMenu, "SALES REPORT", "report_sales");
            addMenuItem(reportsMenu, "SALES SUMMARY", "report_sales_summary");
        }

        utilitiesMenu.add(menuItem("Backup", this::backupDatabase));
        utilitiesMenu.add(menuItem("Password / User Rights", () -> runAdminUtility(
                "Password / User Rights",
                () -> openFrame("utility_users", () -> CisModuleFactory.createFrame("utility_users")))));
        utilitiesMenu.add(menuItem("Login as New User", this::loginAsNewUser));

        if (masterMenu.getItemCount() > 0) {
            menuBar.add(masterMenu);
        }
        if (transactionMenu.getItemCount() > 0) {
            menuBar.add(transactionMenu);
        }
        if (reportsMenu.getItemCount() > 0) {
            menuBar.add(reportsMenu);
        }
        menuBar.add(utilitiesMenu);
        return menuBar;
    }

    private JMenu menu(String title) {
        JMenu menu = new JMenu(title);
        CisTheme.styleMenu(menu);
        return menu;
    }

    private void addMenuItem(JMenu menu, String text, String key) {
        menu.add(menuItem(text, () -> openFrame(key, () -> CisModuleFactory.createFrame(key))));
    }

    private boolean canAccessCategory(String prefix) {
        if (user == null || user.isAdministrator()) {
            return true;
        }
        return switch (prefix.toUpperCase()) {
            case "M" -> user.hasRight("MASTER_ALL") || hasAnyFlag("M");
            case "T" -> user.hasRight("TRANSACTION_ALL") || hasAnyFlag("T");
            case "R" -> user.hasRight("REPORT_ALL") || hasAnyFlag("R");
            case "U" -> user.hasRight("UTILITY_ALL") || hasAnyFlag("U") || hasAnyFlag("S");
            default -> true;
        };
    }

    private boolean hasAnyFlag(String prefix) {
        for (Map.Entry<String, Boolean> entry : user.rawFlags().entrySet()) {
            if (entry.getKey().startsWith(prefix.toUpperCase()) && Boolean.TRUE.equals(entry.getValue())) {
                return true;
            }
        }
        return false;
    }

    private void backupDatabase() {
        try {
            java.nio.file.Path file = utilityService.createBackup();
            CisDialogs.showInfo(this, "Backup created:\n" + file.toAbsolutePath());
        } catch (RuntimeException e) {
            CisDialogs.showError(this, e.getMessage());
        }
    }

    private void runAdminUtility(String label, Runnable action) {
        if (user != null && !user.isAdministrator()) {
            auditDAO.log("ACCESS_DENIED", "Utilities", AppRuntime.username(), label + " requires administrator access.");
            CisDialogs.showInfo(this, label + " requires administrator access.");
            return;
        }
        action.run();
    }
//
    private void loginAsNewUser() {
        auditDAO.log("LOGOUT", "Security", AppRuntime.username(), "User switched account.");
        LoginDialog dialog = new LoginDialog(this);
        dialog.setVisible(true);
        if (dialog.getLoggedInUser() == null) {
            return;
        }
        AppSession nextSession = new AppSession(dialog.getLoggedInUser(), new CompanyProfileDAO().load(), java.time.LocalDateTime.now());
        AppRuntime.setSession(nextSession);
        auditDAO.log("LOGIN", "Security", nextSession.getUserAccount().getUsername(), "User logged in.");
        dispose();
        MainMenuFrame nextFrame = new MainMenuFrame(nextSession);
        nextFrame.setVisible(true);
    }

    private void confirmQuit() {
        LocalDate today = LocalDate.now();
        int answer = CisDialogs.askYesNoCancel(this, "POST INVENTORY", "POST TODAYS " + today.format(PROMPT_DATE) + " INVENTORY TO FILE?");
        if (answer == CisDialogs.CANCEL || answer == CisDialogs.CLOSED) {
            return;
        }
        if (answer == CisDialogs.YES) {
            try {
                utilityService.postTodayInventory(today);
            } catch (RuntimeException e) {
                CisDialogs.showError(this, e.getMessage());
                return;
            }
        }
        auditDAO.log("LOGOUT", "Security", AppRuntime.username(), "User exited application.");
        dispose();
    }

    private JMenuItem menuItem(String text, Runnable action) {
        JMenuItem item = new JMenuItem(text);
        item.addActionListener(e -> action.run());
        return item;
    }

    private void openFrame(String key, Supplier<JInternalFrame> factory) {
        try {
            removeStaleFrameReferences();

            JInternalFrame existing = openFrames.get(key);
            if (existing != null && existing.isDisplayable() && !existing.isClosed()) {
                desktopPane.closeOtherFrames(existing);
                if (!existing.isVisible()) {
                    existing.setVisible(true);
                }
                existing.toFront();
                existing.setSelected(true);
                desktopPane.refreshDesktop();
                return;
            }

            desktopPane.closeOtherFrames(null);
            desktopPane.removeAll();
            openFrames.clear();
            desktopPane.refreshDesktop();

            JInternalFrame frame = factory.get();
            if (frame == null) {
                CisDialogs.showInfo(this, "Module is not yet available in the rebuilt current package tree.");
                return;
            }
            frame.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
            frame.addInternalFrameListener(new InternalFrameAdapter() {
                @Override
                public void internalFrameClosed(InternalFrameEvent e) {
                    openFrames.remove(key);
                    desktopPane.refreshDesktop();
                }
            });

            desktopPane.add(frame);
            openFrames.put(key, frame);
            centerFrame(frame);
            frame.setVisible(true);
            frame.setSelected(true);
            frame.toFront();
            desktopPane.refreshDesktop();
        } catch (PropertyVetoException | RuntimeException e) {
            System.err.println("Cannot open frame [" + key + "]: " + e.getMessage());
            CisDialogs.showError(this, "Cannot open frame: " + e.getMessage());
        }
    }

    private void removeStaleFrameReferences() {
        Iterator<Map.Entry<String, JInternalFrame>> iterator = openFrames.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, JInternalFrame> entry = iterator.next();
            JInternalFrame frame = entry.getValue();
            if (frame == null || !frame.isDisplayable() || frame.isClosed()) {
                if (frame != null) {
                    desktopPane.remove(frame);
                }
                iterator.remove();
            }
        }
        desktopPane.refreshDesktop();
    }

    private void centerFrame(JInternalFrame frame) {
        Dimension desktop = desktopPane.getSize();
        int x = Math.max(20, (desktop.width - frame.getWidth()) / 2);
        int y = Math.max(20, (desktop.height - frame.getHeight()) / 2);
        frame.setLocation(x, y);
    }

    private void refreshTitle() {
        String company = session != null && session.getCompanyProfile() != null && !session.getCompanyProfile().getCompanyName().isBlank()
                ? session.getCompanyProfile().getCompanyName()
                : "RVS FISHWORLD, INC.";
        String username = session != null && session.getUserAccount() != null
                ? session.getUserAccount().getUsername()
                : "SYSTEM";
        setTitle(company + " - (C.I.S. ver20260413) [" + username + "]");
    }
}
