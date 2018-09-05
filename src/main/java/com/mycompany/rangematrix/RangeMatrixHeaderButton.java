/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.rangematrix;

/**
 *
 * @author daniil_pozdeev
 */
public class RangeMatrixHeaderButton {
    private final double x;
    private final double y;
    private final double width;
    private final double height;
    private final Object buttonObject;
    private final String buttonName;
    private boolean collapsed;
    private final boolean group;

    public RangeMatrixHeaderButton(double x, double y, double width, double height, Object buttonObject, String buttonName, boolean group) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.buttonObject = buttonObject;
        this.buttonName = buttonName;
        this.group = group;
    }

    
    
//    public boolean contains(Point click) {
//        if (!(click.getX() >= corner.getX() && click.getX() <= corner.getX() + width)) {
//            return false;
//        }
//        return click.getY() >= corner.getY() && click.getY() <= corner.getY() + height;
//    }
    
    @Override
    public String toString() {
        return buttonName + "   " + x + ", " + y;
    }
    
//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj) {
//            return true;
//        }
//        if (obj == null || !(obj instanceof RangeMatrixColumnHeaderButton)) {
//            return false;
//        }
//        RangeMatrixColumnHeaderButton button = (RangeMatrixColumnHeaderButton) obj;
//        if (!corner.equals(button.getCorner())) {
//            return false;
//        }
//        if (width != button.getWidth()) {
//            return false;
//        }
//        return height == button.getHeight();
//    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public Object getButtonObject() {
        return buttonObject;
    }

    public String getButtonName() {
        return buttonName;
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
    }
    
    public boolean isGroup() {
        return group;
    }
}
