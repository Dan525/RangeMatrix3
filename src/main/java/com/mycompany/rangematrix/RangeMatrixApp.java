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
        JScrollPane rmScrollPane = new JScrollPane(rangeMatrix);
        
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        Container c = this.getContentPane();
        c.setLayout(new BorderLayout());
        c.add(rmScrollPane, BorderLayout.CENTER);
        pack();
    }
}
