package com.rvsfishworld.ui.widgets;

import com.rvsfishworld.ui.FoxProTheme;
import javax.swing.JButton;

@SuppressWarnings("serial")
public class ReceivingCommandButton extends JButton {
    public ReceivingCommandButton(String text) {
        super(text);
        setFont(FoxProTheme.FONT);
        setBackground(FoxProTheme.PANEL);
        setFocusPainted(false);
    }
}
