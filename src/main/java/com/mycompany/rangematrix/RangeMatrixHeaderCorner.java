/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.rangematrix;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;

/**
 *
 * @author daniil_pozdeev
 */
public class RangeMatrixHeaderCorner extends JComponent {

    private RangeMatrixModel model;
    private final RangeMatrixColumnHeader columnHeader;
    private final RangeMatrixRowHeader rowHeader;
    private Graphics2D g2d;
    private Font font;
    private FontMetrics fm;
    private BufferedImage buffer;
    private double width;
    private double height;

    public RangeMatrixHeaderCorner(RangeMatrixColumnHeader columnHeader, RangeMatrixRowHeader rowHeader) {
        this.columnHeader = columnHeader;
        this.rowHeader = rowHeader;
    }

    public RangeMatrixModel getModel() {
        return model;
    }

    public void setModel(RangeMatrixModel model) {
        doSetModel(model);
    }

    private void doSetModel(RangeMatrixModel model) {
        this.model = model;
        font = new Font("Arial Narrow", Font.PLAIN, 12);
        fm = new Canvas().getFontMetrics(font);

        setHeightOfComponent();
        setWidthOfComponent();
    }

    public void setWidthOfComponent() {
        width = rowHeader.getWidthOfComponent();
    }

    public double getWidthOfComponent() {
        return width;
    }

    public void setHeightOfComponent() {
        height = columnHeader.getHeightOfComponent();
    }

    public double getHeightOfComponent() {
        return height;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = new Dimension();
        d.setSize(width, height);
        return d;
    }

    void rebuildBuffer() {
        buffer = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB);

        g2d = buffer.createGraphics();
        g2d.setFont(font);
        g2d.setColor(Color.BLACK);

        drawCorner(0, 0);
    }

    public void drawCorner(double parentCellX, double parentCellY) {

        int columnCount = rowHeader.getColumnCount();
        double cellX = parentCellX;
        double cellY = parentCellY;

        for (int i = 0; i < columnCount; i++) {

            String columnName = "";
            if (i < model.getCornerColumnNames().size()) {
                columnName = model.getCornerColumnNames().get(i);
            }

            double cellWidth = rowHeader.getRowsWidthList().get(i);

            double cellHeight = columnHeader.getHeightOfComponent() - 1;

            Rectangle2D rect = new Rectangle2D.Double(cellX, cellY, cellWidth, cellHeight);
            g2d.draw(rect);
            g2d.drawString(columnName,
                    (float) (cellX + cellWidth / 2 - fm.stringWidth(columnName) / 2),
                    (float) (cellY + cellHeight / 2 - fm.getHeight() / 2 + fm.getAscent()));

            cellX += cellWidth;
        }
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
}
