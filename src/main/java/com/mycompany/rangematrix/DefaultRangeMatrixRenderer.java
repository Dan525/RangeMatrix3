package com.mycompany.rangematrix;

import javax.swing.JLabel;

/**
 *
 * @author oleg_kirienko
 */
public class DefaultRangeMatrixRenderer implements IRangeMatrixRenderer {
    
    private JLabel delegate = new JLabel();

    @Override
    public JLabel getColumnRendererComponent(Object group) {
        return delegate;
    }

    @Override
    public JLabel getRowRendererComponent(Object group) {
        return delegate;
    }

    @Override
    public JLabel getCellRendererComponent(Object row, Object column) {
        return delegate;
    }
    
}
