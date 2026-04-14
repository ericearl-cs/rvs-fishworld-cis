package com.rvsfishworld.ui.transaction;

import com.rvsfishworld.model.ProformaLine;
import com.rvsfishworld.model.ProformaRecord;
import java.awt.BorderLayout;
import java.awt.Window;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

@SuppressWarnings("serial")
public class ProformaPrintDialog extends JDialog {
    public ProformaPrintDialog(Window owner, ProformaRecord record) {
        super(owner, "Proforma Print", ModalityType.APPLICATION_MODAL);
        JTextArea area = new JTextArea(buildPreview(record), 28, 90);
        area.setEditable(false);
        JButton close = new JButton("Close");
        close.addActionListener(e -> dispose());
        setLayout(new BorderLayout(8, 8));
        add(new JScrollPane(area), BorderLayout.CENTER);
        add(close, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(owner);
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
