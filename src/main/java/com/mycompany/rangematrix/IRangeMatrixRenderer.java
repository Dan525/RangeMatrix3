package com.mycompany.rangematrix;

import javax.swing.JLabel;

/**
 *
 * @author daniil_pozdeev
 */
public interface IRangeMatrixRenderer {
    
    public JLabel getColumnRendererComponent(Object column, String columnName, boolean isCollapsed, boolean isGroup);//
    
    public JLabel getRowRendererComponent(Object row, String rowName, boolean isCollapsed, boolean isGroup);
    
    public JLabel getCellRendererComponent(int row, int column, RangeMatrixTableButton button);//String value, boolean isLeadingByColumn, boolean isLeadingByRow, int notEmptyValueCounter);
    
}
