package com.rvsfishworld.ui.core;

import com.rvsfishworld.ui.FoxProTheme;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JPanel;

public final class FormLayoutSupport {
    private FormLayoutSupport() {
    }

    public static GridBagConstraints gbc(int x, int y) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    public static JPanel placeholderPanel(String title, String message) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(FoxProTheme.PANEL);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        header.setBackground(FoxProTheme.PANEL);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FoxProTheme.FONT_BOLD);
        header.add(titleLabel);

        JLabel body = new JLabel("<html>" + message + "</html>");
        body.setFont(FoxProTheme.FONT);

        panel.add(header, BorderLayout.NORTH);
        panel.add(body, BorderLayout.CENTER);
        return panel;
    }
}
