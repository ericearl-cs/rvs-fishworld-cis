package com.rvsfishworld.ui.transaction;

import com.rvsfishworld.ui.FoxProTheme;
import com.rvsfishworld.ui.chrome.FoxProChildDialog;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

@SuppressWarnings("serial")
public class SalesPricePickerDialog extends FoxProChildDialog {
    private String selectedPricingCode = "B";

    public SalesPricePickerDialog(Window owner) {
        this(owner, "B");
    }

    public SalesPricePickerDialog(Window owner, String currentCode) {
        super(owner, "Select Pricing", 420, 260);
        if (currentCode != null && !currentCode.isBlank()) {
            selectedPricingCode = currentCode.trim().toUpperCase();
        }
        setContentPane(buildContent());
    }

    public String getSelectedPricingCode() {
        return selectedPricingCode;
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(FoxProTheme.PANEL);
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel choices = new JPanel(new GridLayout(3, 3, 8, 8));
        choices.setBackground(FoxProTheme.PANEL);
        ButtonGroup group = new ButtonGroup();
        for (String code : new String[]{"A", "B", "C", "D", "E", "F", "G", "S", "L"}) {
            JRadioButton option = new JRadioButton(code);
            option.setBackground(FoxProTheme.PANEL);
            option.setFont(FoxProTheme.FONT_BOLD);
            option.setSelected(code.equalsIgnoreCase(selectedPricingCode));
            option.addActionListener(e -> selectedPricingCode = code);
            group.add(option);
            choices.add(option);
        }

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        buttons.setBackground(FoxProTheme.PANEL);
        JButton ok = FoxProTheme.createButton("OK");
        JButton cancel = FoxProTheme.createButton("Cancel");
        ok.addActionListener(e -> dispose());
        cancel.addActionListener(e -> {
            selectedPricingCode = null;
            dispose();
        });
        buttons.add(ok);
        buttons.add(cancel);

        root.add(choices, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        return root;
    }
}
