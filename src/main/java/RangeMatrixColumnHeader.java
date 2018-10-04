

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
import java.awt.event.MouseMotionListener;
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
public class RangeMatrixColumnHeader extends JComponent {

    private final RangeMatrix rm;
    private RangeMatrixModel model;
    private IRangeMatrixRenderer renderer;
    private CellRendererPane crp;
    private SpatialIndex rTree;
    
    private double spaceAroundName = 4;
    private double minimalCellHeight;
    private int levelsCount;
    //private int columnCount;
    private BufferedImage buffer;
    private double width;
    private double height;
    
    
    /*
    Список кнопок, которые необходимо отрисовывать в данный момент.
    Имеет соответствие с прямоугольниками из RTree.
    */
    private List<RangeMatrixHeaderButton> buttonList;
    
    /*
    Хранит все имеющиеся кнопки
    */
    private Map<Object,RangeMatrixHeaderButton> buttonMap;
    
    /*
    Список с объектами листовых кнопок. Нужен для получения кнопок из buttonMap,
    соответствующих номеру колонки таблицы. Эти кнопки нужны для получения из
    них параметров для отрисовки ячеек соответствующей колонки.
    */
    private List<Object> leafButtonList;
    
    //private ToolTip toolTip;
    //private int currentCell = 0;

    public RangeMatrixColumnHeader(RangeMatrix rm) {
        this.rm = rm;
    }

    public void setModel() {
        this.model = rm.getModel();
        this.renderer = rm.getRenderer();
        this.crp = rm.getCrp();
        //this.toolTip = rm.getToolTip();
        
        initColumnHeaderRTree();

        buttonList = new ArrayList<>();
        buttonMap = new HashMap<>();
        leafButtonList = new ArrayList<>();
        
        //columnCount = calculateTableColumnCount(null, 0);
        calculateMinimalCellHeight();
        
        calculateParams();

        this.addMouseListener(new RangeMatrixMouseHandler());
        //this.addMouseMotionListener(new RangeMatrixMouseMotionHandler());
    }

    public RangeMatrixModel getModel() {
        return model;
    }
    
    public void calculateParams() {
        leafButtonList.clear();
        buttonList.clear();
        calculateColumnCoordinates(null, 0);
        assignColumnIndices(null, 0);
        levelsCount = calculateLevelsCount(null, new ArrayList<>(), 1);
        
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

    /**
     * Вычисляет количество уровней заголовка.
     * @param parentColumn
     * @param maxLevelIndexList
     * @param maxLevelIndex
     * @return
     */
    public int calculateLevelsCount(Object parentColumn, ArrayList<Integer> maxLevelIndexList, int maxLevelIndex) {
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
                maxLevelIndex++;
                calculateLevelsCount(child, maxLevelIndexList, maxLevelIndex);
                maxLevelIndex--;
            }
            maxLevelIndexList.add(maxLevelIndex);
        }
        return Collections.max(maxLevelIndexList);
    }

    public int getLevelsCount() {
        return levelsCount;
    }

    public int calculateHeightMultiplier(Object parentColumn, boolean isGroup, int levelIndex) {
        if (!isGroup) {
            return (levelsCount - levelIndex) + 1;
        } else {
            return 1;
        }
    }
    
    /**
     * Присваивает порядковые номера кнопкам нижнего уровня (иначе говоря, 
     * листовым кнопкам; кнопкам, не имеющим потомков). Нужно для того, чтобы
     * иметь связь между кнопками заголовка и колонками таблицы.
     * По индексам этих кнопок определяются индексы кнопок, имеющих потомков: в
     * момент, когда эти кнопки сворачивают и они оказываются кнопками нижнего
     * уровня.
     * @param parentColumn
     * @param columnCounter
     * @return
     */
    public int assignColumnIndices(Object parentColumn, int columnCounter) {
        int columnCount = model.getColumnGroupCount(parentColumn);

        for (int i = 0; i < columnCount; i++) {
            Object child = model.getColumnGroup(parentColumn, i);
            RangeMatrixHeaderButton button = findButtonInMap(child);

            boolean isGroup = model.isColumnGroup(child);

            if (isGroup) {
                columnCounter = assignColumnIndices(child, columnCounter);
            } else {
                button.setCellIndex(columnCounter);
                leafButtonList.add(child);
                columnCounter++;
            }
        }
        return columnCounter;
    }

    public List<Object> getLeafButtonList() {
        return leafButtonList;
    }
    
    /**
     * Вычисляет координату по оси X и ширину кнопок.
     * @param parentColumn
     * @param parentCellX
     */
    public void calculateColumnCoordinates(Object parentColumn, double parentCellX) {

        int columnCount = model.getColumnGroupCount(parentColumn);
        double cellX = parentCellX;

        for (int i = 0; i < columnCount; i++) {
            Object child = model.getColumnGroup(parentColumn, i);

            double cellWidth;
            boolean isGroup;
            
            RangeMatrixHeaderButton button = findButtonInMap(child);

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
                calculateColumnCoordinates(child, cellX);
            }
            cellX += cellWidth;
        }
    }
    
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
        height = (minimalCellHeight * levelsCount);
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
     * @param parentColumn
     * @param leafColumnList
     * @return
     */
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
    
    /**
     * Возвращает список объектов, принадлежащих листовым кнопкам, независимо 
     * от того, свернуты некоторые из потомков или нет.
     * @param parentColumn
     * @param leafColumnList
     * @return
     */
    public ArrayList<Object> fillFullLeafColumnList(Object parentColumn, ArrayList<Object> leafColumnList) {
        int columnCount = model.getColumnGroupCount(parentColumn);

        for (int i = 0; i < columnCount; i++) {
            Object child = model.getColumnGroup(parentColumn, i);
            boolean isGroup = model.isColumnGroup(child);
            
            if (isGroup) {
                fillFullLeafColumnList(child, leafColumnList);
            } else {
                leafColumnList.add(child);
            }
        }
        return leafColumnList;
    }
    
    /**
     * Возвращает таблицу "индекс колонки - объект" из объектов, принадлежащих
     * кнопкам, которые являются видимыми. Используя реализацию TreeMap,
     * получаем ключи в отсортированном виде. Это нужно для получения первой
     * кнопки - элемента с наименьшим индексом.
     * @param parentColumn
     * @param leafColumnMap
     * @return
     */
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
        
    /**
     * Возвращает индексы всех листовых (если кнопка свернута, то она в данном 
     * случае будет являться листом) кнопок  - предков кнопки, на которую было
     * произведено нажатие. Нужно для присвоения аттрибута Collapsed всем 
     * ячейкам таблицы, начиная со строки, находящейся под второй кнопкой из 
     * списка предков.
     * @param parentColumn
     * @param leafColumnIndexList
     * @return
     */
    public ArrayList<Integer> fillLeafColumnIndexList(Object parentColumn, ArrayList<Integer> leafColumnIndexList) {
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
                fillLeafColumnIndexList(child, leafColumnIndexList);
            } else {
                int columnIndex = calculateColumnIndex(findButtonInMap(child));
                leafColumnIndexList.add(columnIndex);
            }
        }
        return leafColumnIndexList;
    }
    
    /**
     * Возвращает индексы всех листовых (в данном случае кнопка будет листом,
     * только если у нее нет предков, независимо от того свернута она или нет) 
     * кнопок  - предков кнопки, на которую было произведено нажатие. Нужно для
     * проверки наличия непустых ячеек таблицы при сворачивании колонки с целью
     * дальнейшего отображения этой информации в Leading Column.
     * @param parentColumn
     * @param leafColumnIndexList
     * @return
     */
    public ArrayList<Integer> fillFullLeafColumnIndexList(Object parentColumn, ArrayList<Integer> leafColumnIndexList) {
        int columnCount = model.getColumnGroupCount(parentColumn);

        for (int i = 0; i < columnCount; i++) {
            Object child = model.getColumnGroup(parentColumn, i);
            boolean isGroup = model.isColumnGroup(child);
            
            if (isGroup) {
                fillFullLeafColumnIndexList(child, leafColumnIndexList);
            } else {
                int columnIndex = calculateColumnIndex(findButtonInMap(child));
                leafColumnIndexList.add(columnIndex);
            }
        }
        return leafColumnIndexList;
    }
    
    /**
     * Возвращает полное количество колонок таблицы, независимо от того,
     * свернуты некоторые колонки или нет.
     * @param parentColumn
     * @param columnCounter
     * @return
     */
    public int calculateTableColumnCount(Object parentColumn, int columnCounter) {
        int groupColumnCount = model.getColumnGroupCount(parentColumn);

        for (int i = 0; i < groupColumnCount; i++) {
            Object child = model.getColumnGroup(parentColumn, i);
            boolean isGroup = model.isColumnGroup(child);
            
            if (isGroup) {
                columnCounter = calculateTableColumnCount(child, columnCounter);
            } else {
                columnCounter++;
            }
        }
        return columnCounter;
    }
    
    public RangeMatrixHeaderButton findButtonInMap(Object child) {
        
        RangeMatrixHeaderButton button = buttonMap.get(child);

        if (button == null) {
            String columnName = model.getColumnGroupName(child);
            button = new RangeMatrixHeaderButton(child, columnName);
            //button.setButtonToolTip(model.getColumnGroupToolTipName(child));
            buttonMap.put(child, button);
        }
        return button;
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
                assignButtonGroupAttribute(child, button);
                isGroup = false;
                cellWidth = calculateWidthByColumnName(child);
            } else {
                assignButtonGroupAttribute(child, button);
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
        RenderingHints rh = new RenderingHints(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHints(rh);
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
    
    public void processingClickOnColumn(RangeMatrixHeaderButton button) {
        
        int columnIndex = calculateColumnIndex(button);
        
        if (button.isCollapsed() && button.isGroup()) {
            
            TreeMap<Integer, Object> leafColumnMap = (TreeMap<Integer, Object>) fillLeafColumnMap(button.getButtonObject(), new TreeMap<>());
            RangeMatrixHeaderButton leafHeaderButton = findButtonInMap(leafColumnMap.firstEntry().getValue());
            double widthOfLeadingColumn = leafHeaderButton.getWidth();
            
            button.setCollapsed(false);
            calculateParams();
            rm.makeColumnLeading(button, widthOfLeadingColumn, false);
            rm.ignorePaintColumns(button, false);
            rm.shiftColumnsAfterCollapse(columnIndex);
            
        } else if (!button.isCollapsed() && button.isGroup()) {
            
            double widthByName = calculateWidthByColumnName(button.getButtonObject());
            button.setCollapsed(true);
            calculateParams();
            rm.makeColumnLeading(button, widthByName, true);
            rm.ignorePaintColumns(button, true);
            rm.shiftColumnsAfterCollapse(columnIndex);
        }
    }
    
    /**
     * Возвращает индекс колонки. Индекс рассчитывается из условия, что все
     * колонки развернуты (при этом не важно, развернуты они в данный момент
     * или нет). Для колонок всех уровней, кроме последнего, индекс берется по 
     * первому из потомков, находящемуся на последнем (нижнем) уровне.
     * @param button
     * @return
     */
    public int calculateColumnIndex(RangeMatrixHeaderButton button) {
        int columnIndex;
        if (model.isColumnGroup(button.getButtonObject())) {
            Object leaf = fillFullLeafColumnList(button.getButtonObject(), new ArrayList<>()).get(0);
            columnIndex = findButtonInMap(leaf).getCellIndex();
        } else {
            columnIndex = button.getCellIndex();
        }

        return columnIndex;
    }
    
    public void initColumnHeaderRTree() {
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

        }

        @Override
        public void mouseReleased(MouseEvent e) {
            Point rTreePoint = new Point(e.getX(), e.getY());
            
            rTree.nearest(rTreePoint, 
                          new TIntProcedure() {
                            @Override
                            public boolean execute(int i) {
                                System.out.println(buttonList.get(i));
                                RangeMatrixHeaderButton button = buttonList.get(i);
                                
                                System.out.println(calculateColumnIndex(button));
                                processingClickOnColumn(button);
                                return false;
                            }
                          }, 0);
            
            initColumnHeaderRTree();
            repaintCombo();
            
            rm.calculateSizeOfComponent();
            rm.clearTableRTree();
            rm.repaintCombo();
            
            rm.getHeaderCorner().calculateHeightOfComponent();
            rm.getHeaderCorner().repaintCombo();            
        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }

    }
    
    protected class RangeMatrixMouseMotionHandler implements MouseMotionListener {

        @Override
        public void mouseDragged(MouseEvent e) {
        }

        @Override
        public void mouseMoved(MouseEvent e) {
//            Point rTreePoint = new Point(e.getX(), e.getY());
//            
//            rTree.nearest(rTreePoint, 
//                          new TIntProcedure() {
//                            @Override
//                            public boolean execute(int i) {
//                                if (currentCell != i) {
//                                    //System.out.println(buttonList.get(i));
//                                    RangeMatrixHeaderButton button = buttonList.get(i);
//                                    //System.out.println(button.getButtonToolTip());
//                                    toolTip.showToolTip("11", e.getXOnScreen(), e.getYOnScreen()-20);
//                                    //processingClickOnColumn(button);
//                                    currentCell = i;
//                                }
//                                return false;
//                            }
//                          }, 0);
        }
        
    }
}
