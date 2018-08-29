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
import java.util.Collections;
import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JLabel;

/**
 *
 * @author daniil_pozdeev
 */
public class RangeMatrixColumnHeader extends JComponent {

    private RangeMatrixModel model;
    private IRangeMatrixRenderer renderer;
    private CellRendererPane crp;
    private double spaceAroundName = 4;
    private ArrayList<Double> cellXList;
    private ArrayList<Double> cellWidthList;
    private int rowCount;
    private BufferedImage buffer;
    private double width;
    private double height;
    private double minimalCellHeight;

    public RangeMatrixModel getModel() {
        return model;
    }

    public void setModel(RangeMatrixModel model, IRangeMatrixRenderer renderer, CellRendererPane crp) {
        this.model = model;
        this.renderer = renderer;
        this.crp = crp;
        
        cellXList = new ArrayList<>();
        cellWidthList = new ArrayList<>();
        
        calculateMinimalCellHeight();
        fillCellCoordinateList(null, 0, 0);
        calculateRowCount(null, new ArrayList<>(), 1);
    }

    public void setSpaceAroundName(int newSpace) {
        this.spaceAroundName = newSpace;
    }

    public double calculateCellHeight(int heightMultiplier) {
        return minimalCellHeight * heightMultiplier;
    }

    public double calculateWidthByColumnName(Object column) {

        String columnName = model.getColumnGroupName(column);
        JLabel label = renderer.getColumnRendererComponent(column, columnName);
        double columnWidth = label.getPreferredSize().getWidth() + 2 * spaceAroundName;
        if (columnWidth > minimalCellHeight) {
            return columnWidth;
        } else {
            return minimalCellHeight;
        }
    }

    public double calculateWidthOfColumn(Object column) {
        double columnWidth = 0;
        double ownColumnWidth = calculateWidthByColumnName(column);

        ArrayList<Object> leafColumnList = fillLeafColumnList(column, new ArrayList<>());
        if (leafColumnList.isEmpty()) {
            return ownColumnWidth;
        }
        for (Object leafColumn : leafColumnList) {
            double leafColumnWidth = calculateWidthByColumnName(leafColumn);
            columnWidth += leafColumnWidth;
        }
        if (columnWidth > ownColumnWidth) {
            return columnWidth;
        } else {
            return ownColumnWidth;
        }
    }

    public void calculateRowCount(Object parentColumn, ArrayList<Integer> maxRowIndexList, int maxRowIndex) {
        int columnCount = model.getColumnGroupCount(parentColumn);

        for (int i = 0; i < columnCount; i++) {
            Object child = model.getColumnGroup(parentColumn, i);
            boolean isGroup = model.isColumnGroup(child);
            if (isGroup) {
                maxRowIndex++;
                calculateRowCount(child, maxRowIndexList, maxRowIndex);
                maxRowIndex--;
            }
            maxRowIndexList.add(maxRowIndex);
        }
        rowCount = Collections.max(maxRowIndexList);
    }

    public int getRowCount() {
        return rowCount;
    }

    public int calculateHeightMultiplier(Object parentColumn, boolean isGroup, int rowIndex) {
        if (!isGroup) {
            return (rowCount - rowIndex) + 1;
        } else {
            return 1;
        }
    }

    public void fillCellCoordinateList(Object parentColumn, double parentCellX, int rowCounter) {

        int columnCount = model.getColumnGroupCount(parentColumn);
        double cellX = parentCellX;

        for (int i = 0; i < columnCount; i++) {
            Object child = model.getColumnGroup(parentColumn, i);

            double cellWidth = calculateWidthOfColumn(child);

            boolean isGroup = model.isColumnGroup(child);

            if (isGroup) {
                rowCounter++;
                fillCellCoordinateList(child, cellX, rowCounter);
                rowCounter--;
            } else {
                cellXList.add(cellX);
                cellWidthList.add(cellWidth);
            }
            cellX += cellWidth;
        }
    }

    public ArrayList<Double> getCellXList() {
        return cellXList;
    }

    public ArrayList<Double> getCellWidthList() {
        return cellWidthList;
    }

    public void calculateMinimalCellHeight() {
        JLabel label = renderer.getColumnRendererComponent(null, " ");
        minimalCellHeight = label.getPreferredSize().getHeight() + 2 * spaceAroundName;
    }

    public double getMinimalCellHeight() {
        return minimalCellHeight;
    }

    public void calculateWidthOfComponent() {
        width = calculateWidthOfColumn(null);
    }

    public double getWidthOfComponent() {
        return width;
    }

    public void calculateHeightOfComponent() {
        height = (minimalCellHeight * rowCount);
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

    public ArrayList<Object> fillLeafColumnList(Object parentColumn, ArrayList<Object> leafColumnList) {
        int columnCount = model.getColumnGroupCount(parentColumn);

        for (int i = 0; i < columnCount; i++) {
            Object child = model.getColumnGroup(parentColumn, i);
            boolean isGroup = model.isColumnGroup(child);
            if (isGroup) {
                fillLeafColumnList(child, leafColumnList);
            } else {
                leafColumnList.add(child);
            }
        }
        return leafColumnList;
    }
    
    public void drawColumns(Graphics2D g2d, Object parentColumn, double parentCellX, double parentCellY, int rowCounter) {

        int columnCount = model.getColumnGroupCount(parentColumn);
        double cellX = parentCellX;
        double cellY = parentCellY;

        for (int i = 0; i < columnCount; i++) {
            Object child = model.getColumnGroup(parentColumn, i);
            String columnName = model.getColumnGroupName(child);

            double cellWidth = calculateWidthOfColumn(child);

            boolean isGroup = model.isColumnGroup(child);

            int heightMultiplier = calculateHeightMultiplier(parentColumn, isGroup, rowCounter);

            double cellHeight = calculateCellHeight(heightMultiplier);

            Rectangle2D rect = new Rectangle2D.Double(cellX, cellY, cellWidth, cellHeight);
            //g2d.draw(rect);
            
            crp.paintComponent(g2d, renderer.getColumnRendererComponent(child, columnName),
                               this, (int)cellX, (int)cellY, (int)cellWidth, (int)cellHeight);

            if (isGroup) {
                rowCounter++;
                cellY += cellHeight;
                drawColumns(g2d, child, cellX, cellY, rowCounter);
                rowCounter--;
                cellY -= cellHeight;
            }
            cellX += cellWidth;
        }
    }

    void rebuildBuffer() {
        buffer = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = buffer.createGraphics();
        g2d.setColor(Color.BLACK);

        drawColumns(g2d, null, 0, 0, 1);
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
