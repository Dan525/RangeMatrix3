/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.rangematrix;

import java.awt.Point;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 *
 * @author daniil_pozdeev
 */
public class ColumnHeaderButtons {
    public final TreeMap<Point, RangeMatrixColumnHeaderButton> buttons;
    
    public ColumnHeaderButtons() {
        buttons = new TreeMap<>(new Comparator<Point>()
        {
            @Override
            public int compare(Point o1, Point o2)
            {
                if (o1.getX() < o2.getX() && o1.getY() < o2.getY()) {
                    return -1;
                }
                if (o1.getX() > o2.getX() && o1.getY() > o2.getY()) {
                    return +1;
                }
                return 0;
            } 
    });
    }
    
    public void add(RangeMatrixColumnHeaderButton button) {
        Point p = button.getCorner();
        buttons.put(p, button);
    }
    
    public RangeMatrixColumnHeaderButton getButtonAt(Point click) {
        Entry<Point, RangeMatrixColumnHeaderButton> entry = buttons.ceilingEntry(click);
        if (entry == null) {
            return null;
        }
        RangeMatrixColumnHeaderButton button = entry.getValue();
        if (!button.contains(click)) {
            return null;
        }
        return button;
    }
}
