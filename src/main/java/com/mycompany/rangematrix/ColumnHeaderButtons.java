/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.rangematrix;

import java.awt.Point;
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

    public int getClosestYCoordinate(Point click) {
        Entry<Point, RangeMatrixColumnHeaderButton> entry = buttonsMap.floorEntry(click);
        if (entry == null) {
            return 0;
        }
        int newY = (int) entry.getValue().getCorner().getY();
        return newY;
    }
    
    public void clearButtonsMap() {
        buttonsMap.clear();
    }

    public RangeMatrixColumnHeaderButton getButtonAt(Point click, int newY, double minimalCellHeight) {

        boolean isNotContain;
        RangeMatrixColumnHeaderButton button;

        do {
            Point fixedClick = new Point((int) click.getX(), newY);
            Entry<Point, RangeMatrixColumnHeaderButton> entry = buttonsMap.floorEntry(fixedClick);
            if (entry == null) {
                return null;
            }
            button = entry.getValue();
            isNotContain = !button.contains(fixedClick);
            newY = (int) (newY - minimalCellHeight);
        } while (isNotContain);

        return button;
    }
}
