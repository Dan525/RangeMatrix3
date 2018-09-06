package com.mycompany.rangematrix;

import java.awt.image.BufferedImage;

/**
 *
 * @author daniil_pozdeev
 */
public class RangeMatrixTableButton {
    
    private int column;
    private int row;
    private double x;
    private double y;
    private double width;
    private double height;
    private final Object buttonObject;
    private String buttonName;
    private BufferedImage img;
    private boolean collapsed;
    
    public RangeMatrixTableButton(Object buttonObject) {
        this.buttonObject = buttonObject;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public String getButtonName() {
        return buttonName;
    }

    public void setButtonName(String buttonName) {
        this.buttonName = buttonName;
    }

    public BufferedImage getImg() {
        return img;
    }

    public void setImg(BufferedImage img) {
        this.img = img;
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
    }

    public Object getButtonObject() {
        return buttonObject;
    }
    
    
}
