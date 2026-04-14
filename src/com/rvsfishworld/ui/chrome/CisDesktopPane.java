package com.rvsfishworld.ui.chrome;

import com.rvsfishworld.ui.FoxProTheme;
import java.awt.Component;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

@SuppressWarnings("serial")
public class CisDesktopPane extends JDesktopPane {
    public CisDesktopPane() {
        setBackground(FoxProTheme.DESKTOP);
        setOpaque(true);
    }

    public void closeAllFrames() {
        for (JInternalFrame frame : getAllFrames()) {
            if (frame != null) {
                frame.dispose();
            }
        }
        for (Component component : getComponents()) {
            if (!(component instanceof JInternalFrame)) {
                remove(component);
            }
        }
        revalidate();
        repaint();
    }

    public boolean closeOtherFrames(JInternalFrame keep) {
        for (JInternalFrame frame : getAllFrames()) {
            if (frame == null || frame == keep) {
                continue;
            }
            try {
                frame.setClosed(true);
            } catch (Exception ignored) {
                frame.dispose();
            }
        }
        refreshDesktop();
        return true;
    }

    public void refreshDesktop() {
        for (Component component : getComponents()) {
            if (!(component instanceof JInternalFrame)) {
                component.repaint();
            }
        }
        revalidate();
        repaint();
    }
}
