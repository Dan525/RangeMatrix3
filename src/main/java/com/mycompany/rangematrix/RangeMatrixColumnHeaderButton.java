/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.rangematrix;

import java.awt.Point;
import java.io.File;

/**
 *
 * @author daniil_pozdeev
 */
public class RangeMatrixColumnHeaderButton {
    private final Point corner;
    private final double width;
    private final double height;
    private final Object column;
    
    
    public RangeMatrixColumnHeaderButton(Point corner, double width, double height, Object child) {
        this.corner = corner;
        this.width = width;
        this.height = height;
        this.column = child;
    }

    public Point getCorner() {
        return corner;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public Object getColumn() {
        return column;
    }
    
    public boolean contains(Point click) {
        if (!(click.getX() >= corner.getX() && click.getX() <= corner.getX() + width)) {
            return false;
        }
        return click.getY() >= corner.getY() && click.getY() <= corner.getY() + height;
    }
    
    @Override
    public String toString() {
        return ((File)column).getName() + ", " + corner.getX() + ", " + corner.getY();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof RangeMatrixColumnHeaderButton)) {
            return false;
        }
        RangeMatrixColumnHeaderButton button = (RangeMatrixColumnHeaderButton) obj;
        if (!corner.equals(button.getCorner())) {
            return false;
        }
        if (width != button.getWidth()) {
            return false;
        }
        return height == button.getHeight();
    }
}
