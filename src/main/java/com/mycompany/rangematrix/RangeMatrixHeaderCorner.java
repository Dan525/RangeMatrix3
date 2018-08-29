/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.rangematrix;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.CellRendererPane;
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
    private IRangeMatrixRenderer renderer;
    private CellRendererPane crp;
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

    public void setModel(RangeMatrixModel model, IRangeMatrixRenderer renderer, CellRendererPane crp) {
        this.model = model;
        this.renderer = renderer;
        this.crp = crp;
    }
    
    public double calculateWidthOfRowByName(int columnIndex) {
        String rowName = "";
        
        if (columnIndex < model.getCornerColumnNames().size()) {
            rowName = model.getCornerColumnNames().get(columnIndex);
        }
        
        JLabel label = renderer.getColumnRendererComponent(null, rowName);
        return label.getPreferredSize().getWidth() + 2 * spaceAroundName;
    }
    
    public ArrayList<Double> fillRowsWidthList() {
        ArrayList<Double> rowsWidthListTemp = new ArrayList<>();
        for (int i = 0; i < rowHeader.getColumnCount(); i++) {
            double rowWidth = calculateWidthOfRowByName(i);
            rowsWidthListTemp.add(rowWidth);
        }
        return rowsWidthListTemp;
    }

    public void calculateWidthOfComponent() {
        width = rowHeader.getWidthOfComponent();
    }

    public double getWidthOfComponent() {
        return width;
    }

    public void calculateHeightOfComponent() {
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
        int columnCount = rowHeader.getColumnCount();
        double cellX = parentCellX;
        double cellY = parentCellY;

        for (int i = 0; i < columnCount; i++) {

            String columnName = "";
            if (i < model.getCornerColumnNames().size()) {
                columnName = model.getCornerColumnNames().get(i);
            }

            double cellWidth = rowHeader.getRowsWidthList().get(i);

            double cellHeight = height;

            Rectangle2D rect = new Rectangle2D.Double(cellX, cellY, cellWidth, cellHeight);
            //g2d.draw(rect);
            
            crp.paintComponent(g2d, renderer.getColumnRendererComponent(null, columnName),
                               this, (int)cellX, (int)cellY, (int)cellWidth, (int)cellHeight);

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
