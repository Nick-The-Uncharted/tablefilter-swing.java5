/**
 * Author:  Luis M Pena  ( lu@coderazzi.net )
 * License: MIT License
 *
 * Copyright (c) 2007 Luis M. Pena  -  lu@coderazzi.net
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.coderazzi.filters.examples.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

import net.coderazzi.filters.examples.ActionHandler;


public class MenuFont extends JMenu implements ActionListener {

    private static final long serialVersionUID = -6772023653226757860L;
    private static final int RELATIVE_FONT_SIZES[] = {
            -2, -1, 0, 1, 2, 4, 8, 16
        };

    private ActionHandler main;

    public MenuFont(ActionHandler listener) {
        super("font size");
        this.main = listener;

        int size = main.getFilterHeader().getFont().getSize();

        ButtonGroup group = new ButtonGroup();
        for (int i : RELATIVE_FONT_SIZES) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(String.valueOf(
                        size + i));
            item.addActionListener(this);
            this.add(item);
            group.add(item);
            if (i == 0) {
                item.setSelected(true);
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        int size = Integer.valueOf(((JRadioButtonMenuItem) e.getSource())
                    .getText());
        main.getFilterHeader()
            .setFont(main.getFilterHeader().getFont().deriveFont(
                    (float) (size)));
    }

}
