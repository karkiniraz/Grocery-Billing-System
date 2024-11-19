package org.billing;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Inventory extends JPanel {
    private List<StockItem> items;
    private JTable inventoryTable;
    private JTextField searchField;

    // MySQL connection details
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/billingsystem"; // Replace with your actual database name
    private static final String USERNAME = "root"; // Replace with your MySQL username
    private static final String PASSWORD = ""; // Replace with your MySQL password

    public Inventory() {
        initializeData();
        initializeUI();
    }

    private void initializeData() {
        this.items = new ArrayList<>();
        // Fetching data from MySQL database
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM stock")) {

            while (resultSet.next()) {
                String code = resultSet.getString("itemCode");
                String name = resultSet.getString("itemName");
                int quantity = resultSet.getInt("quantity");
                double buyingPrice = resultSet.getDouble("buyingPrice");
                double sellingPrice = resultSet.getDouble("sellingPrice");

                items.add(new StockItem(code, name, quantity, buyingPrice, sellingPrice));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading data from database: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // Create panel for search and display
        JPanel controlPanel = new JPanel(new FlowLayout());
        JLabel searchLabel = new JLabel("Search by Item Name:");
        searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        controlPanel.add(searchLabel);
        controlPanel.add(searchField);
        controlPanel.add(searchButton);

        // Create table model
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Item Code");
        model.addColumn("Item Name");
        model.addColumn("Quantity");
        model.addColumn("Buying Price");
        model.addColumn("Selling Price");

        // Populate table model with initial data
        populateTable(model);

        // Create JTable with the model
        inventoryTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(inventoryTable);

        // Add components to JPanel
        add(controlPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Add action listener to search button
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String searchTerm = searchField.getText().trim().toLowerCase();
                searchItemByName(searchTerm);
            }
        });
    }

    // Method to search for an item by name
    private void searchItemByName(String name) {
        DefaultTableModel model = (DefaultTableModel) inventoryTable.getModel();
        model.setRowCount(0); // Clear previous rows

        for (StockItem item : items) {
            if (item.getName().toLowerCase().contains(name)) {
                model.addRow(new Object[]{item.getCode(), item.getName(), item.getQuantity(),
                        item.getBuyingPrice(), item.getSellingPrice()});
            }
        }

        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No items found with name '" + name + "'.",
                    "Search Results", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Method to populate the table with initial data
    private void populateTable(DefaultTableModel model) {
        model.setRowCount(0); // Clear previous rows

        for (StockItem item : items) {
            model.addRow(new Object[]{item.getCode(), item.getName(), item.getQuantity(),
                    item.getBuyingPrice(), item.getSellingPrice()});
        }
    }
}

class StockItem {
    private String itemCode;
    private String itemName;
    private double buyingPrice;
    private int quantity;
    private double sellingPrice;

    // Constructor
    public StockItem(String itemCode, String itemName, int quantity, double buyingPrice, double sellingPrice) {
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.quantity = quantity;
        this.buyingPrice = buyingPrice;
        this.sellingPrice = sellingPrice;
    }

    // Getters
    public String getCode() {
        return itemCode;
    }

    public String getName() {
        return itemName;
    }

    public double getBuyingPrice() {
        return buyingPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }
}
