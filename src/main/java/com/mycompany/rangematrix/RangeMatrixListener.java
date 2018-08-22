/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.rangematrix;

import java.util.EventListener;
import javax.swing.event.TreeModelEvent;

/**
 *
 * @author daniil_pozdeev
 */
public interface RangeMatrixListener extends EventListener {
    
    void columnHeaderChanged(RangeMatrixEvent e);
    
    void rowHeaderChanged(RangeMatrixEvent e);
    
    void valueChanged(RangeMatrixEvent e);
}
