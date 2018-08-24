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
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.JComponent;
import javax.swing.JLabel;

/**
 *
 * @author daniil_pozdeev
 */
public class RangeMatrixColumnHeader extends JComponent {

    private RangeMatrixModel model;
    private IRangeMatrixRenderer renderer;
//    private Graphics2D g2d;
//    private Font font;
//    private FontMetrics fm;
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
    
    public void setModel(RangeMatrixModel model, FontMetrics fm, IRangeMatrixRenderer renderer) {
        this.model = model;
        this.renderer = renderer;
        cellXList = new ArrayList<>();
        cellWidthList = new ArrayList<>();
        setMinimalCellHeight(fm);
        fillCellCoordinateList(fm, null, 0, 0);
        setRowCount(null, new ArrayList<>(), 1);
    }

    public void setSpaceAroundName(int newSpace) {
        this.spaceAroundName = newSpace;
    }

    public double getCellHeight(FontMetrics fm, int heightMultiplier) {
        return (fm.getHeight() + 2 * spaceAroundName) * heightMultiplier;
    }

    public double getWidthByColumnName(FontMetrics fm, Object column) {
        
        JLabel label = renderer.getColumnRendererComponent(column);
        String columnName = model.getColumnGroupName(column);
        label.setText(columnName);
        double columnWidth = label.getPreferredSize().getWidth() + 2 * spaceAroundName;
        if (columnWidth > minimalCellHeight) {
            return columnWidth;
        } else {
            return minimalCellHeight;
        }
    }

    public double getWidthOfColumn(FontMetrics fm, Object column) {
        double columnWidth = 0;
        double ownColumnWidth = getWidthByColumnName(fm, column);

        ArrayList<Object> leafColumnList = getLeafColumns(column, new ArrayList<>());
        if (leafColumnList.isEmpty()) {
            return ownColumnWidth;
        }
        for (Object leafColumn : leafColumnList) {
            double leafColumnWidth = getWidthByColumnName(fm, leafColumn);
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

    public void fillCellCoordinateList(FontMetrics fm, Object parentColumn, double parentCellX, int rowCounter) {

        int columnCount = model.getColumnGroupCount(parentColumn);
        double cellX = parentCellX;

        for (int i = 0; i < columnCount; i++) {
            Object child = model.getColumnGroup(parentColumn, i);

            double cellWidth = getWidthOfColumn(fm, child);

            boolean isGroup = model.isColumnGroup(child);

            if (isGroup) {
                rowCounter++;
                fillCellCoordinateList(fm, child, cellX, rowCounter);
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
    
    public void setMinimalCellHeight(FontMetrics fm) {
        minimalCellHeight =  fm.getHeight() + 2 * spaceAroundName;
    }

    public double getMinimalCellHeight() {
        return minimalCellHeight;
    }

    public void setWidthOfComponent(FontMetrics fm) {
        width = getWidthOfColumn(fm, null);
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

        Graphics2D g2d = buffer.createGraphics();
        g2d.setColor(Color.BLACK);
        FontMetrics fm = g2d.getFontMetrics();

        drawColumns(g2d, fm, null, -1, 0, 1);
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

    public void drawColumns(Graphics2D g2d, FontMetrics fm, Object parentColumn, double parentCellX, double parentCellY, int rowCounter) {
        
        int columnCount = model.getColumnGroupCount(parentColumn);
        double cellX = parentCellX;
        double cellY = parentCellY;

        for (int i = 0; i < columnCount; i++) {
            Object child = model.getColumnGroup(parentColumn, i);
            String columnName = model.getColumnGroupName(child);

            double cellWidth = getWidthOfColumn(fm, child);

            boolean isGroup = model.isColumnGroup(child);

            int heightMultiplier = getHeightMultiplier(parentColumn, isGroup, rowCounter);

            double cellHeight = getCellHeight(fm, heightMultiplier);

            Rectangle2D rect = new Rectangle2D.Double(cellX, cellY, cellWidth, cellHeight);
            g2d.draw(rect);
            JLabel label = renderer.getColumnRendererComponent(child);
            label.setText(columnName);
            label.setLocation((int)(cellX + cellWidth / 2 - fm.stringWidth(columnName) / 2), (int)(cellY + cellHeight / 2 - fm.getHeight() / 2 + fm.getAscent()));
            
            g2d.drawString(columnName,
                    (float)(cellX + cellWidth / 2 - fm.stringWidth(columnName) / 2),
                    (float)(cellY + cellHeight / 2 - fm.getHeight() / 2 + fm.getAscent()));

            if (isGroup) {
                rowCounter++;
                cellY += cellHeight;
                drawColumns(g2d, fm, child, cellX, cellY, rowCounter);
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
        Graphics2D g2d = (Graphics2D) g;
        
        g2d.drawImage(buffer, 0, 0, this);
    }
}
