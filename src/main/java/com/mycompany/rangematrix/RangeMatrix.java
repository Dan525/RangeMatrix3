/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.rangematrix;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
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
import javax.swing.CellRendererPane;

/**
 *
 * @author daniil_pozdeev
 */
public class RangeMatrix extends JComponent {

    private RangeMatrixModel model;
    private final RangeMatrixColumnHeader columnHeader;
    private final RangeMatrixRowHeader rowHeader;
    private final RangeMatrixHeaderCorner headerCorner;
    private IRangeMatrixRenderer renderer;
    private CellRendererPane crp;
    private double width;
    private double height;
    private BufferedImage buffer;

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

        renderer = new DefaultRangeMatrixRenderer();
        crp = new CellRendererPane();

        this.model = model;

        columnHeader.setModel(model, renderer, crp);
        rowHeader.setModel(model, renderer, crp);
        headerCorner.setModel(model, renderer, crp);

        setRowsWidthList();
        setWidthOfComponents();
        setHeightOfComponents();

    }

    public void setWidthOfComponents() {
        columnHeader.setWidthOfComponent();
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

    public void setRowsWidthList() {
        ArrayList<Double> rowsWidthList = rowHeader.calculateRowsWidthList();
        ArrayList<Double> cornerRowsWidthList = headerCorner.calculateRowsWidthList();

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
        ArrayList<Double> cellXList = columnHeader.getCellXList();
        ArrayList<Double> cellWidthList = columnHeader.getCellWidthList();
        ArrayList<Double> cellYList = rowHeader.getCellYList();
        double minimalCellHeight = rowHeader.getMinimalCellHeight();

        for (int i = 0; i < cellYList.size(); i++) {
            for (int j = 0; j < cellXList.size(); j++) {
                String value = (model.getValueAt(j, i)).toString();

                crp.paintComponent(g2d, renderer.getCellRendererComponent(j, i, value), this,
                                   cellXList.get(j).intValue(),
                                   cellYList.get(i).intValue(),
                                   cellWidthList.get(j).intValue() - 1,
                                   (int) minimalCellHeight - 1);
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
        buffer = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB);

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
