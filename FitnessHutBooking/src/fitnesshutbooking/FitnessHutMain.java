/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fitnesshutbooking;

/**
 *
 * @author Rui Trigo
 */
public class FitnessHutMain {
    
    public static void main(String[] args) throws Exception {
    
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FitnessHutBookingGUI().setVisible(true);
            }
        });
    }
    
}
