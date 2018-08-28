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
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.CellRendererPane;
import javax.swing.JComponent;

/**
 *
 * @author daniil_pozdeev
 */
public class RangeMatrixRowHeader extends JComponent {

    private RangeMatrixModel model;
    private IRangeMatrixRenderer renderer;
    private CellRendererPane crp;
    private ArrayList<Double> cellYList;
    private double minimalCellHeight;
    private int columnCount;
    private ArrayList<Double> rowsWidthList;
    private double spaceAroundName = 4;
    private BufferedImage buffer;
    private double width;
    private double height;

    public void setModel(RangeMatrixModel model, FontMetrics fm, IRangeMatrixRenderer renderer, CellRendererPane crp) {
        this.model = model;
        this.renderer = renderer;
        this.crp = crp;
        
        cellYList = new ArrayList<>();
        
        setMinimalCellHeight(fm);
        fillCellCoordinateList(null, 0, 0);
        setColumnCount(null, new ArrayList<>(), 1);
    }

    public RangeMatrixModel getModel() {
        return model;
    }

    public void setSpaceAroundName(int newSpace) {
        this.spaceAroundName = newSpace;
    }

    public void setMinimalCellHeight(FontMetrics fm) {
        minimalCellHeight = fm.getHeight() + 2 * spaceAroundName;
    }

    public double getMinimalCellHeight() {
        return minimalCellHeight;
    }

    public double getWidthOfRowByName(FontMetrics fm, Object row) {
        String rowName = model.getRowGroupName(row);
        return fm.stringWidth(rowName) + 2 * spaceAroundName;
    }

    public double getMaxRowWidthInColumn(FontMetrics fm, int indexOfColumn, int columnCounter, ArrayList<Double> rowsOfColumnList) {
        int rowCount = model.getRowGroupCount(null);

        for (int i = 0; i < rowCount; i++) {
            Object child = model.getRowGroup(null, i);

            boolean isGroup = model.isColumnGroup(child);

            if (isGroup && columnCounter < indexOfColumn) {
                columnCounter++;
                getMaxRowWidthInColumn(fm, indexOfColumn, columnCounter, rowsOfColumnList);
                columnCounter--;

            } else if (columnCounter == indexOfColumn) {
                rowsOfColumnList.add(getWidthOfRowByName(fm, child));
            }
        }
        return Collections.max(rowsOfColumnList);
    }

    public ArrayList<Double> calculateRowsWidthList(FontMetrics fm) {
        ArrayList<Double> rowsWidthListTemp = new ArrayList<>();
        for (int i = 0; i < columnCount; i++) {
            double rowWidth = getMaxRowWidthInColumn(fm, i, 0, new ArrayList<>());
            rowsWidthListTemp.add(rowWidth);
        }
        return rowsWidthListTemp;
    }

    public ArrayList<Double> getRowsWidthList() {
        return rowsWidthList;
    }

    public void setRowsWidthList(ArrayList<Double> rowsWidthList) {
        this.rowsWidthList = rowsWidthList;
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
        buffer = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = buffer.createGraphics();
        g2d.setColor(Color.BLACK);
        FontMetrics fm = g2d.getFontMetrics();

        drawRows(g2d, fm, null, 0, -1, 0);
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

    public void drawRows(Graphics2D g2d, FontMetrics fm, Object parentRow, double parentCellX, double parentCellY, int columnCounter) {
        int rowCount = model.getRowGroupCount(parentRow);
        double cellX = parentCellX;
        double cellY = parentCellY;

        for (int i = 0; i < rowCount; i++) {
            Object child = model.getRowGroup(parentRow, i);
            String rowName = model.getRowGroupName(child);

            double cellWidth = rowsWidthList.get(columnCounter);

            boolean isGroup = model.isColumnGroup(child);

            double cellHeight = getHeightOfRow(child);

            Rectangle2D rect = new Rectangle2D.Double(cellX, cellY, cellWidth, cellHeight);
            g2d.draw(rect);
            
            crp.paintComponent(g2d, renderer.getRowRendererComponent(child, rowName),
                               this, (int)cellX, (int)cellY, (int)cellWidth, (int)cellHeight);

            if (isGroup) {
                columnCounter++;
                cellX += cellWidth;
                drawRows(g2d, fm, child, cellX, cellY, columnCounter);
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
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(buffer, 0, 0, this);
    }
}
