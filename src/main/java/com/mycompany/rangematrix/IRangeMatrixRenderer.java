package com.mycompany.rangematrix;

import javax.swing.JLabel;

/**
 *
 * @author oleg_kirienko
 */
public interface IRangeMatrixRenderer {
    
    public JLabel getColumnRendererComponent(Object column, String columnName);
    
    public JLabel getRowRendererComponent(Object row, String rowName);
    
    public JLabel getCellRendererComponent(int row, int column, String value);
    
}
