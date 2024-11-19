package org.billing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class EditBill extends JPanel {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/billingsystem";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = ""; // Replace with your actual password

    private JTextField billIdField;
    private JTextField discountField;
    private JTable itemsTable;
    private DefaultTableModel tableModel;

    public EditBill(String billId) {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(240, 248, 255));

        JPanel topPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        topPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        topPanel.add(new JLabel("Bill ID:"));
        billIdField = new JTextField();
        billIdField.setEditable(false);
        topPanel.add(billIdField);

        topPanel.add(new JLabel("Discount:"));
        discountField = new JTextField();
        topPanel.add(discountField);

        add(topPanel, BorderLayout.NORTH);

        // Table for items
        tableModel = new DefaultTableModel(new Object[]{"Item Code", "Item Name", "Price", "Quantity", "Total"}, 0);
        itemsTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(itemsTable);

        add(scrollPane, BorderLayout.CENTER);

        // Add a TableModelListener to recalculate total when quantity or price changes
        tableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                int row = e.getFirstRow();
                int column = e.getColumn();

                // Only recalculate total if Quantity or Price is changed
                if (column == 2 || column == 3) {  // Price or Quantity column
                    updateTotalForRow(row);
                }
            }
        });

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new SaveButtonListener());
        add(saveButton, BorderLayout.SOUTH);

        if (billId != null && !billId.isEmpty()) {
            loadBillData(billId);
        }
    }

    private void updateTotalForRow(int row) {
        try {
            double price = Double.parseDouble(tableModel.getValueAt(row, 2).toString());
            int quantity = Integer.parseInt(tableModel.getValueAt(row, 3).toString());
            double total = price * quantity;

            // Update the Total column
            tableModel.setValueAt(total, row, 4);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid data in Price or Quantity field.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadBillData(String billId) {
        String sql = "SELECT b.bill_id, bd.item_code, bd.item_name, bd.price, bd.quantity, bd.total, b.discount " +
                "FROM billing_details bd " +
                "JOIN bills b ON bd.bill_id = b.bill_id " +
                "WHERE bd.bill_id = ?";
        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, billId);
            ResultSet rs = pstmt.executeQuery();

            billIdField.setText(billId);
            while (rs.next()) {
                String itemCode = rs.getString("item_code");
                String itemName = rs.getString("item_name");
                double price = rs.getDouble("price");
                int quantity = rs.getInt("quantity");
                double total = rs.getDouble("total");

                tableModel.addRow(new Object[]{itemCode, itemName, price, quantity, total});
            }

            if (rs.next()) {
                discountField.setText(String.valueOf(rs.getDouble("discount")));
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading bill data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private class SaveButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String billId = billIdField.getText();
            try {
                double discount = Double.parseDouble(discountField.getText());

                try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
                    conn.setAutoCommit(false);

                    // Update the discount in the bills table
                    String updateBillSql = "UPDATE bills SET discount = ? WHERE bill_id = ?";
                    try (PreparedStatement pstmt = conn.prepareStatement(updateBillSql)) {
                        pstmt.setDouble(1, discount);
                        pstmt.setString(2, billId);
                        pstmt.executeUpdate();
                    }

                    // Update each item in the billing_detail table
                    String updateItemSql = "UPDATE billing_details SET item_name = ?, price = ?, quantity = ?, total = ? WHERE bill_id = ? AND item_code = ?";
                    try (PreparedStatement pstmt = conn.prepareStatement(updateItemSql)) {
                        for (int i = 0; i < tableModel.getRowCount(); i++) {
                            try {
                                String itemCode = (String) tableModel.getValueAt(i, 0);
                                String itemName = (String) tableModel.getValueAt(i, 1);

                                double price = Double.parseDouble(tableModel.getValueAt(i, 2).toString());
                                int quantity = Integer.parseInt(tableModel.getValueAt(i, 3).toString());
                                double total = Double.parseDouble(tableModel.getValueAt(i, 4).toString());

                                pstmt.setString(1, itemName);
                                pstmt.setDouble(2, price);
                                pstmt.setInt(3, quantity);
                                pstmt.setDouble(4, total);
                                pstmt.setString(5, billId);
                                pstmt.setString(6, itemCode);

                                pstmt.addBatch();
                            } catch (ClassCastException | NumberFormatException ex) {
                                System.out.println("Error processing row " + i + ": " + ex.getMessage());
                                JOptionPane.showMessageDialog(EditBill.this,
                                        "Error in data conversion at row " + (i + 1) + ": " + ex.getMessage(),
                                        "Conversion Error", JOptionPane.ERROR_MESSAGE);
                                return;  // Exit the loop if there's an error
                            }
                        }
                        pstmt.executeBatch();
                    }

                    conn.commit();
                    JOptionPane.showMessageDialog(EditBill.this, "Bill updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(EditBill.this, "Error updating bill data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(EditBill.this, "Invalid discount value: " + discountField.getText(), "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
