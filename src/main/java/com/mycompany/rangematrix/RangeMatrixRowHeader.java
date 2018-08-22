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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.JComponent;

/**
 *
 * @author Daniil
 */
public class RangeMatrixRowHeader extends JComponent {

    private RangeMatrixModel model;
    private RangeMatrixColumnHeader columnHeader;
    private Graphics2D g2d;
    private ArrayList<Float> cellYList = new ArrayList<>();
    private BufferedImage buffer;
    private int width;
    private int height;

    public RangeMatrixRowHeader(RangeMatrixModel model, RangeMatrixColumnHeader columnHeader) {
        this.model = model;
        this.columnHeader = columnHeader;
        addComponentListener(new RowHeaderListenerImpl());
    }

    public RangeMatrixModel getModel() {
        return model;
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

    float spaceAroundName = 4;

    public void setSpaceAroundName(int newSpace) {
        this.spaceAroundName = newSpace;
    }

    public float getCellHeight(int heightMultiplier) {
        FontMetrics fm = g2d.getFontMetrics();
        return (fm.getHeight() + 2 * spaceAroundName) * heightMultiplier;
    }

    public float getWidthOfRowByName(Object row) {
        FontMetrics fm = g2d.getFontMetrics();
        String rowName = model.getRowGroupName(row);
        return fm.stringWidth(rowName) + 2 * spaceAroundName;
    }

    public float getWidthOfRow(int indexOfColumn, int columnCounter, ArrayList<Float> rowsOfColumnList) {
        FontMetrics fm = g2d.getFontMetrics();
        int rowCount = model.getRowGroupCount(null);

        for (int i = 0; i < rowCount; i++) {
            Object child = model.getRowGroup(null, i);

            boolean isGroup = model.isColumnGroup(child);

            if (isGroup && columnCounter < indexOfColumn) {
                columnCounter++;
                getWidthOfRow(indexOfColumn, columnCounter, rowsOfColumnList);
                columnCounter--;

            } else if (columnCounter == indexOfColumn) {
                rowsOfColumnList.add(getWidthOfRowByName(child));
            }
        }
        return Collections.max(rowsOfColumnList);
    }

    public float getWidthOfRow(int indexOfColumn) {
        ArrayList<Float> cellWidthList = columnHeader.getCellWidthList();
        return cellWidthList.get(indexOfColumn);
    }

    public float getHeightOfRow(Object row) {
        ArrayList<Object> leafRowList = getLeafRows(row, new ArrayList<>());
        if (leafRowList.isEmpty()) {
            return getCellHeight(1);
        } else {
            return getCellHeight(leafRowList.size());
        }
    }

    public int getMaxColumnIndex(Object parentRow, ArrayList<Integer> maxColumnIndexList, int maxColumnIndex) {
        int rowCount = model.getRowGroupCount(parentRow);

        for (int i = 0; i < rowCount; i++) {
            Object child = model.getRowGroup(parentRow, i);
            boolean isGroup = model.isRowGroup(child);
            if (isGroup) {
                maxColumnIndex++;
                getMaxColumnIndex(child, maxColumnIndexList, maxColumnIndex);
                maxColumnIndex--;
            }
            maxColumnIndexList.add(maxColumnIndex);
        }
        return Collections.max(maxColumnIndexList);
    }

    public void drawRows(Object parentRow, float parentCellX, float parentCellY, int columnCounter) {
        FontMetrics fm = g2d.getFontMetrics();
        int rowCount = model.getRowGroupCount(parentRow);
        float cellX = parentCellX;
        float cellY = parentCellY;

        for (int i = 0; i < rowCount; i++) {
            Object child = model.getRowGroup(parentRow, i);
            String columnName = model.getRowGroupName(child);

            float cellWidth = getWidthOfRow(columnCounter);//getWidthOfRow(columnCounter, 1, new ArrayList<Float>());

            boolean isGroup = model.isColumnGroup(child);

            float cellHeight = getHeightOfRow(child);

            Rectangle2D rect = new Rectangle2D.Float(cellX, cellY, cellWidth, cellHeight);
            g2d.draw(rect);
            g2d.drawString(columnName,
                    cellX + cellWidth / 2 - fm.stringWidth(columnName) / 2,
                    cellY + cellHeight / 2 - fm.getHeight() / 2 + 12);        //12 - высота верхней панели окна

            if (isGroup) {
                columnCounter++;
                cellX += cellWidth;
                drawRows(child, cellX, cellY, columnCounter);
                columnCounter--;
                cellX -= cellWidth;
            } else {
                cellYList.add(cellY);
                Shape l = new Line2D.Double(cellX + cellWidth, cellY, getWidthOfComponent(), cellY);
                g2d.draw(l);
            }
            cellY += cellHeight;
        }
    }

    public ArrayList<Float> getCellYList() {
        return cellYList;
    }

    public void setHeightOfComponent() {
        height = (int) (getHeightOfRow(null));
    }

    public void setWidthOfComponent() {
        int maxColumnIndex = getMaxColumnIndex(null, new ArrayList<Integer>(), 1);
        ArrayList<Float> cellWidthList = columnHeader.getCellWidthList();
        for (int i = 0; i < maxColumnIndex; i++) {
            width += cellWidthList.get(i);
        }
        width += 1;
    }

    public int getWidthOfComponent() {
        if (g2d == null) {
            g2d = (Graphics2D) getGraphics();
        }
        if (width == 0) {
            setWidthOfComponent();
        }
        return width;
    }

    public int getHeightOfComponent() {
        if (g2d == null) {
            g2d = (Graphics2D) getGraphics();
        }
        if (height == 0) {
            setHeightOfComponent();
        }
        return height;
    }

    @Override
    public Dimension getPreferredSize() {
        if (width == 0 || height == 0) {
            g2d = (Graphics2D) getGraphics();
            setWidthOfComponent();
            setHeightOfComponent();
        }
        return new Dimension(width, height);
    }

    private void rebuildBuffer() {

        int w = (int) getWidthOfComponent();
        int h = (int) getHeightOfComponent();
        buffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        g2d = buffer.createGraphics();
        g2d.setColor(Color.BLACK);

        drawRows(null, 0, -1, 0);
        Shape l = new Line2D.Double(getWidthOfComponent()-1, 0, getWidthOfComponent()-1, getHeightOfComponent());
        g2d.draw(l);
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

    private class RowHeaderListenerImpl extends ComponentAdapter {

    }
}
