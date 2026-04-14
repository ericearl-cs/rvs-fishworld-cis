package com.rvsfishworld.ui.widgets;

import com.rvsfishworld.ui.FoxProTheme;
import javax.swing.JToggleButton;

@SuppressWarnings("serial")
public class ReceivingTabButton extends JToggleButton {
    public ReceivingTabButton(String text) {
        super(text);
        setFont(FoxProTheme.FONT_BOLD);
        setFocusPainted(false);
    }
}
