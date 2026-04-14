package com.rvsfishworld.ui.transaction;

import com.rvsfishworld.dao.ReceivingDAO;
import com.rvsfishworld.ui.FoxProTheme;
import com.rvsfishworld.ui.chrome.FoxProChildDialog;
import com.rvsfishworld.ui.core.CisDialogs;
import com.rvsfishworld.ui.core.CisScale;
import com.rvsfishworld.ui.core.CisTheme;
import java.awt.Window;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class ReceivingPrintDialog extends FoxProChildDialog {
    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private final ReceivingDAO.BrowseRow row;
    private final JTextField txtFrom = CisTheme.createTextField(10);
    private final JTextField txtTo = CisTheme.createTextField(10);
    private final JRadioButton optEmpty = new JRadioButton("Empty P.O. No.", true);
    private final JRadioButton optWith = new JRadioButton("With P.O. No.");
    private final JRadioButton optAll = new JRadioButton("ALL Data");

    public ReceivingPrintDialog(Window owner, ReceivingDAO.BrowseRow row) {
        super(owner, "Print", CisScale.scale(266), CisScale.scale(330));
        this.row = row;
        setResizable(false);
        txtFrom.setText(LocalDate.now().format(DISPLAY_DATE));
        txtTo.setText(LocalDate.now().format(DISPLAY_DATE));
        setContentPane(buildContent());
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(null);
        root.setBackground(CisTheme.PANEL);

        JPanel groupForm = groupBox("Form Generated", 16, 18, 142, 46);
        groupForm.add(actionButton("GENERATE REPORT", 18, 18, 104, 22, this::showGeneratedReport));

        JPanel groupSlip = groupBox("Slip - In", 16, 74, 142, 46);
        groupSlip.add(actionButton("GENERATE REPORT", 18, 18, 104, 22, this::showSlipInReport));

        JPanel groupMac = groupBox("MAC Standard", 16, 130, 142, 46);
        groupMac.add(actionButton("GENERATE REPORT", 18, 18, 104, 22, this::showMacStandardReport));

        JButton exit = actionButton("Exit", 176, 28, 62, 24, this::dispose);
        JButton excelTop = actionButton("To Excel", 176, 84, 62, 24, this::writeSummaryToFile);
        root.add(exit);
        root.add(excelTop);

        JPanel summary = groupBox("SUMMARY", 16, 188, 222, 118);
        JLabel from = new JLabel("From");
        from.setBounds(CisScale.scale(12), CisScale.scale(18), CisScale.scale(24), CisScale.scale(16));
        summary.add(from);
        txtFrom.setBounds(CisScale.scale(42), CisScale.scale(16), CisScale.scale(62), CisScale.scale(20));
        summary.add(txtFrom);

        JLabel to = new JLabel("To");
        to.setBounds(CisScale.scale(112), CisScale.scale(18), CisScale.scale(14), CisScale.scale(16));
        summary.add(to);
        txtTo.setBounds(CisScale.scale(130), CisScale.scale(16), CisScale.scale(62), CisScale.scale(20));
        summary.add(txtTo);

        JPanel radioBox = new JPanel(null);
        radioBox.setBackground(CisTheme.PANEL);
        radioBox.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        radioBox.setBounds(CisScale.scale(38), CisScale.scale(40), CisScale.scale(108), CisScale.scale(50));
        summary.add(radioBox);

        ButtonGroup group = new ButtonGroup();
        addRadio(radioBox, optEmpty, 8, 4, group);
        addRadio(radioBox, optWith, 8, 20, group);
        addRadio(radioBox, optAll, 8, 36, group);

        summary.add(actionButton("View", 10, 92, 44, 20, this::showSummaryPreview));
        summary.add(actionButton("Print", 84, 92, 44, 20, this::showSummaryPreview));
        summary.add(actionButton("To Excel", 154, 92, 56, 20, this::writeSummaryToFile));

        root.add(groupForm);
        root.add(groupSlip);
        root.add(groupMac);
        root.add(summary);
        return root;
    }

    private JPanel groupBox(String title, int x, int y, int width, int height) {
        JPanel panel = new JPanel(null);
        panel.setBackground(CisTheme.PANEL);
        panel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), title));
        panel.setBounds(CisScale.scale(x), CisScale.scale(y), CisScale.scale(width), CisScale.scale(height));
        return panel;
    }

    private JButton actionButton(String text, int x, int y, int width, int height, Runnable action) {
        JButton button = CisTheme.createFormButton(text, CisScale.scale(width), CisScale.scale(height));
        button.setBounds(CisScale.scale(x), CisScale.scale(y), CisScale.scale(width), CisScale.scale(height));
        button.addActionListener(e -> action.run());
        return button;
    }

    private void addRadio(JPanel panel, JRadioButton button, int x, int y, ButtonGroup group) {
        button.setBackground(CisTheme.PANEL);
        button.setFont(FoxProTheme.FONT);
        button.setBounds(CisScale.scale(x), CisScale.scale(y), CisScale.scale(96), CisScale.scale(14));
        group.add(button);
        panel.add(button);
    }

    private void showGeneratedReport() {
        showPreview("FORM GENERATED");
    }

    private void showSlipInReport() {
        showPreview("SLIP-IN");
    }

    private void showMacStandardReport() {
        showPreview("MAC STANDARD");
    }

    private void showSummaryPreview() {
        showPreview("SUMMARY");
    }

    private void showPreview(String mode) {
        PreviewDialog dialog = new PreviewDialog(this, buildPreview(mode));
        dialog.setVisible(true);
    }

    private void writeSummaryToFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("receiving-report.txt"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            Files.writeString(Path.of(chooser.getSelectedFile().toURI()), buildPreview("SUMMARY"));
            CisDialogs.showInfo(this, "File written:\n" + chooser.getSelectedFile().getAbsolutePath());
        } catch (Exception e) {
            CisDialogs.showError(this, "Unable to write file: " + e.getMessage());
        }
    }

    private String buildPreview(String mode) {
        if (row == null) {
            return mode + "\n\nNo receiving row selected.";
        }
        return """
                %s

                R.R. No.      : %s
                Supplier      : %s - %s
                Supplier Ref. : %s
                Date          : %s
                Fish Purchase : %s
                C.V. Amount   : %s
                Amount Paid   : %s
                Balance       : %s
                Date Range    : %s to %s
                Filter        : %s
                """.formatted(
                mode,
                row.getRrNo(),
                row.getSupplierCode(),
                row.getSupplierName(),
                row.getSuppRef(),
                row.getDateText(),
                row.getFishPurchase(),
                row.getCvAmount(),
                row.getAmountPaid(),
                row.getBalance(),
                txtFrom.getText().trim(),
                txtTo.getText().trim(),
                currentFilter());
    }

    private String currentFilter() {
        if (optWith.isSelected()) {
            return "WITH P.O. NO.";
        }
        if (optAll.isSelected()) {
            return "ALL DATA";
        }
        return "EMPTY P.O. NO.";
    }

    private static class PreviewDialog extends FoxProChildDialog {
        PreviewDialog(Window owner, String text) {
            super(owner, "Preview", CisScale.scale(520), CisScale.scale(360));
            JTextArea area = new JTextArea(text);
            area.setEditable(false);
            area.setFont(FoxProTheme.FONT);
            JScrollPane scroll = new JScrollPane(area);
            scroll.setBorder(null);
            setContentPane(scroll);
        }
    }
}
