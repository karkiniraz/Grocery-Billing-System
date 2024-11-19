package org.billing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;

public class BillingPanel extends JPanel {
    private JTextField itemCodeField;
    private JTextField discountField;
    private JTextField itemNameField;
    private JTextField itemPriceField;
    private JTextField itemQuantityField;
    private JTable billTable;
    private DefaultTableModel tableModel;
    private JLabel totalLabel;
    private JComboBox<String> themeComboBox;
    private ArrayList<Item> items;
    private JPopupMenu productPopupMenu;

    // MySQL connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/billingsystem";
    private static final String USER = "root";
    private static final String PASS = ""; // Replace with your MySQL password

    public BillingPanel() {
        items = new ArrayList<>();
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(240, 248, 255));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Initialize product popup menu
        productPopupMenu = new JPopupMenu();

        // Top Panel for Bill Table
        String[] columns = {"Item Code", "Item Name", "Price", "Quantity", "Total"};
        tableModel = new DefaultTableModel(columns, 0);
        billTable = new JTable(tableModel);
        billTable.setFont(new Font("Tahoma", Font.PLAIN, 14));
        billTable.setRowHeight(20);
        billTable.setSelectionBackground(new Color(173, 216, 230));
        billTable.setSelectionForeground(Color.BLACK);

        JScrollPane tableScrollPane = new JScrollPane(billTable);
        add(tableScrollPane, BorderLayout.CENTER);

        // JLabel below the JTable
        JLabel tableLabel = new JLabel("Bill Details:");
        tableLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
        tableLabel.setHorizontalAlignment(SwingConstants.CENTER);
        tableScrollPane.setColumnHeaderView(tableLabel);

        // Bottom Panel for Input Fields and Total Amount
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.setBackground(new Color(240, 248, 255));
        bottomPanel.setBorder(new EmptyBorder(10, 0, 80, 0));

        // Panel for Text Fields
        JPanel textFieldPanel = new JPanel();
        textFieldPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        textFieldPanel.setBackground(new Color(240, 248, 255));

        JLabel itemCodeLabel = new JLabel("Item Code:");
        itemCodeField = new JTextField(10);
        textFieldPanel.add(itemCodeLabel);
        textFieldPanel.add(itemCodeField);

        JLabel itemNameLabel = new JLabel("Item Name:");
        itemNameField = new JTextField(10);
        textFieldPanel.add(Box.createHorizontalStrut(10));
        textFieldPanel.add(itemNameLabel);
        textFieldPanel.add(itemNameField);

        JLabel itemPriceLabel = new JLabel("Item Price:");
        itemPriceField = new JTextField(10);
        textFieldPanel.add(Box.createHorizontalStrut(10));
        textFieldPanel.add(itemPriceLabel);
        textFieldPanel.add(itemPriceField);

        JLabel itemQuantityLabel = new JLabel("Quantity:");
        itemQuantityField = new JTextField(10);
        textFieldPanel.add(Box.createHorizontalStrut(20));
        textFieldPanel.add(itemQuantityLabel);
        textFieldPanel.add(itemQuantityField);

        JLabel discountLabel = new JLabel("Discount (%):");
        discountField = new JTextField(10);
        textFieldPanel.add(Box.createHorizontalStrut(20));
        textFieldPanel.add(discountLabel);
        textFieldPanel.add(discountField);

        bottomPanel.add(textFieldPanel, BorderLayout.NORTH);

        // Panel for Buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
        buttonPanel.setBackground(new Color(240, 248, 255));

        JButton addButton = new JButton("Add Item");
        addButton.setBackground(new Color(144, 238, 144));
        buttonPanel.add(addButton);

        JLabel totalTextLabel = new JLabel("Total Amount:");
        totalTextLabel.setFont(new Font("Tahoma", Font.BOLD, 18));
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(totalTextLabel);

        totalLabel = new JLabel("0.00");
        totalLabel.setFont(new Font("Tahoma", Font.BOLD, 18));
        buttonPanel.add(totalLabel);

        JButton calculateTotalButton = new JButton("Calculate Total");
        calculateTotalButton.setBackground(new Color(135, 206, 250));
        buttonPanel.add(calculateTotalButton);

        JButton clearButton = new JButton("Clear Bill");
        clearButton.setBackground(new Color(255, 99, 71));
        buttonPanel.add(clearButton);

        JButton payBillButton = new JButton("Pay Bill");
        payBillButton.setBackground(new Color(255, 99, 71));
        buttonPanel.add(payBillButton);

        JLabel themeLabel = new JLabel("Theme:");
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(themeLabel);

        themeComboBox = new JComboBox<>(new String[]{"Light", "Dark", "Classic"});
        themeComboBox.setBackground(new Color(255, 228, 196));
        buttonPanel.add(themeComboBox);

        bottomPanel.add(buttonPanel, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);

        // Add Item Action
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String itemCode = itemCodeField.getText().trim();
                String itemName = itemNameField.getText().trim();
                String itemPrice = itemPriceField.getText().trim();
                String itemQuantity = itemQuantityField.getText().trim();
                if (!itemCode.isEmpty() && !itemName.isEmpty() && !itemPrice.isEmpty() && !itemQuantity.isEmpty()) {
                    try {
                        double price = Double.parseDouble(itemPrice);
                        int quantity = Integer.parseInt(itemQuantity);
                        if (price <= 0 || quantity <= 0) {
                            JOptionPane.showMessageDialog(BillingPanel.this, "Price and Quantity must be positive numbers.", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        double total = price * quantity;

                        tableModel.addRow(new Object[]{itemCode, itemName, price, quantity, total});

                        itemCodeField.setText("");
                        itemNameField.setText("");
                        itemPriceField.setText("");
                        itemQuantityField.setText("");

                        items.add(new Item(itemCode, itemName, price, quantity));

                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(BillingPanel.this, "Please enter valid price and quantity.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(BillingPanel.this, "Please fill all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Calculate Total Action
        calculateTotalButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double totalAmount = 0.00;
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    totalAmount += (double) tableModel.getValueAt(i, 4);
                }
                try {
                    double discount = discountField.getText().trim().isEmpty() ? 0.0 : Double.parseDouble(discountField.getText().trim());
                    if (discount < 0 || discount > 100) {
                        JOptionPane.showMessageDialog(BillingPanel.this, "Discount must be between 0 and 100.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    double discountedTotal = totalAmount - (totalAmount * (discount / 100));
                    totalLabel.setText(String.format("%.2f", discountedTotal));
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(BillingPanel.this, "Please enter a valid discount percentage.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Clear Bill Action
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tableModel.setRowCount(0);
                items.clear();
                totalLabel.setText("0.00");
            }
        });

        // Pay Bill Action
        // Pay Bill Action
        payBillButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!items.isEmpty()) {
                    try {
                        insertBillingDetails();
                        // Show print options dialog
                        print printOptionsDialog = new print((Frame) SwingUtilities.getWindowAncestor(BillingPanel.this));
                        printOptionsDialog.setVisible(true);
                        JOptionPane.showMessageDialog(BillingPanel.this, "Bill payment recorded successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        tableModel.setRowCount(0);
                        items.clear();
                        totalLabel.setText("0.00");
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(BillingPanel.this, "Error saving bill: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(BillingPanel.this, "Please add items to the bill.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });


        // Theme Change Action
        themeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedTheme = (String) themeComboBox.getSelectedItem();
                switch (selectedTheme) {
                    case "Light":
                        setBackground(Color.WHITE);
                        bottomPanel.setBackground(new Color(240, 248, 255));
                        break;
                    case "Dark":
                        setBackground(Color.DARK_GRAY);
                        bottomPanel.setBackground(Color.GRAY);
                        break;
                    case "Classic":
                        setBackground(new Color(240, 248, 255));
                        bottomPanel.setBackground(new Color(240, 248, 255));
                        break;
                }
                SwingUtilities.updateComponentTreeUI(BillingPanel.this);
            }
        });

        // Add Key Listener to itemCodeField
        itemCodeField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String itemCode = itemCodeField.getText().trim();
                if (!itemCode.isEmpty()) {
                    try {
                        showProductSuggestions(itemCode);
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(BillingPanel.this, "Error fetching product details: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    productPopupMenu.setVisible(false); // Hide popup if no input
                }
            }
        });

        // Add Focus Listener to itemCodeField
        itemCodeField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                SwingUtilities.invokeLater(() -> {
                    /*Timer timer = new Timer(200, evt -> productPopupMenu.setVisible(false));
                    timer.setRepeats(false);
                    timer.start();*/
                });
            }
        });
    }

    private void showProductSuggestions(String itemCode) throws SQLException {
        SwingUtilities.invokeLater(() -> {
            productPopupMenu.setVisible(false); // Hide previous popup
            productPopupMenu.removeAll(); // Clear previous menu items
        });

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            String sql = "SELECT itemCode, itemName, sellingPrice FROM stock WHERE itemCode LIKE ?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, itemCode + "%");
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String code = resultSet.getString("itemCode");
                String name = resultSet.getString("itemName");
                double price = resultSet.getDouble("sellingPrice");

                JMenuItem menuItem = new JMenuItem(name + " (" + code + ")");
                menuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        itemCodeField.setText(code);
                        itemNameField.setText(name);
                        itemPriceField.setText(String.valueOf(price));
                        productPopupMenu.setVisible(false);
                    }
                });
                SwingUtilities.invokeLater(() -> productPopupMenu.add(menuItem));
            }

            if (productPopupMenu.getComponentCount() > 0) {
                SwingUtilities.invokeLater(() -> productPopupMenu.show(itemCodeField, 0, itemCodeField.getHeight()));
            }

        } catch (SQLException ex) {
            throw ex;
        } finally {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            if (connection != null) connection.close();
        }
    }

    private void insertBillingDetails() throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            connection.setAutoCommit(false);

            // Insert into bills table
            String insertBillSQL = "INSERT INTO bills (bill_date, total_amount, discount, final_total) VALUES (NOW(), ?, ?, ?)";
            statement = connection.prepareStatement(insertBillSQL, Statement.RETURN_GENERATED_KEYS);
            double totalAmount = 0.00;
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                totalAmount += (double) tableModel.getValueAt(i, 4);
            }
            double discount = discountField.getText().trim().isEmpty() ? 0.0 : Double.parseDouble(discountField.getText().trim());
            double finalTotal = totalAmount - (totalAmount * (discount / 100));
            statement.setDouble(1, totalAmount);
            statement.setDouble(2, discount);
            statement.setDouble(3, finalTotal);
            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                long billId = generatedKeys.getLong(1);

                // Insert into billing_detail table
                String insertDetailSQL = "INSERT INTO billing_details (bill_id, item_code, item_name, price, quantity, total) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement detailStatement = connection.prepareStatement(insertDetailSQL);
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    detailStatement.setLong(1, billId);
                    detailStatement.setString(2, (String) tableModel.getValueAt(i, 0));
                    detailStatement.setString(3, (String) tableModel.getValueAt(i, 1));
                    detailStatement.setDouble(4, (Double) tableModel.getValueAt(i, 2));
                    detailStatement.setInt(5, (Integer) tableModel.getValueAt(i, 3));
                    detailStatement.setDouble(6, (Double) tableModel.getValueAt(i, 4));
                    detailStatement.addBatch();
                }
                detailStatement.executeBatch();
                detailStatement.close();
            }

            connection.commit();

        } catch (SQLException ex) {
            if (connection != null) {
                connection.rollback();
            }
            throw ex;
        } finally {
            if (statement != null) statement.close();
            if (connection != null) connection.close();
        }
    }
}
