package com.rvsfishworld.ui.transaction;

import com.rvsfishworld.model.ProformaLine;
import com.rvsfishworld.model.ProformaRecord;
import com.rvsfishworld.ui.FoxProTheme;
import com.rvsfishworld.ui.chrome.FoxProChildDialog;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

@SuppressWarnings("serial")
public class ProformaPrintDialog extends FoxProChildDialog {
    public ProformaPrintDialog(Window owner, ProformaRecord record) {
        super(owner, "Proforma Print Preview", 760, 540);
        setContentPane(buildContent(record));
    }

    private JPanel buildContent(ProformaRecord record) {
        String preview = buildPreview(record);
        JTextArea area = new JTextArea(preview, 28, 90);
        area.setEditable(false);
        area.setFont(FoxProTheme.FONT);
        area.setCaretPosition(0);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        buttons.setBackground(FoxProTheme.PANEL);
        JButton toFile = FoxProTheme.createButton("To File");
        JButton exit = FoxProTheme.createButton("Exit");
        toFile.addActionListener(e -> writePreview(preview));
        exit.addActionListener(e -> dispose());
        buttons.add(toFile);
        buttons.add(exit);

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(FoxProTheme.PANEL);
        root.add(new JScrollPane(area), BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        return root;
    }

    private void writePreview(String preview) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("proforma-preview.txt"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            Files.writeString(Path.of(chooser.getSelectedFile().getAbsolutePath()), preview);
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, e.getMessage(), "Write Failed", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private String buildPreview(ProformaRecord record) {
        if (record == null) {
            return "No Proforma selected.";
        }
        DecimalFormat amountFormat = new DecimalFormat("#,##0.00");
        StringBuilder sb = new StringBuilder();
        sb.append("PROFORMA").append(System.lineSeparator());
        sb.append("No: ").append(value(record.getProformaNo())).append(System.lineSeparator());
        sb.append("Date: ").append(record.getInvoiceDate() == null ? "" : record.getInvoiceDate()).append(System.lineSeparator());
        sb.append("Customer: ").append(value(record.getCustomerCode())).append(" - ").append(value(record.getCustomerName())).append(System.lineSeparator());
        sb.append("Branch: ").append(value(record.getBranchCode())).append(" ").append(value(record.getBranchName())).append(System.lineSeparator());
        sb.append("Salesman: ").append(value(record.getSalesmanCode())).append(" ").append(value(record.getSalesmanName())).append(System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("LINES").append(System.lineSeparator());
        for (ProformaLine line : record.getLines()) {
            sb.append(value(line.getProductCode())).append("  ")
                    .append(value(line.getDescription())).append("  Qty:")
                    .append(line.getQuantity()).append("  Price:")
                    .append(amountFormat.format(line.getPrice())).append("  Total:")
                    .append(amountFormat.format(line.getTotalPrice())).append(System.lineSeparator());
        }
        sb.append(System.lineSeparator());
        sb.append("Grand Total: ").append(amountFormat.format(nonNull(record.getTotalAmount()))).append(System.lineSeparator());
        sb.append("Total Payables: ").append(amountFormat.format(nonNull(record.getTotalPayables()))).append(System.lineSeparator());
        return sb.toString();
    }

    private String value(String value) {
        return value == null ? "" : value;
    }

    private BigDecimal nonNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
