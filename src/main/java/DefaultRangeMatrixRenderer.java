

import java.awt.Color;
import java.awt.Font;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;

/**
 *
 * @author daniil_pozdeev
 */
public class DefaultRangeMatrixRenderer implements IRangeMatrixRenderer {

    //private final ClassLoader cl = this.getClass().getClassLoader();
    private final JLabel delegate = new JLabel();
    private final Border cellBorder = new MatteBorder(0, 0, 1, 1, Color.GRAY);
//    private final ImageIcon collapsedIcon = new ImageIcon(cl.getResource("collapsed_icon2.png"));
    private final ImageIcon collapsedIcon = new ImageIcon(getClass().getResource("collapsed_icon2.png"));
    private final ImageIcon expandedIcon = new ImageIcon(getClass().getResource("expanded_icon2.png"));
    
    public DefaultRangeMatrixRenderer() {
        delegate.setOpaque(true);
    }

    @Override
    public JLabel getColumnRendererComponent(RangeMatrixHeaderButton button) {
        Object column = button.getButtonObject();
        String columnName = button.getButtonName();
        String fullColumnName = button.getButtonFullName();
        boolean isCollapsed = button.isCollapsed();
        boolean isGroup = button.isGroup();
        
        
        
        delegate.setFont(delegate.getFont().deriveFont(Font.BOLD, 12));
        delegate.setHorizontalAlignment(JLabel.CENTER);
        delegate.setBackground(javax.swing.UIManager.getDefaults().getColor("TableHeader.background"));
        delegate.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        if (isCollapsed && isGroup) {
            delegate.setIcon(collapsedIcon);
            delegate.setText(columnName);
        } else if (!isCollapsed && isGroup) {
            delegate.setIcon(expandedIcon);
            if (button.getButtonFullName() != null) {
                delegate.setText(fullColumnName);
            } else {
                delegate.setText(columnName);
            }
            
        } else {
            delegate.setIcon(null);
            delegate.setText(columnName);
        }
        return delegate;
    }
    
    @Override
    public JLabel getColumnRendererComponent(String text) {
        delegate.setText(text);
        delegate.setFont(delegate.getFont().deriveFont(Font.BOLD, 12));
        return delegate;
    }

    @Override
    public JLabel getRowRendererComponent(RangeMatrixHeaderButton button) {
        Object row = button.getButtonObject();
        String rowName = button.getButtonName();
        boolean isCollapsed = button.isCollapsed();
        boolean isGroup = button.isGroup();
        
        delegate.setText(rowName);
        delegate.setFont(delegate.getFont().deriveFont(Font.BOLD, 12));
        delegate.setHorizontalAlignment(JLabel.CENTER);
        delegate.setBackground(javax.swing.UIManager.getDefaults().getColor("TableHeader.background"));
        delegate.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        if (isCollapsed && isGroup) {
            delegate.setIcon(collapsedIcon);
        } else if (!isCollapsed && isGroup) {
            delegate.setIcon(expandedIcon);
        } else {
            delegate.setIcon(null);
        }
        return delegate;
    }
    
    @Override
    public JLabel getRowRendererComponent(String text) {
        delegate.setText(text);
        delegate.setFont(delegate.getFont().deriveFont(Font.BOLD, 12));
        return delegate;
    }

    @Override
    public JLabel getCellRendererComponent(int column, int row, RangeMatrixTableButton button) {//String value, boolean isLeadingByColumn, boolean isLeadingByRow, int notEmptyInRowCounter) {
        
        boolean isLeadingByColumn = button.isLeadingByColumn();
        boolean isLeadingByRow = button.isLeadingByRow();
        boolean hasAnyValuesInColumn;
        boolean hasAnyValuesInRow;
        String value = button.getButtonName();
        
        delegate.setFont(delegate.getFont().deriveFont(Font.BOLD, 12));
        
        if (isLeadingByColumn && !isLeadingByRow) {
            hasAnyValuesInRow = button.getNotEmptyInRowStack().peek();
            if (hasAnyValuesInRow) {
                delegate.setText("●");
            } else {
                delegate.setText("○");
            }
            
        } else if (!isLeadingByColumn && isLeadingByRow) {
            hasAnyValuesInColumn = button.getNotEmptyInColumnStack().peek();
            if (hasAnyValuesInColumn) {
                delegate.setText("●");
            } else {
                delegate.setText("○");
            }
            
        } else if (isLeadingByColumn && isLeadingByRow) {
            hasAnyValuesInColumn = button.getNotEmptyInColumnStack().peek();
            hasAnyValuesInRow = button.getNotEmptyInRowStack().peek();
            if (hasAnyValuesInColumn || hasAnyValuesInRow) {
                delegate.setText("●");
            } else {
                delegate.setText("○");
            }
        } else {
            
            delegate.setText(value);
            delegate.setFont(delegate.getFont().deriveFont(Font.PLAIN, 12));
        }
        
        if (!button.isEntered()) {
            if (button.getCurrentRow() % 2 == 0) {
                delegate.setBackground(javax.swing.UIManager.getDefaults().getColor("Table.background"));
            } else {
                delegate.setBackground(new Color(245,245,245));
            }
            
        } else {
            //подсветка ячеек
            delegate.setBackground(new Color(214, 230, 245));//javax.swing.UIManager.getDefaults().getColor("Table.selectionBackground")
        }
        
        delegate.setHorizontalAlignment(JLabel.CENTER);
        delegate.setBorder(cellBorder);
        delegate.setIcon(null);
        return delegate;
    }

}
