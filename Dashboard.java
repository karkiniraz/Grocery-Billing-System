package org.billing;

import javax.swing.*;
import java.awt.*;

public class Dashboard extends JFrame {
    private JPanel contentPanel;

    public Dashboard() {
        initialize();
    }

    private void initialize() {
        setTitle("Billing System Dashboard");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Create menu bar
        JMenuBar menuBar = new JMenuBar();

        // Create menus
        JMenu productMenu = new JMenu("Update Stock");
        productMenu.setFont(new Font("Times New Roman", Font.PLAIN, 16));
        JMenu inventoryMenu = new JMenu("Inventory");
        inventoryMenu.setFont(new Font("Times New Roman", Font.PLAIN, 16));
        JMenu salesMenu = new JMenu("Sales");
        salesMenu.setFont(new Font("Times New Roman", Font.PLAIN, 16));
        JMenu createBillMenu = new JMenu("Create Bill");
        createBillMenu.setFont(new Font("Times New Roman", Font.PLAIN, 16));
        JMenu editBillMenu = new JMenu("Edit Bill");
        editBillMenu.setFont(new Font("Times New Roman", Font.PLAIN, 16));
        JMenu customersMenu = new JMenu("Customers");
        customersMenu.setFont(new Font("Times New Roman", Font.PLAIN, 16));

        // Add menus to menu bar
        menuBar.add(productMenu);
        menuBar.add(inventoryMenu);
        menuBar.add(salesMenu);
        menuBar.add(createBillMenu);
        menuBar.add(editBillMenu);
        menuBar.add(customersMenu);

        // Set the menu bar for the frame
        setJMenuBar(menuBar);
        menuBar.setBackground(new Color(212, 212, 212));
        // Create a panel for displaying content
        contentPanel = new JPanel();
        contentPanel.setLayout(new CardLayout());
        add(contentPanel, BorderLayout.CENTER);

        // Add an image panel to the content panel
        ImagePanel imagePanel = new ImagePanel("C:\\Users\\karki\\IdeaProjects\\Billing system\\src\\main\\resources\\logo.jpg");
        contentPanel.add(imagePanel);

//         Handle menu item actions
        JMenuItem addProduct= new JMenuItem("Add Product");
        addProduct.addActionListener(e -> showProduct(contentPanel));
        productMenu.add(addProduct);

        JMenuItem inventoryItem = new JMenuItem("View Inventory");
        inventoryItem.addActionListener(e -> showInventory(contentPanel));
        inventoryMenu.add(inventoryItem);


        JMenuItem salesItem = new JMenuItem("View Sales");
        salesItem.addActionListener(e -> showSales(contentPanel));
        salesMenu.add(salesItem);

        JMenuItem createBillItem = new JMenuItem("Create New Bill");
        createBillItem.addActionListener(e -> showCreateBill(contentPanel));
        createBillMenu.add(createBillItem);

        JMenuItem editBillItem = new JMenuItem("Edit Existing Bill");
        editBillItem.addActionListener(e -> showEditBill(contentPanel));
        editBillMenu.add(editBillItem);

        JMenuItem customersItem = new JMenuItem("Manage Customers");
        customersItem.addActionListener(e -> showCustomers(contentPanel));
        customersMenu.add(customersItem);


    }

    private void showProduct(JPanel contentPanel) {
        contentPanel.removeAll();
        UpdateStock updateStock= new UpdateStock();
        contentPanel.add(updateStock);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showInventory(JPanel contentPanel) {
        contentPanel.removeAll();
        Inventory inventory = new Inventory();
        contentPanel.add(inventory);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showSales(JPanel contentPanel) {
        contentPanel.removeAll();
        Sales sales = new Sales();
        contentPanel.add(sales);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showCreateBill(JPanel contentPanel) {
        contentPanel.removeAll();
        BillingPanel billingPanel = new BillingPanel();
        contentPanel.add(billingPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showEditBill(JPanel contentPanel) {
        // Ask for Bill ID to edit
        String billId = JOptionPane.showInputDialog(this, "Enter Bill ID to edit:");
        if (billId != null && !billId.trim().isEmpty()) {
            contentPanel.removeAll();
            EditBill editBillPanel = new EditBill(billId);
            contentPanel.add(editBillPanel);
            contentPanel.revalidate();
            contentPanel.repaint();
        } else {
            JOptionPane.showMessageDialog(this, "Bill ID cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void showCustomers(JPanel contentPanel) {
        contentPanel.removeAll();
        contentPanel.add(new JLabel("Manage Customers"), BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Dashboard dashboard = new Dashboard();
            dashboard.setVisible(true);
        });
    }


    // Inner class for displaying an image
    class ImagePanel extends JPanel {
        private Image image;

        public ImagePanel(String imagePath) {
            // Load the image
            image = new ImageIcon(imagePath).getImage();
            // Set window size to screen size
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            setSize(screenSize.width, screenSize.height);
            // Center the window on the screen
            setLocationRelativeTo(null);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            // Draw the image
            g.drawImage(image, 0, 0, screenSize.width, screenSize.height, this);
        }
    }
}
