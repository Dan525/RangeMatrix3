/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.rangematrix;

import java.awt.Point;
import java.util.Comparator;

/**
 *
 * @author daniil_pozdeev
 */
public class PointComparator implements Comparator<Point> {

    @Override
    public int compare(Point o1, Point o2) {
        double x1 = o1.getX();
        double x2 = o2.getX();
        double y1 = o1.getY();
        double y2 = o2.getY();

        if (y1 > y2) {
            return 1;
        } else if (y1 < y2) {
            return -1;
        } else {
            if (x1 > x2) {
                return 1;
            } else if (x1 < x2) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}
