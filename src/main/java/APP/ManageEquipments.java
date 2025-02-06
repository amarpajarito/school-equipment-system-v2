/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

package APP;
import LIB.DatabaseConnection;
import LIB.DatabaseExporter;
import LIB.Room;
import LIB.Equipment;
import LIB.Facade;
import LIB.IFacade;
import java.awt.HeadlessException;
import java.awt.event.ItemEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
/**
 *
 * @author Amar Pajarito
 */
public class ManageEquipments extends javax.swing.JFrame {

    private int selectedEquipmentID = 0;

    public ManageEquipments() {
        initComponents();
        setLocationRelativeTo(null);
        setTitle("SEAM Manage Equipments");
        setResizable(false);
        ImageIcon icon = new ImageIcon(getClass().getResource("/IMAGES/ssulogo.png"));
        setIconImage(icon.getImage());
        update();
    }

    public void update() {
        selectedEquipmentID = 0;
        DefaultTableModel tableModel = (DefaultTableModel) jTable1.getModel();
        tableModel.getDataVector().removeAllElements();

        nameText.setText("");
        typeBox.setSelectedIndex(0);
        locationBox.setSelectedIndex(0);
        quantityText.setText("");

        try {
            DatabaseConnection connect = DatabaseConnection.getInstance();
            Connection conn = connect.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM EQUIPMENT;");

            while (rs.next()) {
                int equipmentID = rs.getInt("equipmentID");
                String name = rs.getString("name");
                String type = rs.getString("type");
                String condition = rs.getString("condition");
                String location = rs.getString("location");
                int quantity = rs.getInt("quantity");

                String[] rowData = {String.valueOf(equipmentID), name, type, condition, location, String.valueOf(quantity)};
                tableModel.addRow(rowData);
            }
            conn.close();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void register() {
        if (selectedEquipmentID != 0) {
            JOptionPane.showMessageDialog(this, "ERROR: Equipment Exists In The Database!");
        } else {
            try {
                String name = nameText.getText();
                String type = String.valueOf(typeBox.getSelectedItem());
                String condition = String.valueOf(conditionBox.getSelectedItem());
                String locationName = String.valueOf(locationBox.getSelectedItem());
                int quantity = Integer.parseInt(quantityText.getText());

                if ("".equals(name) || "".equals(type) || "".equals(condition) || "".equals(locationName)) {
                    JOptionPane.showMessageDialog(this, "ERROR: Insufficient Information!");
                } else {
                    Room location = new Room(locationName);
                    IFacade create = new Facade(new Equipment(name, type, condition, location, quantity));
                    create.registerEquipment();
                    update();
                }
            } catch (HeadlessException | NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "ERROR: Please Check Your Entries and Try Again!");
            }
        }
    }

    public void updateEquipment() {
        if (selectedEquipmentID == 0) {
            JOptionPane.showMessageDialog(this, "ERROR: No Equipment Selected!");
        } else {
            try {
                String name = nameText.getText();
                String type = String.valueOf(typeBox.getSelectedItem());
                String condition = String.valueOf(conditionBox.getSelectedItem());
                String location = String.valueOf(locationBox.getSelectedItem());
                int quantity = Integer.parseInt(quantityText.getText());

                if ("".equals(name) || "".equals(type) || "".equals(condition) || "".equals(location)) {
                    JOptionPane.showMessageDialog(this, "ERROR: Insufficient Information!");
                    return;
                }

                String query = "UPDATE EQUIPMENT SET name=?, type=?, condition=?, location=?, quantity=? WHERE equipmentID=?";
                DatabaseConnection connect = DatabaseConnection.getInstance();
                Connection conn = connect.getConnection();

                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setString(1, name);
                    pstmt.setString(2, type);
                    pstmt.setString(3, condition);
                    pstmt.setString(4, location);
                    pstmt.setInt(5, quantity);
                    pstmt.setInt(6, selectedEquipmentID);

                    int rowsUpdated = pstmt.executeUpdate();
                    if (rowsUpdated > 0) {
                        JOptionPane.showMessageDialog(this, "Equipment details updated successfully.");
                    } else {
                        JOptionPane.showMessageDialog(this, "ERROR: Update failed. Equipment ID not found.");
                    }
                }

                update();

            } catch (HeadlessException | NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "ERROR: Please Check Your Entries and Try Again!");
            } catch (SQLException e) {
                System.out.println("ERROR: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "ERROR: Failed to update equipment details. Please try again.");
            }
        }
    }

    public void delete() {
        if (selectedEquipmentID == 0) {
            JOptionPane.showMessageDialog(this, "ERROR: No Equipment Selected!");
        } else {
            int input = JOptionPane.showConfirmDialog(this, "Confirm Equipment Deletion.");
            if (input == JOptionPane.YES_OPTION) {
                String query = "DELETE FROM EQUIPMENT WHERE equipmentID = " + selectedEquipmentID + ";";
                try {
                    DatabaseConnection connect = DatabaseConnection.getInstance();
                    Connection conn = connect.getConnection();
                    Statement stmt = conn.createStatement();
                    int rowsDeleted = stmt.executeUpdate(query);
                
                    if (rowsDeleted > 0) {
                        JOptionPane.showMessageDialog(this, "Equipment record deleted successfully.");
                        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
                        int selectedRow = jTable1.getSelectedRow();
                        if (selectedRow != -1) {
                        model.removeRow(selectedRow);
                        }
                        selectedEquipmentID = 0;
                    
                        String checkQuery = "SELECT COUNT(*) AS count FROM EQUIPMENT;";
                        ResultSet rs = stmt.executeQuery(checkQuery);
                        if (rs.next() && rs.getInt("count") == 0) {
                            stmt.executeUpdate("DELETE FROM sqlite_sequence WHERE name='EQUIPMENT';");
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "ERROR: Record not found.");
                    }
                    conn.close();
                    update();
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "ERROR: " + e.getMessage());
                }
            }
        }
    }
    
    public void exportChoice() {
        DatabaseExporter exporter = new DatabaseExporter();
        String selectedFormat = (String) jComboBox2.getSelectedItem();

        if ("Excel".equals(selectedFormat)) {
            exporter.exportAllEquipmentsToCSV();
        } else if ("CSV".equals(selectedFormat)) {
            exporter.exportAllEquipmentsToExcel();
        } else {
            JOptionPane.showMessageDialog(this, "Please select a valid export format.");
        }
    }
    
    public void updateFilteredResults() {
    DefaultTableModel tableModel = (DefaultTableModel) jTable1.getModel();
    tableModel.getDataVector().removeAllElements();
    tableModel.fireTableDataChanged();

    String selectedType = String.valueOf(typeBox.getSelectedItem());
    String selectedCondition = String.valueOf(conditionBox.getSelectedItem());
    String selectedLocation = String.valueOf(locationBox.getSelectedItem());

    try {
        DatabaseConnection connect = DatabaseConnection.getInstance();
        Connection conn = connect.getConnection();
        Statement stmt = conn.createStatement();

        // Construct the SQL query with filters
        String query = "SELECT * FROM EQUIPMENT WHERE 1=1";
        if (!"All".equals(selectedType)) {
            query += " AND type = '" + selectedType + "'";
        }
        if (!"All".equals(selectedCondition)) {
            query += " AND condition = '" + selectedCondition + "'";
        }
        if (!"All".equals(selectedLocation)) {
            query += " AND location = '" + selectedLocation + "'";
        }

        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            int equipmentID = rs.getInt("equipmentID");
            String name = rs.getString("name");
            String type = rs.getString("type");
            String condition = rs.getString("condition");
            String location = rs.getString("location");
            int quantity = rs.getInt("quantity");

            String[] rowData = {
                String.valueOf(equipmentID), name, type, condition, location, String.valueOf(quantity)
            };
            tableModel.addRow(rowData);
        }
        conn.close();
    } catch (SQLException e) {
        System.out.println("Error: " + e.getMessage());
    }
}
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel7 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jSeparator4 = new javax.swing.JSeparator();
        jPanel5 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        nameText = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        typeBox = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        locationBox = new javax.swing.JComboBox<>();
        jPanel6 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        conditionBox = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        quantityText = new javax.swing.JTextField();
        jPanel7 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jComboBox2 = new javax.swing.JComboBox<>();
        jButton8 = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(128, 0, 0));
        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel3.setBackground(new java.awt.Color(128, 0, 0));

        jSeparator2.setForeground(new java.awt.Color(248, 246, 240));

        jLabel7.setFont(new java.awt.Font("Segoe UI Black", 1, 48)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(248, 246, 240));
        jLabel7.setText("MANAGE EQUIPMENTS");

        jSeparator3.setForeground(new java.awt.Color(248, 246, 240));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(133, Short.MAX_VALUE)
                .addComponent(jLabel7)
                .addGap(159, 159, 159))
            .addComponent(jSeparator2)
            .addComponent(jSeparator3)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel1.add(jPanel3, java.awt.BorderLayout.NORTH);

        jPanel4.setBackground(new java.awt.Color(128, 0, 0));

        jTable1.setBackground(new java.awt.Color(248, 246, 240));
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Name", "Type", "Condition", "Location", "Quantity"
            }
        ));
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);
        if (jTable1.getColumnModel().getColumnCount() > 0) {
            jTable1.getColumnModel().getColumn(1).setResizable(false);
            jTable1.getColumnModel().getColumn(3).setResizable(false);
        }

        jSeparator4.setForeground(new java.awt.Color(248, 246, 240));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator4, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 854, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 252, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel1.add(jPanel4, java.awt.BorderLayout.SOUTH);

        jPanel5.setBackground(new java.awt.Color(128, 0, 0));

        jPanel2.setBackground(new java.awt.Color(128, 0, 0));
        jPanel2.setPreferredSize(new java.awt.Dimension(593, 34));
        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 5));

        jLabel2.setForeground(new java.awt.Color(248, 246, 240));
        jLabel2.setText("Name:");
        jPanel2.add(jLabel2);
        jPanel2.add(nameText);

        jLabel3.setForeground(new java.awt.Color(248, 246, 240));
        jLabel3.setText("Type:");
        jPanel2.add(jLabel3);

        typeBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Air Conditioner", "Electric Fan", "Personal Computer (PC)", "Television (TV)", "White Board" }));
        typeBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                typeBoxItemStateChanged(evt);
            }
        });
        jPanel2.add(typeBox);

        jLabel5.setForeground(new java.awt.Color(248, 246, 240));
        jLabel5.setText("Location:");
        jPanel2.add(jLabel5);

        locationBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "MKT310 ", "MKT410 ", "MKT501" }));
        locationBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                locationBoxItemStateChanged(evt);
            }
        });
        jPanel2.add(locationBox);

        jPanel6.setBackground(new java.awt.Color(128, 0, 0));
        jPanel6.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 5));

        jLabel4.setForeground(new java.awt.Color(248, 246, 240));
        jLabel4.setText("Condition:");
        jPanel6.add(jLabel4);

        conditionBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "New", "Good", "Needs Repair", "For Replacement", "Lost" }));
        conditionBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                conditionBoxItemStateChanged(evt);
            }
        });
        jPanel6.add(conditionBox);

        jLabel6.setForeground(new java.awt.Color(248, 246, 240));
        jLabel6.setText("Quantity:");
        jPanel6.add(jLabel6);
        jPanel6.add(quantityText);

        jPanel7.setBackground(new java.awt.Color(128, 0, 0));
        jPanel7.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 5));

        jLabel8.setForeground(new java.awt.Color(248, 246, 240));
        jLabel8.setText("Export to:");
        jPanel7.add(jLabel8);

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "CSV", "Excel" }));
        jPanel7.add(jComboBox2);

        jButton8.setBackground(new java.awt.Color(51, 204, 0));
        jButton8.setText("Generate");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });
        jPanel7.add(jButton8);

        jPanel8.setBackground(new java.awt.Color(128, 0, 0));
        jPanel8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 15, 5));

        jButton1.setBackground(new java.awt.Color(248, 246, 240));
        jButton1.setText("Cancel");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel8.add(jButton1);

        jButton4.setBackground(new java.awt.Color(85, 194, 218));
        jButton4.setText("Register");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jPanel8.add(jButton4);

        jButton5.setBackground(new java.awt.Color(246, 114, 128));
        jButton5.setText("Remove");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        jPanel8.add(jButton5);

        jButton6.setBackground(new java.awt.Color(126, 212, 173));
        jButton6.setText("Update");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        jPanel8.add(jButton6);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 866, Short.MAX_VALUE)
            .addComponent(jPanel6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(9, Short.MAX_VALUE))
        );

        jPanel1.add(jPanel5, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        register();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        // TODO add your handling code here:
        updateEquipment();
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
        // TODO add your handling code here:
        int selectedRow = jTable1.getSelectedRow();
        if (selectedRow != -1) { 
            selectedEquipmentID = Integer.parseInt(String.valueOf(jTable1.getValueAt(selectedRow, 0)));
            nameText.setText(String.valueOf(jTable1.getValueAt(selectedRow, 1)));
            typeBox.setSelectedItem(String.valueOf(jTable1.getValueAt(selectedRow, 2)));
            conditionBox.setSelectedItem(String.valueOf(jTable1.getValueAt(selectedRow, 3)));
            locationBox.setSelectedItem(String.valueOf(jTable1.getValueAt(selectedRow, 4)));
            quantityText.setText(String.valueOf(jTable1.getValueAt(selectedRow, 5)));
        } else {
        selectedEquipmentID = 0; 
        nameText.setText("");
        typeBox.setSelectedIndex(0);
        conditionBox.setSelectedIndex(0);
        locationBox.setSelectedIndex(0);
        quantityText.setText("");
        }   
    }//GEN-LAST:event_jTable1MouseClicked

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
        delete();
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        this.dispose();
        new Login().setVisible(true);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        // TODO add your handling code here:
        exportChoice();
    }//GEN-LAST:event_jButton8ActionPerformed

    private void typeBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_typeBoxItemStateChanged
        // TODO add your handling code here:
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            updateFilteredResults();
        }
    }//GEN-LAST:event_typeBoxItemStateChanged

    private void locationBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_locationBoxItemStateChanged
        // TODO add your handling code here:
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            updateFilteredResults();
        }
    }//GEN-LAST:event_locationBoxItemStateChanged

    private void conditionBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_conditionBoxItemStateChanged
        // TODO add your handling code here:
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            updateFilteredResults();
        }
    }//GEN-LAST:event_conditionBoxItemStateChanged

    /**
     * @param args the command line arguments
     */
    private void formWindowClosed(java.awt.event.WindowEvent evt) {                                  
        // TODO add your handling code here:
        new Login().setVisible(true);
    } 
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> conditionBox;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton8;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JTable jTable1;
    private javax.swing.JComboBox<String> locationBox;
    private javax.swing.JTextField nameText;
    private javax.swing.JTextField quantityText;
    private javax.swing.JComboBox<String> typeBox;
    // End of variables declaration//GEN-END:variables
}
