/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.util.List;

/**
 *
 * @author daniil_pozdeev
 */
public interface RangeMatrixModel {
    
    
    
    //Column group
    
    int getColumnGroupCount(Object parent);
    
    Object getColumnGroup(Object parent, int index);
    
    boolean isColumnGroup(Object column);
    
    String getColumnGroupName(Object column);
    
    String getColumnGroupFullName(Object column);
    
    String getColumnGroupType(Object column);
    
    //Row Group
    
    int getRowGroupCount(Object row);
    
    Object getRowGroup(Object row, int index);
    
    boolean isRowGroup(Object row);
    
    String getRowGroupName(Object row);
    
    String getRowGroupFullName(Object row);
    
    String getRowGroupType(Object row);
    
    boolean hasType(Object row);
    
    //Corner
    
    List<String> getCornerColumnNames();
    
    //Values
    
    Object getValueAt(int row, int col);
    
    //Listeners
    
    public void addRangeMatrixListener(RangeMatrixListener l);
    
    public void removeRangeMatrixListener(RangeMatrixListener l);
}
