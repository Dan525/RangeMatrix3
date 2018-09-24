package com.mycompany.rangematrix;

import com.mycompany.rangematrix.test.TestModelData;
import com.mycompany.rangematrix.test.TestModelData.M;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author daniil_pozdeev
 */
public class RangeMatrixModelCycleImpl implements RangeMatrixModel {
    
    private final ArrayList<RangeMatrixListener> listeners = new ArrayList<>();
    private final TestModelData data;
    
    RangeMatrixModelCycleImpl(TestModelData data) {
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
    
    @Override
    public String getColumnGroupType(Object column) {
        if (column == null) {
            return "";
        }
        return ((M)column).type;
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
    
    @Override
    public String getRowGroupType(Object row) {
        if (row == null) {
            return "";
        }
        return ((M)row).type;
    }
    
    @Override
    public boolean hasType(Object row) {
        if (((M)row).type == null) {
            return false;
        } else {
            return true;
        }
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
        String[] randArray = {"1","","","","","",""};
        //int randomNum = ThreadLocalRandom.current().nextInt(0, 2);
        return getRandom(randArray);
    }
    
    public String getRandom(String[] array) {
        int rnd = new Random().nextInt(array.length);
        return array[rnd];
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
