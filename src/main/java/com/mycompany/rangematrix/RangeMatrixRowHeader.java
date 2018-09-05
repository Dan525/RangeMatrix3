/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.rangematrix;

import com.infomatiq.jsi.Point;
import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.SpatialIndex;
import com.infomatiq.jsi.rtree.RTree;
import gnu.trove.TIntProcedure;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
    private SpatialIndex rTree;
    
    private ArrayList<Double> cellYList;
    private ArrayList<RangeMatrixHeaderButton> buttonList;
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
        rTree = new RTree();
        rTree.init(null);

        buttonList = new ArrayList<>();
        cellYList = new ArrayList<>();
        
        calculateParams();
        
        this.addMouseListener(new RangeMatrixMouseHandler());
    }
    
    public void calculateParams() {
        calculateMinimalCellHeight();
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
    
    public void calculateRows(Object parentRow, double parentCellX, double parentCellY, int columnCounter) {
        int rowCount = model.getRowGroupCount(parentRow);
        double cellX = parentCellX;
        double cellY = parentCellY;

        for (int i = 0; i < rowCount; i++) {
            Object child = model.getRowGroup(parentRow, i);
            String rowName = model.getRowGroupName(child);

            double cellWidth = rowsWidthList.get(columnCounter);

            boolean isGroup = model.isColumnGroup(child);

            double cellHeight = calculateHeightOfRow(child);

            RangeMatrixHeaderButton button = new RangeMatrixHeaderButton(cellX, cellY, cellWidth, cellHeight, child, rowName, isGroup);
            buttonList.add(button);
            
            Rectangle rect = new Rectangle((float)cellX, (float)cellY, (float)(cellX + cellWidth), (float)(cellY + cellHeight));
            
            rTree.add(rect, buttonList.indexOf(button));

            if (isGroup) {
                
                columnCounter++;
                cellX += cellWidth;
                calculateRows(child, cellX, cellY, columnCounter);
                columnCounter--;
                cellX -= cellWidth;
                
            } else if (!isGroup && columnCounter < columnCount) {
                
                columnCounter++;
                cellX += cellWidth;
                calculateEmptyRows(cellX, cellY, columnCounter);
                columnCounter--;
                cellX -= cellWidth;
                
            }  else {
                cellYList.add(cellY);
            }
            cellY += cellHeight;
        }
    }
    
    public void calculateEmptyRows(double parentCellX, double parentCellY, int columnCounter) {

        if (columnCounter < columnCount) {
            
            String rowName = "";
            double cellX = parentCellX;
            double cellWidth = rowsWidthList.get(columnCounter);
            boolean isGroup = false;

            RangeMatrixHeaderButton button = new RangeMatrixHeaderButton(cellX, parentCellY, cellWidth, minimalCellHeight, null, rowName, isGroup);
            buttonList.add(button);
            
            Rectangle rect = new Rectangle((float)cellX, (float)parentCellY, (float)(cellX + cellWidth), (float)(parentCellY + minimalCellHeight));
            
            rTree.add(rect, buttonList.indexOf(button));
            
            cellX += cellWidth;
            columnCounter++;

            calculateEmptyRows(cellX, parentCellY, columnCounter);

            columnCounter--;
            cellX -= cellWidth;
        }

    }
    
    public void drawRows(Graphics2D g2d) {
        
        for (RangeMatrixHeaderButton button : buttonList) {
            crp.paintComponent(g2d, 
                               renderer.getColumnRendererComponent(button.getButtonObject(),
                                                                   button.getButtonName()),
                               this,
                               (int) button.getX(),
                               (int) button.getY(),
                               (int) button.getWidth(),
                               (int) button.getHeight());
        }
        
    }
    
//    public void drawEmptyRows(Graphics2D g2d, double parentCellX, double parentCellY, int columnCounter) {
//
//        if (columnCounter < columnCount) {
//            
//            String rowName = " ";
//            double cellX = parentCellX;
//            double cellWidth = rowsWidthList.get(columnCounter);
//
//            Rectangle2D rect = new Rectangle2D.Double(parentCellX, parentCellY, cellWidth, minimalCellHeight);
//            //g2d.draw(rect);
//
//            crp.paintComponent(g2d, renderer.getRowRendererComponent(null, rowName),
//                    this, (int) cellX, (int) parentCellY, (int) cellWidth, (int) minimalCellHeight);
//            
//            cellX += cellWidth;
//            columnCounter++;
//
//            drawEmptyRows(g2d, cellX, parentCellY, columnCounter);
//
//            columnCounter--;
//            cellX -= cellWidth;
//        }
//
//    }
//
//    public void drawRows(Graphics2D g2d, Object parentRow, double parentCellX, double parentCellY, int columnCounter) {
//        int rowCount = model.getRowGroupCount(parentRow);
//        double cellX = parentCellX;
//        double cellY = parentCellY;
//
//        for (int i = 0; i < rowCount; i++) {
//            Object child = model.getRowGroup(parentRow, i);
//            String rowName = model.getRowGroupName(child);
//
//            double cellWidth = rowsWidthList.get(columnCounter);
//
//            boolean isGroup = model.isColumnGroup(child);
//
//            double cellHeight = calculateHeightOfRow(child);
//
//            Rectangle2D rect = new Rectangle2D.Double(cellX, cellY, cellWidth, cellHeight);
//            //g2d.draw(rect);
//            
//            crp.paintComponent(g2d, renderer.getRowRendererComponent(child, rowName),
//                               this, (int)cellX, (int)cellY, (int)cellWidth, (int)cellHeight);
//
//            if (isGroup) {
//                
//                columnCounter++;
//                cellX += cellWidth;
//                drawRows(g2d, child, cellX, cellY, columnCounter);
//                columnCounter--;
//                cellX -= cellWidth;
//                
//            } else if (!isGroup && columnCounter < columnCount) {
//                
//                columnCounter++;
//                cellX += cellWidth;
//                drawEmptyRows(g2d, cellX, cellY, columnCounter);
//                columnCounter--;
//                cellX -= cellWidth;
//                
//            }  else {
//
//            }
//            cellY += cellHeight;
//        }
//    }

    void rebuildBuffer() {
        buffer = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = buffer.createGraphics();
        g2d.setColor(Color.BLACK);

        calculateRows(null, 0, 0, 0);
        drawRows(g2d);
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
    
    protected class RangeMatrixMouseHandler implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            Point rTreePoint = new Point(e.getX(), e.getY());
            rTree.nearest(rTreePoint, new TIntProcedure() {         // a procedure whose execute() method will be called with the results
                @Override
                public boolean execute(int i) {
                    System.out.println(buttonList.get(i));
                    return false;              // return true here to continue receiving results
                }
            }, Float.MAX_VALUE);
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }

    }
}
