/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.rangematrix;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import java.awt.Font;
import javax.swing.JLabel;
/**
 *
 * @author daniil_pozdeev
 */
public class RangeMatrix extends JComponent {

    private RangeMatrixModel model;
    private final RangeMatrixColumnHeader columnHeader;
    private final RangeMatrixRowHeader rowHeader;
    private final RangeMatrixHeaderCorner headerCorner;
    private double width;
    private double height;
    private BufferedImage buffer;
    private final double spaceAroundName = 4;

    public RangeMatrix(RangeMatrixModel model) {
        columnHeader = new RangeMatrixColumnHeader();
        rowHeader = new RangeMatrixRowHeader();
        headerCorner = new RangeMatrixHeaderCorner(columnHeader, rowHeader);
        doSetModel(model);
        
    }

    public RangeMatrixModel getModel() {
        return model;
    }
    
    public void setModel(RangeMatrixModel model) {
        doSetModel(model);
        
    }
    
    private void doSetModel(RangeMatrixModel model) {
        
        FontMetrics fm = getFontMetrics();
        
        this.model = model;
        DefaultRangeMatrixRenderer renderer = new DefaultRangeMatrixRenderer();
        columnHeader.setModel(model, fm, renderer);
        rowHeader.setModel(model, fm);
        headerCorner.setModel(model);
        setRowsWidthList(fm);
//        setMinimalCellHeight(fm);
        setWidthOfComponents(fm);
        setHeightOfComponents();
        
    }
    
    public FontMetrics getFontMetrics() {
//        Canvas c = new Canvas();
//        Font f = c.getFont();
//        return c.getFontMetrics(f);
        JLabel label = new JLabel();
        Font f = label.getFont();
        return label.getFontMetrics(f);
    }
    
//    public void setMinimalCellHeight(FontMetrics fm) {
//        Double minimalCellHeight =  fm.getHeight() + 2 * spaceAroundName;
//        columnHeader.setMinimalCellHeight(minimalCellHeight);
//        rowHeader.setMinimalCellHeight(minimalCellHeight);
//    }
    
    public void setWidthOfComponents(FontMetrics fm) {
        columnHeader.setWidthOfComponent(fm);
        rowHeader.setWidthOfComponent();
        headerCorner.setWidthOfComponent();
        setWidthOfComponent();
    }
    
    public void setHeightOfComponents() {
        columnHeader.setHeightOfComponent();
        rowHeader.setHeightOfComponent();
        headerCorner.setHeightOfComponent();
        setHeightOfComponent();
    }
    
    public ArrayList<Double> getMaxOfTwoLists(ArrayList<Double> rowsWidthList, ArrayList<Double> cornerRowsWidthList) {
        ArrayList<Double> newList = new ArrayList<>();
        for (int i = 0; i < rowHeader.getColumnCount(); i++) {
            newList.add(Math.max(rowsWidthList.get(i), cornerRowsWidthList.get(i)));
        }
        return newList;
    }
    
    public void setRowsWidthList(FontMetrics fm) {
        ArrayList<Double> rowsWidthList = rowHeader.calculateRowsWidthList(fm);
        ArrayList<Double> cornerRowsWidthList = headerCorner.calculateRowsWidthList(fm);
        
        ArrayList<Double> newList = getMaxOfTwoLists(rowsWidthList, cornerRowsWidthList);
        
        rowHeader.setRowsWidthList(newList);
    }

    public void drawVerticalLines(Graphics2D g2d) {
        ArrayList<Double> cellXList = columnHeader.getCellXList();
        ArrayList<Double> cellWidthList = columnHeader.getCellWidthList();
        
        for (int i = 0; i < cellXList.size(); i++) {
            double x = cellXList.get(i) + cellWidthList.get(i) - 1;
            Shape l = new Line2D.Double(x, 0, x, height);
            g2d.draw(l);
        }
    }

    public void drawHorizontalLines(Graphics2D g2d) {
        ArrayList<Double> cellYList = rowHeader.getCellYList();
        
        for (int i = 0; i < cellYList.size(); i++) {
            double y = cellYList.get(i) + rowHeader.getMinimalCellHeight() - 1;
            Shape l = new Line2D.Double(0, y, width, y);
            g2d.draw(l);
        }
    }
    
    public void drawValues(Graphics2D g2d) {
        FontMetrics fm = g2d.getFontMetrics();
        ArrayList<Double> cellXList = columnHeader.getCellXList();
        ArrayList<Double> cellWidthList = columnHeader.getCellWidthList();
        ArrayList<Double> cellYList = rowHeader.getCellYList();
        double minimalCellHeight = rowHeader. getMinimalCellHeight();
        
        for (int i = 0; i < cellYList.size(); i++) {
            for (int j = 0; j < cellXList.size(); j++) {
                String value = (model.getValueAt(j, i)).toString();
                g2d.drawString(value, 
                               (float)(cellXList.get(j) + cellWidthList.get(j)/2 - fm.stringWidth(value)/2 - 1),
                               (float)(cellYList.get(i) + minimalCellHeight/2 - fm.getHeight()/2 + fm.getAscent()));
            }            
        }
    }

    public void setWidthOfComponent() {
        width = columnHeader.getWidthOfComponent();
    }

    public double getWidthOfComponent() {
        return width;
    }
    
    public void setHeightOfComponent() {
        height = rowHeader.getHeightOfComponent();
    }

    public double getHeightOfComponent() {
        return height;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        configureEnclosingScrollPane();
    }

    protected void configureEnclosingScrollPane() {
        Container parent = SwingUtilities.getUnwrappedParent(this);
        if (parent instanceof JViewport) {
            JViewport port = (JViewport) parent;
            Container gp = port.getParent();
            if (gp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) gp;
                JViewport viewport = scrollPane.getViewport();
                if (viewport == null
                        || SwingUtilities.getUnwrappedView(viewport) != this) {
                    return;
                }
                scrollPane.setColumnHeaderView(columnHeader);
                scrollPane.setRowHeaderView(rowHeader);
                scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, headerCorner);
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = new Dimension();
        d.setSize(width, height);
        return d;
    }

    private void rebuildBuffer() {
        buffer = new BufferedImage((int)width, (int)height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = buffer.createGraphics();
        g2d.setColor(Color.GRAY);

        drawVerticalLines(g2d);
        drawHorizontalLines(g2d);
        drawValues(g2d);
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

    protected class RangeMatrixHandler implements RangeMatrixListener {

        @Override
        public void columnHeaderChanged(RangeMatrixEvent e) {
            columnHeader.rebuildBuffer();
        }

        @Override
        public void rowHeaderChanged(RangeMatrixEvent e) {
            rowHeader.rebuildBuffer();
        }

        @Override
        public void valueChanged(RangeMatrixEvent e) {
            rebuildBuffer();
        }
    }
}
