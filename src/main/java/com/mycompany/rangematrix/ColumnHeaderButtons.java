/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.rangematrix;

import java.awt.Point;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 *
 * @author daniil_pozdeev
 */
public class ColumnHeaderButtons {

    public final TreeMap<Point, RangeMatrixColumnHeaderButton> buttonsMap;
    public final PointComparator pointComparator;

    public ColumnHeaderButtons() {
        pointComparator = new PointComparator();
        buttonsMap = new TreeMap<>(pointComparator);
    }
    
    public void add(RangeMatrixColumnHeaderButton button) {
        Point p = button.getCorner();
        buttonsMap.put(p, button);
    }
    
    public int getYCoordinate(Point clickY) {
        Entry<Point, RangeMatrixColumnHeaderButton> entry = buttonsMap.floorEntry(clickY);
        if (entry == null) {
            return 0;
        }
        int newY = (int) entry.getValue().getCorner().getY();
        return newY;
    }

    public RangeMatrixColumnHeaderButton getButtonAt(Point click, int newY, double minimalCellHeight) {
        
        Point fixedClick = new Point((int)click.getX(), newY);
        Entry<Point, RangeMatrixColumnHeaderButton> entry = buttonsMap.floorEntry(fixedClick);
        if (entry == null) {
            return null;
        }
        RangeMatrixColumnHeaderButton button = entry.getValue();
        if (!button.contains(click)) {
            newY = (int) (newY - minimalCellHeight);
            this.getButtonAt(fixedClick, newY, minimalCellHeight);
        }
        return button;
    }
}
