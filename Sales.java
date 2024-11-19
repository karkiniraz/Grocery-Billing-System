package org.billing;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.Font;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class Sales extends JPanel {
    private JTable sale;
    private static DefaultTableModel dtm;
    private JTextField searchField;
    private JButton searchButton;

    // MySQL connection details
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/billingsystem";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = ""; // Replace with your actual password

    public Sales() {
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(240, 248, 255));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout());

        // Search field and button
        searchField = new JTextField(15);
        searchButton = new JButton("Search");
        searchButton.setFont(new Font("Tahoma", Font.BOLD, 14));

        topPanel.add(new JLabel("Search by Bill ID:"));
        topPanel.add(searchField);
        topPanel.add(searchButton);

        JButton btnLoad = new JButton("Refresh");
        btnLoad.setFont(new Font("Tahoma", Font.BOLD, 14));
        btnLoad.setBackground(new Color(144, 238, 144));
        topPanel.add(btnLoad);

        add(topPanel, BorderLayout.NORTH);

        String[] header = {"Bill ID", "Item Code", "Item Name", "Price", "Quantity", "Item Total", "Discount Amount"};
        dtm = new DefaultTableModel(header, 0);
        sale = new JTable(dtm);
        sale.setFont(new Font("Tahoma", Font.PLAIN, 14));
        sale.setRowHeight(30);
        sale.setSelectionBackground(new Color(173, 216, 230));
        sale.setSelectionForeground(Color.BLACK);

        // Set custom cell renderer for the table
        sale.setDefaultRenderer(Object.class, new CustomTableCellRenderer());

        JScrollPane scrollPane = new JScrollPane(sale);
        add(scrollPane, BorderLayout.CENTER);

        btnLoad.addActionListener(e -> loadSalesData(null));

        searchButton.addActionListener(e -> loadSalesData(searchField.getText().trim()));

        JButton btnPrint = new JButton("PRINT");
        btnPrint.setFont(new Font("Tahoma", Font.BOLD, 14));
        btnPrint.setBackground(new Color(135, 206, 250));
        add(btnPrint, BorderLayout.SOUTH);

        btnPrint.addActionListener(e -> generatePDFReport());
    }

    private void loadSalesData(String billId) {
        dtm.setRowCount(0); // Clear existing table rows

        // Base SQL query
        String sql = "SELECT bd.bill_id, bd.item_code, bd.item_name, bd.price, SUM(bd.quantity) AS quantity, " +
                "SUM(bd.total) AS total, b.discount, " +
                "SUM(bd.total * (b.discount / 100)) AS discount_amount, " +
                "SUM(bd.total - (bd.total * (b.discount / 100))) AS final_total " +
                "FROM billing_details bd " +
                "JOIN bills b ON bd.bill_id = b.bill_id ";

        // Append WHERE clause if billId is provided
        if (billId != null && !billId.isEmpty()) {
            sql += "WHERE bd.bill_id = '" + billId + "' ";
        }

        sql += "GROUP BY bd.bill_id, bd.item_code, bd.item_name, bd.price, b.discount " +
                "ORDER BY bd.bill_id";

        double currentBillTotalDiscount = 0.0;
        double currentBillTotalAmount = 0.0;
        String currentBillId = "";

        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String billIdValue = rs.getString("bill_id");
                String itemCode = rs.getString("item_code");
                String itemName = rs.getString("item_name");
                Double price = rs.getDouble("price");
                Integer quantity = rs.getInt("quantity");
                Double itemTotal = rs.getDouble("total");
                Double discountAmount = rs.getDouble("discount_amount");
                Double discountedTotalAmount = rs.getDouble("final_total");

                // If the bill ID changes, add a row for the total discount and total amount for the previous bill
                if (!billIdValue.equals(currentBillId) && !currentBillId.isEmpty()) {
                    dtm.addRow(new Object[]{"", "", "", "", "", "Total Discount", currentBillTotalDiscount});
                    dtm.addRow(new Object[]{"", "", "", "", "", "Total Amount", currentBillTotalAmount});
                    currentBillTotalDiscount = 0.0;
                    currentBillTotalAmount = 0.0;
                }

                dtm.addRow(new Object[]{billIdValue, itemCode, itemName, price, quantity, itemTotal, discountAmount});

                currentBillId = billIdValue;
                currentBillTotalDiscount += discountAmount;
                currentBillTotalAmount += discountedTotalAmount;
            }

            // Add the total discount and total amount for the last bill
            if (!currentBillId.isEmpty()) {
                dtm.addRow(new Object[]{"", "", "", "", "", "Total Discount", currentBillTotalDiscount});
                dtm.addRow(new Object[]{"", "", "", "", "", "Total Amount", currentBillTotalAmount});
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error loading sales data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generatePDFReport() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save PDF Report");
        fileChooser.setSelectedFile(new File("report.pdf")); // Default file name
        int userSelection = fileChooser.showSaveDialog(null);

        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return; // User cancelled the operation
        }

        File fileToSave = fileChooser.getSelectedFile();

        // Ensure file has a .pdf extension
        if (!fileToSave.getName().endsWith(".pdf")) {
            fileToSave = new File(fileToSave.getAbsolutePath() + ".pdf");
        }

        Document document = new Document();
        try {
            // Initialize PdfWriter and associate it with the Document
            PdfWriter.getInstance(document, new FileOutputStream(fileToSave));
            document.open();

            // Add a title and space
            document.add(new Paragraph("Sales Report"));
            document.add(new Paragraph(" "));

            // Create PDF table with the same number of columns as the JTable
            PdfPTable pdfTable = new PdfPTable(dtm.getColumnCount());
            pdfTable.setWidthPercentage(100); // Adjust table width to fit the page

            // Add table headers
            for (int col = 0; col < dtm.getColumnCount(); col++) {
                pdfTable.addCell(new PdfPCell(new Phrase(dtm.getColumnName(col))));
            }

            // Add table rows
            for (int row = 0; row < dtm.getRowCount(); row++) {
                for (int col = 0; col < dtm.getColumnCount(); col++) {
                    Object cellValue = dtm.getValueAt(row, col);
                    pdfTable.addCell(new PdfPCell(new Phrase(cellValue != null ? cellValue.toString() : "")));
                }
            }

            // Add the table to the document
            document.add(pdfTable);

            JOptionPane.showMessageDialog(null, "PDF Report saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error saving PDF report: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            // Ensure the document is closed properly
            if (document.isOpen()) {
                document.close();
            }
        }
    }

    // Custom cell renderer class
    static class CustomTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                // Highlight row based on itemCode or discountedTotalAmount presence
                String itemCode = (String) table.getValueAt(row, 1);
                Double discountedTotalAmount = (Double) table.getValueAt(row, 6);

//                if (itemCode != null && !itemCode.isEmpty() || discountedTotalAmount != null && discountedTotalAmount > 0) {
//                    c.setBackground(new Color(173, 216, 230)); // Light blue color for rows with data
//                } else {
//                    c.setBackground(Color.WHITE); // Default background color
//                }
            }
            return c;
        }
    }
}
