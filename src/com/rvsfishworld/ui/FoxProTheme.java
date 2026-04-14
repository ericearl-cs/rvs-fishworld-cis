package com.rvsfishworld.ui;

import com.rvsfishworld.ui.core.CisScale;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.table.JTableHeader;
import javax.swing.text.*;
import java.awt.*;

public class FoxProTheme {
    public static final Color DESKTOP = new Color(212, 212, 212);
    public static final Color PANEL = new Color(224, 224, 224);
    public static final Color PANEL_DARK = new Color(204, 204, 204);
    public static final Color NAVY = new Color(0, 0, 153);
    public static final Color WHITE = Color.WHITE;
    public static final Color GRID = new Color(160, 160, 160);
    public static final Font FONT = new Font("Tahoma", Font.PLAIN, CisScale.scale(11));
    public static final Font FONT_BOLD = new Font("Tahoma", Font.BOLD, CisScale.scale(12));

    public static void applyGlobalFont() {
        UIManager.put("Label.font", FONT);
        UIManager.put("Button.font", FONT);
        UIManager.put("TextField.font", FONT);
        UIManager.put("Table.font", FONT);
        UIManager.put("TableHeader.font", FONT_BOLD);
        UIManager.put("CheckBox.font", FONT);
        UIManager.put("ComboBox.font", FONT);
        UIManager.put("OptionPane.font", FONT);
        UIManager.put("Menu.font", FONT);
        UIManager.put("MenuItem.font", FONT);
        UIManager.put("InternalFrame.titleFont", FONT_BOLD);
    }

    public static JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(CisScale.scale(104), CisScale.scale(30)));
        button.setBackground(WHITE);
        button.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        return button;
    }

    public static JButton createLookupButton() {
        JButton button = new JButton(new BinocularIcon());
        button.setFont(FONT);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(CisScale.scale(30), CisScale.scale(24)));
        button.setBackground(PANEL);
        button.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        return button;
    }

    public static JButton createOrderButton(String text, boolean selected) {
        JButton button = new JButton(text);
        button.setFont(FONT_BOLD);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        button.setPreferredSize(new Dimension(CisScale.scale(220), CisScale.scale(28)));
        if (selected) {
            button.setBackground(NAVY);
            button.setForeground(WHITE);
        } else {
            button.setBackground(WHITE);
            button.setForeground(Color.BLACK);
        }
        return button;
    }

    public static JTextField createTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(FONT);
        field.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        applyUppercase(field);
        return field;
    }

    public static void applyUppercase(JTextComponent component) {
        Document doc = component.getDocument();
        if (doc instanceof AbstractDocument abstractDocument) {
            abstractDocument.setDocumentFilter(new DocumentFilter() {
                @Override
                public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                    super.insertString(fb, offset, string == null ? null : string.toUpperCase(), attr);
                }

                @Override
                public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                    super.replace(fb, offset, length, text == null ? null : text.toUpperCase(), attrs);
                }
            });
        }
    }

    public static Border sectionBorder(String title) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                title,
                0,
                0,
                FONT_BOLD
        );
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT);
        table.setRowHeight(CisScale.scale(22));
        table.setGridColor(GRID);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD.deriveFont((float) CisScale.scale(11)));
        header.setReorderingAllowed(false);
        header.setBackground(WHITE);
        header.setForeground(Color.BLACK);
        header.setBorder(BorderFactory.createLineBorder(GRID));
    }

    public static JPanel createCommandBar(String... labels) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PANEL);

        for (String label : labels) {
            JButton button = createButton(label);
            button.setMaximumSize(new Dimension(106, 32));
            panel.add(button);
            panel.add(Box.createVerticalStrut(8));
        }
        return panel;
    }

    private static class BinocularIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.BLACK);
            g2.fillOval(x + 1, y + 3, 6, 6);
            g2.fillOval(x + 9, y + 3, 6, 6);
            g2.fillRect(x + 4, y + 2, 8, 3);
            g2.fillRect(x + 3, y + 8, 3, 4);
            g2.fillRect(x + 10, y + 8, 3, 4);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 16;
        }

        @Override
        public int getIconHeight() {
            return 14;
        }
    }
}
