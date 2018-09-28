
package com.mycompany.rangematrix;

import java.awt.*;
import javax.swing.*;
 
public class ToolTip
{
    
    JWindow toolTip;
    JLabel label;
 
    public ToolTip(Frame f)
    {
        
        // initialize toolTip
        toolTip = new JWindow(f);
        label = new JLabel();
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setOpaque(true);
        label.setBackground(UIManager.getColor("ToolTip.background"));
        label.setBorder(UIManager.getBorder("ToolTip.border"));
        toolTip.add(label);
        //setOpaque(true);
    }
    
    public void showToolTip(String text, int x, int y) {
        label.setText(text);
        toolTip.pack();
        toolTip.setLocation(x,y);
        toolTip.setVisible(true);
    }
 
    public void hideToolTip()
    {
        toolTip.dispose();
    }
 
    public boolean isToolTipShowing()
    {
        return toolTip.isShowing();
    }
}
