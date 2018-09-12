package com.mycompany.rangematrix;

import javax.swing.JLabel;

/**
 *
 * @author daniil_pozdeev
 */
public interface IRangeMatrixRenderer {
    
    public JLabel getColumnRendererComponent(Object column, String columnName, boolean isCollapsed, boolean isGroup);//
    
    public JLabel getRowRendererComponent(Object row, String rowName);
    
    public JLabel getCellRendererComponent(int row, int column, String value, boolean isLeading);
    
}
