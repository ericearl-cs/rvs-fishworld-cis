package com.rvsfishworld.ui.chrome;

import com.rvsfishworld.ui.FoxProTheme;
import java.awt.Dimension;
import java.awt.Window;
import javax.swing.JDialog;

@SuppressWarnings("serial")
public class FoxProChildDialog extends JDialog {
    public FoxProChildDialog(Window owner, String title, int width, int height) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        FoxProTheme.applyGlobalFont();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(new Dimension(width, height));
        setLocationRelativeTo(owner);
    }
}
