package com.mycompany.rangematrix;

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

    private final JLabel delegate = new JLabel();
    private final Border cellBorder = new MatteBorder(0, 0, 1, 1, Color.GRAY);
    private final ImageIcon collapsedIcon = new ImageIcon("src\\res\\icons\\collapsed_icon2.png");
    private final ImageIcon expandedIcon = new ImageIcon("src\\res\\icons\\expanded_icon2.png");
    
    public DefaultRangeMatrixRenderer() {
        delegate.setOpaque(true);
    }

    @Override
    public JLabel getColumnRendererComponent(Object column, String columnName, boolean isCollapsed, boolean isGroup) {
        delegate.setText(columnName);
        delegate.setFont(delegate.getFont().deriveFont(Font.BOLD));
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
    public JLabel getRowRendererComponent(Object row, String rowName) {
        delegate.setText(rowName);
        delegate.setFont(delegate.getFont().deriveFont(Font.BOLD));
        delegate.setHorizontalAlignment(JLabel.CENTER);
        delegate.setBackground(javax.swing.UIManager.getDefaults().getColor("TableHeader.background"));
        delegate.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        delegate.setIcon(null);
        return delegate;
    }

    @Override
    public JLabel getCellRendererComponent(int column, int row, String value, boolean isLeading) {
        if (isLeading) {
            delegate.setText("<...>");
            delegate.setFont(delegate.getFont().deriveFont(Font.BOLD));
        } else {
            delegate.setText(value);
            delegate.setFont(delegate.getFont().deriveFont(Font.PLAIN));
        }
        
        delegate.setHorizontalAlignment(JLabel.CENTER);
        delegate.setBackground(javax.swing.UIManager.getDefaults().getColor("Table.background"));
        delegate.setBorder(cellBorder);
        delegate.setIcon(null);
        return delegate;
    }

}
