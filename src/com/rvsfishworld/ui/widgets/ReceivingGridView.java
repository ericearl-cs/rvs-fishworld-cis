package com.rvsfishworld.ui.widgets;

import com.rvsfishworld.ui.FoxProTheme;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

@SuppressWarnings("serial")
public class ReceivingGridView<T> extends JTable {
    public static final int ALIGN_LEFT = javax.swing.SwingConstants.LEFT;
    public static final int ALIGN_CENTER = javax.swing.SwingConstants.CENTER;
    public static final int ALIGN_RIGHT = javax.swing.SwingConstants.RIGHT;

    public record Column(String title, int width, int alignment) {
    }

    private final DefaultTableModel model = new DefaultTableModel();
    private final List<Column> columns = new ArrayList<>();
    private final List<T> rows = new ArrayList<>();
    private BiFunction<T, Integer, String> textProvider = (row, column) -> row == null ? "" : row.toString();
    private BiFunction<T, Boolean, Color> rowBackgroundProvider = (row, selected) -> selected ? getSelectionBackground() : Color.WHITE;
    private BiFunction<T, Boolean, Color> rowForegroundProvider = (row, selected) -> selected ? getSelectionForeground() : Color.BLACK;

    public ReceivingGridView() {
        super();
        setModel(model);
        initialize();
    }

    public ReceivingGridView(TableModel model) {
        super(model);
        initialize();
    }

    private void initialize() {
        FoxProTheme.styleTable(this);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setAutoResizeMode(AUTO_RESIZE_OFF);
        setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (row >= 0 && row < rows.size()) {
                    T rowValue = rows.get(row);
                    component.setBackground(rowBackgroundProvider.apply(rowValue, isSelected));
                    component.setForeground(rowForegroundProvider.apply(rowValue, isSelected));
                }
                return component;
            }
        });
    }

    public void setColumns(List<Column> newColumns) {
        columns.clear();
        columns.addAll(newColumns);
        model.setColumnCount(0);
        for (Column column : columns) {
            model.addColumn(column.title());
        }
        for (int i = 0; i < columns.size() && i < getColumnModel().getColumnCount(); i++) {
            getColumnModel().getColumn(i).setPreferredWidth(columns.get(i).width());
            DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
            renderer.setHorizontalAlignment(columns.get(i).alignment());
            getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    public void setHeaderHeight(int height) {
        getTableHeader().setPreferredSize(new java.awt.Dimension(getTableHeader().getPreferredSize().width, height));
    }

    public void setBodyFont(Font font) {
        setFont(font);
    }

    public void setHeaderFont(Font font) {
        getTableHeader().setFont(font);
    }

    public void setFitColumnsToViewport(boolean fit) {
        setAutoResizeMode(fit ? AUTO_RESIZE_SUBSEQUENT_COLUMNS : AUTO_RESIZE_OFF);
    }

    public void setTextProvider(BiFunction<T, Integer, String> provider) {
        this.textProvider = provider;
    }

    public void setRowBackgroundProvider(BiFunction<T, Boolean, Color> provider) {
        this.rowBackgroundProvider = provider;
    }

    public void setRowForegroundProvider(BiFunction<T, Boolean, Color> provider) {
        this.rowForegroundProvider = provider;
    }

    public void setRows(List<T> newRows) {
        rows.clear();
        rows.addAll(newRows);
        model.setRowCount(0);
        for (T row : rows) {
            Object[] values = new Object[columns.size()];
            for (int i = 0; i < columns.size(); i++) {
                values[i] = textProvider.apply(row, i);
            }
            model.addRow(values);
        }
    }

    public void setSelectedIndex(int index) {
        if (index >= 0 && index < getRowCount()) {
            setRowSelectionInterval(index, index);
        }
    }
}
