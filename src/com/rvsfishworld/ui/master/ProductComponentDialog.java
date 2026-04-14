package com.rvsfishworld.ui.master;

import com.rvsfishworld.ui.FoxProTheme;
import com.rvsfishworld.ui.chrome.FoxProChildDialog;
import com.rvsfishworld.ui.core.CisDialogs;
import com.rvsfishworld.ui.core.CisScale;
import com.rvsfishworld.ui.core.CisTheme;
import java.awt.Window;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
public class ProductComponentDialog extends FoxProChildDialog {
    public ProductComponentDialog(Window owner, String productCode, String description) {
        super(owner, "Product Component", CisScale.scale(470), CisScale.scale(360));
        setResizable(false);
        setContentPane(buildContent(productCode, description));
    }

    private JPanel buildContent(String productCode, String description) {
        JPanel root = new JPanel(null);
        root.setBackground(CisTheme.PANEL);

        addReadOnly(root, "Product Code", value(productCode), 18, 18, 64, 116);
        addReadOnly(root, "Description", value(description), 18, 44, 64, 278);

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Prod. Code", "Description", "Req. Qty.", "Unit Cost(Ave. Cost)", "Total"}, 14) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column < 4;
            }
        };
        JTable table = new JTable(model);
        FoxProTheme.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(CisScale.scale(72));
        table.getColumnModel().getColumn(1).setPreferredWidth(CisScale.scale(160));
        table.getColumnModel().getColumn(2).setPreferredWidth(CisScale.scale(48));
        table.getColumnModel().getColumn(3).setPreferredWidth(CisScale.scale(86));
        table.getColumnModel().getColumn(4).setPreferredWidth(CisScale.scale(72));

        JScrollPane scroll = new JScrollPane(table);
        CisTheme.styleGridScrollPane(scroll);
        scroll.setBounds(CisScale.scale(18), CisScale.scale(74), CisScale.scale(430), CisScale.scale(194));
        root.add(scroll);

        JButton delete = CisTheme.createFormButton("Delete", CisScale.scale(60), CisScale.scale(24));
        delete.setBounds(CisScale.scale(106), CisScale.scale(286), CisScale.scale(58), CisScale.scale(24));
        delete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                model.removeRow(row);
            }
        });
        root.add(delete);

        JButton save = CisTheme.createFormButton("Save", CisScale.scale(60), CisScale.scale(24));
        save.setBounds(CisScale.scale(176), CisScale.scale(286), CisScale.scale(58), CisScale.scale(24));
        save.addActionListener(e -> CisDialogs.showInfo(this, "Component shell is restored. Storage wiring is on the next pass."));
        root.add(save);

        JButton exit = CisTheme.createFormButton("Exit", CisScale.scale(60), CisScale.scale(24));
        exit.setBounds(CisScale.scale(246), CisScale.scale(286), CisScale.scale(58), CisScale.scale(24));
        exit.addActionListener(e -> dispose());
        root.add(exit);

        JLabel totalLabel = new JLabel("Grand Total :");
        totalLabel.setBounds(CisScale.scale(332), CisScale.scale(290), CisScale.scale(58), CisScale.scale(18));
        root.add(totalLabel);

        JTextField totalField = CisTheme.createTextField(8);
        CisTheme.styleReadOnlyField(totalField);
        totalField.setHorizontalAlignment(JTextField.RIGHT);
        totalField.setText("0.00");
        totalField.setBounds(CisScale.scale(392), CisScale.scale(286), CisScale.scale(56), CisScale.scale(22));
        root.add(totalField);

        return root;
    }

    private void addReadOnly(JPanel root, String label, String value, int x, int y, int labelWidth, int fieldWidth) {
        JLabel jLabel = new JLabel(label);
        jLabel.setBounds(CisScale.scale(x), CisScale.scale(y), CisScale.scale(labelWidth), CisScale.scale(18));
        root.add(jLabel);

        JTextField field = CisTheme.createTextField(Math.max(8, fieldWidth / 8));
        field.setText(value);
        CisTheme.styleReadOnlyField(field);
        field.setBounds(CisScale.scale(x + labelWidth + 6), CisScale.scale(y - 2), CisScale.scale(fieldWidth), CisScale.scale(22));
        root.add(field);
    }

    private String value(String value) {
        return value == null ? "" : value;
    }
}
