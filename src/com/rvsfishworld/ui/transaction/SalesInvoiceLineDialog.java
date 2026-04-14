package com.rvsfishworld.ui.transaction;

import com.rvsfishworld.dao.LookupDAO;
import com.rvsfishworld.model.LookupItem;
import com.rvsfishworld.model.SalesInvoiceLine;
import com.rvsfishworld.ui.FoxProTheme;
import com.rvsfishworld.ui.chrome.FoxProChildDialog;
import com.rvsfishworld.ui.core.CisDialogs;
import com.rvsfishworld.ui.core.CisScale;
import com.rvsfishworld.ui.generic.LookupDialog;
import java.awt.Window;
import java.math.BigDecimal;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class SalesInvoiceLineDialog extends FoxProChildDialog {
    private final LookupDAO lookupDAO = new LookupDAO();
    private final JTextField txtTransShipper = FoxProTheme.createTextField(8);
    private final JTextField txtTransShipperName = FoxProTheme.createTextField(22);
    private final JTextField txtBoxNo = FoxProTheme.createTextField(6);
    private final JTextField txtSpecial = FoxProTheme.createTextField(8);
    private final JCheckBox chkInvertebrates = new JCheckBox("Invertebrates ?");
    private final JTextField txtProductCode = FoxProTheme.createTextField(10);
    private final JTextField txtDescription = FoxProTheme.createTextField(24);
    private final JTextField txtQuantity = FoxProTheme.createTextField(6);
    private final JTextField txtSellingPrice = FoxProTheme.createTextField(8);
    private final JTextField txtTotalPrice = FoxProTheme.createTextField(8);
    private boolean saved;
    private SalesInvoiceLine line;

    public SalesInvoiceLineDialog(Window owner, SalesInvoiceLine existing) {
        super(owner, existing == null ? "Adding Of Sales" : "Edit Product Sales", 360, 240);
        this.line = existing == null ? new SalesInvoiceLine() : copy(existing);
        setContentPane(buildContent());
        loadLine();
    }

    public boolean isSaved() {
        return saved;
    }

    public SalesInvoiceLine getLine() {
        return line;
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(null);
        root.setBackground(FoxProTheme.PANEL);
        root.setPreferredSize(new java.awt.Dimension(CisScale.scale(360), CisScale.scale(240)));

        addLabel(root, "Tran-Shipper", 18, 24, 58);
        placeField(root, txtTransShipper, 88, 20, 42, 22);
        JButton transLookup = FoxProTheme.createLookupButton();
        transLookup.setBounds(s(132), s(20), s(24), s(22));
        transLookup.addActionListener(e -> copyProductFromLookup());
        root.add(transLookup);
        placeReadOnly(root, txtTransShipperName, 160, 20, 170, 22);

        addLabel(root, "Box No.", 18, 52, 58);
        placeField(root, txtBoxNo, 88, 48, 42, 22);
        addLabel(root, "SPECIAL-1 :", 142, 52, 64);
        placeField(root, txtSpecial, 212, 48, 36, 22);
        chkInvertebrates.setBackground(FoxProTheme.PANEL);
        chkInvertebrates.setBounds(s(252), s(50), s(96), s(20));
        root.add(chkInvertebrates);

        addLabel(root, "Product", 18, 80, 58);
        placeField(root, txtProductCode, 88, 76, 60, 22);
        JButton productLookup = FoxProTheme.createLookupButton();
        productLookup.setBounds(s(150), s(76), s(24), s(22));
        productLookup.addActionListener(e -> openProductLookup());
        root.add(productLookup);
        placeReadOnly(root, txtDescription, 178, 76, 152, 22);

        addLabel(root, "Qty. Out", 18, 108, 58);
        placeNumericField(root, txtQuantity, 88, 104, 54, 22);
        addLabel(root, "Selling Price", 18, 136, 58);
        placeNumericField(root, txtSellingPrice, 88, 132, 54, 22);
        JButton select = FoxProTheme.createButton("Select");
        select.setBounds(s(148), s(132), s(64), s(22));
        select.addActionListener(e -> openProductLookup());
        root.add(select);

        addLabel(root, "Total Price", 18, 164, 58);
        txtTotalPrice.setHorizontalAlignment(SwingConstants.RIGHT);
        txtTotalPrice.setEditable(false);
        placeField(root, txtTotalPrice, 88, 160, 54, 22);

        JButton accept = FoxProTheme.createButton("Accept");
        accept.setBounds(s(126), s(192), s(70), s(24));
        accept.addActionListener(e -> onSave());
        root.add(accept);

        JButton exit = FoxProTheme.createButton("Exit");
        exit.setBounds(s(214), s(192), s(70), s(24));
        exit.addActionListener(e -> dispose());
        root.add(exit);

        return root;
    }

    private void addLabel(JPanel root, String text, int x, int y, int width) {
        JLabel label = new JLabel(text);
        label.setFont(FoxProTheme.FONT);
        label.setBounds(s(x), s(y), s(width), s(18));
        root.add(label);
    }

    private void placeField(JPanel root, JTextField field, int x, int y, int width, int height) {
        field.setBounds(s(x), s(y), s(width), s(height));
        root.add(field);
    }

    private void placeReadOnly(JPanel root, JTextField field, int x, int y, int width, int height) {
        field.setEditable(false);
        placeField(root, field, x, y, width, height);
    }

    private void placeNumericField(JPanel root, JTextField field, int x, int y, int width, int height) {
        field.setHorizontalAlignment(SwingConstants.RIGHT);
        placeField(root, field, x, y, width, height);
    }

    private void loadLine() {
        txtTransShipper.setText(defaultString(line.getTransShipperCode()));
        txtTransShipperName.setText(defaultString(line.getSupplierCode()));
        txtBoxNo.setText(defaultString(line.getBoxNo()));
        txtSpecial.setText(defaultString(line.getSpecialValue()));
        chkInvertebrates.setSelected(false);
        txtProductCode.setText(defaultString(line.getProductCode()));
        txtDescription.setText(defaultString(line.getDescription()));
        txtQuantity.setText(Integer.toString(Math.max(0, line.getQuantity())));
        txtSellingPrice.setText(line.getSellingPrice() == null ? "0.00" : line.getSellingPrice().toPlainString());
        txtTotalPrice.setText(line.getTotalPrice() == null ? "0.00" : line.getTotalPrice().toPlainString());
    }

    private void openProductLookup() {
        LookupDialog dialog = new LookupDialog(this, "Product Lookup", lookupDAO::findProducts);
        dialog.setVisible(true);
        LookupItem item = dialog.getSelectedItem();
        if (item == null) {
            return;
        }
        txtProductCode.setText(item.getCode());
        txtDescription.setText(item.getName());
    }

    private void copyProductFromLookup() {
        if (!txtProductCode.getText().isBlank()) {
            return;
        }
        openProductLookup();
    }

    private void onSave() {
        if (txtProductCode.getText().isBlank()) {
            CisDialogs.showError(this, "Product code is required.");
            return;
        }
        try {
            line.setTransShipperCode(txtTransShipper.getText().trim());
            line.setSupplierCode(txtTransShipperName.getText().trim());
            line.setBoxNo(txtBoxNo.getText().trim());
            line.setProductCode(txtProductCode.getText().trim());
            line.setDescription(txtDescription.getText().trim());
            line.setQuantity(Integer.parseInt(txtQuantity.getText().trim()));
            line.setSellingPrice(new BigDecimal(txtSellingPrice.getText().trim()));
            line.setSpecial(!txtSpecial.getText().trim().isBlank());
            line.setSpecialValue(txtSpecial.getText().trim());
            line.recompute();
            txtTotalPrice.setText(line.getTotalPrice().toPlainString());
            saved = true;
            dispose();
        } catch (Exception e) {
            CisDialogs.showError(this, "Invalid line values: " + e.getMessage());
        }
    }

    private SalesInvoiceLine copy(SalesInvoiceLine source) {
        SalesInvoiceLine copy = new SalesInvoiceLine();
        copy.setId(source.getId());
        copy.setLineNo(source.getLineNo());
        copy.setTransShipperCode(source.getTransShipperCode());
        copy.setBoxNo(source.getBoxNo());
        copy.setProductCode(source.getProductCode());
        copy.setDescription(source.getDescription());
        copy.setSupplierCode(source.getSupplierCode());
        copy.setQuantity(source.getQuantity());
        copy.setSellingPrice(source.getSellingPrice());
        copy.setTotalPrice(source.getTotalPrice());
        copy.setSpecial(source.isSpecial());
        copy.setSpecialValue(source.getSpecialValue());
        return copy;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private int s(int value) {
        return CisScale.scale(value);
    }
}
