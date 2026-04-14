package com.rvsfishworld.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;

public class MainMenuFrame extends JFrame {
    private final JDesktopPane desktopPane = new JDesktopPane();
    private final Map<String, JInternalFrame> openFrames = new HashMap<>();

    public MainMenuFrame() {
        FoxProTheme.applyGlobalFont();

        setTitle("RVS FISHWORLD, INC. - C.I.S. ver2026_03_30");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setSize(1366, 768);
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // this keeps the main close button from exiting the app directly
            }
        });

        desktopPane.setBackground(FoxProTheme.DESKTOP);
        setContentPane(desktopPane);
        setJMenuBar(createMenuBar());
        add(buildStatusBar(), BorderLayout.SOUTH);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setFont(FoxProTheme.FONT);

        JMenu masterMenu = new JMenu("Master File");
        JMenu transactionMenu = new JMenu("Transaction");
        JMenu reportsMenu = new JMenu("Reports");
        JMenu utilitiesMenu = new JMenu("Utilities");

        masterMenu.add(menuItem("Product Master File", () -> openFrame("product", ProductMasterInternalFrame::new)));
        masterMenu.add(menuItem("Category Master File", () -> openFrame("category", CategoryMasterInternalFrame::new)));
        masterMenu.add(menuItem("Supplier Master File", () -> openFrame("supplier", SupplierMasterInternalFrame::new)));
        masterMenu.add(menuItem("Customer Master File", () -> openFrame("customer", CustomerMasterInternalFrame::new)));
        masterMenu.add(menuItem("Tran-Shipper Master File", () -> openFrame("trans", TransShipperInternalFrame::new)));
        masterMenu.addSeparator();
        masterMenu.add(menuItem("Quit", this::confirmQuit));

        transactionMenu.add(menuItem("Receiving of Fish Purchases", () -> openFrame("receiving_purchase", ReceivingPurchaseInternalFrame::new)));
        transactionMenu.add(menuItem("Receiving Sub-Station", () -> openFrame("receiving_substation", ReceivingSubStationInternalFrame::new)));
        transactionMenu.addSeparator();
        transactionMenu.add(menuItem("Proforma", () -> openFrame("proforma", ProformaInternalFrame::new)));
        transactionMenu.add(menuItem("Invoicing", () -> openFrame("sales_invoice", SalesInvoiceInternalFrame::new)));
        transactionMenu.add(menuItem("Mortality", () -> openFrame("mortality", MortalityInternalFrame::new)));

        reportsMenu.add(menuItem("Stock Report", () -> openFrame("stock_report", StockReportInternalFrame::new)));

        utilitiesMenu.add(menuItem("Quit", this::confirmQuit));

        menuBar.add(masterMenu);
        menuBar.add(transactionMenu);
        menuBar.add(reportsMenu);
        menuBar.add(utilitiesMenu);
        return menuBar;
    }

    private void confirmQuit() {
        int answer = JOptionPane.showConfirmDialog(
                this,
                "Post Today's Inventory to File?",
                "Confirm Quit",
                JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            dispose();
        }
    }

    private JPanel buildStatusBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(FoxProTheme.PANEL_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));

        JLabel left = new JLabel("Ready");
        left.setFont(FoxProTheme.FONT);
        JLabel right = new JLabel("Use Quit from the menu to exit.");
        right.setFont(FoxProTheme.FONT);

        panel.add(left, BorderLayout.WEST);
        panel.add(right, BorderLayout.EAST);
        return panel;
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
                if (!existing.isVisible()) {
                    existing.setVisible(true);
                }
                existing.toFront();
                existing.setSelected(true);
                return;
            }

            for (JInternalFrame frame : desktopPane.getAllFrames()) {
                if (frame != null && frame.isDisplayable() && !frame.isClosed() && frame.isVisible()) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Close the active window first.",
                            "Single-Window Restriction",
                            JOptionPane.WARNING_MESSAGE);
                    frame.toFront();
                    frame.setSelected(true);
                    return;
                }
            }

            JInternalFrame frame = factory.get();
            frame.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
            frame.addInternalFrameListener(new javax.swing.event.InternalFrameAdapter() {
                @Override
                public void internalFrameClosed(javax.swing.event.InternalFrameEvent e) {
                    openFrames.remove(key);
                }
            });

            desktopPane.add(frame);
            openFrames.put(key, frame);
            centerFrame(frame);
            frame.setVisible(true);
            frame.setSelected(true);
            frame.toFront();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Cannot open frame: " + e.getMessage(), "Open Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeStaleFrameReferences() {
        Iterator<Map.Entry<String, JInternalFrame>> iterator = openFrames.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, JInternalFrame> entry = iterator.next();
            JInternalFrame frame = entry.getValue();
            if (frame == null || !frame.isDisplayable() || frame.isClosed()) {
                iterator.remove();
            }
        }
    }

    private void centerFrame(JInternalFrame frame) {
        Dimension desktop = desktopPane.getSize();
        int x = Math.max(20, (desktop.width - frame.getWidth()) / 2);
        int y = Math.max(20, (desktop.height - frame.getHeight()) / 2);
        frame.setLocation(x, y);
    }
}
