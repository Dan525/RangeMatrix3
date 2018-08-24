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
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.JComponent;

/**
 *
 * @author daniil_pozdeev
 */
public class RangeMatrixRowHeader extends JComponent {

    private RangeMatrixModel model;
    private Graphics2D g2d;
    private Font font;
    private ArrayList<Double> cellYList;
    private double minimalCellHeight;
    private int columnCount;
    private ArrayList<Double> rowsWidthList;
    private double spaceAroundName = 4;
    private BufferedImage buffer;
    private double width;
    private double height;
    private FontMetrics fm;
    
    public void setModel(RangeMatrixModel model) {
        doSetModel(model);
    }
    
    private void doSetModel(RangeMatrixModel model) {
        this.model = model;
        font = new Font("Arial Narrow", Font.PLAIN, 12);
        fm = new Canvas().getFontMetrics(font);
        
        cellYList = new ArrayList<>();
        
        setMinimalCellHeight();
        fillCellCoordinateList(null, 0, 0);
        setColumnCount(null, new ArrayList<>(), 1);
        
        rowsWidthList = new ArrayList<>();
        fillRowsWidthList();
        
        setHeightOfComponent();
        setWidthOfComponent();
    }

    public RangeMatrixModel getModel() {
        return model;
    }

    public ArrayList<Double> getRowsWidthList() {
        return rowsWidthList;
    }

    public void setSpaceAroundName(int newSpace) {
        this.spaceAroundName = newSpace;
    }
    
    public void setMinimalCellHeight() {
        minimalCellHeight =  fm.getHeight() + 2 * spaceAroundName;
    }

    public double getMinimalCellHeight() {
        return minimalCellHeight;
    }

    public double getWidthOfRowByName(Object row) {
        String rowName = model.getRowGroupName(row);
        return fm.stringWidth(rowName) + 2 * spaceAroundName;
    }

    public double getMaxRowWidthInColumn(int indexOfColumn, int columnCounter, ArrayList<Double> rowsOfColumnList) {
        int rowCount = model.getRowGroupCount(null);

        for (int i = 0; i < rowCount; i++) {
            Object child = model.getRowGroup(null, i);

            boolean isGroup = model.isColumnGroup(child);

            if (isGroup && columnCounter < indexOfColumn) {
                columnCounter++;
                getMaxRowWidthInColumn(indexOfColumn, columnCounter, rowsOfColumnList);
                columnCounter--;

            } else if (columnCounter == indexOfColumn) {
                rowsOfColumnList.add(getWidthOfRowByName(child));
            }
        }
        return Collections.max(rowsOfColumnList) + 60;
    }
    
    public void fillRowsWidthList() {
        for (int i = 0; i < columnCount; i++) {
            double rowWidth = getMaxRowWidthInColumn(i, 0, new ArrayList<>());
            rowsWidthList.add(rowWidth);
        }
    }

    public double getHeightOfRow(Object row) {
        ArrayList<Object> leafRowList = getLeafRows(row, new ArrayList<>());
        
        if (leafRowList.isEmpty()) {
            return minimalCellHeight;
        } else {
            return minimalCellHeight * leafRowList.size();
        }
    }

    public void setColumnCount(Object parentRow, ArrayList<Integer> maxColumnIndexList, int maxColumnIndex) {
        int rowCount = model.getRowGroupCount(parentRow);

        for (int i = 0; i < rowCount; i++) {
            Object child = model.getRowGroup(parentRow, i);
            boolean isGroup = model.isRowGroup(child);
            if (isGroup) {
                maxColumnIndex++;
                setColumnCount(child, maxColumnIndexList, maxColumnIndex);
                maxColumnIndex--;
            }
            maxColumnIndexList.add(maxColumnIndex);
        }
        columnCount = Collections.max(maxColumnIndexList);
    }

    public int getColumnCount() {
        return columnCount;
    }
    
    public void fillCellCoordinateList(Object parentRow, double parentCellY, int columnCounter) {
        
        int rowCount = model.getRowGroupCount(parentRow);
        double cellY = parentCellY;

        for (int i = 0; i < rowCount; i++) {
            Object child = model.getRowGroup(parentRow, i);
            
            boolean isGroup = model.isColumnGroup(child);

            double cellHeight = getHeightOfRow(child);
            
            if (isGroup) {
                columnCounter++;
                fillCellCoordinateList(child, cellY, columnCounter);
                columnCounter--;
            } else {
                cellYList.add(cellY);
            }
            cellY += cellHeight;
        }
    }

    public ArrayList<Double> getCellYList() {
        return cellYList;
    }

    public void setWidthOfComponent() {
        for (double rowWidth : rowsWidthList) {
            width += rowWidth;
        }
        width += 1;
    }

    public double getWidthOfComponent() {
        return width;
    }

    public void setHeightOfComponent() {
        height = getHeightOfRow(null);
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
        buffer = new BufferedImage((int)width, (int)height, BufferedImage.TYPE_INT_ARGB);

        g2d = buffer.createGraphics();
        g2d.setFont(font);
        g2d.setColor(Color.BLACK);

        drawRows(null, 0, -1, 0);
        Shape l = new Line2D.Double(width - 1, 0, width - 1, height);
        g2d.draw(l);
    }
    
    public ArrayList<Object> getLeafRows(Object parentRow, ArrayList<Object> leafRowList) {
        int rowCount = model.getRowGroupCount(parentRow);

        for (int i = 0; i < rowCount; i++) {
            Object child = model.getRowGroup(parentRow, i);
            boolean isGroup = model.isRowGroup(child);
            if (isGroup) {
                getLeafRows(child, leafRowList);
            } else {
                leafRowList.add(child);
            }
        }
        return leafRowList;
    }

    public void drawRows(Object parentRow, double parentCellX, double parentCellY, int columnCounter) {
        int rowCount = model.getRowGroupCount(parentRow);
        double cellX = parentCellX;
        double cellY = parentCellY;

        for (int i = 0; i < rowCount; i++) {
            Object child = model.getRowGroup(parentRow, i);
            String columnName = model.getRowGroupName(child);

            double cellWidth = rowsWidthList.get(columnCounter);

            boolean isGroup = model.isColumnGroup(child);

            double cellHeight = getHeightOfRow(child);

            Rectangle2D rect = new Rectangle2D.Double(cellX, cellY, cellWidth, cellHeight);
            g2d.draw(rect);
            g2d.drawString(columnName,
                    (float)(cellX + cellWidth/2 - fm.stringWidth(columnName)/2),
                    (float)(cellY + cellHeight/2 - fm.getHeight()/2) + fm.getAscent());

            if (isGroup) {
                columnCounter++;
                cellX += cellWidth;
                drawRows(child, cellX, cellY, columnCounter);
                columnCounter--;
                cellX -= cellWidth;
            } else {
                Shape l = new Line2D.Double(cellX + cellWidth, cellY, getWidthOfComponent(), cellY);
                g2d.draw(l);
            }
            cellY += cellHeight;
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
