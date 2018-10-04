

import com.google.common.collect.HashBasedTable;
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
    
    private double spaceAroundName = 4;
    private double minimalCellHeight;
    private int levelsCount;
    private int rowCount;
    private BufferedImage buffer;
    private double width;
    private double height;
    
    /*
    Список кнопок, которые необходимо отрисовывать в данный момент.
    Имеет соответствие с прямоугольниками из RTree.
    */
    private ArrayList<RangeMatrixHeaderButton> buttonList;
    
    /*
    Хранит кнопки, принадлежащие объектам.
    */
    private Map<Object,RangeMatrixHeaderButton> buttonMap;
    
    /*
    Хранит кнопки, принадлежащие типам соответствующих объектов.
    */
    private Map<Object,RangeMatrixHeaderButton> typeButtonMap;
    
    /*
    Хранит пустые кнопки. Пустые кнопки принадлежат объектам. Объект может иметь
    более одной пустой кнопки, поэтому они дополнительно проиндексированы по
    номеру колонки (уровню) rowHeader-а.
    */
    private Table<Object, Integer, RangeMatrixHeaderButton> emptyButtonTable;
    
    /*
    Список с объектами листовых кнопок. Нужен для получения кнопок из buttonMap,
    соответствующих номеру ряда таблицы. Эти кнопки нужны для получения из
    них параметров для отрисовки ячеек соответствующего ряда.
    */
    private List<Object> leafButtonList;
    
    /*
    Список с шириной каждого уровня rowHeader-а. Предварительно вычисляется на
    основании ширины соответствующих уровней rowHeader-а и headerCorner-а.
    */
    private ArrayList<Double> rowsWidthList;
    
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
        leafButtonList = new ArrayList<>();
        
        rowCount = calculateTableRowCount(null, 0);
        calculateMinimalCellHeight();
        rm.setupRowsWidthList();
        
        calculateParams();
        
        this.addMouseListener(new RangeMatrixMouseHandler());
    }
    
    public void calculateParams() {
        leafButtonList.clear();
        buttonList.clear();
        calculateRowCoordinates(null, 0);
        assignRowIndices(null, 0);        
        levelsCount = calculateLevelsCount(null, new ArrayList<>(), 1);
        
        calculateWidthOfComponent();
        calculateHeightOfComponent();
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
        RangeMatrixHeaderButton button = findButtonInMap(row);
        JLabel label = renderer.getRowRendererComponent(button.getButtonObject(),
                                                        button.getButtonName(),
                                                        button.isCollapsed(), 
                                                        true);
        return label.getPreferredSize().getWidth() + 2 * spaceAroundName;
    }
    
    public double calculateWidthOfRowByType(Object row) {
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

            boolean isGroup;
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
        for (int i = 0; i < levelsCount; i++) {
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

    public int calculateLevelsCount(Object parentRow, ArrayList<Integer> maxLevelIndexList, int maxLevelIndex) {
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
                    maxLevelIndex+=2;
                    calculateLevelsCount(child, maxLevelIndexList, maxLevelIndex);
                    maxLevelIndex-=2;
                } else {
                    maxLevelIndex++;
                    calculateLevelsCount(child, maxLevelIndexList, maxLevelIndex);
                    maxLevelIndex--;
                }                
            } else {
                if (hasType) {
                    maxLevelIndex++;
                    maxLevelIndexList.add(maxLevelIndex);
                    maxLevelIndex--;
                } else {
                    maxLevelIndexList.add(maxLevelIndex);
                }
            }
            
        }
        return Collections.max(maxLevelIndexList);
    }

    public int getLevelsCount() {
        return levelsCount;
    }

    public List<Object> getLeafButtonList() {
        return leafButtonList;
    }
    
    /**
     * Присваивает порядковые номера кнопкам нижнего уровня (иначе говоря, 
     * листовым кнопкам; кнопкам, не имеющим потомков). Нужно для того, чтобы
     * иметь связь между кнопками заголовка и рядами таблицы.
     * По индексам этих кнопок определяются индексы кнопок, имеющих потомков: в
     * момент, когда эти кнопки сворачивают и они оказываются кнопками нижнего
     * уровня.
     * @param parentRow
     * @param rowCounter
     * @return
     */
    public int assignRowIndices(Object parentRow, int rowCounter) {
        int rowCount = model.getRowGroupCount(parentRow);

        for (int i = 0; i < rowCount; i++) {
            Object child = model.getRowGroup(parentRow, i);
            
            RangeMatrixHeaderButton button = findButtonInMap(child);

            boolean isGroup = model.isRowGroup(child);

            if (isGroup) {
                rowCounter = assignRowIndices(child, rowCounter);
            } else {
                button.setCellIndex(rowCounter);
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
        if (!rowsWidthList.isEmpty()) {
            for (int i = 0; i < levelsCount; i++) {
                width += rowsWidthList.get(i);
            }
        }
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

    /**
     * Возвращает список объектов, принадлежащих листовым или свернутым кнопкам.
     * @param parentRow
     * @param leafRowList
     * @return
     */
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
    
    /**
     * При сворачивании принудительно устанавливается isGroup = false, чтобы не
     * отрисовывать потомков.
     * В данном методе устанавливается для кнопки настоящее свойство ее объекта:
     * является он группой (имеет ли потомков) или нет.
     * Необходимо, чтобы отображать +/- только на кнопках групп.
     * @param child
     * @param button
     */
    public void assignButtonGroupAttribute(Object child, RangeMatrixHeaderButton button) {
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
                assignButtonGroupAttribute(child, button);
                isGroup = false;
                cellHeight = minimalCellHeight;
                
            } else {
                assignButtonGroupAttribute(child, button);
                isGroup = model.isColumnGroup(child);
                cellHeight = calculateHeightOfRow(child);
            }
            double cellWidth = rowsWidthList.get(columnCounter);
            double cellWidthType = 0;
            
            //////////////////////////////////
            button.setX(cellX);
            button.setWidth(cellWidth);
            //////////////////////////////////
            
            /*
            Если объект имеет тип, то отрисовывается дополнительная кнопка с
            типом объекта. При этом с точки зрения нажатий, эти кнопки
            являются единым целым.
            */
            if (hasType) {
                columnCounter++;
                RangeMatrixHeaderButton typeButton = findTypeButtonInMap(child);
                
                cellWidthType = rowsWidthList.get(columnCounter);
                double cellXType = cellX + cellWidth;

                //////////////////////////////////
                typeButton.setX(cellXType);
                typeButton.setY(cellY);
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
                
            } else if (!isGroup && columnCounter < levelsCount) {
                
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
            }
            cellY += cellHeight;
        }
    }
    
    /**
     * Отрисовывает кнопки для заполнения пустого пространства.
     * @param child
     * @param parentCellX
     * @param parentCellY
     * @param columnCounter
     */
    public void calculateEmptyRows(Object child,double parentCellX, double parentCellY, int columnCounter) {

        if (columnCounter < levelsCount) {
            
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
        RenderingHints rh = new RenderingHints(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHints(rh);
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
    
    public void processingClickOnRow(RangeMatrixHeaderButton button) {
        
        int rowIndex = calculateRowIndex(button);
        
        if (button.isCollapsed() && button.isGroup()) {
            
            button.setCollapsed(false);
            calculateParams();
            rm.makeRowLeading(button, false);
            int collapsedRowCount = rm.ignorePaintRows(button, false);
            rm.shiftRowsAfterCollapse(rowIndex, collapsedRowCount, false);
            
        } else if (!button.isCollapsed() && button.isGroup()) {
            
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
     * или нет). Для рядов всех уровней, кроме последнего, индекс берется по 
     * первому из предков, находящемуся на последнем (крайнем правом) уровне.
     * @param button
     * @return
     */
    public int calculateRowIndex(RangeMatrixHeaderButton button) {
        int rowIndex;
        if (model.isRowGroup(button.getButtonObject())) {
            Object leaf = fillFullLeafRowList(button.getButtonObject(), new ArrayList<>()).get(0);
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
    public ArrayList<Object> fillFullLeafRowList(Object parentRow, ArrayList<Object> leafRowList) {
        int rowCount = model.getRowGroupCount(parentRow);

        for (int i = 0; i < rowCount; i++) {
            Object child = model.getRowGroup(parentRow, i);
            boolean isGroup = model.isRowGroup(child);
            
            if (isGroup) {
                fillFullLeafRowList(child, leafRowList);
            } else {
                leafRowList.add(child);
            }
        }
        return leafRowList;
    }
    
    /**
     * Возвращает полное количество рядов таблицы, независимо от того,
     * свернуты некоторые ряды или нет.
     * @param parentRow
     * @param rowCounter
     * @return
     */
    public int calculateTableRowCount(Object parentRow, int rowCounter) {
        int groupRowCount = model.getRowGroupCount(parentRow);

        for (int i = 0; i < groupRowCount; i++) {
            Object child = model.getRowGroup(parentRow, i);
            boolean isGroup = model.isRowGroup(child);
            
            if (isGroup) {
                rowCounter = calculateTableRowCount(child, rowCounter);
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
     * Возвращает индексы всех листовых (если кнопка свернута, то она в данном 
     * случае будет являться листом) кнопок  - предков кнопки, на которую было
     * произведено нажатие. Нужно для присвоения аттрибута Collapsed всем 
     * ячейкам таблицы, начиная со столбца, находящегося под второй кнопкой из 
     * списка предков. 
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
    
    /**
     * Возвращает индексы всех листовых (в данном случае кнопка будет листом,
     * только если у нее нет предков, независимо от того свернута она или нет) 
     * кнопок  - предков кнопки, на которую было произведено нажатие. Нужно для
     * проверки наличия непустых ячеек таблицы при сворачивании ряда с целью
     * дальнейшего отображения этой информации в Leading Row.
     * @param parentRow
     * @param leafRowIndexList
     * @return
     */
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
    
    public void clearRowHeaderRTree() {
        rTree = new RTree();
        rTree.init(null);
    }
    
    public void repaintCombo() {
        rebuildBuffer();
        revalidate();
        repaint();
    }
    
    protected class RangeMatrixMouseHandler implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {

        }

        @Override
        public void mousePressed(MouseEvent e) {
//            Point rTreePoint = new Point(e.getX(), e.getY());
//            rTree.nearest(rTreePoint, new TIntProcedure() {         // a procedure whose execute() method will be called with the results
//                @Override
//                public boolean execute(int i) {
//                    System.out.println(buttonList.get(i));
//                    RangeMatrixHeaderButton button = buttonList.get(i);
//                    rm.toolTip.showToolTip(button.getButtonName(), e.getXOnScreen(), e.getYOnScreen()-20);
//                    return false;              // return true here to continue receiving results
//                }
//            }, 0);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
//            rm.toolTip.hideToolTip();
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
            clearRowHeaderRTree();
            repaintCombo();
            
            rm.calculateSizeOfComponent();
            rm.clearTableRTree();
            rm.repaintCombo();
            
            rm.getHeaderCorner().calculateWidthOfComponent();
            rm.getHeaderCorner().repaintCombo();
        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }

    }
}
