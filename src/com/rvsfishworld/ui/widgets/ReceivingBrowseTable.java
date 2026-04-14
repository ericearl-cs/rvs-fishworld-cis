package com.rvsfishworld.ui.widgets;

import com.rvsfishworld.ui.FoxProTheme;
import javax.swing.JTable;
import javax.swing.table.TableModel;

@SuppressWarnings("serial")
public class ReceivingBrowseTable extends JTable {
    public ReceivingBrowseTable() {
        super();
        FoxProTheme.styleTable(this);
        setAutoResizeMode(AUTO_RESIZE_OFF);
    }

    public ReceivingBrowseTable(TableModel model) {
        super(model);
        FoxProTheme.styleTable(this);
        setAutoResizeMode(AUTO_RESIZE_OFF);
    }
}
