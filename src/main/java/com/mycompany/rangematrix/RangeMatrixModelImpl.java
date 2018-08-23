/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.rangematrix;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author daniil_pozdeev
 */
public class RangeMatrixModelImpl implements RangeMatrixModel {
    
    private final ArrayList<RangeMatrixListener> listeners = new ArrayList<>();

    //Column Group
    
    @Override
    public int getColumnGroupCount(Object column) {
        if (column == null) {
            return new File("src\\res\\Заголовок колонок - копия").list().length;
        }
        return ((File)column).list().length;
    }

    @Override
    public Object getColumnGroup(Object column, int index) {
        if (column == null) {
            return new File("src\\res\\Заголовок колонок - копия").listFiles()[index];
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
            return new File("src\\res\\Заголовок колонок - копия").getName();
        }
        return ((File)column).getName();
    }
    
    //Row Group

    @Override
    public int getRowGroupCount(Object row) {
        if (row == null) {
            return new File("src\\res\\rows - копия").list().length;
        }
        return ((File)row).list().length;
    }

    @Override
    public Object getRowGroup(Object row, int index) {
        if (row == null) {
            return new File("src\\res\\rows - копия").listFiles()[index];
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
            return new File("src\\res\\rows - копия").getName();
        }
        return ((File)row).getName();
    }
    
    //Corner

    @Override
    public ArrayList<String> getCornerColumnNames() {
        ArrayList<String> cornerNames = new ArrayList<>();
        cornerNames.add("      ");
        cornerNames.add("Название сигнала");
        cornerNames.add("Тип сигнала");
        return cornerNames;
    }
    
    //Values

    @Override
    public Object getValueAt(int row, int col) {
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
