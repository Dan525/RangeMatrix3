package com.mycompany.rangematrix;

import java.awt.Font;
import javax.swing.JLabel;

/**
 *
 * @author oleg_kirienko
 */
public class DefaultRangeMatrixRenderer implements IRangeMatrixRenderer {

    private final JLabel delegate = new JLabel();

    @Override
    public JLabel getColumnRendererComponent(Object column, String columnName) {
        delegate.setText(columnName);
        delegate.setHorizontalAlignment(JLabel.CENTER);
        return delegate;
    }

    @Override
    public JLabel getRowRendererComponent(Object row, String rowName) {
        delegate.setText(rowName);
        delegate.setHorizontalAlignment(JLabel.CENTER);
        return delegate;
    }

    @Override
    public JLabel getCellRendererComponent(int row, int column, String value) {
        delegate.setText(value);
        delegate.setFont(delegate.getFont().deriveFont(Font.PLAIN));
        delegate.setHorizontalAlignment(JLabel.CENTER);
//        if (row <= 3 && column == 5 || row == 3 && column <= 5) {
//            delegate.setOpaque(true);
//            delegate.setBackground(Color.YELLOW);
//        }
        return delegate;
    }

}
