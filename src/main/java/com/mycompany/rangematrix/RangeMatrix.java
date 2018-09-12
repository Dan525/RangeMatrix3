/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.rangematrix;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.infomatiq.jsi.SpatialIndex;
import com.infomatiq.jsi.rtree.RTree;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.CellRendererPane;
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
    private IRangeMatrixRenderer renderer;
    private CellRendererPane crp;
    private SpatialIndex rTree;
    private Table<Integer, Integer, RangeMatrixTableButton> buttonTable;
    
    private double width;
    private double height;
    private BufferedImage buffer;

    public RangeMatrix(RangeMatrixModel model) {
        columnHeader = new RangeMatrixColumnHeader(this);
        rowHeader = new RangeMatrixRowHeader(this);
        headerCorner = new RangeMatrixHeaderCorner(this, columnHeader, rowHeader);
        rTree = new RTree();
        rTree.init(null);
        
        renderer = new DefaultRangeMatrixRenderer();
        crp = new CellRendererPane();
        
        doSetModel(model);
    }

    public RangeMatrixModel getModel() {
        return model;
    }

    public void setModel(RangeMatrixModel model) {
        doSetModel(model);

    }

    private void doSetModel(RangeMatrixModel model) {
        
        this.model = model;
        columnHeader.setModel();
        rowHeader.setModel();
        headerCorner.setModel();

        buttonTable = HashBasedTable.create();
        setupRowsWidthList();
        calculateWidthOfComponents();
        calculateHeightOfComponents();
        calculateCells();
        
        this.addMouseListener(new RangeMatrixMouseHandler());

    }

    public IRangeMatrixRenderer getRenderer() {
        return renderer;
    }

    public void setRenderer(IRangeMatrixRenderer renderer) {
        this.renderer = renderer;
    }

    public CellRendererPane getCrp() {
        return crp;
    }

    public void setCrp(CellRendererPane crp) {
        this.crp = crp;
    }

    public RangeMatrixHeaderCorner getHeaderCorner() {
        return headerCorner;
    }

    public Table<Integer, Integer, RangeMatrixTableButton> getButtonTable() {
        return buttonTable;
    }

    public void calculateWidthOfComponents() {
        columnHeader.calculateWidthOfComponent();
        rowHeader.calculateWidthOfComponent();
        headerCorner.calculateWidthOfComponent();
        calculateWidthOfComponent();
    }

    public void calculateHeightOfComponents() {
        columnHeader.calculateHeightOfComponent();
        rowHeader.calculateHeightOfComponent();
        headerCorner.calculateHeightOfComponent();
        calculateHeightOfComponent();
    }

    public ArrayList<Double> maxOfTwoLists(ArrayList<Double> rowsWidthList, ArrayList<Double> cornerRowsWidthList) {
        ArrayList<Double> newList = new ArrayList<>();
        for (int i = 0; i < rowHeader.getColumnCount(); i++) {
            newList.add(Math.max(rowsWidthList.get(i), cornerRowsWidthList.get(i)));
        }
        return newList;
    }

    public void setupRowsWidthList() {
        ArrayList<Double> rowsWidthList = rowHeader.fillRowsWidthList();
        ArrayList<Double> cornerRowsWidthList = headerCorner.fillRowsWidthList();

        ArrayList<Double> newList = maxOfTwoLists(rowsWidthList, cornerRowsWidthList);

        rowHeader.setRowsWidthList(newList);
    }

//    public void drawVerticalLines(Graphics2D g2d) {
//        List<Double> cellXList = columnHeader.getCellXList();
//        List<Double> cellWidthList = columnHeader.getCellWidthList();
//
//        for (int i = 0; i < cellXList.size(); i++) {
//            double x = cellXList.get(i) + cellWidthList.get(i) - 1;
//            Shape l = new Line2D.Double(x, 0, x, height);
//            g2d.draw(l);
//        }
//    }
//
//    public void drawHorizontalLines(Graphics2D g2d) {
//        ArrayList<Double> cellYList = rowHeader.getCellYList();
//
//        for (int i = 0; i < cellYList.size(); i++) {
//            double y = cellYList.get(i) + rowHeader.getMinimalCellHeight() - 1;
//            Shape l = new Line2D.Double(0, y, width, y);
//            g2d.draw(l);
//        }
//    }
    
    public RangeMatrixTableButton findButtonInTable(int column, int row) {
        
        RangeMatrixTableButton button = buttonTable.get(row, column);
        if (button == null) {
            Object value = model.getValueAt(column, row);
            button = new RangeMatrixTableButton(value);
            //////////
            button.setColumn(column);
            button.setRow(row);
            button.setButtonName(value.toString());
            //////////
            buttonTable.put(row, column, button);
        }
        return button;
    }
    
    public void ignorePaintColumns(RangeMatrixHeaderButton button, int collapsedCount, int columnIndex, boolean isCollapsed) {
        //int collapsedCount = model.getColumnGroupCount(button.getButtonObject());
        for (int i = 1; i < collapsedCount; i++) {
            Map<Integer, RangeMatrixTableButton> column = buttonTable.column(columnIndex + i);
            for (RangeMatrixTableButton entry : column.values()) {
                entry.setCollapsed(isCollapsed);
            }
        }
    }
    
    public void makeColumnLeading(int columnIndex, double cellWidth, boolean isLeading) {
        Map<Integer, RangeMatrixTableButton> column = buttonTable.column(columnIndex);
        //Graphics2D g = (Graphics2D) this.getGraphics();
        
        for (int row = 0; row < column.size(); row++) {
            RangeMatrixTableButton button = column.get(row);
            button.setLeading(isLeading);
            button.setWidth(cellWidth);
            BufferedImage bufferedCell = new BufferedImage((int) button.getWidth(), (int) button.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = bufferedCell.createGraphics();
                
                JLabel label = renderer.getCellRendererComponent(columnIndex, row, button.getButtonName(), button.isLeading());
                label.setBounds((int)button.getX(),
                            (int)button.getY(),
                            (int)button.getWidth(),
                            (int)button.getHeight());
                label.paint(g2d);
                button.setImg(bufferedCell);
        }
    }
    
    public void shiftColumnsAfterCollapse(double shift, int columnIndex) {
        for (Cell<Integer, Integer, RangeMatrixTableButton> cell : buttonTable.cellSet()) {
            RangeMatrixTableButton button = cell.getValue();
            if (button.getColumn() > columnIndex) {
                button.setX(button.getX() + shift);
            }
        }
//        buttonTable.cellSet().stream()
//                .filter(cell -> (cell.getValue().getColumn() > columnIndex))
//                .map(cell -> (cell.getValue().setX(width-100)));
//        buffer.getSubimage(x, 0, buffer.getWidth()-x, buffer.getHeight());
    }
    
    public void calculateCells() {
        List<Double> cellXList = columnHeader.getCellXList();
        List<Double> cellWidthList = columnHeader.getCellWidthList();
        ArrayList<Double> cellYList = rowHeader.getCellYList();
        double minimalCellHeight = rowHeader.getMinimalCellHeight();
        List<RangeMatrixHeaderButton> leafButtonList = columnHeader.getLeafButtonList();

        for (int column = 0; column < leafButtonList.size(); column++) {
            for (int row = 0; row < cellYList.size(); row++) {
                
                RangeMatrixTableButton button = findButtonInTable(column, row);
                ///////////
                button.setHeight(minimalCellHeight);
                button.setWidth(leafButtonList.get(column).getWidth());
                button.setX(leafButtonList.get(column).getX());
                button.setY(cellYList.get(row));
                ///////////
                BufferedImage bufferedCell = new BufferedImage((int) button.getWidth(), (int) button.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = bufferedCell.createGraphics();
                
                JLabel label = renderer.getCellRendererComponent(column, row, button.getButtonName(), button.isLeading());
                label.setBounds((int)button.getX(),
                            (int)button.getY(),
                            (int)button.getWidth(),
                            (int)button.getHeight());
                label.paint(g2d);
                button.setImg(bufferedCell);
//                File outputfile = new File("image" + column + row + ".png");
//                try {
//                    ImageIO.write(bufferedCell, "png", outputfile);
//                } catch (IOException ex) {
//                    Logger.getLogger(RangeMatrix.class.getName()).log(Level.SEVERE, null, ex);
//                }
            }
        }
    }
    

    public void drawValues(Graphics2D g2d) {
        List<Double> cellXList = columnHeader.getCellXList();
        List<Double> cellWidthList = columnHeader.getCellWidthList();
        ArrayList<Double> cellYList = rowHeader.getCellYList();
        double minimalCellHeight = rowHeader.getMinimalCellHeight();
        List<RangeMatrixHeaderButton> leafButtonList = columnHeader.getLeafButtonList();

        for (int column = 0; column < leafButtonList.size(); column++) {
            for (int row = 0; row < cellYList.size(); row++) {
                RangeMatrixTableButton button = findButtonInTable(column, row);

                if (!button.isCollapsed()) {
                    g2d.drawImage(button.getImg(), (int)button.getX(), (int)button.getY(), this);
                }
                
            }
        }
    }

    public void calculateWidthOfComponent() {
        width = columnHeader.getWidthOfComponent();
    }

    public double getWidthOfComponent() {
        return width;
    }

    public void calculateHeightOfComponent() {
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
                
                scrollPane.getVerticalScrollBar().setUnitIncrement(10);
                scrollPane.getHorizontalScrollBar().setUnitIncrement(10);
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = new Dimension();
        d.setSize(width, height);
        return d;
    }

    public void rebuildBuffer() {
        buffer = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = buffer.createGraphics();
        g2d.setColor(Color.GRAY);

        //drawVerticalLines(g2d);
        //drawHorizontalLines(g2d);
        drawValues(g2d);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (buffer == null) {
            rebuildBuffer();
        }
        Graphics2D g2d = (Graphics2D) g;
        //drawValues(g2d);
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
    
    protected class RangeMatrixMouseHandler implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            System.out.println("Table coordinates: " + e.getX() + ", " + e.getY());
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
