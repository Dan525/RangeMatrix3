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
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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

    private final RangeMatrixModel model;
    private Graphics2D g2d;
    private float spaceAroundName = 4;
    private final ArrayList<Float> cellXList = new ArrayList<>();
    private final ArrayList<Float> cellWidthList = new ArrayList<>();
    private BufferedImage buffer;
    private int width;
    private int height;
    private int staticColumnCount = 4;

    public RangeMatrixColumnHeader(RangeMatrixModel model) {
        this.model = model;
        addComponentListener(new ColumnHeaderListenerImpl());
    }

    public RangeMatrixModel getModel() {
        return model;
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

    public void setSpaceAroundName(int newSpace) {
        this.spaceAroundName = newSpace;
    }

    public float getCellHeight(int heightMultiplier) {
        FontMetrics fm = g2d.getFontMetrics();
        return (fm.getHeight() + 2 * spaceAroundName) * heightMultiplier;
    }

    public float getWidthOfColumnName(Object column) {
        FontMetrics fm = g2d.getFontMetrics();
        String columnName = model.getColumnGroupName(column);
        return fm.stringWidth(columnName) + 2 * spaceAroundName;
    }

    public float getWidthOfColumn(Object column) {
        FontMetrics fm = g2d.getFontMetrics();
        float columnWidth = 0;
        String name = model.getColumnGroupName(column);
        float ownColumnWidth = fm.stringWidth(name) + 2 * spaceAroundName;

        ArrayList<Object> leafColumnList = getLeafColumns(column, new ArrayList<>());
        if (leafColumnList.isEmpty()) {
            return ownColumnWidth;
        }
        for (Object leafColumn : leafColumnList) {
            float leafColumnWidth = getWidthOfColumnName(leafColumn);
            columnWidth += leafColumnWidth;
        }
        if (columnWidth > ownColumnWidth) {
            return columnWidth;
        } else {
            return ownColumnWidth;
        }
    }

    public int getMaxRowIndex(Object parentColumn, ArrayList<Integer> maxRowIndexList, int maxRowIndex) {
        int columnCount = model.getColumnGroupCount(parentColumn);

        for (int i = 0; i < columnCount; i++) {
            Object child = model.getColumnGroup(parentColumn, i);
            boolean isGroup = model.isColumnGroup(child);
            if (isGroup) {
                maxRowIndex++;
                getMaxRowIndex(child, maxRowIndexList, maxRowIndex);
                maxRowIndex--;
            }
            maxRowIndexList.add(maxRowIndex);
        }
        return Collections.max(maxRowIndexList);
    }

    public int getHeightMultiplier(Object parentColumn, boolean isGroup, int rowIndex, int maxRowIndex) {
        if (!isGroup) {
            return (maxRowIndex - rowIndex) + 1;
        } else {
            return 1;
        }
    }

    public void drawColumns(Object parentColumn, float parentCellX, float parentCellY, int rowCounter, int maxRowIndex) {
        FontMetrics fm = g2d.getFontMetrics();
        int columnCount = model.getColumnGroupCount(parentColumn);
        float cellX = parentCellX;
        float cellY = parentCellY;

        for (int i = 0; i < columnCount; i++) {
            Object child = model.getColumnGroup(parentColumn, i);
            String columnName = model.getColumnGroupName(child);

            float cellWidth = getWidthOfColumn(child);

            boolean isGroup = model.isColumnGroup(child);

            int heightMultiplier = getHeightMultiplier(parentColumn, isGroup, rowCounter, maxRowIndex);

            float cellHeight = getCellHeight(heightMultiplier);

            Rectangle2D rect = new Rectangle2D.Float(cellX, cellY, cellWidth, cellHeight);
            g2d.draw(rect);
            g2d.drawString(columnName,
                    cellX + cellWidth / 2 - fm.stringWidth(columnName) / 2,
                    cellY + cellHeight / 2 - fm.getHeight() / 2 + 12);        //12 - высота верхней панели окна

            if (isGroup) {
                rowCounter++;
                cellY += cellHeight;
                drawColumns(child, cellX, cellY, rowCounter, maxRowIndex);
                rowCounter--;
                cellY -= cellHeight;
            }
            cellX += cellWidth;
        }
    }

    public void fillCellCoordinateList(Object parentColumn, float parentCellX, int rowCounter) {

        int columnCount = model.getColumnGroupCount(parentColumn);
        float cellX = parentCellX;

        for (int i = 0; i < columnCount; i++) {
            Object child = model.getColumnGroup(parentColumn, i);

            float cellWidth = getWidthOfColumn(child);

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

    public ArrayList<Float> getCellXList() {
        if (g2d == null) {
            g2d = (Graphics2D) getGraphics();
        }
        if (cellXList.isEmpty()) {
            fillCellCoordinateList(null, 0, 0);
        }
        return cellXList;
    }

    public ArrayList<Float> getCellWidthList() {
        if (g2d == null) {
            g2d = (Graphics2D) this.getGraphics();
        }
        if (cellWidthList.isEmpty()) {
            fillCellCoordinateList(null, 0, 0);
        }
        return cellWidthList;
    }

    public void setHeightOfComponent() {
        height = (int) (getCellHeight(1) * getMaxRowIndex(null, new ArrayList<Integer>(), 1)) + 1;
    }

    public void setWidthOfComponent() {
        width = (int)getWidthOfColumn(null);
    }

    public int getWidthOfComponent() {
        if (width == 0) {
            if (g2d == null) {
                g2d = (Graphics2D) getGraphics();
            }
            setWidthOfComponent();
        }
        return width;
    }

    public int getHeightOfComponent() {
        if (height == 0) {
            if (g2d == null) {
                g2d = (Graphics2D) getGraphics();
            }
            setHeightOfComponent();
        }
        return height;
    }

    public int getStaticColumnCount() {
        return staticColumnCount;
    }

    public void setStaticColumnCount(int staticColumnCount) {
        this.staticColumnCount = staticColumnCount;
    }

    public float getStaticColumnWidth() {
        float staticColumnWidth = 0;
        for (int i = 0; i < staticColumnCount; i++) {
            staticColumnWidth += getCellWidthList().get(i);
        }
        return staticColumnWidth;
    }

    public BufferedImage getBuffer() {
        if (buffer == null) {
            rebuildBuffer();
        }
        return buffer;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(getWidthOfComponent(), getHeightOfComponent());
    }

    private void rebuildBuffer() {
        buffer = new BufferedImage(getWidthOfComponent(), getHeightOfComponent(), BufferedImage.TYPE_INT_ARGB);

        g2d = buffer.createGraphics();
        g2d.setColor(Color.BLACK);
        int maxRowIndex = getMaxRowIndex(null, new ArrayList<>(), 0);

        drawColumns(null, 0, 0, 0, maxRowIndex);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (buffer == null) {
            rebuildBuffer();
        }
        g2d = (Graphics2D) g;
        float x = cellXList.get(staticColumnCount) + 1;
        Image newImage = buffer.getSubimage((int) x, 0, buffer.getWidth() - (int) x, buffer.getHeight());
        g2d.drawImage(newImage, 0, 0, this);
    }

    private class ColumnHeaderListenerImpl extends ComponentAdapter {

    }
}
