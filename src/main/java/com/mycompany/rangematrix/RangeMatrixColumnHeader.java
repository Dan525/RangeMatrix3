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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JLabel;

/**
 *
 * @author daniil_pozdeev
 */
public class RangeMatrixColumnHeader extends JComponent {

    private final RangeMatrix rm;
    private RangeMatrixModel model;
    private IRangeMatrixRenderer renderer;
    private CellRendererPane crp;
    private SpatialIndex rTree;
    
    private double spaceAroundName = 4;
    private List<RangeMatrixHeaderButton> buttonList;
    private Map<Object,RangeMatrixHeaderButton> buttonMap;
    //private Map<Integer,RangeMatrixHeaderButton> leafButtonMap;
    private List<Object> leafButtonList;
//    private List<Double> cellXList;
//    private List<Double> cellWidthList;
    private int rowCount;
    private BufferedImage buffer;
    private double width;
    private double height;
    private double minimalCellHeight;

    public RangeMatrixColumnHeader(RangeMatrix rm) {
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
//        cellXList = new ArrayList<>();
//        cellWidthList = new ArrayList<>();
        //leafButtonMap = new HashMap<>();
        leafButtonList = new ArrayList<>();

        calculateParams();

        this.addMouseListener(new RangeMatrixMouseHandler());
    }

    public RangeMatrixModel getModel() {
        return model;
    }
    
    public void calculateParams() {
        calculateMinimalCellHeight();
//        cellXList.clear();
//        cellWidthList.clear();
        leafButtonList.clear();
        calculateColumnCoordinates(null, 0, 0, 0);
        calculateColumnIndices(null, 0);
        calculateRowCount(null, new ArrayList<>(), 1);
        calculateWidthOfComponent();
        calculateHeightOfComponent();        
    }

    public void setSpaceAroundName(int newSpace) {
        this.spaceAroundName = newSpace;
    }

    public double calculateCellHeight(int heightMultiplier) {
        return minimalCellHeight * heightMultiplier;
    }

    public double calculateWidthByColumnName(Object column) {

        //String columnName = model.getColumnGroupName(column);
        RangeMatrixHeaderButton button = findButtonInMap(column);
        JLabel label = renderer.getColumnRendererComponent(button.getButtonObject(),
                                                           button.getButtonName(),
                                                           button.isCollapsed(), 
                                                           button.isGroup());
        double columnWidth = label.getPreferredSize().getWidth() + 2 * spaceAroundName;
        if (columnWidth > minimalCellHeight) {
            return columnWidth;
        } else {
            return minimalCellHeight;
        }
    }

    public double calculateWidthOfColumn(Object column) {
        double columnWidth = 0;
        double ownColumnWidth = calculateWidthByColumnName(column);
        RangeMatrixHeaderButton button = findButtonInMap(column);

        ArrayList<Object> leafColumnList = fillLeafColumnList(column, new ArrayList<>());
        if (leafColumnList.isEmpty()) {
            return ownColumnWidth;
        }
        for (Object leafColumn : leafColumnList) {
            double leafColumnWidth = calculateWidthByColumnName(leafColumn);
            columnWidth += leafColumnWidth;
        }
        if (columnWidth > ownColumnWidth) {
            button.setWidth(columnWidth);
            return columnWidth;
        } else {
            button.setWidth(ownColumnWidth);
            return ownColumnWidth;
        }
    }

    public void calculateRowCount(Object parentColumn, ArrayList<Integer> maxRowIndexList, int maxRowIndex) {
        int columnCount = model.getColumnGroupCount(parentColumn);

        for (int i = 0; i < columnCount; i++) {
            Object child = model.getColumnGroup(parentColumn, i);
            boolean isGroup = model.isColumnGroup(child);
//            RangeMatrixHeaderButton button = findButtonInMap(child);
//            if (button.isCollapsed()) {
//                isGroup = false;
//            } else {
//                isGroup = model.isColumnGroup(child);
//            }
            if (isGroup) {
                maxRowIndex++;
                calculateRowCount(child, maxRowIndexList, maxRowIndex);
                maxRowIndex--;
            }
            maxRowIndexList.add(maxRowIndex);
        }
        rowCount = Collections.max(maxRowIndexList);
    }

    public int getRowCount() {
        return rowCount;
    }

    public int calculateHeightMultiplier(Object parentColumn, boolean isGroup, int rowIndex) {
        if (!isGroup) {
            return (rowCount - rowIndex) + 1;
        } else {
            return 1;
        }
    }
    
//    public <K, V> Map<K, V> filterByValue(Map<K, V> map, Predicate<V> predicate) {
//        return map.entrySet()
//                .stream()
//                .filter(entry -> predicate.test(entry.getValue()))
//                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
//    }
//    
//    public RangeMatrixHeaderButton calculateButtonByColumnIndex(int columnIndex) {
//        
//    }
    
    public int calculateColumnIndices(Object parentColumn, int columnCounter) {
        int columnCount = model.getColumnGroupCount(parentColumn);

        for (int i = 0; i < columnCount; i++) {
            Object child = model.getColumnGroup(parentColumn, i);
            RangeMatrixHeaderButton button = findButtonInMap(child);

            boolean isGroup = model.isColumnGroup(child);

            if (isGroup) {
                columnCounter = calculateColumnIndices(child, columnCounter);
            } else {
                button.setColumn(columnCounter);
                //leafButtonMap.put(columnCounter, button);
                leafButtonList.add(child);
                columnCounter++;
            }
        }
        return columnCounter;
    }
    
    public void calculateColumnCoordinates(Object parentColumn, double parentCellX, int rowCounter, int columnCounter) {

        int columnCount = model.getColumnGroupCount(parentColumn);
        double cellX = parentCellX;

        for (int i = 0; i < columnCount; i++) {
            Object child = model.getColumnGroup(parentColumn, i);

            double cellWidth;
            RangeMatrixHeaderButton button = findButtonInMap(child);

            boolean isGroup;
            if (button.isCollapsed()) {
                isGroup = false;

                cellWidth = calculateWidthByColumnName(child);

            } else {
                isGroup = model.isColumnGroup(child);

                cellWidth = calculateWidthOfColumn(child);

            }
            button.setWidth(cellWidth);
            button.setX(cellX);

            if (isGroup) {
                calculateColumnCoordinates(child, cellX, rowCounter, columnCounter);
            }
            cellX += cellWidth;
        }
    }

    public List<Object> getLeafButtonList() {
        return leafButtonList;
    }

//    public Map<Integer, RangeMatrixHeaderButton> getLeafButtonMap() {
//        return leafButtonMap;
//    }

//    public List<Double> getCellXList() {
//        return cellXList;
//    }
//
//    public List<Double> getCellWidthList() {
//        return cellWidthList;
//    }

    public void calculateMinimalCellHeight() {
        JLabel label = renderer.getColumnRendererComponent(null, " ", false, false);
        minimalCellHeight = label.getPreferredSize().getHeight() + 2 * spaceAroundName;
    }

    public double getMinimalCellHeight() {
        return minimalCellHeight;
    }

    public void calculateWidthOfComponent() {
        width = calculateWidthOfColumn(null);
    }

    public double getWidthOfComponent() {
        return width;
    }

    public void calculateHeightOfComponent() {
        height = (minimalCellHeight * rowCount);
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

    public ArrayList<Object> fillLeafColumnList(Object parentColumn, ArrayList<Object> leafColumnList) {
        int columnCount = model.getColumnGroupCount(parentColumn);

        for (int i = 0; i < columnCount; i++) {
            Object child = model.getColumnGroup(parentColumn, i);
            boolean isGroup;
            RangeMatrixHeaderButton button = findButtonInMap(child);
            if (button.isCollapsed()) {
                isGroup = false;
            } else {
                isGroup = model.isColumnGroup(child);
            }
            if (isGroup) {
                fillLeafColumnList(child, leafColumnList);
            } else {
                leafColumnList.add(child);
            }
        }
        return leafColumnList;
    }
    
        public Map<Integer,Object> fillLeafColumnMap(Object parentColumn, Map<Integer,Object> leafColumnMap) {
        int columnCount = model.getColumnGroupCount(parentColumn);

        for (int i = 0; i < columnCount; i++) {
            Object child = model.getColumnGroup(parentColumn, i);
            boolean isGroup;
            RangeMatrixHeaderButton button = findButtonInMap(child);
            if (button.isCollapsed()) {
                isGroup = false;
            } else {
                isGroup = model.isColumnGroup(child);
            }
            if (isGroup) {
                fillLeafColumnMap(child, leafColumnMap);
            } else {
                //RangeMatrixHeaderButton leafButton = findButtonInMap(child);
                int columnIndex = calculateColumnIndex(findButtonInMap(child));
                leafColumnMap.put(columnIndex, child);
            }
        }
        return leafColumnMap;
    }
    
    public int calculateColumnCount(Object parentColumn, int columnCounter) {
        
        int columnCount = model.getColumnGroupCount(parentColumn);

        for (int i = 0; i < columnCount; i++) {
            Object child = model.getColumnGroup(parentColumn, i);
            boolean isGroup = model.isColumnGroup(child);
            RangeMatrixHeaderButton button = findButtonInMap(child);
//            if (button.isCollapsed()) {
//                isGroup = false;
//            } else {
//                isGroup = model.isColumnGroup(child);
//            }
            if (isGroup) {
                columnCounter = calculateColumnCount(child, columnCounter);
            } else {
                columnCounter++;
            }
        }
        return columnCounter;
    }
    
    public RangeMatrixHeaderButton findButtonInMap(Object child) {

        String columnName = model.getColumnGroupName(child);
        RangeMatrixHeaderButton button = buttonMap.get(child);

        if (button == null) {
            button = new RangeMatrixHeaderButton(child, columnName);
            buttonMap.put(child, button);
        }
        return button;
    }
    
    public void calculateButtonGroupAttribute(Object child, RangeMatrixHeaderButton button) {
        if (model.isColumnGroup(child)) {
            button.setGroup(true);
        } else {
            button.setGroup(false);
        }
    }
    
    public void calculateColumns(Object parentColumn, double parentCellX, double parentCellY, int rowCounter) {

        boolean isGroup;
        double cellWidth;

        int columnCount = model.getColumnGroupCount(parentColumn);
        double cellX = parentCellX;
        double cellY = parentCellY;

        for (int i = 0; i < columnCount; i++) {
            
            Object child = model.getColumnGroup(parentColumn, i);
            
            RangeMatrixHeaderButton button = findButtonInMap(child);

            if (button.isCollapsed()) {
                calculateButtonGroupAttribute(child, button);
                isGroup = false;
                cellWidth = calculateWidthByColumnName(child);
            } else {
                calculateButtonGroupAttribute(child, button);
                isGroup = model.isColumnGroup(child);
                cellWidth = calculateWidthOfColumn(child);
            }

            int heightMultiplier = calculateHeightMultiplier(parentColumn, isGroup, rowCounter);

            double cellHeight = calculateCellHeight(heightMultiplier);
            
            //button.setX(cellX);
            button.setY(cellY);            
            //button.setWidth(cellWidth);
            button.setHeight(cellHeight);
            
            buttonList.add(button);
            
            Rectangle rect = new Rectangle((float)cellX, (float)cellY, (float)(cellX + cellWidth), (float)(cellY + cellHeight));
            
            rTree.add(rect, buttonList.indexOf(button));

            if (isGroup) {
                rowCounter++;
                cellY += cellHeight;
                calculateColumns(child, cellX, cellY, rowCounter);
                rowCounter--;
                cellY -= cellHeight;
            }
            cellX += cellWidth;
        }
    }
    
    public void drawColumns(Graphics2D g2d) {
        
        for (RangeMatrixHeaderButton button : buttonList) {
            crp.paintComponent(g2d, 
                               renderer.getColumnRendererComponent(button.getButtonObject(),
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

        calculateColumns(null, 0, 0, 1);
        drawColumns(g2d);
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

//    public void processingClickOnColumn(Object column) {
//        if (collapsedColumns.contains(column)) {
//            collapsedColumns.remove(column);
//        } else {
//            collapsedColumns.add(column);
//        }
//    }
    
//    public double calculateWidthOfLeafButtons(RangeMatrixHeaderButton button) {
//        
//        ArrayList<Object> leafColumns = fillLeafColumnList(button.getButtonObject(), new ArrayList<>());
//        double widthOfLeafButtons = 0;
//        for (Object leafColumn : leafColumns) {
//            RangeMatrixHeaderButton leafButton = buttonMap.get(leafColumn);
//            if (!leafButton.isCollapsed()) {
//                widthOfLeafButtons += buttonMap.get(leafColumn).getWidth();
//            }
//        }
//        return widthOfLeafButtons;
//    }
    
    public void processingClickOnColumn(RangeMatrixHeaderButton button) {
        
        int columnIndex = calculateColumnIndex(button);
        
        int collapsedCount = calculateColumnCount(button.getButtonObject(), 0);
        
        if (button.isCollapsed() && button.isGroup()) {
            
            RangeMatrixHeaderButton leafHeaderButton = findButtonInMap(leafButtonList.get(columnIndex));
            double widthOfLeadingColumn = leafHeaderButton.getWidth();//calculateWidthOfColumn(model.getColumnGroup(button.getButtonObject(), 0));
            button.setCollapsed(false);
            calculateParams();
            
            rm.makeColumnLeading(columnIndex, widthOfLeadingColumn, false);
            rm.ignorePaintColumns(button, collapsedCount, columnIndex, false);
            rm.shiftColumnsAfterCollapse(columnIndex);
            
        } else if (!button.isCollapsed() && button.isGroup()) {
            
            double widthByName = calculateWidthByColumnName(button.getButtonObject());
            button.setCollapsed(true);
            calculateParams();
            
            rm.makeColumnLeading(columnIndex, widthByName, true);
            rm.ignorePaintColumns(button, collapsedCount, columnIndex, true);
            rm.shiftColumnsAfterCollapse(columnIndex);
        }
    }
    
    public int calculateColumnIndex(RangeMatrixHeaderButton button) {
        int columnIndex;
        if (model.isColumnGroup(button.getButtonObject())) {
            Object leaf = fillLeafColumnList(button.getButtonObject(), new ArrayList<>()).get(0);
            columnIndex = findButtonInMap(leaf).getColumn();
        } else {
            columnIndex = button.getColumn();
        }

        return columnIndex;
    }

    protected class RangeMatrixMouseHandler implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            Point rTreePoint = new Point(e.getX(), e.getY());
            
            rTree.nearest(rTreePoint, 
                          new TIntProcedure() {
                            @Override
                            public boolean execute(int i) {
                                System.out.println(buttonList.get(i));
                                RangeMatrixHeaderButton button = buttonList.get(i);
                                
                                System.out.println(calculateColumnIndex(button));
                                //calculateColumns(button.getButtonObject(), button.getX(), button.getY(), 1);
                                processingClickOnColumn(button);
                                return false;
                            }
                          },
                          Float.MAX_VALUE);
            
            rTree = new RTree();
            rTree.init(null);
            calculateParams();
            rebuildBuffer();
            repaint();
            rm.calculateHeightOfComponent();
            rm.calculateWidthOfComponent();
            //rm.calculateCells();
            rm.rebuildBuffer();
            rm.repaint();
//            rm.getHeaderCorner().calculateHeightOfComponent();
//            rm.getHeaderCorner().rebuildBuffer();
//            rm.getHeaderCorner().repaint();            
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
