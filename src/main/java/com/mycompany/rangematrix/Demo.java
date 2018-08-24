/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.rangematrix;

import java.awt.Dimension;
import java.awt.EventQueue;
import javax.swing.JLabel;

/**
 *
 * @author daniil_pozdeev
 */
public class Demo {
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {

        @Override
        public void run() {
            new RangeMatrixApp();
        }
    });
    }
}
