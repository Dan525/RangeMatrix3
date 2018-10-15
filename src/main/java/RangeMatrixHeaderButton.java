/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.util.Objects;

/**
 *
 * @author daniil_pozdeev
 */
public class RangeMatrixHeaderButton {
    private double x;
    private double y;
    private double width;
    private double height;
    private final Object buttonObject;
    private Object parentObject;
    private final String buttonName;
    private String buttonFullName;
    private boolean collapsed;
    private boolean group;
    private int cellIndex;

    public RangeMatrixHeaderButton(Object buttonObject, String buttonName) {
        this.buttonObject = buttonObject;
        this.buttonName = buttonName;
    }
    
    @Override
    public String toString() {
        return buttonObject + "   " + x + ", " + y;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
        hash = 23 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
        hash = 23 * hash + (int) (Double.doubleToLongBits(this.width) ^ (Double.doubleToLongBits(this.width) >>> 32));
        hash = 23 * hash + (int) (Double.doubleToLongBits(this.height) ^ (Double.doubleToLongBits(this.height) >>> 32));
        hash = 23 * hash + Objects.hashCode(this.buttonObject);
        hash = 23 * hash + (this.collapsed ? 1 : 0);
        hash = 23 * hash + (this.group ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RangeMatrixHeaderButton other = (RangeMatrixHeaderButton) obj;
        if (Double.doubleToLongBits(this.x) != Double.doubleToLongBits(other.x)) {
            return false;
        }
        if (Double.doubleToLongBits(this.y) != Double.doubleToLongBits(other.y)) {
            return false;
        }
        if (Double.doubleToLongBits(this.width) != Double.doubleToLongBits(other.width)) {
            return false;
        }
        if (Double.doubleToLongBits(this.height) != Double.doubleToLongBits(other.height)) {
            return false;
        }
        if (this.collapsed != other.collapsed) {
            return false;
        }
        if (this.group != other.group) {
            return false;
        }
        if (!Objects.equals(this.buttonObject, other.buttonObject)) {
            return false;
        }
        return true;
    }

    

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

    public boolean isCollapsed() {
        return collapsed;
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
    }
    
    public boolean isGroup() {
        return group;
    }

    public void setGroup(boolean group) {
        this.group = group;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public int getCellIndex() {
        return cellIndex;
    }

    public void setCellIndex(int cellIndex) {
        this.cellIndex = cellIndex;
    }

    public String getButtonName() {
        return buttonName;
    }

    public String getButtonFullName() {
        return buttonFullName;
    }

    public void setButtonFullName(String buttonFullName) {
        this.buttonFullName = buttonFullName;
    }

    public Object getParentObject() {
        return parentObject;
    }

    public void setParentObject(Object parentObject) {
        this.parentObject = parentObject;
    }
}
