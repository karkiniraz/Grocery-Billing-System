package org.billing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.text.NumberFormat;

public class UpdateStock extends JPanel {

    private JTextField itemCodeField;
    private JTextField itemNameField;
    private JSpinner itemQuantitySpinner;
    private JFormattedTextField itemBuyingPriceField;
    private JTextField supplierNameField;
    private JFormattedTextField itemSellingPriceField;
    private JButton updateButton;

    // MySQL connection details
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/billingsystem";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = ""; // Replace with your actual password

    public UpdateStock() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel itemCodeLabel = new JLabel("Item Code:");
        inputPanel.add(itemCodeLabel, gbc);

        gbc.gridx++;
        itemCodeField = new JTextField(20);
        inputPanel.add(itemCodeField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel itemNameLabel = new JLabel("Item Name:");
        inputPanel.add(itemNameLabel, gbc);

        gbc.gridx++;
        itemNameField = new JTextField(20);
        inputPanel.add(itemNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel itemQuantityLabel = new JLabel("Quantity:");
        inputPanel.add(itemQuantityLabel, gbc);

        gbc.gridx++;
        itemQuantitySpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        inputPanel.add(itemQuantitySpinner, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel itemBuyingPriceLabel = new JLabel("Buying Price:");
        inputPanel.add(itemBuyingPriceLabel, gbc);

        gbc.gridx++;
        NumberFormatter priceFormatter = new NumberFormatter(NumberFormat.getNumberInstance());
        priceFormatter.setValueClass(Double.class);
        priceFormatter.setMinimum(0.0);
        priceFormatter.setMaximum(Double.MAX_VALUE);
        itemBuyingPriceField = new JFormattedTextField(priceFormatter);
        itemBuyingPriceField.setColumns(10);
        inputPanel.add(itemBuyingPriceField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel itemSellingPriceLabel = new JLabel("Selling Price:");
        inputPanel.add(itemSellingPriceLabel, gbc);

        gbc.gridx++;
        itemSellingPriceField = new JFormattedTextField(priceFormatter);
        itemSellingPriceField.setColumns(10);
        inputPanel.add(itemSellingPriceField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel supplierNameLabel = new JLabel("Supplier Name:");
        inputPanel.add(supplierNameLabel, gbc);

        gbc.gridx++;
        supplierNameField = new JTextField(20);
        inputPanel.add(supplierNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        updateButton = new JButton("Update Stock");
        inputPanel.add(updateButton, gbc);

        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateStock();
            }
        });

        add(inputPanel, BorderLayout.CENTER);
    }

    private void updateStock() {
        String itemCode = itemCodeField.getText().trim();
        String itemName = itemNameField.getText().trim();
        int quantity = (int) itemQuantitySpinner.getValue();
        double buyingPrice = ((Number) itemBuyingPriceField.getValue()).doubleValue();
        double sellingPrice = ((Number) itemSellingPriceField.getValue()).doubleValue();
        String supplierName = supplierNameField.getText().trim();

        if (itemCode.isEmpty() || itemName.isEmpty() || supplierName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Item Code, Item Name, and Supplier Name cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (quantity <= 0 || buyingPrice <= 0 || sellingPrice <= buyingPrice) {
            JOptionPane.showMessageDialog(this, "Quantity and Prices must be greater than zero. Selling Price must be greater than Buying Price.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Database update logic
        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
            String sql = "INSERT INTO stock (itemCode, itemName, quantity, buyingPrice, sellingPrice, supplierName) " +
                    "VALUES (?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity), buyingPrice = VALUES(buyingPrice), sellingPrice = VALUES(sellingPrice), supplierName = VALUES(supplierName)";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, itemCode);
                stmt.setString(2, itemName);
                stmt.setInt(3, quantity);
                stmt.setDouble(4, buyingPrice);
                stmt.setDouble(5, sellingPrice);
                stmt.setString(6, supplierName);
                stmt.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Stock updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

            // Clear fields after update
            itemCodeField.setText("");
            itemNameField.setText("");
            itemQuantitySpinner.setValue(0);
            itemBuyingPriceField.setValue(0.00);
            itemSellingPriceField.setValue(0.00);
            supplierNameField.setText("");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating stock: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Update Stock");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new UpdateStock());
            frame.pack();
            frame.setLocationRelativeTo(null); // Center the window
            frame.setVisible(true);
        });
    }
}
