package com.mycompany.rangematrix;

import com.mycompany.rangematrix.test.TestModelData2;
import com.mycompany.rangematrix.test.TestModelData2.M;
import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author daniil_pozdeev
 */
public class RangeMatrixModelCycleImpl implements RangeMatrixModel {
    
    private final ArrayList<RangeMatrixListener> listeners = new ArrayList<>();
    private final TestModelData2 data;
    
    RangeMatrixModelCycleImpl(TestModelData2 data) {
        this.data = data;
    }
    

    //Column Group
    
    @Override
    public int getColumnGroupCount(Object column) {
        if (column == null) {
            return data.columnHeaderDataSource.length;
        } else if (((M)column).groups == null) {
            return 0;
        } else {
            return ((M)column).groups.length;
        }
    }

    @Override
    public Object getColumnGroup(Object column, int index) {
        if (column == null) {
            return data.columnHeaderDataSource[index];
        }
        return ((M)column).groups[index];
    }
    
    @Override
    public boolean isColumnGroup(Object column) {
        if (((M)column).groups == null) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public String getColumnGroupName(Object column) {
        if (column == null) {
            return "";
        }
        return ((M)column).name;
    }
    
    //Row Group

    @Override
    public int getRowGroupCount(Object row) {
        if (row == null) {
            return data.rowHeaderDataSource.length;
        } else if (((M)row).groups == null) {
            return 0;
        } else {
            return ((M)row).groups.length;
        }
    }

    @Override
    public Object getRowGroup(Object row, int index) {
        if (row == null) {
            return data.rowHeaderDataSource[index];
        }
        return ((M)row).groups[index];
    }

    @Override
    public boolean isRowGroup(Object row) {
        if (((M)row).groups == null) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public String getRowGroupName(Object row) {
        if (row == null) {
            return "";
        }
        return ((M)row).name;
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
