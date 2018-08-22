/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.rangematrix;

import java.awt.BorderLayout;
import java.awt.Container;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

/**
 *
 * @author daniil_pozdeev
 */
public class RangeMatrixApp extends JFrame {
    
    public RangeMatrixApp() {
        super("Матрица ранжирования");
        RangeMatrixModel model = new RangeMatrixModelImpl();
        RangeMatrix rangeMatrix = new RangeMatrix(model);
        //RangeMatrixColumnHeader col = new RangeMatrixColumnHeader(model);
        //RangeMatrixRowHeader row = new RangeMatrixRowHeader(model);
        
        JScrollPane rmScrollPane = new JScrollPane(rangeMatrix);
        //rmScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        //rmScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        Container c = this.getContentPane();
        c.setLayout(new BorderLayout());
        //c.add(col, BorderLayout.NORTH);
        //c.add(row, BorderLayout.WEST);
        c.add(rmScrollPane, BorderLayout.CENTER);
        pack();
    }
    
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//                createAndShowGUI();
//            }
//        });
//    }
//    
//    private static void createAndShowGUI() {
//        System.out.println("Created GUI on EDT? "+
//                SwingUtilities.isEventDispatchThread());
//        JFrame f = new JFrame("Матрица ранжирования");
//        
//        RangeMatrixModel model = new RangeMatrixModelImpl();
//        RangeMatrixColumnHeader col = new RangeMatrixColumnHeader(model);
//        
//        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        
//        Container c = f.getContentPane();
//        c.setLayout(new BorderLayout());
//        c.add(col, BorderLayout.NORTH);
//        
//        f.pack();
//        f.setVisible(true);
//    }
//    
}
