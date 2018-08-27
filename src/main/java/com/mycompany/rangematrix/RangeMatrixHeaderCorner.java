/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.rangematrix;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.JLabel;

/**
 *
 * @author daniil_pozdeev
 */
public class RangeMatrixHeaderCorner extends JComponent {

    private RangeMatrixModel model;
    private final RangeMatrixColumnHeader columnHeader;
    private final RangeMatrixRowHeader rowHeader;
    private BufferedImage buffer;
    private double width;
    private double height;
    private final int spaceAroundName = 4;

    public RangeMatrixHeaderCorner(RangeMatrixColumnHeader columnHeader, RangeMatrixRowHeader rowHeader) {
        this.columnHeader = columnHeader;
        this.rowHeader = rowHeader;
    }

    public RangeMatrixModel getModel() {
        return model;
    }

    public void setModel(RangeMatrixModel model) {
        this.model = model;
    }
    
    public double getWidthOfRowByName(FontMetrics fm, int columnIndex) {
        String rowName = model.getCornerColumnNames().get(columnIndex);
        return fm.stringWidth(rowName) + 2 * spaceAroundName;
    }
    
    public ArrayList<Double> calculateRowsWidthList(FontMetrics fm) {
        ArrayList<Double> rowsWidthListTemp = new ArrayList<>();
        for (int i = 0; i < rowHeader.getColumnCount(); i++) {
            double rowWidth = getWidthOfRowByName(fm, i);
            rowsWidthListTemp.add(rowWidth);
        }
        return rowsWidthListTemp;
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

        Graphics2D g2d = buffer.createGraphics();
        g2d.setColor(Color.BLACK);

        drawCorner(g2d, 0, 0);
    }

    public void drawCorner(Graphics2D g2d, double parentCellX, double parentCellY) {
        FontMetrics fm = g2d.getFontMetrics();    
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
            
            JLabel label = new JLabel(columnName);
            label.setBounds((int) cellX, (int) cellY, (int) cellWidth, (int) cellHeight);
            label.setHorizontalAlignment(JLabel.CENTER);
            this.add(label);
            
//            g2d.drawString(columnName,
//                    (float) (cellX + cellWidth / 2 - fm.stringWidth(columnName) / 2),
//                    (float) (cellY + cellHeight / 2 - fm.getHeight() / 2 + fm.getAscent()));

            cellX += cellWidth;
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (buffer == null) {
            rebuildBuffer();
        }
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(buffer, 0, 0, this);
    }
}
