/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.schoolequipmentsystemv2;

import APP.*;
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
/**
 *
 * @author Amar Pajarito
 */
public class SchoolEquipmentSystemv2 {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Error: " + e.getMessage());
        }

        java.awt.EventQueue.invokeLater(() -> {
            new Login().setVisible(true); 
        });
    }
}
