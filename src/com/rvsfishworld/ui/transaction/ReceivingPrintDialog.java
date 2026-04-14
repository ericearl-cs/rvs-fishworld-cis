package com.rvsfishworld.ui.transaction;

import com.rvsfishworld.dao.ReceivingDAO;
import java.awt.BorderLayout;
import java.awt.Window;
import java.text.DecimalFormat;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

@SuppressWarnings("serial")
public class ReceivingPrintDialog extends JDialog {
    public ReceivingPrintDialog(Window owner, ReceivingDAO.BrowseRow row) {
        super(owner, "Receiving Print", ModalityType.APPLICATION_MODAL);
        JTextArea area = new JTextArea(buildPreview(row), 24, 80);
        area.setEditable(false);
        JButton close = new JButton("Close");
        close.addActionListener(e -> dispose());
        setLayout(new BorderLayout(8, 8));
        add(new JScrollPane(area), BorderLayout.CENTER);
        add(close, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(owner);
    }

    private String buildPreview(ReceivingDAO.BrowseRow row) {
        if (row == null) {
            return "No receiving row selected.";
        }
        DecimalFormat fmt = new DecimalFormat("#,##0.00");
        return """
                RECEIVING OF FISH PURCHASES
                R.R. No: %s
                Supplier: %s - %s
                Supplier Ref: %s
                Date: %s
                Fish Purchase: %s
                C.V. Amount: %s
                Amount Paid: %s
                Balance: %s
                """.formatted(
                row.getRrNo(),
                row.getSupplierCode(),
                row.getSupplierName(),
                row.getSuppRef(),
                row.getDateText(),
                fmt.format(row.getFishPurchase()),
                fmt.format(row.getCvAmount()),
                fmt.format(row.getAmountPaid()),
                fmt.format(row.getBalance()));
    }
}
