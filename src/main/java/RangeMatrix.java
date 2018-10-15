/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.infomatiq.jsi.Point;
import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.SpatialIndex;
import com.infomatiq.jsi.rtree.RTree;
import gnu.trove.TIntProcedure;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.CellRendererPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JWindow;
import javax.swing.UIManager;

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
    //JWindow toolTip;
    //JTable toolTipTable;
    //JLabel toolTipLabel;
    ToolTip toolTip;
    
    private int columnCount;
    private int rowCount;
    private RangeMatrixTableButton previousButton;
    private int currentCell = 0;
    
    private double width;
    private double height;
    private BufferedImage buffer;

    public RangeMatrix(RangeMatrixModel model, ToolTip toolTip) {
        columnHeader = new RangeMatrixColumnHeader(this);
        rowHeader = new RangeMatrixRowHeader(this);
        headerCorner = new RangeMatrixHeaderCorner(this, columnHeader, rowHeader);
        rTree = new RTree();
        rTree.init(null);
        
        renderer = new DefaultRangeMatrixRenderer();
        crp = new CellRendererPane();
        this.toolTip = toolTip;
        
        doSetModel(model);
    }
    
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
        headerCorner.setModel();
        rowHeader.setModel();

        buttonTable = HashBasedTable.create();
        setupRowsWidthList();
        calculateWidthOfComponents();
        calculateHeightOfComponents();
        calculateCells();
        columnCount = columnHeader.calculateTableColumnCount(null, 0);
        rowCount = rowHeader.calculateTableRowCount(null, 0);
        
        this.addMouseMotionListener(new RangeMatrixMouseMotionHandler());
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

    public ToolTip getToolTip() {
        return toolTip;
    }

    public RangeMatrixHeaderCorner getHeaderCorner() {
        return headerCorner;
    }

    public Table<Integer, Integer, RangeMatrixTableButton> getButtonTable() {
        return buttonTable;
    }

    public void calculateWidthOfComponents() {
        //columnHeader.calculateWidthOfComponent();
        rowHeader.calculateWidthOfComponent();
        headerCorner.calculateWidthOfComponent();
        calculateWidthOfComponent();
    }

    public void calculateHeightOfComponents() {
        //columnHeader.calculateHeightOfComponent();
        rowHeader.calculateHeightOfComponent();
        headerCorner.calculateHeightOfComponent();
        calculateHeightOfComponent();
    }

    public ArrayList<Double> maxOfTwoLists(ArrayList<Double> rowsWidthList, ArrayList<Double> cornerRowsWidthList) {
        ArrayList<Double> newList = new ArrayList<>();
        for (int i = 0; i < rowHeader.getLevelsCount(); i++) {
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
    
    public RangeMatrixTableButton findButtonInTable(int column, int row) {
        
        RangeMatrixTableButton button = buttonTable.get(row, column);
        if (button == null) {
            Object value = model.getValueAt(column, row);
            button = new RangeMatrixTableButton(value);
            //////////
            button.setColumn(column);
            button.setRow(row);
            button.setCurrentRow(row);
            button.setButtonName(value.toString());
            //////////
            buttonTable.put(row, column, button);
        }
        return button;
    }
    
    public void ignorePaintColumns(RangeMatrixHeaderButton headerButton, boolean isCollapsed) {
        
        ArrayList<Integer> leafColumnIndexList = columnHeader.fillLeafColumnIndexList(headerButton.getButtonObject(), new ArrayList<>());

        for (Integer columnIndex : leafColumnIndexList.subList(1, leafColumnIndexList.size())) {
            Map<Integer, RangeMatrixTableButton> column = buttonTable.column(columnIndex);
            for (RangeMatrixTableButton button : column.values()) {
                button.setCollapsedByColumn(isCollapsed);
            }
        }
    }
    
    public int recalculateBrotherColumn(RangeMatrixHeaderButton leadingHeaderButton, int leadingColumnIndex) {
        Object parentObject;
        RangeMatrixHeaderButton parentHeaderButton = leadingHeaderButton;
        do {
        parentObject = parentHeaderButton.getParentObject();
        parentHeaderButton = columnHeader.findButtonInMap(parentObject);
        } while (parentObject != null);
        TreeMap<Integer, Object> leafColumnMap = (TreeMap<Integer, Object>) columnHeader.fillLeafColumnMap(parentHeaderButton.getButtonObject(), new TreeMap<>());
        int columnIndex = leadingColumnIndex;
        
        for (Object headerButtonObject : leafColumnMap.values()) {
            RangeMatrixHeaderButton headerButton = columnHeader.findButtonInMap(headerButtonObject);
            columnIndex = columnHeader.calculateColumnIndex(headerButton);
            
            //if (columnIndex != leadingColumnIndex) {
                Map<Integer, RangeMatrixTableButton> column = buttonTable.column(columnIndex);
                for (int row = 0; row < column.size(); row++) {
                    RangeMatrixTableButton button = column.get(row);
                    button.setX(headerButton.getX());
                    button.setWidth(headerButton.getWidth());
                    repaintCell(button);
                }
            //}
            
        }
        return columnIndex;
    }
    
    public void makeColumnLeading(RangeMatrixHeaderButton headerButton, double cellWidth, boolean isLeadingByColumn) {
        
        Object leadingHeaderButtonObject = columnHeader.fillLeafColumnList(headerButton.getButtonObject(), new ArrayList<>()).get(0);
        RangeMatrixHeaderButton leadingHeaderButton = columnHeader.findButtonInMap(leadingHeaderButtonObject);
        boolean isCollapsedLeadingButton = leadingHeaderButton.isCollapsed();
        
        ArrayList<Integer> leafColumnIndexList = columnHeader.fillLeafColumnIndexList(headerButton.getButtonObject(), new ArrayList<>());
        ArrayList<Integer> fullLeafColumnIndexList = columnHeader.fillFullLeafColumnIndexList(headerButton.getButtonObject(), new ArrayList<>());

        Map<Integer, RangeMatrixTableButton> column = buttonTable.column(leafColumnIndexList.get(0));
        
        int notEmptyInRowCounter = 0;
        
        for (int row = 0; row < column.size(); row++) {
            RangeMatrixTableButton button = column.get(row);

            if (isCollapsedLeadingButton) {
                button.setWidth(cellWidth);
            } else {
                button.setLeadingByColumn(isLeadingByColumn);
                button.setWidth(cellWidth);
            }
            
            if (isLeadingByColumn) {
                
                for (int columnIndex : fullLeafColumnIndexList) {
                    if (!buttonTable.get(row, columnIndex).getButtonName().isEmpty()) {
                        notEmptyInRowCounter++;
                        break;
                    }
                }
                
                button.getNotEmptyInRowStack().push(notEmptyInRowCounter > 0);
                notEmptyInRowCounter = 0;
            } else {
                button.getNotEmptyInRowStack().pop();
            }
            
            repaintCell(button);
        }
    }
    
    public void shiftColumnsAfterCollapse(int columnIndex) {
        //List<Object> leafButtonList = columnHeader.getLeafButtonList();
        Map<Integer, Object> leafColumnMap = columnHeader.fillLeafColumnMap(null, new HashMap<>());
        for (Cell<Integer, Integer, RangeMatrixTableButton> cell : buttonTable.cellSet()) {
            RangeMatrixTableButton button = cell.getValue();
            int columnIndexToShift = button.getColumn();
            if (columnIndexToShift > columnIndex) {
                RangeMatrixHeaderButton headerButton = columnHeader.findButtonInMap(leafColumnMap.get(columnIndexToShift));
                button.setX(headerButton.getX());
            }
        }
    }
    
    public int ignorePaintRows(RangeMatrixHeaderButton headerButton, boolean isCollapsed) {
        ArrayList<Integer> leafRowIndexList = rowHeader.fillLeafRowIndexList(headerButton.getButtonObject(), new ArrayList<>());
        int collapsedRowCount = leafRowIndexList.size() - 1;
        
        for (Integer rowIndex : leafRowIndexList.subList(1, leafRowIndexList.size())) {
            Map<Integer, RangeMatrixTableButton> row = buttonTable.row(rowIndex);
            for (RangeMatrixTableButton button : row.values()) {
                button.setCollapsedByRow(isCollapsed);
            }
        }
        return collapsedRowCount;
    }
    
    public void makeRowLeading(RangeMatrixHeaderButton headerButton, boolean isLeadingByRow) {
        
        Object leadingHeaderButtonObject = rowHeader.fillLeafRowList(headerButton.getButtonObject(), new ArrayList<>()).get(0);
        RangeMatrixHeaderButton leadingHeaderButton = rowHeader.findButtonInMap(leadingHeaderButtonObject);
        boolean isCollapsedLeadingButton = leadingHeaderButton.isCollapsed();
        
        ArrayList<Integer> leafRowIndexList = rowHeader.fillLeafRowIndexList(headerButton.getButtonObject(), new ArrayList<>());
        ArrayList<Integer> fullLeafRowIndexList = rowHeader.fillFullLeafRowIndexList(headerButton.getButtonObject(), new ArrayList<>());

        Map<Integer, RangeMatrixTableButton> row = buttonTable.row(leafRowIndexList.get(0));
        
        int notEmptyInColumnCounter = 0;
        
        for (int column = 0; column < row.size(); column++) {
            RangeMatrixTableButton button = row.get(column);
            
            if (!isCollapsedLeadingButton) {
                button.setLeadingByRow(isLeadingByRow);
            }
            
            if (isLeadingByRow) {
                
                for (int rowIndex : fullLeafRowIndexList) {
                    if (!buttonTable.get(rowIndex, column).getButtonName().isEmpty()) {
                        notEmptyInColumnCounter++;
                        break;
                    }
                }
                
                button.getNotEmptyInColumnStack().push(notEmptyInColumnCounter > 0);
                notEmptyInColumnCounter = 0;
            } else {
                button.getNotEmptyInColumnStack().pop();
            }
            repaintCell(button);
        }
    }
    
    public void shiftRowsAfterCollapse(int rowIndex, int collapsedRowCount, boolean isCollapsed) {
        //List<Object> leafButtonList = columnHeader.getLeafButtonList();
        Map<Integer, Object> leafRowMap = rowHeader.fillLeafRowMap(null, new HashMap<>());
        for (Cell<Integer, Integer, RangeMatrixTableButton> cell : buttonTable.cellSet()) {
            RangeMatrixTableButton button = cell.getValue();
            int rowIndexToShift = button.getRow();
            if (rowIndexToShift > rowIndex) {
                RangeMatrixHeaderButton headerButton = rowHeader.findButtonInMap(leafRowMap.get(rowIndexToShift));
                button.setY(headerButton.getY());
                
                int row = button.getCurrentRow();
                button.setCurrentRow(isCollapsed ? row + collapsedRowCount : row - collapsedRowCount);
                if (collapsedRowCount % 2 != 0) {
                    repaintCell(button);
                }
            }
        }
    }
    
    public void calculateCells() {
        List<Object> leafColumnButtonList = columnHeader.getLeafButtonList();
        List<Object> leafRowButtonList = rowHeader.getLeafButtonList();
        int indexInTable = 1; //нужен для выделения строки и столбца при наведении

        for (int column = 0; column < leafColumnButtonList.size(); column++) {
            for (int row = 0; row < leafRowButtonList.size(); row++) {
                
                RangeMatrixTableButton button = findButtonInTable(column, row);
                RangeMatrixHeaderButton columnHeaderButton = columnHeader.findButtonInMap(leafColumnButtonList.get(column));
                RangeMatrixHeaderButton rowHeaderButton = rowHeader.findButtonInMap(leafRowButtonList.get(row));
                ///////////
                button.setWidth(columnHeaderButton.getWidth());
                button.setX(columnHeaderButton.getX());
                button.setHeight(rowHeaderButton.getHeight());
                button.setY(rowHeaderButton.getY());
                button.setIndexInTable(indexInTable);
                ///////////

                indexInTable++;

                repaintCell(button);
            }
        }
    }
    
    public void addButtonToRTree(RangeMatrixTableButton button) {
        Rectangle rect = new Rectangle((float) button.getX(),
                (float) button.getY(),
                (float) (button.getX() + button.getWidth()),
                (float) (button.getY() + button.getHeight()));

        rTree.add(rect, button.getIndexInTable());
    }    

    public void drawValues(Graphics2D g2d) {
        List<Object> leafColumnButtonList = columnHeader.getLeafButtonList();
        List<Object> leafRowButtonList = rowHeader.getLeafButtonList();

        for (int column = 0; column < leafColumnButtonList.size(); column++) {
            for (int row = 0; row < leafRowButtonList.size(); row++) {
                RangeMatrixTableButton button = findButtonInTable(column, row);

                if (!button.isCollapsed()) {
                    g2d.drawImage(button.getImg(), (int)button.getX(), (int)button.getY(), this);
                    addButtonToRTree(button);
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
    
    public void calculateSizeOfComponent() {
        calculateHeightOfComponent();
        calculateWidthOfComponent();
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
    
    public void clearTableRTree() {
        rTree = new RTree();
        rTree.init(null);
    }
    
    public void iterateOnButtonCross(RangeMatrixTableButton button, boolean isEntered) {
        
        Map<Integer, RangeMatrixTableButton> column = buttonTable.column(button.getColumn());
        for (RangeMatrixTableButton verticalButton : column.values()) {
            verticalButton.setEntered(isEntered);
            repaintCell(verticalButton);
        }

        Map<Integer, RangeMatrixTableButton> row = buttonTable.row(button.getRow());
        for (RangeMatrixTableButton horizontalButton : row.values()) {
            horizontalButton.setEntered(isEntered);
            repaintCell(horizontalButton);
        }
    }
    
//    public void iterateOnButtonHorizontal(RangeMatrixTableButton button, boolean isEntered) {
//        Map<Integer, RangeMatrixTableButton> row = buttonTable.row(button.getRow());
//        for (RangeMatrixTableButton horizontalButton : row.values()) {
//            if (!horizontalButton.equals(button)) {
//                horizontalButton.setEntered(isEntered);
//                repaintCell(horizontalButton);
//            }
//        }
//    }
//    
//    public void iterateOnButtonVertical(RangeMatrixTableButton button, boolean isEntered) {
//        Map<Integer, RangeMatrixTableButton> column = buttonTable.column(button.getColumn());
//        for (RangeMatrixTableButton verticalButton : column.values()) {
//            if (!verticalButton.equals(button)) {
//                verticalButton.setEntered(isEntered);
//                repaintCell(verticalButton);
//            }
//        }
//    }
    
    private void repaintCell(RangeMatrixTableButton button) {
        BufferedImage bufferedCell = new BufferedImage((int) button.getWidth(), (int) button.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bufferedCell.createGraphics();

        JLabel label = renderer.getCellRendererComponent(button.getColumn(), button.getRow(), button);

        label.setBounds((int) button.getX(),
                (int) button.getY(),
                (int) button.getWidth(),
                (int) button.getHeight());
        label.paint(g2d);
        button.setImg(bufferedCell);
    }
    
    public void repaintCombo() {
        rebuildBuffer();
        revalidate();
        repaint();
    }
    
//    public void showToolTip(String text, int x, int y) {
//        toolTipLabel.setText(text);
//        toolTip.pack();
//        toolTip.setLocation(x,y);
//        toolTip.setVisible(true);
//    }
//    
//    public void hideToolTip()
//    {
//        toolTip.dispose();
//    }
//    
//    public boolean isToolTipShowing()
//    {
//        return toolTip.isShowing();
//    }

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
            //System.out.println("Table coordinates: " + e.getX() + ", " + e.getY());
        }

        @Override
        public void mousePressed(MouseEvent e) {
//            Point rTreePoint = new Point(e.getX(), e.getY());
//                rTree.nearest(rTreePoint,
//                        new TIntProcedure() {
//                    @Override
//                    public boolean execute(int i) {
//
//                        int enteredRow = i % rowCount == 0 ? rowCount : i % rowCount;
//                            int enteredColumn = i % rowCount == 0 ? i / rowCount : (i / rowCount) + 1;
//                            
//                            RangeMatrixTableButton tableButton = buttonTable.get(enteredRow - 1, enteredColumn - 1);
//                            
//                            if (tableButton.isLeading()) {
//                                String text = i + ": ряд " + enteredRow + ", колонка " + enteredColumn;
//                            
//                                toolTip.showToolTip(text, e.getXOnScreen(), e.getYOnScreen()-20);
//                            }
//                            
//                        return false;
//                    }
//                }, 0);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            //toolTip.hideToolTip();
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            
        }

        @Override
        public void mouseExited(MouseEvent e) {
//            java.awt.Point p = new java.awt.Point(e.getLocationOnScreen());
//        SwingUtilities.convertPointFromScreen(p, e.getComponent());
//        if(e.getComponent().contains(p)) {return;}
            if (previousButton != null) {
                iterateOnButtonCross(previousButton, false);
                currentCell = 0;
                repaintCombo();
            }
        }
    }
    
    protected class RangeMatrixMouseMotionHandler implements MouseMotionListener {

        @Override
        public void mouseDragged(MouseEvent e) {
            
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if (e.getX() <= getPreferredSize().getWidth() &&
                e.getY() <= getPreferredSize().getHeight()) {
                
                Point rTreePoint = new Point(e.getX(), e.getY());
                rTree.nearest(rTreePoint,
                        new TIntProcedure() {
                    @Override
                    public boolean execute(int i) {

                        if (currentCell != i) {
                            //int columnCount = columnHeader.getColumnCount();
                            //int rowCount = rowHeader.getRowCount();
                            int enteredRow = i % rowCount == 0 ? rowCount : i % rowCount;
                            int enteredColumn = i % rowCount == 0 ? i / rowCount : (i / rowCount) + 1;
                            

                            //System.out.println();
                            RangeMatrixTableButton tableButton = buttonTable.get(enteredRow - 1, enteredColumn - 1);
                            if (previousButton != null) {
                                iterateOnButtonCross(previousButton, false);
                            }
                            iterateOnButtonCross(tableButton, true);
                            //String text = i + ": ряд " + enteredRow + ", колонка " + enteredColumn;
                            
                            
//                        if (previousButton == null) {
//                            iterateOnButtonCross(tableButton, true);
//                        } else {
//                            if (previousButton.getColumn() != tableButton.getColumn() && 
//                                previousButton.getRow() == tableButton.getRow()) {
//                                iterateOnButtonVertical(previousButton, false);
//                                iterateOnButtonVertical(tableButton, true);
//                            } else if (previousButton.getColumn() == tableButton.getColumn() && 
//                                previousButton.getRow() != tableButton.getRow()) {
//                                iterateOnButtonHorizontal(previousButton, false);
//                                iterateOnButtonHorizontal(tableButton, true);
//                            } else {
//                                iterateOnButtonCross(previousButton, false);
//                                iterateOnButtonCross(tableButton, true);
//                            }
//                        }
                            
                            repaintCombo();

                            previousButton = tableButton;
                            currentCell = i;
                        }

                        return false;
                    }
                }, 0);
                
            } else {
                iterateOnButtonCross(previousButton, false);
                currentCell = 0;
                repaintCombo();
            }
            
        }
    }
}
