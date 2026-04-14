package com.rvsfishworld.ui.master;

import com.rvsfishworld.dao.LookupDAO;
import com.rvsfishworld.dao.MasterFileDAO;
import com.rvsfishworld.model.LookupItem;
import com.rvsfishworld.ui.chrome.FoxProChildDialog;
import com.rvsfishworld.ui.core.CisDialogs;
import com.rvsfishworld.ui.core.CisScale;
import com.rvsfishworld.ui.core.CisTheme;
import com.rvsfishworld.ui.core.ReceivingUiMetrics;
import com.rvsfishworld.ui.generic.LookupDialog;
import java.awt.Dimension;
import java.awt.Window;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class ProductRecordDialog extends FoxProChildDialog {
    private final MasterFileDAO dao = new MasterFileDAO();
    private final LookupDAO lookupDAO = new LookupDAO();
    private final Map<String, Object> baseValues = new LinkedHashMap<>();
    private final Map<String, JComponent> inputs = new LinkedHashMap<>();
    private boolean saved;

    public ProductRecordDialog(Window owner, String title, Map<String, Object> values, boolean readOnly) {
        super(owner, title, CisScale.scale(650), CisScale.scale(445));
        if (values != null) {
            baseValues.putAll(values);
        }
        setResizable(false);
        buildUi(readOnly);
    }

    public boolean isSaved() {
        return saved;
    }

    public Map<String, Object> values() {
        Map<String, Object> merged = new LinkedHashMap<>(baseValues);
        for (Map.Entry<String, JComponent> entry : inputs.entrySet()) {
            merged.put(entry.getKey(), readComponentValue(entry.getValue()));
        }
        return merged;
    }

    private void buildUi(boolean readOnly) {
        JPanel root = new JPanel(null);
        root.setBackground(CisTheme.PANEL);
        root.setPreferredSize(new Dimension(CisScale.scale(640), CisScale.scale(414)));

        drawSectionTitles(root);
        addLeftFields(root, readOnly);
        addRightFields(root, readOnly);
        addPriceFields(root, readOnly);
        addButtons(root, readOnly);

        ReceivingUiMetrics.applyFormFont(root);
        CisTheme.installEnterAsNextField(root);
        setContentPane(root);
        pack();
        setLocationRelativeTo(getOwner());
    }

    private void drawSectionTitles(JPanel root) {
        JLabel pricing = new JLabel("Pricing", JLabel.CENTER);
        pricing.setBounds(CisScale.scale(120), CisScale.scale(10), CisScale.scale(140), CisScale.scale(18));
        root.add(pricing);

        JPanel pricingRule = new JPanel(null);
        pricingRule.setBackground(CisTheme.PANEL_DARK);
        pricingRule.setBounds(CisScale.scale(10), CisScale.scale(30), CisScale.scale(248), CisScale.scale(1));
        root.add(pricingRule);

        JLabel inventory = new JLabel("Branches/Inventory", JLabel.CENTER);
        inventory.setBounds(CisScale.scale(360), CisScale.scale(10), CisScale.scale(160), CisScale.scale(18));
        root.add(inventory);

        JPanel inventoryRule = new JPanel(null);
        inventoryRule.setBackground(CisTheme.PANEL_DARK);
        inventoryRule.setBounds(CisScale.scale(292), CisScale.scale(30), CisScale.scale(330), CisScale.scale(1));
        root.add(inventoryRule);
    }

    private void addLeftFields(JPanel root, boolean readOnly) {
        addField(root, "Product No.", "product_code", 18, 36, 92, 130, readOnly);
        addField(root, "Description", "description", 18, 62, 92, 350, readOnly);
        addField(root, "Scientific Name", "scientific_name", 18, 88, 92, 185, readOnly);

        addCodeLookupRow(root, "Category", "category_code", "category_name", 18, 114, 92, 54, 24, 240, readOnly,
                () -> openLookup("Category Lookup", lookupDAO::findCategories, "category_code", "category_name"),
                this::refreshCategoryFromCode);
        addCodeLookupRow(root, "Brand", "brand_code", "brand_name", 18, 140, 92, 54, 24, 240, readOnly,
                () -> openLookup("Brand Lookup", lookupDAO::findBrands, "brand_code", "brand_name"),
                this::refreshBrandFromCode);

        addDecimalField(root, "Reordering Pt.", "reorder_point", 18, 168, 92, 70, readOnly);
        addDecimalField(root, "Maximum Pt.", "maximum_point", 18, 194, 92, 70, readOnly);
    }

    private void addRightFields(JPanel root, boolean readOnly) {
        addField(root, "U/M :", "unit_of_measure", 484, 36, 38, 85, readOnly);

        JCheckBox invertebrate = new JCheckBox("Invertibrate ?");
        invertebrate.setBackground(CisTheme.PANEL);
        invertebrate.setSelected(truthy(baseValues.get("is_invertebrate")));
        invertebrate.setEnabled(!readOnly);
        invertebrate.setBounds(CisScale.scale(474), CisScale.scale(87), CisScale.scale(116), CisScale.scale(18));
        root.add(invertebrate);
        inputs.put("is_invertebrate", invertebrate);

        JLabel extendedLabel = new JLabel("Extended Description");
        extendedLabel.setBounds(CisScale.scale(442), CisScale.scale(112), CisScale.scale(120), CisScale.scale(18));
        root.add(extendedLabel);

        JTextArea area = new JTextArea(text("extended_description"));
        CisTheme.styleTextArea(area, readOnly);
        JScrollPane pane = new JScrollPane(area);
        CisTheme.styleGridScrollPane(pane);
        pane.setBounds(CisScale.scale(402), CisScale.scale(132), CisScale.scale(170), CisScale.scale(54));
        root.add(pane);
        inputs.put("extended_description", area);
    }

    private void addPriceFields(JPanel root, boolean readOnly) {
        int baseY = 226;
        addDecimalField(root, "Price A (HK)", "price_a", 18, baseY, 92, 70, readOnly);
        addDecimalField(root, "Price B (US/SQ/EU)", "price_b", 18, baseY + 26, 92, 70, readOnly);
        addDecimalField(root, "Price C", "price_c", 18, baseY + 52, 92, 70, readOnly);
        addDecimalField(root, "Price D", "price_d", 18, baseY + 78, 92, 70, readOnly);
        addDecimalField(root, "Price E", "price_e", 18, baseY + 104, 92, 70, readOnly);
        addDecimalField(root, "Price F", "price_f", 18, baseY + 130, 92, 70, readOnly);
        addDecimalField(root, "Price G", "price_g", 18, baseY + 156, 92, 70, readOnly);

        addDecimalField(root, "Special", "special_price", 206, baseY, 70, 74, readOnly);
        addDecimalField(root, "LOCAL SALES", "local_sales_price", 206, baseY + 26, 70, 74, readOnly);
        addDecimalField(root, "DELIVERIES", "deliveries_price", 206, baseY + 52, 70, 74, readOnly);
    }

    private void addButtons(JPanel root, boolean readOnly) {
        if (!readOnly) {
            JButton save = CisTheme.createFormButton("\\<Save", CisScale.scale(84), CisScale.scale(26));
            save.setBounds(CisScale.scale(438), CisScale.scale(378), CisScale.scale(82), CisScale.scale(26));
            save.addActionListener(e -> onSave());
            root.add(save);
            getRootPane().setDefaultButton(save);
        }

        JButton exit = CisTheme.createFormButton("E\\<xit", CisScale.scale(84), CisScale.scale(26));
        exit.setBounds(CisScale.scale(532), CisScale.scale(378), CisScale.scale(82), CisScale.scale(26));
        exit.addActionListener(e -> dispose());
        root.add(exit);
    }

    private void addField(JPanel root, String label, String key, int x, int y, int labelWidth, int inputWidth, boolean readOnly) {
        JLabel jLabel = new JLabel(label);
        jLabel.setBounds(CisScale.scale(x), CisScale.scale(y), CisScale.scale(labelWidth), CisScale.scale(18));
        root.add(jLabel);

        JTextField field = CisTheme.createTextField(Math.max(8, inputWidth / 8));
        field.setText(text(key));
        if (readOnly) {
            CisTheme.styleReadOnlyField(field);
        }
        field.setBounds(CisScale.scale(x + labelWidth + 6), CisScale.scale(y - 2), CisScale.scale(inputWidth), CisScale.scale(22));
        root.add(field);
        inputs.put(key, field);
    }

    private void addDecimalField(JPanel root, String label, String key, int x, int y, int labelWidth, int inputWidth, boolean readOnly) {
        addField(root, label, key, x, y, labelWidth, inputWidth, readOnly);
        if (inputs.get(key) instanceof JTextField field) {
            field.setHorizontalAlignment(JTextField.RIGHT);
        }
    }

    private void addCodeLookupRow(
            JPanel root,
            String label,
            String codeKey,
            String nameKey,
            int x,
            int y,
            int labelWidth,
            int codeWidth,
            int lookupWidth,
            int nameWidth,
            boolean readOnly,
            Runnable lookupAction,
            Runnable refreshAction) {
        JLabel jLabel = new JLabel(label);
        jLabel.setBounds(CisScale.scale(x), CisScale.scale(y), CisScale.scale(labelWidth), CisScale.scale(18));
        root.add(jLabel);

        JTextField code = CisTheme.createTextField(Math.max(6, codeWidth / 8));
        code.setText(text(codeKey));
        code.setBounds(CisScale.scale(x + labelWidth + 6), CisScale.scale(y - 2), CisScale.scale(codeWidth), CisScale.scale(22));
        if (readOnly) {
            CisTheme.styleReadOnlyField(code);
        } else {
            code.addActionListener(e -> refreshAction.run());
        }
        root.add(code);
        inputs.put(codeKey, code);

        JButton lookup = CisTheme.createFormButton("...", CisScale.scale(lookupWidth), CisScale.scale(22));
        lookup.setBounds(CisScale.scale(x + labelWidth + 6 + codeWidth + 4), CisScale.scale(y - 2), CisScale.scale(lookupWidth), CisScale.scale(22));
        lookup.setEnabled(!readOnly);
        lookup.addActionListener(e -> lookupAction.run());
        root.add(lookup);

        JTextField name = CisTheme.createTextField(Math.max(12, nameWidth / 8));
        name.setText(text(nameKey));
        CisTheme.styleReadOnlyField(name);
        name.setBounds(CisScale.scale(x + labelWidth + 6 + codeWidth + lookupWidth + 10), CisScale.scale(y - 2), CisScale.scale(nameWidth), CisScale.scale(22));
        root.add(name);
        inputs.put(nameKey, name);
    }

    private void openLookup(
            String title,
            java.util.function.Function<String, java.util.List<LookupItem>> loader,
            String codeKey,
            String nameKey) {
        LookupDialog dialog = new LookupDialog(this, title, loader);
        dialog.setVisible(true);
        LookupItem item = dialog.getSelectedItem();
        if (item == null) {
            return;
        }
        setText(codeKey, item.getCode());
        setText(nameKey, item.getName());
    }

    private void refreshCategoryFromCode() {
        String code = textField("category_code");
        LookupItem item = code.isBlank() ? null : lookupDAO.findCategoryByCode(code);
        setText("category_name", item == null ? "" : item.getName());
    }

    private void refreshBrandFromCode() {
        String code = textField("brand_code");
        LookupItem item = code.isBlank() ? null : lookupDAO.findBrandByCode(code);
        setText("brand_name", item == null ? "" : item.getName());
    }

    private void onSave() {
        try {
            String productCode = textField("product_code");
            String description = textField("description");
            String categoryCode = textField("category_code");
            if (productCode.isBlank()) {
                CisDialogs.showInfo(this, "Product No. is required.");
                return;
            }
            if (description.isBlank()) {
                CisDialogs.showInfo(this, "Description is required.");
                return;
            }
            if (categoryCode.isBlank()) {
                CisDialogs.showInfo(this, "Category is required.");
                return;
            }
            dao.saveProduct(values());
            saved = true;
            dispose();
        } catch (IllegalArgumentException e) {
            CisDialogs.showInfo(this, e.getMessage());
        } catch (RuntimeException e) {
            CisDialogs.showError(this, e.getMessage());
        }
    }

    private Object readComponentValue(JComponent component) {
        if (component instanceof JTextArea area) {
            return area.getText().trim();
        }
        if (component instanceof JTextField field) {
            return field.getText().trim();
        }
        if (component instanceof JCheckBox box) {
            return box.isSelected();
        }
        return "";
    }

    private void setText(String key, String value) {
        JComponent component = inputs.get(key);
        if (component instanceof JTextField field) {
            field.setText(value == null ? "" : value);
        }
    }

    private String textField(String key) {
        JComponent component = inputs.get(key);
        return component instanceof JTextField field ? field.getText().trim() : "";
    }

    private String text(String key) {
        Object value = baseValues.get(key);
        if (value instanceof BigDecimal decimal) {
            return decimal.stripTrailingZeros().scale() <= 0 ? decimal.toPlainString() + ".00" : decimal.toPlainString();
        }
        return value == null ? "" : value.toString().trim();
    }

    private boolean truthy(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        String text = value == null ? "" : value.toString().trim().toUpperCase();
        return "TRUE".equals(text) || "T".equals(text) || "1".equals(text) || "Y".equals(text);
    }
}
