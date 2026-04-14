package com.rvsfishworld.ui.generic;

import com.rvsfishworld.db.Database;
import com.rvsfishworld.ui.core.CisDialogs;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class ApprovedMasterBrowseInternalFrame extends DataBrowseInternalFrame {
    private static final DateTimeFormatter UI_DATE = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private final BrowseConfig config;

    private ApprovedMasterBrowseInternalFrame(BrowseConfig config) {
        super(
                config.title,
                config.title,
                config.orderLabels,
                config.orderSqls,
                config.commandButtons,
                config.columns,
                config.footerText);
        this.config = config;
        if (config.hideFirstColumn) {
            var column = getTable().getColumnModel().getColumn(0);
            column.setMinWidth(0);
            column.setMaxWidth(0);
            column.setPreferredWidth(0);
        }
    }

    public static ApprovedMasterBrowseInternalFrame forKey(String key) {
        return new ApprovedMasterBrowseInternalFrame(configFor(key));
    }

    @Override
    protected void handleCommand(String label) {
        switch (label.toUpperCase()) {
            case "ADD" -> openEditor("Add " + config.title, Map.of(), false);
            case "VIEW" -> openSelected(true);
            case "EDIT" -> openSelected(false);
            case "DELETE" -> deleteSelected();
            default -> super.handleCommand(label);
        }
    }

    private void openSelected(boolean readOnly) {
        Map<String, Object> values = selectedValues();
        if (values.isEmpty()) {
            CisDialogs.showInfo(this, "Select a row first.");
            return;
        }
        openEditor((readOnly ? "View " : "Edit ") + config.title, values, readOnly);
    }

    private void openEditor(String title, Map<String, Object> values, boolean readOnly) {
        CompactCrudDialog dialog = new CompactCrudDialog(
                SwingUtilities.getWindowAncestor(this),
                title,
                config.crudConfig,
                values,
                readOnly);
        dialog.setVisible(true);
        if (!readOnly && dialog.isSaved()) {
            saveRow(dialog.values());
            loadRows();
        }
    }

    private void saveRow(Map<String, Object> values) {
        Map<String, Object> merged = new LinkedHashMap<>(config.fixedValues);
        merged.putAll(values);
        boolean insert = blank(values.get(config.idKey));
        try (Connection conn = Database.getConnection()) {
            if (insert) {
                String columns = config.fieldSpecs().stream()
                        .map(CrudFieldSpec::getKey)
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("");
                String placeholders = config.fieldSpecs().stream()
                        .map(spec -> "?")
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("");
                String sql = "INSERT INTO " + config.tableName + " (" + columns + ") VALUES (" + placeholders + ")";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    bindFields(ps, merged);
                    ps.executeUpdate();
                }
            } else {
                String setClause = config.fieldSpecs().stream()
                        .map(spec -> spec.getKey() + " = ?")
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("");
                String sql = "UPDATE " + config.tableName + " SET " + setClause + " WHERE " + config.idKey + " = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    int index = bindFields(ps, merged);
                    bindValue(ps, index, CrudFieldType.NUMBER, merged.get(config.idKey));
                    ps.executeUpdate();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to save " + config.title + ": " + e.getMessage(), e);
        }
    }

    private void deleteSelected() {
        if (!config.allowDelete) {
            CisDialogs.showInfo(this, "Delete is not available for this file.");
            return;
        }
        Map<String, Object> values = selectedValues();
        if (values.isEmpty()) {
            CisDialogs.showInfo(this, "Select a row first.");
            return;
        }
        if (CisDialogs.askYesNo(this, "Delete " + config.title, "Delete selected row?") != CisDialogs.YES) {
            return;
        }
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM " + config.tableName + " WHERE " + config.idKey + " = ?")) {
            bindValue(ps, 1, CrudFieldType.NUMBER, values.get(config.idKey));
            ps.executeUpdate();
            loadRows();
        } catch (Exception e) {
            CisDialogs.showError(this, e.getMessage());
        }
    }

    private int bindFields(PreparedStatement ps, Map<String, Object> values) throws Exception {
        int index = 1;
        for (CrudFieldSpec spec : config.fieldSpecs()) {
            bindValue(ps, index++, spec.getType(), values.get(spec.getKey()));
        }
        return index;
    }

    private void bindValue(PreparedStatement ps, int index, CrudFieldType type, Object rawValue) throws Exception {
        switch (type) {
            case CHECKBOX -> ps.setBoolean(index, parseBoolean(rawValue));
            case NUMBER -> {
                String text = stringValue(rawValue);
                if (text.isBlank()) {
                    ps.setNull(index, Types.DECIMAL);
                } else {
                    ps.setBigDecimal(index, new BigDecimal(text));
                }
            }
            case DATE -> {
                String text = stringValue(rawValue);
                if (text.isBlank()) {
                    ps.setDate(index, null);
                } else {
                    LocalDate date = text.contains("/") ? LocalDate.parse(text, UI_DATE) : LocalDate.parse(text);
                    ps.setDate(index, java.sql.Date.valueOf(date));
                }
            }
            default -> ps.setString(index, blank(rawValue) ? null : stringValue(rawValue));
        }
    }

    private Map<String, Object> selectedValues() {
        int row = getTable().getSelectedRow();
        if (row < 0) {
            return Map.of();
        }
        int modelRow = getTable().convertRowIndexToModel(row);
        Map<String, Object> values = new LinkedHashMap<>();
        for (int i = 0; i < config.rowKeys.length; i++) {
            values.put(config.rowKeys[i], getModel().getValueAt(modelRow, i));
        }
        return values;
    }

    private static BrowseConfig configFor(String key) {
        return switch (key) {
            case "master_net_prices" -> new BrowseConfig(
                    "Net Prices",
                    new String[]{"Order by Product", "Order by Customer"},
                    new String[]{
                            "SELECT net_price_id, product_code, COALESCE(customer_code, ''), COALESCE(currency_code, ''), net_price, COALESCE(remarks, '') FROM net_prices ORDER BY product_code, customer_code",
                            "SELECT net_price_id, product_code, COALESCE(customer_code, ''), COALESCE(currency_code, ''), net_price, COALESCE(remarks, '') FROM net_prices ORDER BY customer_code, product_code"
                    },
                    new String[]{"Find", "View", "Add", "Edit", "Delete", "Refresh", "Exit"},
                    new String[]{"ID", "PRODUCT", "CUSTOMER", "CURRENCY", "NET PRICE", "REMARKS"},
                    "This browse loads live net-price rows from MySQL.",
                    "net_prices",
                    "net_price_id",
                    new String[]{"net_price_id", "product_code", "customer_code", "currency_code", "net_price", "remarks"},
                    new CrudTableConfig("Net Prices")
                            .addField(new CrudFieldSpec("product_code", "Product Code", CrudFieldType.TEXT, true))
                            .addField(new CrudFieldSpec("customer_code", "Customer Code", CrudFieldType.TEXT, false))
                            .addField(new CrudFieldSpec("currency_code", "Currency Code", CrudFieldType.TEXT, false))
                            .addField(new CrudFieldSpec("net_price", "Net Price", CrudFieldType.NUMBER, true))
                            .addField(new CrudFieldSpec("remarks", "Remarks", CrudFieldType.TEXT, false)),
                    true,
                    true);
            case "master_stop_week" -> new BrowseConfig(
                    "Stop Of The Week",
                    new String[]{"Order by Product", "Order by Week"},
                    new String[]{
                            "SELECT stop_week_id, product_code, COALESCE(week_label, ''), COALESCE(remarks, ''), CASE WHEN is_active THEN 'YES' ELSE 'NO' END FROM stop_of_week ORDER BY product_code",
                            "SELECT stop_week_id, product_code, COALESCE(week_label, ''), COALESCE(remarks, ''), CASE WHEN is_active THEN 'YES' ELSE 'NO' END FROM stop_of_week ORDER BY week_label, product_code"
                    },
                    new String[]{"Find", "View", "Add", "Edit", "Delete", "Refresh", "Exit"},
                    new String[]{"ID", "PRODUCT", "WEEK", "REMARKS", "ACTIVE"},
                    "This browse loads live stop-of-week rows from MySQL.",
                    "stop_of_week",
                    "stop_week_id",
                    new String[]{"stop_week_id", "product_code", "week_label", "remarks", "is_active"},
                    new CrudTableConfig("Stop Of The Week")
                            .addField(new CrudFieldSpec("product_code", "Product Code", CrudFieldType.TEXT, true))
                            .addField(new CrudFieldSpec("week_label", "Week Label", CrudFieldType.TEXT, false))
                            .addField(new CrudFieldSpec("remarks", "Remarks", CrudFieldType.TEXT, false))
                            .addField(new CrudFieldSpec("is_active", "Active", CrudFieldType.CHECKBOX, false)),
                    true,
                    true);
            case "master_contract_trans" -> new BrowseConfig(
                    "Contract Price (Tran-Shipper)",
                    new String[]{"Order by Product", "Order by Tran-Shipper"},
                    new String[]{
                            "SELECT contract_price_id, product_code, party_code, price, COALESCE(remarks, ''), CASE WHEN is_active THEN 'YES' ELSE 'NO' END FROM contract_prices WHERE contract_type = 'TRANSHIPPER' ORDER BY product_code, party_code",
                            "SELECT contract_price_id, product_code, party_code, price, COALESCE(remarks, ''), CASE WHEN is_active THEN 'YES' ELSE 'NO' END FROM contract_prices WHERE contract_type = 'TRANSHIPPER' ORDER BY party_code, product_code"
                    },
                    new String[]{"Find", "View", "Add", "Edit", "Delete", "Refresh", "Exit"},
                    new String[]{"ID", "PRODUCT", "TRAN-SHIPPER", "PRICE", "REMARKS", "ACTIVE"},
                    "This browse loads live tran-shipper contract prices from MySQL.",
                    "contract_prices",
                    "contract_price_id",
                    new String[]{"contract_price_id", "product_code", "party_code", "price", "remarks", "is_active"},
                    new CrudTableConfig("Contract Price (Tran-Shipper)")
                            .addField(new CrudFieldSpec("contract_type", "Contract Type", CrudFieldType.TEXT, true))
                            .addField(new CrudFieldSpec("product_code", "Product Code", CrudFieldType.TEXT, true))
                            .addField(new CrudFieldSpec("party_code", "Tran-Shipper Code", CrudFieldType.TEXT, true))
                            .addField(new CrudFieldSpec("price", "Price", CrudFieldType.NUMBER, true))
                            .addField(new CrudFieldSpec("remarks", "Remarks", CrudFieldType.TEXT, false))
                            .addField(new CrudFieldSpec("is_active", "Active", CrudFieldType.CHECKBOX, false)),
                    true,
                    true,
                    Map.of("contract_type", "TRANSHIPPER"));
            case "master_contract_supplier" -> new BrowseConfig(
                    "Contract Price (Supplier)",
                    new String[]{"Order by Product", "Order by Supplier"},
                    new String[]{
                            "SELECT contract_price_id, product_code, party_code, price, COALESCE(remarks, ''), CASE WHEN is_active THEN 'YES' ELSE 'NO' END FROM contract_prices WHERE contract_type = 'SUPPLIER' ORDER BY product_code, party_code",
                            "SELECT contract_price_id, product_code, party_code, price, COALESCE(remarks, ''), CASE WHEN is_active THEN 'YES' ELSE 'NO' END FROM contract_prices WHERE contract_type = 'SUPPLIER' ORDER BY party_code, product_code"
                    },
                    new String[]{"Find", "View", "Add", "Edit", "Delete", "Refresh", "Exit"},
                    new String[]{"ID", "PRODUCT", "SUPPLIER", "PRICE", "REMARKS", "ACTIVE"},
                    "This browse loads live supplier contract prices from MySQL.",
                    "contract_prices",
                    "contract_price_id",
                    new String[]{"contract_price_id", "product_code", "party_code", "price", "remarks", "is_active"},
                    new CrudTableConfig("Contract Price (Supplier)")
                            .addField(new CrudFieldSpec("contract_type", "Contract Type", CrudFieldType.TEXT, true))
                            .addField(new CrudFieldSpec("product_code", "Product Code", CrudFieldType.TEXT, true))
                            .addField(new CrudFieldSpec("party_code", "Supplier Code", CrudFieldType.TEXT, true))
                            .addField(new CrudFieldSpec("price", "Price", CrudFieldType.NUMBER, true))
                            .addField(new CrudFieldSpec("remarks", "Remarks", CrudFieldType.TEXT, false))
                            .addField(new CrudFieldSpec("is_active", "Active", CrudFieldType.CHECKBOX, false)),
                    true,
                    true,
                    Map.of("contract_type", "SUPPLIER"));
            case "master_flat_price" -> new BrowseConfig(
                    "Flat Price (Supplier)",
                    new String[]{"Order by Supplier", "Order by Product"},
                    new String[]{
                            "SELECT flat_price_id, supplier_code, product_code, flat_price, COALESCE(remarks, ''), CASE WHEN is_active THEN 'YES' ELSE 'NO' END FROM flat_prices ORDER BY supplier_code, product_code",
                            "SELECT flat_price_id, supplier_code, product_code, flat_price, COALESCE(remarks, ''), CASE WHEN is_active THEN 'YES' ELSE 'NO' END FROM flat_prices ORDER BY product_code, supplier_code"
                    },
                    new String[]{"Find", "View", "Add", "Edit", "Delete", "Refresh", "Exit"},
                    new String[]{"ID", "SUPPLIER", "PRODUCT", "FLAT PRICE", "REMARKS", "ACTIVE"},
                    "This browse loads live flat-price rows from MySQL.",
                    "flat_prices",
                    "flat_price_id",
                    new String[]{"flat_price_id", "supplier_code", "product_code", "flat_price", "remarks", "is_active"},
                    new CrudTableConfig("Flat Price (Supplier)")
                            .addField(new CrudFieldSpec("supplier_code", "Supplier Code", CrudFieldType.TEXT, true))
                            .addField(new CrudFieldSpec("product_code", "Product Code", CrudFieldType.TEXT, true))
                            .addField(new CrudFieldSpec("flat_price", "Flat Price", CrudFieldType.NUMBER, true))
                            .addField(new CrudFieldSpec("remarks", "Remarks", CrudFieldType.TEXT, false))
                            .addField(new CrudFieldSpec("is_active", "Active", CrudFieldType.CHECKBOX, false)),
                    true,
                    true);
            case "master_stop_forever" -> new BrowseConfig(
                    "Stop Forever",
                    new String[]{"Order by Supplier", "Order by Name"},
                    new String[]{
                            "SELECT supplier_id, supplier_code, supplier_name, COALESCE(supplier_address, ''), CASE WHEN exempt_stop_forever THEN 'YES' ELSE 'NO' END FROM suppliers WHERE exempt_stop_forever = TRUE ORDER BY supplier_code",
                            "SELECT supplier_id, supplier_code, supplier_name, COALESCE(supplier_address, ''), CASE WHEN exempt_stop_forever THEN 'YES' ELSE 'NO' END FROM suppliers WHERE exempt_stop_forever = TRUE ORDER BY supplier_name"
                    },
                    new String[]{"Find", "View", "Edit", "Refresh", "Exit"},
                    new String[]{"ID", "SUPPLIER", "SUPPLIER NAME", "ADDRESS", "EXEMPT"},
                    "This browse loads suppliers tagged exempt from stop-forever from MySQL.",
                    "suppliers",
                    "supplier_id",
                    new String[]{"supplier_id", "supplier_code", "supplier_name", "supplier_address", "exempt_stop_forever"},
                    new CrudTableConfig("Stop Forever")
                            .addField(new CrudFieldSpec("supplier_code", "Supplier Code", CrudFieldType.TEXT, true))
                            .addField(new CrudFieldSpec("supplier_name", "Supplier Name", CrudFieldType.TEXT, true))
                            .addField(new CrudFieldSpec("supplier_address", "Address", CrudFieldType.MULTILINE, false))
                            .addField(new CrudFieldSpec("exempt_stop_forever", "Exempt", CrudFieldType.CHECKBOX, false)),
                    true,
                    false);
            case "master_group" -> new BrowseConfig(
                    "Group File",
                    new String[]{"Order by Sort", "Order by Category"},
                    new String[]{
                            "SELECT category_id, category_code, category_name, COALESCE(sort_code, '') FROM categories ORDER BY sort_code, category_code",
                            "SELECT category_id, category_code, category_name, COALESCE(sort_code, '') FROM categories ORDER BY category_name"
                    },
                    new String[]{"Find", "View", "Add", "Edit", "Delete", "Refresh", "Exit"},
                    new String[]{"ID", "GROUP CODE", "GROUP NAME", "SORT"},
                    "This browse uses the current mirrored group/category data from MySQL.",
                    "categories",
                    "category_id",
                    new String[]{"category_id", "category_code", "category_name", "sort_code"},
                    new CrudTableConfig("Group File")
                            .addField(new CrudFieldSpec("category_code", "Group Code", CrudFieldType.TEXT, true))
                            .addField(new CrudFieldSpec("category_name", "Group Name", CrudFieldType.TEXT, true))
                            .addField(new CrudFieldSpec("sort_code", "Sort", CrudFieldType.TEXT, false)),
                    true,
                    true);
            default -> new BrowseConfig(
                    key,
                    new String[]{"Order by Code"},
                    new String[]{"SELECT 0, 'N/A'"},
                    new String[]{"Exit"},
                    new String[]{"ID", "VALUE"},
                    "No browse is configured for this master key yet.",
                    "",
                    "id",
                    new String[]{"id", "value"},
                    new CrudTableConfig(key),
                    true,
                    false);
        };
    }

    private static boolean blank(Object value) {
        return value == null || String.valueOf(value).trim().isBlank();
    }

    private static boolean parseBoolean(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        String text = stringValue(value).toUpperCase();
        return "1".equals(text) || "YES".equals(text) || "TRUE".equals(text);
    }

    private static String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private record BrowseConfig(
            String title,
            String[] orderLabels,
            String[] orderSqls,
            String[] commandButtons,
            String[] columns,
            String footerText,
            String tableName,
            String idKey,
            String[] rowKeys,
            CrudTableConfig crudConfig,
            boolean hideFirstColumn,
            boolean allowDelete,
            Map<String, Object> fixedValues) {
        private BrowseConfig(
                String title,
                String[] orderLabels,
                String[] orderSqls,
                String[] commandButtons,
                String[] columns,
                String footerText,
                String tableName,
                String idKey,
                String[] rowKeys,
                CrudTableConfig crudConfig,
                boolean hideFirstColumn,
                boolean allowDelete) {
            this(title, orderLabels, orderSqls, commandButtons, columns, footerText, tableName, idKey, rowKeys, crudConfig, hideFirstColumn, allowDelete, Map.of());
        }

        private java.util.List<CrudFieldSpec> fieldSpecs() {
            return crudConfig.getFields();
        }
    }
}
