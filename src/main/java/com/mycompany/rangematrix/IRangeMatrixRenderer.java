package com.mycompany.rangematrix;

import javax.swing.JLabel;

/**
 *
 * @author oleg_kirienko
 */
public interface IRangeMatrixRenderer {
    
    public JLabel getColumnRendererComponent(Object group);
    
    public JLabel getRowRendererComponent(Object group);
    
    public JLabel getCellRendererComponent(Object row, Object column);
    
}
