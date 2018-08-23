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
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.JComponent;

/**
 *
 * @author daniil_pozdeev
 */
public class RangeMatrixColumnHeader extends JComponent {

    private RangeMatrixModel model;
    private Graphics2D g2d;
    private Font font;
    private FontMetrics fm;
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
    
    public void setModel(RangeMatrixModel model) {
        doSetModel(model);
    }
    
    private void doSetModel(RangeMatrixModel model) {
        this.model = model;
        font = new Font("Arial Narrow", Font.PLAIN, 12);
        fm = new Canvas().getFontMetrics(font);
        
        cellXList = new ArrayList<>();
        cellWidthList = new ArrayList<>();
        fillCellCoordinateList(null, 0, 0);
        
        setRowCount(null, new ArrayList<>(), 1);
        setMinimalCellHeight();
        
        setHeightOfComponent();
        setWidthOfComponent();
    }

    public void setSpaceAroundName(int newSpace) {
        this.spaceAroundName = newSpace;
    }

    public double getCellHeight(int heightMultiplier) {
        return (fm.getHeight() + 2 * spaceAroundName) * heightMultiplier;
    }

    public double getWidthOfColumnName(Object column) {
        String columnName = model.getColumnGroupName(column);
        return fm.stringWidth(columnName) + 2 * spaceAroundName;
    }

    public double getWidthOfColumn(Object column) {
        double columnWidth = 0;
        String name = model.getColumnGroupName(column);
        double ownColumnWidth = fm.stringWidth(name) + 2 * spaceAroundName;

        ArrayList<Object> leafColumnList = getLeafColumns(column, new ArrayList<>());
        if (leafColumnList.isEmpty()) {
            return ownColumnWidth;
        }
        for (Object leafColumn : leafColumnList) {
            double leafColumnWidth = getWidthOfColumnName(leafColumn);
            columnWidth += leafColumnWidth;
        }
        if (columnWidth > ownColumnWidth) {
            return columnWidth;
        } else {
            return ownColumnWidth;
        }
    }

    public void setRowCount(Object parentColumn, ArrayList<Integer> maxRowIndexList, int maxRowIndex) {
        int columnCount = model.getColumnGroupCount(parentColumn);

        for (int i = 0; i < columnCount; i++) {
            Object child = model.getColumnGroup(parentColumn, i);
            boolean isGroup = model.isColumnGroup(child);
            if (isGroup) {
                maxRowIndex++;
                setRowCount(child, maxRowIndexList, maxRowIndex);
                maxRowIndex--;
            }
            maxRowIndexList.add(maxRowIndex);
        }
        rowCount = Collections.max(maxRowIndexList);
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getHeightMultiplier(Object parentColumn, boolean isGroup, int rowIndex) {
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

            double cellWidth = getWidthOfColumn(child);

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
    
    public void setMinimalCellHeight() {
        minimalCellHeight =  fm.getHeight() + 2 * spaceAroundName;
    }

    public double getMinimalCellHeight() {
        return minimalCellHeight;
    }

    public void setWidthOfComponent() {
        width = getWidthOfColumn(null);
    }

    public double getWidthOfComponent() {
        return width;
    }

    public void setHeightOfComponent() {
        height = (minimalCellHeight * rowCount) + 1;
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

        drawColumns(null, -1, 0, 1);
    }

    public ArrayList<Object> getLeafColumns(Object parentColumn, ArrayList<Object> leafColumnList) {
        int columnCount = model.getColumnGroupCount(parentColumn);

        for (int i = 0; i < columnCount; i++) {
            Object child = model.getColumnGroup(parentColumn, i);
            boolean isGroup = model.isColumnGroup(child);
            if (isGroup) {
                getLeafColumns(child, leafColumnList);
            } else {
                leafColumnList.add(child);
            }
        }
        return leafColumnList;
    }

    public void drawColumns(Object parentColumn, double parentCellX, double parentCellY, int rowCounter) {
        
        int columnCount = model.getColumnGroupCount(parentColumn);
        double cellX = parentCellX;
        double cellY = parentCellY;

        for (int i = 0; i < columnCount; i++) {
            Object child = model.getColumnGroup(parentColumn, i);
            String columnName = model.getColumnGroupName(child);

            double cellWidth = getWidthOfColumn(child);

            boolean isGroup = model.isColumnGroup(child);

            int heightMultiplier = getHeightMultiplier(parentColumn, isGroup, rowCounter);

            double cellHeight = getCellHeight(heightMultiplier);

            Rectangle2D rect = new Rectangle2D.Double(cellX, cellY, cellWidth, cellHeight);
            g2d.draw(rect);
            g2d.drawString(columnName,
                    (float)(cellX + cellWidth / 2 - fm.stringWidth(columnName) / 2),
                    (float)(cellY + cellHeight / 2 - fm.getHeight() / 2 + 12));        //12 - высота верхней панели окна

            if (isGroup) {
                rowCounter++;
                cellY += cellHeight;
                drawColumns(child, cellX, cellY, rowCounter);
                rowCounter--;
                cellY -= cellHeight;
            }
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
