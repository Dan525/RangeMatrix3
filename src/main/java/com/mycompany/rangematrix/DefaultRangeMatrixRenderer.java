package com.mycompany.rangematrix;

import java.awt.Color;
import java.awt.Font;
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
    
    public DefaultRangeMatrixRenderer() {
        delegate.setOpaque(true);
    }

    @Override
    public JLabel getColumnRendererComponent(Object column, String columnName) {
        delegate.setText(columnName);
        delegate.setHorizontalAlignment(JLabel.CENTER);
        delegate.setBackground(javax.swing.UIManager.getDefaults().getColor("TableHeader.background"));
        delegate.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        return delegate;
    }

    @Override
    public JLabel getRowRendererComponent(Object row, String rowName) {
        delegate.setText(rowName);
        delegate.setHorizontalAlignment(JLabel.CENTER);
        delegate.setBackground(javax.swing.UIManager.getDefaults().getColor("TableHeader.background"));
        delegate.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        return delegate;
    }

    @Override
    public JLabel getCellRendererComponent(int row, int column, String value) {
        delegate.setText(value);
        delegate.setFont(delegate.getFont().deriveFont(Font.PLAIN));
        delegate.setHorizontalAlignment(JLabel.CENTER);
        delegate.setBackground(javax.swing.UIManager.getDefaults().getColor("Table.background"));
        delegate.setBorder(cellBorder);
        return delegate;
    }

}
