/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author daniil_pozdeev
 */
public class RangeMatrixModelImpl implements RangeMatrixModel {
    
    private final ArrayList<RangeMatrixListener> listeners = new ArrayList<>();
    private final String columnPath = "C:\\Users\\daniil_pozdeev\\Documents\\NetBeansProjects\\RangeMatrix\\src\\res\\Заголовок колонок - копия";
    private final String rowPath = "C:\\Users\\daniil_pozdeev\\Documents\\NetBeansProjects\\RangeMatrix\\src\\res\\rows - копия";

    //Column Group
    
    @Override
    public int getColumnGroupCount(Object column) {
        if (column == null) {
            return new File(columnPath).list().length;
        }
        return ((File)column).list().length;
    }

    @Override
    public Object getColumnGroup(Object column, int index) {
        if (column == null) {
            return new File(columnPath).listFiles()[index];
        }
        return ((File)column).listFiles()[index];
    }
    
    @Override
    public boolean isColumnGroup(Object column) {
        if (((File)column).list().length == 0) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public String getColumnGroupName(Object column) {
        if (column == null) {
            return new File(columnPath).getName();
        }
        return ((File)column).getName();
    }
    
    @Override
    public String getColumnGroupFullName(Object column) {
        if (column == null) {
            return new File(columnPath).getName();
        }
        return ((File)column).getPath();
    }
    
    @Override
    public String getColumnGroupType(Object column) {
        if (column == null) {
            return new File(columnPath).getName();
        }
        return ((File)column).getName();
    }
    
    //Row Group

    @Override
    public int getRowGroupCount(Object row) {
        if (row == null) {
            return new File(rowPath).list().length;
        }
        return ((File)row).list().length;
    }

    @Override
    public Object getRowGroup(Object row, int index) {
        if (row == null) {
            return new File(rowPath).listFiles()[index];
        }
        return ((File)row).listFiles()[index];
    }

    @Override
    public boolean isRowGroup(Object row) {
        if (((File)row).list().length == 0) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public String getRowGroupName(Object row) {
        if (row == null) {
            return new File(rowPath).getName();
        }
        return ((File)row).getName();
    }

    @Override
    public String getRowGroupFullName(Object row) {
        if (row == null) {
            return new File(rowPath).getName();
        }
        return ((File)row).getPath();
    }
    
    @Override
    public String getRowGroupType(Object row) {
        if (row == null) {
            return new File(rowPath).getName();
        }
        return ((File)row).getName();
    }
    
    @Override
    public boolean hasType(Object row) {
        if (((File)row).getPath() == null) {
            return false;
        }
        return true;
    }
    
    //Corner

    @Override
    public ArrayList<String> getCornerColumnNames() {
        ArrayList<String> cornerNames = new ArrayList<>();
        cornerNames.add("");
        cornerNames.add("Название сигнала");
        cornerNames.add("Тип сигнала");
        return cornerNames;
    }
    
    //Values

    @Override
    public Object getValueAt(int column, int row) {
        return 1;
    }
    
    //Listeners

    @Override
    public void addRangeMatrixListener(RangeMatrixListener l) {
        listeners.add(l);
    }
    
    @Override
    public void removeRangeMatrixListener(RangeMatrixListener l) {
        listeners.remove(l);
    }

}
