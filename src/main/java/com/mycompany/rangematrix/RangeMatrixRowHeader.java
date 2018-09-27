/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.rangematrix;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Table;
import com.infomatiq.jsi.Point;
import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.SpatialIndex;
import com.infomatiq.jsi.rtree.RTree;
import gnu.trove.TIntProcedure;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
    
    //private ArrayList<Double> cellYList;
    private ArrayList<RangeMatrixHeaderButton> buttonList;
    private Map<Object,RangeMatrixHeaderButton> buttonMap;
    private Map<Object,RangeMatrixHeaderButton> typeButtonMap;
    //private Map<Object,RangeMatrixHeaderButton> emptyButtonMap;
    private Table<Object, Integer, RangeMatrixHeaderButton> emptyButtonTable;
    private List<Object> leafButtonList;
    private double minimalCellHeight;
    private int columnCount;
    private int rowCount;
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
        buttonMap = new HashMap<>();
        typeButtonMap = new HashMap<>();
        emptyButtonTable = HashBasedTable.create();
        //cellYList = new ArrayList<>();
        leafButtonList = new ArrayList<>();
        rowCount = calculateTableRowsCount(null, 0);
        
        calculateParams();
        
        this.addMouseListener(new RangeMatrixMouseHandler());
    }
    
    public void calculateParams() {
        leafButtonList.clear();
        buttonList.clear();
        calculateMinimalCellHeight();
        calculateRowCoordinates(null, 0);
        calculateColumnCount(null, new ArrayList<>(), 1);
        
        calculateRowIndices(null, 0);
    }

    public RangeMatrixModel getModel() {
        return model;
    }

    public void setSpaceAroundName(int newSpace) {
        this.spaceAroundName = newSpace;
    }

    public void calculateMinimalCellHeight() {
        JLabel label = renderer.getRowRendererComponent(null, " ", false, false);
        minimalCellHeight = label.getPreferredSize().getHeight() + 2 * spaceAroundName;
    }

    public double getMinimalCellHeight() {
        return minimalCellHeight;
    }

    public double calculateWidthOfRowByName(Object row) {
        //String rowName = model.getColumnGroupName(row);
        RangeMatrixHeaderButton button = findButtonInMap(row);
        JLabel label = renderer.getRowRendererComponent(button.getButtonObject(),
                                                        button.getButtonName(),
                                                        button.isCollapsed(), 
                                                        true);
        return label.getPreferredSize().getWidth() + 2 * spaceAroundName;
    }
    
    public double calculateWidthOfRowByType(Object row) {
        //String rowName = model.getColumnGroupType(row);
        RangeMatrixHeaderButton button = findTypeButtonInMap(row);
        JLabel label = renderer.getRowRendererComponent(button.getButtonObject(),
                                                        button.getButtonName(),
                                                        button.isCollapsed(), 
                                                        false);
        return label.getPreferredSize().getWidth() + 2 * spaceAroundName;
    }

    public ArrayList<Double> fillRowsWidthInColumnList(Object parent, int indexOfColumn, int columnCounter, ArrayList<Double> rowsOfColumnList) {
        int rowCount = model.getRowGroupCount(parent);

        for (int i = 0; i < rowCount; i++) {
            Object child = model.getRowGroup(parent, i);

            boolean isGroup;// = model.isColumnGroup(child);
            boolean hasType = model.hasType(child);
            
            RangeMatrixHeaderButton button = findButtonInMap(child);
            
            if (button.isCollapsed()) {
                isGroup = false;
            } else {
                isGroup = model.isRowGroup(child);
            }
            
            if (columnCounter == indexOfColumn) {
                rowsOfColumnList.add(calculateWidthOfRowByName(child));
            }
            if (hasType) {
                columnCounter++;
                if (columnCounter == indexOfColumn) {
                    rowsOfColumnList.add(calculateWidthOfRowByType(child));
                }
                columnCounter--;
            }
            if (isGroup && columnCounter < indexOfColumn) {
                if (hasType) {
                    columnCounter+=2;
                    fillRowsWidthInColumnList(child, indexOfColumn, columnCounter, rowsOfColumnList);
                    columnCounter-=2;
                } else {
                    columnCounter++;
                    fillRowsWidthInColumnList(child, indexOfColumn, columnCounter, rowsOfColumnList);
                    columnCounter--;
                }
                
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
        ArrayList<Object> leafRowList = fillLeafRowList(row, new ArrayList<>());

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
            
            boolean isGroup;
            boolean hasType = model.hasType(child);
            
            RangeMatrixHeaderButton button = findButtonInMap(child);
            
            if (button.isCollapsed()) {
                isGroup = false;
            } else {
                isGroup = model.isRowGroup(child);
            }
            
            if (isGroup) {
                if (hasType) {
                    maxColumnIndex+=2;
                    calculateColumnCount(child, maxColumnIndexList, maxColumnIndex);
                    maxColumnIndex-=2;
                } else {
                    maxColumnIndex++;
                    calculateColumnCount(child, maxColumnIndexList, maxColumnIndex);
                    maxColumnIndex--;
                }                
            } else {
                if (hasType) {
                    maxColumnIndex++;
                    maxColumnIndexList.add(maxColumnIndex);
                    maxColumnIndex--;
                } else {
                    maxColumnIndexList.add(maxColumnIndex);
                }
            }
            
        }
        columnCount = Collections.max(maxColumnIndexList);
    }

    public int getColumnCount() {
        return columnCount;
    }

    public List<Object> getLeafButtonList() {
        return leafButtonList;
    }
    
    public int calculateRowIndices(Object parentRow, int rowCounter) {
        int rowCount = model.getRowGroupCount(parentRow);

        for (int i = 0; i < rowCount; i++) {
            Object child = model.getRowGroup(parentRow, i);
            
            RangeMatrixHeaderButton button = findButtonInMap(child);

            boolean isGroup = model.isRowGroup(child);

            if (isGroup) {
                rowCounter = calculateRowIndices(child, rowCounter);
            } else {
                button.setCellIndex(rowCounter);
                //leafButtonMap.put(columnCounter, button);
                leafButtonList.add(child);
                rowCounter++;
            }
        }
        return rowCounter;
    }

    public void calculateRowCoordinates(Object parentRow, double parentCellY) {

        int rowCount = model.getRowGroupCount(parentRow);
        double cellY = parentCellY;

        for (int i = 0; i < rowCount; i++) {
            Object child = model.getRowGroup(parentRow, i);
            
            double cellHeight;
            boolean isGroup;
            boolean hasType = model.hasType(child);
            
            RangeMatrixHeaderButton button = findButtonInMap(child);

            if (button.isCollapsed()) {
                isGroup = false;

                cellHeight = minimalCellHeight;

            } else {
                isGroup = model.isRowGroup(child);

                cellHeight = calculateHeightOfRow(child);

            }
            
            button.setHeight(cellHeight);
            button.setY(cellY);
            
            if (hasType) {
                RangeMatrixHeaderButton typeButton = findTypeButtonInMap(child);
                typeButton.setHeight(cellHeight);
                typeButton.setY(cellY);
            }
            
            if (isGroup) {
                calculateRowCoordinates(child, cellY);
            }
            cellY += cellHeight;
        }
    }

    public void calculateWidthOfComponent() {
        width = 0;
        for (double rowWidth : rowsWidthList) {
            width += rowWidth;
        }
        //width += 300;
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

    public ArrayList<Object> fillLeafRowList(Object parentRow, ArrayList<Object> leafRowList) {
        int rowCount = model.getRowGroupCount(parentRow);

        for (int i = 0; i < rowCount; i++) {
            Object child = model.getRowGroup(parentRow, i);
            boolean isGroup;
            RangeMatrixHeaderButton button = findButtonInMap(child);
            
            if (button.isCollapsed()) {
                isGroup = false;
            } else {
                isGroup = model.isRowGroup(child);
            }
            
            if (isGroup) {
                fillLeafRowList(child, leafRowList);
            } else {
                leafRowList.add(child);
            }
        }
        return leafRowList;
    }
    
    public void calculateButtonGroupAttribute(Object child, RangeMatrixHeaderButton button) {
        if (model.isRowGroup(child)) {
            button.setGroup(true);
        } else {
            button.setGroup(false);
        }
    }
    
    public void calculateRows(Object parentRow, double parentCellX, double parentCellY, int columnCounter) {
        boolean isGroup;
        double cellHeight;
                
        int rowCount = model.getRowGroupCount(parentRow);
        double cellX = parentCellX;
        double cellY = parentCellY;

        for (int i = 0; i < rowCount; i++) {
            Object child = model.getRowGroup(parentRow, i);
            RangeMatrixHeaderButton button = findButtonInMap(child);
            boolean hasType = model.hasType(child);

            if (button.isCollapsed()) {
                calculateButtonGroupAttribute(child, button);
                isGroup = false;
                cellHeight = minimalCellHeight;
                
            } else {
                calculateButtonGroupAttribute(child, button);
                isGroup = model.isColumnGroup(child);
                cellHeight = calculateHeightOfRow(child);
            }
            double cellWidth = rowsWidthList.get(columnCounter);
            double cellWidthType = 0;
            
            //////////////////////////////////
            button.setX(cellX);
            //button.setY(cellY);
            //button.setGroup(isGroup);
            button.setWidth(cellWidth);
            //button.setHeight(cellHeight);
            //////////////////////////////////
            
            if (hasType) {
                columnCounter++;
                RangeMatrixHeaderButton typeButton = findTypeButtonInMap(child);
                
                cellWidthType = rowsWidthList.get(columnCounter);
                double cellXType = cellX + cellWidth;

                //////////////////////////////////
                typeButton.setX(cellXType);
                typeButton.setY(cellY);
                //typeButton.setGroup(isGroup);
                typeButton.setWidth(cellWidthType);
                typeButton.setHeight(cellHeight);
                //////////////////////////////////
                
                buttonList.add(typeButton);
                
                
                
                columnCounter--;
            }
            
            buttonList.add(button);
            
            Rectangle rect = new Rectangle((float)cellX, (float)cellY, (float)(cellX + cellWidth + cellWidthType), (float)(cellY + cellHeight));
            
            rTree.add(rect, buttonList.indexOf(button));

            if (isGroup) {
                if (hasType) {
                    columnCounter+=2;
                    cellX += (cellWidth + cellWidthType);
                    calculateRows(child, cellX, cellY, columnCounter);
                    columnCounter-=2;
                    cellX -= (cellWidth + cellWidthType);
                } else {
                    columnCounter++;
                    cellX += (cellWidth + cellWidthType);
                    calculateRows(child, cellX, cellY, columnCounter);
                    columnCounter--;
                    cellX -= (cellWidth + cellWidthType);
                }
                
            } else if (!isGroup && columnCounter < columnCount) {
                
                if (hasType) {
                    columnCounter+=2;
                    cellX += (cellWidth + cellWidthType);
                    calculateEmptyRows(child, cellX, cellY, columnCounter);
                    columnCounter-=2;
                    cellX -= (cellWidth + cellWidthType);
                } else {
                    columnCounter++;
                    cellX += (cellWidth + cellWidthType);
                    calculateEmptyRows(child, cellX, cellY, columnCounter);
                    columnCounter--;
                    cellX -= (cellWidth + cellWidthType);
                }
            }  else {
                //cellYList.add(cellY);
            }
            cellY += cellHeight;
        }
    }
    
    public void calculateEmptyRows(Object child,double parentCellX, double parentCellY, int columnCounter) {

        if (columnCounter < columnCount) {
            
            RangeMatrixHeaderButton button = findEmptyButtonInMap(child, columnCounter);
            double cellX = parentCellX;
            double cellWidth = rowsWidthList.get(columnCounter);
            boolean isGroup = false;
            
            button.setX(cellX);
            button.setY(parentCellY);
            button.setGroup(isGroup);
            button.setWidth(cellWidth);
            button.setHeight(minimalCellHeight);
            
            buttonList.add(button);
            
            //Rectangle rect = new Rectangle((float)cellX, (float)parentCellY, (float)(cellX + cellWidth), (float)(parentCellY + minimalCellHeight));
            
            //rTree.add(rect, buttonList.indexOf(button));
            
            cellX += cellWidth;
            columnCounter++;

            calculateEmptyRows(child, cellX, parentCellY, columnCounter);

            columnCounter--;
            cellX -= cellWidth;
        }

    }
    
    public RangeMatrixHeaderButton findButtonInMap(Object child) {
        
        RangeMatrixHeaderButton button = buttonMap.get(child);

        if (button == null) {
            
            String rowName;
            if (child == null) {
                rowName = "";
            } else {
                rowName = model.getRowGroupName(child);
            }

            button = new RangeMatrixHeaderButton(child, rowName);
            buttonMap.put(child, button);
        }
        return button;
    }
    
    public RangeMatrixHeaderButton findTypeButtonInMap(Object child) {
        
        RangeMatrixHeaderButton button = typeButtonMap.get(child);

        if (button == null) {
            
            String rowName;
            if (child == null) {
                rowName = "";
            } else {
                rowName = model.getRowGroupType(child);
            }

            button = new RangeMatrixHeaderButton(child, rowName);
            typeButtonMap.put(child, button);
        }
        return button;
    }
    
    public RangeMatrixHeaderButton findEmptyButtonInMap(Object child, int columnCounter) {
        
        RangeMatrixHeaderButton button = emptyButtonTable.get(child, columnCounter);

        if (button == null) {
            
            String rowName = "";

            button = new RangeMatrixHeaderButton(child, rowName);
            emptyButtonTable.put(child, columnCounter, button);
        }
        return button;
    }
    
    public void drawRows(Graphics2D g2d) {
        
        for (RangeMatrixHeaderButton button : buttonList) {
            crp.paintComponent(g2d, 
                               renderer.getRowRendererComponent(button.getButtonObject(),
                                                                button.getButtonName(),
                                                                button.isCollapsed(), 
                                                                button.isGroup()),
                               this,
                               (int) button.getX(),
                               (int) button.getY(),
                               (int) button.getWidth(),
                               (int) button.getHeight());
        }
        
    }

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
        RenderingHints rh = new RenderingHints(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHints(rh);
        g2d.drawImage(buffer, 0, 0, this);
    }
    
    public void processingClickOnRow(RangeMatrixHeaderButton button) {
        
        int rowIndex = calculateRowIndex(button);
        
        if (button.isCollapsed() && button.isGroup()) {
            
            //TreeMap<Integer, Object> leafRowMap = (TreeMap<Integer, Object>) fillLeafRowMap(button.getButtonObject(), new TreeMap<>());
            //RangeMatrixHeaderButton leafHeaderButton = findButtonInMap(leafRowMap.firstEntry().getValue());
            //double heightOfLeadingRow = leafHeaderButton.getHeight();
            
            button.setCollapsed(false);
            calculateParams();
            rm.makeRowLeading(button, false);
            int collapsedRowCount = rm.ignorePaintRows(button, false);
            rm.shiftRowsAfterCollapse(rowIndex, collapsedRowCount, false);
            
        } else if (!button.isCollapsed() && button.isGroup()) {
            
            //double widthByName = calculateWidthByColumnName(button.getButtonObject());
            button.setCollapsed(true);
            calculateParams();
            rm.makeRowLeading(button, true);
            int collapsedRowCount = rm.ignorePaintRows(button, true);
            rm.shiftRowsAfterCollapse(rowIndex, collapsedRowCount, false);
        }
    }
    
    /**
     * Возвращает индекс ряда. Индекс рассчитывается из условия, что все
     * ряды развернуты (при этом не важно, развернуты они в данный момент
     * или нет). Для рядов из всех колонок, кроме последней, индекс
     * берется по первому из предков, находящемуся в последней (крайней правой) колонке.
     * @param button
     * @return
     */
    public int calculateRowIndex(RangeMatrixHeaderButton button) {
        int rowIndex;
        if (model.isRowGroup(button.getButtonObject())) {
            Object leaf = fillLeafRowFullList(button.getButtonObject(), new ArrayList<>()).get(0);
            rowIndex = findButtonInMap(leaf).getCellIndex();
        } else {
            rowIndex = button.getCellIndex();
        }

        return rowIndex;
    }
    
    /**
     * Возвращает список всех листовых элементов объекта, независимо от того, 
     * свернуты некоторые из предков или нет.
     * @param parentRow
     * @param leafRowList
     * @return
     */
    public ArrayList<Object> fillLeafRowFullList(Object parentRow, ArrayList<Object> leafRowList) {
        int rowCount = model.getRowGroupCount(parentRow);

        for (int i = 0; i < rowCount; i++) {
            Object child = model.getRowGroup(parentRow, i);
            boolean isGroup = model.isRowGroup(child);
            
            if (isGroup) {
                fillLeafRowFullList(child, leafRowList);
            } else {
                leafRowList.add(child);
            }
        }
        return leafRowList;
    }
    
    public int calculateTableRowsCount(Object parentRow, int rowCounter) {
        int groupRowCount = model.getRowGroupCount(parentRow);

        for (int i = 0; i < groupRowCount; i++) {
            Object child = model.getRowGroup(parentRow, i);
            boolean isGroup = model.isRowGroup(child);
            
            if (isGroup) {
                rowCounter = calculateTableRowsCount(child, rowCounter);
            } else {
                rowCounter++;
            }
        }
        return rowCounter;
    }

    public int getRowCount() {
        return rowCount;
    }
    
    /**
     * Возвращает таблицу "индекс ряда - объект" из объектов, принадлежащих
     * кнопкам, которые являются видимыми. Используя реализацию TreeMap,
     * получаем ключи в отсортированном виде. Это нужно для получения первой
     * кнопки - элемента с наименьшим индексом.
     *
     * @param parentRow
     * @param leafRowMap
     * @return
     */
    public Map<Integer, Object> fillLeafRowMap(Object parentRow, Map<Integer, Object> leafRowMap) {
        int rowCount = model.getRowGroupCount(parentRow);

        for (int i = 0; i < rowCount; i++) {
            Object child = model.getRowGroup(parentRow, i);
            boolean isGroup;
            RangeMatrixHeaderButton button = findButtonInMap(child);
            if (button.isCollapsed()) {
                isGroup = false;
            } else {
                isGroup = model.isRowGroup(child);
            }
            if (isGroup) {
                fillLeafRowMap(child, leafRowMap);
            } else {
                //RangeMatrixHeaderButton leafButton = findButtonInMap(child);
                int rowIndex = calculateRowIndex(findButtonInMap(child));
                leafRowMap.put(rowIndex, child);
            }
        }
        return leafRowMap;
    }
    
    /**
     * Возвращает индексы всех не свернутых кнопок - предков кнопки,
     * на которую было произведено нажатие. Нужно для присвоения аттрибута 
     * Collapsed всем ячейкам таблицы, начиная со столбца, находящегося под 
     * второй кнопкой из списка предков. 
     * @param parentRow
     * @param leafRowIndexList
     * @return
     */
    public ArrayList<Integer> fillLeafRowIndexList(Object parentRow, ArrayList<Integer> leafRowIndexList) {
        int rowCount = model.getRowGroupCount(parentRow);

        for (int i = 0; i < rowCount; i++) {
            Object child = model.getRowGroup(parentRow, i);
            boolean isGroup;
            RangeMatrixHeaderButton button = findButtonInMap(child);
            if (button.isCollapsed()) {
                isGroup = false;
            } else {
                isGroup = model.isRowGroup(child);
            }
            if (isGroup) {
                fillLeafRowIndexList(child, leafRowIndexList);
            } else {
                int rowIndex = calculateRowIndex(findButtonInMap(child));
                leafRowIndexList.add(rowIndex);
            }
        }
        return leafRowIndexList;
    }
    
    public ArrayList<Integer> fillFullLeafRowIndexList(Object parentRow, ArrayList<Integer> leafRowIndexList) {
        int rowCount = model.getRowGroupCount(parentRow);

        for (int i = 0; i < rowCount; i++) {
            Object child = model.getRowGroup(parentRow, i);
            boolean isGroup = model.isRowGroup(child);
            
            if (isGroup) {
                fillFullLeafRowIndexList(child, leafRowIndexList);
            } else {
                int rowIndex = calculateRowIndex(findButtonInMap(child));
                leafRowIndexList.add(rowIndex);
            }
        }
        return leafRowIndexList;
    }
    
    protected class RangeMatrixMouseHandler implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            Point rTreePoint = new Point(e.getX(), e.getY());
            rTree.nearest(rTreePoint, new TIntProcedure() {         // a procedure whose execute() method will be called with the results
                @Override
                public boolean execute(int i) {
                    System.out.println(buttonList.get(i));
                    RangeMatrixHeaderButton button = buttonList.get(i);
                    processingClickOnRow(button);
                    return false;              // return true here to continue receiving results
                }
            }, 0);
            rTree = new RTree();
            rTree.init(null);
            calculateParams();
            rm.setupRowsWidthList();
            calculateWidthOfComponent();
            calculateHeightOfComponent();
            rebuildBuffer();
            revalidate();
            repaint();
            
            rm.calculateHeightOfComponent();
            rm.calculateWidthOfComponent();
            rm.clearTableRTree();
            rm.rebuildBuffer();
            rm.revalidate();
            rm.repaint();
            
            rm.getHeaderCorner().calculateHeightOfComponent();
            rm.getHeaderCorner().rebuildBuffer();
            rm.getHeaderCorner().revalidate();
            rm.getHeaderCorner().repaint();
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
