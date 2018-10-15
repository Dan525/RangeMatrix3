

import javax.swing.JLabel;

/**
 *
 * @author daniil_pozdeev
 */
public interface IRangeMatrixRenderer {
    
    public JLabel getColumnRendererComponent(RangeMatrixHeaderButton button);//Object column, String columnName, boolean isCollapsed, boolean isGroup
    
    public JLabel getColumnRendererComponent(String text);
    
    public JLabel getRowRendererComponent(RangeMatrixHeaderButton button);//Object row, String rowName, boolean isCollapsed, boolean isGroup
    
    public JLabel getRowRendererComponent(String text);
    
    public JLabel getCellRendererComponent(int row, int column, RangeMatrixTableButton button);//String value, boolean isLeadingByColumn, boolean isLeadingByRow, int notEmptyValueCounter);
    
}
