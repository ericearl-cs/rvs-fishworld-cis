package com.rvsfishworld.ui.core;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;

/**
 * Compatibility facade over the restored FoxPro theme implementation.
 */
public final class CisTheme {
    public static final Color DESKTOP = com.rvsfishworld.ui.FoxProTheme.DESKTOP;
    public static final Color PANEL = com.rvsfishworld.ui.FoxProTheme.PANEL;
    public static final Color PANEL_DARK = com.rvsfishworld.ui.FoxProTheme.PANEL_DARK;
    public static final Font FONT = com.rvsfishworld.ui.FoxProTheme.FONT;
    public static final Font FONT_BOLD = com.rvsfishworld.ui.FoxProTheme.FONT_BOLD;

    private CisTheme() {
    }

    public static void applyGlobalFont() {
        com.rvsfishworld.ui.FoxProTheme.applyGlobalFont();
    }

    public static JButton createFormButton(String text, int width, int height) {
        JButton button = com.rvsfishworld.ui.FoxProTheme.createButton(text);
        button.setPreferredSize(new Dimension(width, height));
        return button;
    }

    public static JTextField createTextField(int columns) {
        return com.rvsfishworld.ui.FoxProTheme.createTextField(columns);
    }

    public static void styleMenuBar(JMenuBar menuBar) {
        menuBar.setFont(FONT);
    }

    public static void styleMenu(JMenu menu) {
        menu.setFont(FONT);
    }

    public static void styleGridScrollPane(JScrollPane scrollPane) {
        JViewport viewport = scrollPane.getViewport();
        viewport.setBackground(Color.WHITE);
        viewport.setOpaque(true);
    }

    public static void styleReadOnlyField(JTextField field) {
        field.setEditable(false);
        field.setBackground(Color.WHITE);
    }

    public static void styleTextArea(JTextArea area, boolean readOnly) {
        area.setFont(FONT);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(!readOnly);
        area.setBackground(Color.WHITE);
    }

    public static void installEnterAsNextField(Container root) {
        for (Component component : root.getComponents()) {
            if (component instanceof Container child) {
                installEnterAsNextField(child);
            }
            if (component instanceof JTextField || component instanceof JTextArea) {
                component.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            e.consume();
                            KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent(component);
                        }
                    }
                });
            }
        }
    }
}
