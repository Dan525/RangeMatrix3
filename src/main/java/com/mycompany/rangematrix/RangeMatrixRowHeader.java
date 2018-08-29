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
import java.awt.Shape;
import java.awt.geom.Line2D;
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
public class RangeMatrixRowHeader extends JComponent {

    private final RangeMatrix rm;
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
    
    public RangeMatrixRowHeader(RangeMatrix rm) {
        this.rm = rm;
    }

    public void setModel() {
        this.model = rm.getModel();
        this.renderer = rm.getRenderer();
        this.crp = rm.getCrp();
        
        cellYList = new ArrayList<>();
        
        calculateMinimalCellHeight();
        fillCellCoordinateList(null, 0, 0);
        calculateColumnCount(null, new ArrayList<>(), 1);
    }

    public RangeMatrixModel getModel() {
        return model;
    }

    public void setSpaceAroundName(int newSpace) {
        this.spaceAroundName = newSpace;
    }

    public void calculateMinimalCellHeight() {
        JLabel label = renderer.getRowRendererComponent(null, " ");
        minimalCellHeight = label.getPreferredSize().getHeight() + 2 * spaceAroundName;
    }

    public double getMinimalCellHeight() {
        return minimalCellHeight;
    }

    public double calculateWidthOfRowByName(Object row) {
        String rowName = model.getColumnGroupName(row);
        JLabel label = renderer.getColumnRendererComponent(row, rowName);
        return label.getPreferredSize().getWidth() + 2 * spaceAroundName;
    }

    public ArrayList<Double> fillRowsWidthInColumnList(Object parent, int indexOfColumn, int columnCounter, ArrayList<Double> rowsOfColumnList) {
        int rowCount = model.getRowGroupCount(parent);

        for (int i = 0; i < rowCount; i++) {
            Object child = model.getRowGroup(parent, i);

            boolean isGroup = model.isColumnGroup(child);
            
            if (columnCounter == indexOfColumn) {
                rowsOfColumnList.add(calculateWidthOfRowByName(child));
            }
            if (isGroup && columnCounter < indexOfColumn) {
                columnCounter++;
                fillRowsWidthInColumnList(child, indexOfColumn, columnCounter, rowsOfColumnList);
                columnCounter--;

            }
        }
        return rowsOfColumnList;
    }

    public ArrayList<Double> fillRowsWidthList() {
        ArrayList<Double> rowsWidthListTemp = new ArrayList<>();
        for (int i = 0; i < columnCount; i++) {
            ArrayList<Double> rowsOfColumnList = fillRowsWidthInColumnList(null, i, 0, new ArrayList<>());
            double rowWidth = Collections.max(rowsOfColumnList);
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

    public double calculateHeightOfRow(Object row) {
        ArrayList<Object> leafRowList = fillLeafRows(row, new ArrayList<>());

        if (leafRowList.isEmpty()) {
            return minimalCellHeight;
        } else {
            return minimalCellHeight * leafRowList.size();
        }
    }

    public void calculateColumnCount(Object parentRow, ArrayList<Integer> maxColumnIndexList, int maxColumnIndex) {
        int rowCount = model.getRowGroupCount(parentRow);

        for (int i = 0; i < rowCount; i++) {
            Object child = model.getRowGroup(parentRow, i);
            boolean isGroup = model.isRowGroup(child);
            if (isGroup) {
                maxColumnIndex++;
                calculateColumnCount(child, maxColumnIndexList, maxColumnIndex);
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

            double cellHeight = calculateHeightOfRow(child);

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

    public void calculateWidthOfComponent() {
        for (double rowWidth : rowsWidthList) {
            width += rowWidth;
        }
        //width += 1;
    }

    public double getWidthOfComponent() {
        return width;
    }

    public void calculateHeightOfComponent() {
        height = calculateHeightOfRow(null);
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

    public ArrayList<Object> fillLeafRows(Object parentRow, ArrayList<Object> leafRowList) {
        int rowCount = model.getRowGroupCount(parentRow);

        for (int i = 0; i < rowCount; i++) {
            Object child = model.getRowGroup(parentRow, i);
            boolean isGroup = model.isRowGroup(child);
            if (isGroup) {
                fillLeafRows(child, leafRowList);
            } else {
                leafRowList.add(child);
            }
        }
        return leafRowList;
    }
    
    public void drawEmptyRows(Graphics2D g2d, double parentCellX, double parentCellY, int columnCounter) {

        if (columnCounter < columnCount) {
            
            String rowName = " ";
            double cellX = parentCellX;
            double cellWidth = rowsWidthList.get(columnCounter);

            Rectangle2D rect = new Rectangle2D.Double(parentCellX, parentCellY, cellWidth, minimalCellHeight);
            //g2d.draw(rect);

            crp.paintComponent(g2d, renderer.getRowRendererComponent(null, rowName),
                    this, (int) cellX, (int) parentCellY, (int) cellWidth, (int) minimalCellHeight);
            
            cellX += cellWidth;
            columnCounter++;

            drawEmptyRows(g2d, cellX, parentCellY, columnCounter);

            columnCounter--;
            cellX -= cellWidth;
        }

    }

    public void drawRows(Graphics2D g2d, Object parentRow, double parentCellX, double parentCellY, int columnCounter) {
        int rowCount = model.getRowGroupCount(parentRow);
        double cellX = parentCellX;
        double cellY = parentCellY;

        for (int i = 0; i < rowCount; i++) {
            Object child = model.getRowGroup(parentRow, i);
            String rowName = model.getRowGroupName(child);

            double cellWidth = rowsWidthList.get(columnCounter);

            boolean isGroup = model.isColumnGroup(child);

            double cellHeight = calculateHeightOfRow(child);

            Rectangle2D rect = new Rectangle2D.Double(cellX, cellY, cellWidth, cellHeight);
            //g2d.draw(rect);
            
            crp.paintComponent(g2d, renderer.getRowRendererComponent(child, rowName),
                               this, (int)cellX, (int)cellY, (int)cellWidth, (int)cellHeight);

            if (isGroup) {
                
                columnCounter++;
                cellX += cellWidth;
                drawRows(g2d, child, cellX, cellY, columnCounter);
                columnCounter--;
                cellX -= cellWidth;
                
            } else if (!isGroup && columnCounter < columnCount) {
                
                cellX += cellWidth;
                columnCounter++;
                drawEmptyRows(g2d, cellX, cellY, columnCounter);
                columnCounter--;
                cellX -= cellWidth;
                
            }  else {
//                Shape l = new Line2D.Double(cellX + cellWidth, cellY, getWidthOfComponent(), cellY);
//                g2d.draw(l);
            }
            cellY += cellHeight;
        }
    }

    void rebuildBuffer() {
        buffer = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = buffer.createGraphics();
        g2d.setColor(Color.BLACK);

        drawRows(g2d, null, 0, 0, 0);
        Shape l = new Line2D.Double(width - 1, 0, width - 1, height);
        //g2d.draw(l);
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
