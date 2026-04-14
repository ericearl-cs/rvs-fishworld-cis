package com.rvsfishworld.ui;

import com.rvsfishworld.model.LookupItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.function.Function;

public class LookupDialog extends JDialog {
    private final JTextField txtFind = FoxProTheme.createTextField(18);
    private final JTable table = new JTable();
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Code", "Name"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private LookupItem selectedItem;
    private final Function<String, List<LookupItem>> loader;

    public LookupDialog(Window owner, String title, Function<String, List<LookupItem>> loader) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        this.loader = loader;

        setSize(760, 520);
        setLocationRelativeTo(owner);
        buildUi();
        loadData();
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(FoxProTheme.PANEL);
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        root.add(buildTopBar(), BorderLayout.NORTH);
        root.add(buildCommandBar(), BorderLayout.WEST);
        root.add(buildTablePane(), BorderLayout.CENTER);

        setContentPane(root);
    }

    private JPanel buildTopBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setBackground(FoxProTheme.PANEL);
        panel.add(FoxProTheme.createOrderButton("Order by Code", true));
        panel.add(FoxProTheme.createOrderButton("Order by Name", false));
        return panel;
    }

    private JPanel buildCommandBar() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(FoxProTheme.PANEL);

        JButton btnSelect = FoxProTheme.createButton("Select");
        JButton btnFind = FoxProTheme.createButton("Find");
        JButton btnAdd = FoxProTheme.createButton("Add");
        JButton btnEdit = FoxProTheme.createButton("Edit");
        JButton btnDelete = FoxProTheme.createButton("Delete");
        JButton btnPrint = FoxProTheme.createButton("Print");
        JButton btnExit = FoxProTheme.createButton("Exit");

        btnSelect.addActionListener(e -> selectCurrent());
        btnFind.addActionListener(e -> loadData());
        btnAdd.addActionListener(e -> showNotWired());
        btnEdit.addActionListener(e -> showNotWired());
        btnDelete.addActionListener(e -> showNotWired());
        btnPrint.addActionListener(e -> showNotWired());
        btnExit.addActionListener(e -> dispose());

        for (JButton button : new JButton[]{btnSelect, btnFind, btnAdd, btnEdit, btnDelete, btnPrint, btnExit}) {
            button.setMaximumSize(new Dimension(106, 32));
            panel.add(button);
            panel.add(Box.createVerticalStrut(8));
        }
        return panel;
    }

    private JPanel buildTablePane() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(FoxProTheme.PANEL);

        JPanel findBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        findBar.setBackground(FoxProTheme.PANEL);
        findBar.add(new JLabel("Find"));
        findBar.add(txtFind);

        table.setModel(model);
        FoxProTheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.removeColumn(table.getColumnModel().getColumn(0));
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    selectCurrent();
                }
            }
        });

        panel.add(findBar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void loadData() {
        model.setRowCount(0);
        List<LookupItem> items = loader.apply(txtFind.getText());
        for (LookupItem item : items) {
            model.addRow(new Object[]{item.getId(), item.getCode(), item.getName()});
        }
    }

    private void selectCurrent() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row first.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(row);
        selectedItem = new LookupItem(
                ((Number) model.getValueAt(modelRow, 0)).longValue(),
                String.valueOf(model.getValueAt(modelRow, 1)),
                String.valueOf(model.getValueAt(modelRow, 2))
        );
        dispose();
    }

    private void showNotWired() {
        JOptionPane.showMessageDialog(this, "Lookup maintenance is not wired yet.");
    }

    public LookupItem getSelectedItem() {
        return selectedItem;
    }
}
