/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.rangematrix;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.event.ComponentAdapter;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;

/**
 *
 * @author daniil_pozdeev
 */
public class RangeMatrix extends JComponent {

    private final RangeMatrixModel model;
    private Graphics2D g2d;
    transient protected RangeMatrixColumnHeader columnHeader;
    protected RangeMatrixRowHeader rowHeader;
    //private final RangeMatrixHeaderCorner headerCorner;
    float width;
    float height;
    private BufferedImage buffer;

    public RangeMatrix(RangeMatrixModel model) {
        this.model = model;
        this.columnHeader = new RangeMatrixColumnHeader(model);
        this.rowHeader = new RangeMatrixRowHeader(model, columnHeader);
        //this.headerCorner = new RangeMatrixHeaderCorner(model);
        addComponentListener(new RangeMatrixListenerImpl());
        model.addRangeMatrixListener(new RangeMatrixHandler());
    }

    public RangeMatrixModel getModel() {
        return model;
    }

    public void drawVerticalLines() {
        ArrayList<Float> cellX = columnHeader.getCellXList();
        for (int i = columnHeader.getStaticColumnCount() + 1; i < cellX.size(); i++) {
            float x = cellX.get(i) - columnHeader.getStaticColumnWidth() - 1;
            Shape l = new Line2D.Double(x, 0, x, getHeightOfComponent());
            g2d.draw(l);
        }
    }

    public void drawHorizontalLines() {
        ArrayList<Float> cellY = rowHeader.getCellYList();
        for (int i = 0; i < cellY.size(); i++) {
            Shape l = new Line2D.Double(0, cellY.get(i) + rowHeader.getCellHeight(1), getWidthOfComponent(), cellY.get(i) + rowHeader.getCellHeight(1));
            g2d.draw(l);
        }
    }

    public float getHeightOfComponent() {
        return rowHeader.getHeightOfComponent();
    }

    public float getWidthOfComponent() {
        return columnHeader.getWidthOfComponent() - columnHeader.getStaticColumnWidth() - 1;
    }

    public Image getCornerImage(Image image) {
        ArrayList<Float> cellX = columnHeader.getCellXList();
        float x = cellX.get(columnHeader.getStaticColumnCount());
        Image newImage = buffer.getSubimage(0, 0, (int) x, buffer.getHeight());
        return newImage;
    }

    public void addNotify() {
        super.addNotify();
        configureEnclosingScrollPane();
    }

    protected void configureEnclosingScrollPane() {
        Container parent = SwingUtilities.getUnwrappedParent(this);
        if (parent instanceof JViewport) {
            JViewport port = (JViewport) parent;
            Container gp = port.getParent();
            if (gp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) gp;
                // Make certain we are the viewPort's view and not, for
                // example, the rowHeaderView of the scrollPane -
                // an implementor of fixed columns might do this.
                JViewport viewport = scrollPane.getViewport();
                if (viewport == null
                        || SwingUtilities.getUnwrappedView(viewport) != this) {
                    return;
                }
                scrollPane.setColumnHeaderView(columnHeader);
                scrollPane.setRowHeaderView(rowHeader);
                scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, new JComponent() {
                    @Override
                    public void paintComponent(Graphics g) {
                        BufferedImage cornerBuffer = columnHeader.getBuffer();
                        super.paintComponent(g);
                        g2d = (Graphics2D) g;
                        float x = columnHeader.getCellXList().get(columnHeader.getStaticColumnCount()) + 1;
                        Image newImage = cornerBuffer.getSubimage(0, 0, (int) x, cornerBuffer.getHeight());
                        g2d.drawImage(newImage, 0, 0, this);
                    }
                });
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        g2d = (Graphics2D) getGraphics();
        int width = (int) getWidthOfComponent();
        int height = (int) getHeightOfComponent();

        return new Dimension(width, height);
    }

    private void rebuildBuffer() {
        int w = (int) getWidthOfComponent();
        int h = (int) getHeightOfComponent();
        buffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        g2d = buffer.createGraphics();
        g2d.setColor(Color.GRAY);

        drawVerticalLines();
        drawHorizontalLines();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (buffer == null) {
            rebuildBuffer();
        }
        g2d = (Graphics2D) g;
        g2d.drawImage(buffer, 0, 0, this);
    }

    private class RangeMatrixListenerImpl extends ComponentAdapter {

    }

    protected class RangeMatrixHandler implements RangeMatrixListener {

        @Override
        public void columnHeaderChanged(RangeMatrixEvent e) {
            columnHeader.rebuildBuffer();
        }

        @Override
        public void rowHeaderChanged(RangeMatrixEvent e) {
            rowHeader.rebuildBuffer();
        }

        @Override
        public void valueChanged(RangeMatrixEvent e) {
            rebuildBuffer();
        }

    }
}
