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
package net.coderazzi.filters.examples;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import net.coderazzi.filters.Filter;
import net.coderazzi.filters.examples.menu.MenuAutoChoices;
import net.coderazzi.filters.examples.menu.MenuAutoCompletion;
import net.coderazzi.filters.examples.menu.MenuAutoResize;
import net.coderazzi.filters.examples.menu.MenuColorAction;
import net.coderazzi.filters.examples.menu.MenuColumnRemove;
import net.coderazzi.filters.examples.menu.MenuCountryFlagRenderer;
import net.coderazzi.filters.examples.menu.MenuCountrySpecialSorter;
import net.coderazzi.filters.examples.menu.MenuEditable;
import net.coderazzi.filters.examples.menu.MenuEnabled;
import net.coderazzi.filters.examples.menu.MenuEventsWindow;
import net.coderazzi.filters.examples.menu.MenuFont;
import net.coderazzi.filters.examples.menu.MenuHeaderOnUse;
import net.coderazzi.filters.examples.menu.MenuHeaderVisible;
import net.coderazzi.filters.examples.menu.MenuHtmlCountry;
import net.coderazzi.filters.examples.menu.MenuIgnoreCase;
import net.coderazzi.filters.examples.menu.MenuInstantFiltering;
import net.coderazzi.filters.examples.menu.MenuLookAndFeel;
import net.coderazzi.filters.examples.menu.MenuMaleCustomChoices;
import net.coderazzi.filters.examples.menu.MenuMaxHistory;
import net.coderazzi.filters.examples.menu.MenuMaxPopupRows;
import net.coderazzi.filters.examples.menu.MenuModelAdd;
import net.coderazzi.filters.examples.menu.MenuModelChange;
import net.coderazzi.filters.examples.menu.MenuModelColumnsChange;
import net.coderazzi.filters.examples.menu.MenuModelRemove;
import net.coderazzi.filters.examples.menu.MenuPosition;
import net.coderazzi.filters.examples.menu.MenuReset;
import net.coderazzi.filters.examples.menu.MenuRowSize;
import net.coderazzi.filters.examples.menu.MenuUIEnabled;
import net.coderazzi.filters.examples.menu.MenuUserFilterEnable;
import net.coderazzi.filters.examples.menu.MenuUserFilterInclude;
import net.coderazzi.filters.examples.utils.AgeCustomChoice;
import net.coderazzi.filters.examples.utils.Ages60sCustomChoice;
import net.coderazzi.filters.examples.utils.CenteredRenderer;
import net.coderazzi.filters.examples.utils.DateRenderer;
import net.coderazzi.filters.examples.utils.FlagRenderer;
import net.coderazzi.filters.examples.utils.MaleRenderer;
import net.coderazzi.filters.examples.utils.TestTableModel;
import net.coderazzi.filters.examples.utils.UserFilter;
import net.coderazzi.filters.gui.AutoChoices;
import net.coderazzi.filters.gui.CustomChoice;
import net.coderazzi.filters.gui.FilterSettings;
import net.coderazzi.filters.gui.IFilterEditor;
import net.coderazzi.filters.gui.IFilterHeaderObserver;
import net.coderazzi.filters.gui.TableFilterHeader;


/** Main example showing the {@link TableFilterHeader} functionality. */
public class TableFilterExample extends JFrame implements ActionHandler {

    private static final long serialVersionUID = 382439526043424294L;
    
    private static final int DEFAULT_MODEL_ROWS=1000;
    private static final boolean START_LARGE_MODEL = false;

    private TestTableModel tableModel;
    private JTable table;
    private TableFilterHeader filterHeader;
    private JCheckBoxMenuItem allEnabled;
    private TableSorter tableSorter;
    JMenu filtersMenu;

    public TableFilterExample(int modelRows) {
        super("Table Filter Example / Java 5");
        if (START_LARGE_MODEL){
        	TestTableModel.setLargeModel(true);
        }
        JPanel tablePanel = createGui(modelRows);
        getContentPane().add(tablePanel);
        setJMenuBar(createMenu(tablePanel, modelRows));
        filterHeader.setTable(table);
    }

    private JPanel createGui(int modelRows) {
        tableModel = TestTableModel.createTestTableModel(modelRows);
        // >>special difference with Java 6
        // instead of table = new JTable(tableModel);
        table = new JTable();
        tableSorter = new TableSorter(tableModel, table.getTableHeader());
        table.setModel(tableSorter);

        // <<<
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        filterHeader = new TableFilterHeader();
        filterHeader.addHeaderObserver(new IFilterHeaderObserver() {

                public void tableFilterUpdated(TableFilterHeader header,
                                               IFilterEditor     editor,
                                               TableColumn       tableColumn) {
                    // no need to react
                }

                public void tableFilterEditorExcluded(
                        TableFilterHeader header,
                        IFilterEditor     editor,
                        TableColumn       tableColumn) {
                    // remove the menu entry associated to this editor
                    getMenu(filtersMenu, (String) tableColumn.getHeaderValue(),
                        true);
                }

                public void tableFilterEditorCreated(
                        TableFilterHeader header,
                        IFilterEditor     editor,
                        TableColumn       tableColumn) {
                    handleNewColumn(editor, tableColumn);
                }
            });

        return tablePanel;
    }

    private JMenuBar createMenu(JPanel tablePanel, int modelRows) {
        JMenuBar menu = new JMenuBar();
        menu.add(createTableMenu(modelRows));
        menu.add(createHeaderMenu(tablePanel));
        menu.add(createFiltersMenu());
        menu.add(createMiscellaneousMenu());

        return menu;
    }

    private JMenu createTableMenu(int modelRows) {
        JMenu tableMenu = new JMenu("Table");
        tableMenu.setMnemonic(KeyEvent.VK_T);
        tableMenu.add(new MenuModelAdd(this, true));
        tableMenu.add(new MenuModelAdd(this, false));
        tableMenu.add(new MenuModelRemove(this));
        tableMenu.addSeparator();
        tableMenu.add(new MenuAutoResize(this));
        tableMenu.addSeparator();
        tableMenu.add(new MenuModelChange(this, modelRows));
        tableMenu.add(new MenuModelChange(this, -1));
        tableMenu.add(new MenuModelColumnsChange(this, false));
        tableMenu.add(new MenuModelColumnsChange(this, true));

        return tableMenu;
    }

    private JMenu createHeaderMenu(JPanel tablePanel) {
        allEnabled = new MenuEnabled(this, null);

        JMenu ret = new JMenu("Filter Header");
        ret.setMnemonic(KeyEvent.VK_H);
        ret.add(new MenuHeaderOnUse(this));
        ret.add(new MenuHeaderVisible(this));
        ret.add(new MenuPosition(this, tablePanel));
        ret.addSeparator();
        ret.add(new MenuAutoCompletion(this, null));
        ret.add(allEnabled);
        ret.add(new MenuIgnoreCase(this, null));
        ret.add(new MenuInstantFiltering(this, null));
        ret.add(new MenuAutoChoices(this, null));
        ret.addSeparator();
        ret.add(createAppearanceMenu());
        ret.add(new MenuMaxPopupRows(this));
        ret.add(new MenuMaxHistory(this, null));
        ret.addSeparator();
        ret.add(new MenuReset(this, null));

        return ret;
    }

    private JMenu createFiltersMenu() {
        Filter userFilter = new UserFilter(this);
        filtersMenu = new JMenu("Filters");
        filtersMenu.setMnemonic(KeyEvent.VK_F);

        JMenu menu = new JMenu("User filter (name without 'e')");
        menu.add(new MenuUserFilterInclude(this, userFilter));
        menu.add(new MenuUserFilterEnable(this, userFilter));
        filtersMenu.add(menu);
        filtersMenu.addSeparator();

        return filtersMenu;

    }

    private JMenu createMiscellaneousMenu() {

        JMenu ret = new JMenu("Miscellaneous");
        ret.setMnemonic(KeyEvent.VK_M);
        ret.add(new MenuEventsWindow(this));
        ret.addSeparator();
        ret.add(new MenuLookAndFeel(this));

        return ret;
    }

    private JMenu createAppearanceMenu() {
        JMenu ret = new JMenu("appearance");
        for (MenuColorAction.Target color : MenuColorAction.Target.values()) {
            ret.add(new MenuColorAction(this, color));
        }

        ret.addSeparator();
        ret.add(new MenuFont(this));
        ret.addSeparator();
        ret.add(new MenuRowSize(this));

        return ret;
    }

    /** Method to handle the information associated to a (new) filter editor. */
    void handleNewColumn(IFilterEditor editor, TableColumn tc) {
        String name = (String) tc.getHeaderValue();
        boolean countryColumn = name.equalsIgnoreCase(TestTableModel.COUNTRY);
        boolean maleColumn = name.equalsIgnoreCase(TestTableModel.MALE);

        if (countryColumn) {
            tc.setCellRenderer(new FlagRenderer());
            editor.setEditable(false);
        } else if (name.equalsIgnoreCase(TestTableModel.NOTE)) {
        	editor.setEditable(false);
        } else if (name.equalsIgnoreCase(TestTableModel.AGE)) {
            tc.setCellRenderer(new CenteredRenderer());
            editor.setCustomChoices(AgeCustomChoice.getCustomChoices());
        } else if (name.equalsIgnoreCase(TestTableModel.DATE)) {
            tc.setCellRenderer(new DateRenderer(
                    filterHeader.getParserModel().getFormat(Date.class)));

            Set<CustomChoice> choices = new HashSet<CustomChoice>();
            choices.add(new Ages60sCustomChoice());
            CustomChoice december = CustomChoice.create(
            		Pattern.compile("\\d+/12/\\d+"));
            december.setPrecedence(CustomChoice.DEFAULT_PRECEDENCE + 1);
            december.setRepresentation("from December");
            choices.add(december);
            editor.setCustomChoices(choices);
        } else if (maleColumn) {
            tc.setCellRenderer(new MaleRenderer(this, tableSorter));
        }

        JMenu menu = (JMenu) getMenu(filtersMenu, name, false);
        menu.add(new MenuEditable(this, editor));
        menu.add(new MenuEnabled(this, editor));
        menu.add(new MenuAutoChoices(this, editor));
        menu.add(new MenuUIEnabled(this, editor));
        menu.addSeparator();
        menu.add(new MenuAutoCompletion(this, editor));
        menu.add(new MenuIgnoreCase(this, editor));
        menu.add(new MenuInstantFiltering(this, editor));
        menu.add(new MenuMaxHistory(this, editor));
        menu.addSeparator();

        if (countryColumn) {
            menu.add(new MenuCountryFlagRenderer(this, editor));
            menu.add(new MenuCountrySpecialSorter(this));
            menu.addSeparator();
        } else if (maleColumn) {
            MenuMaleCustomChoices cc = new MenuMaleCustomChoices(this, editor);
            menu.add(cc);
            cc.actionPerformed(false);
        } else if (name.equalsIgnoreCase(TestTableModel.HTML_COUNTRY)){
        	menu.add(new MenuHtmlCountry(this, editor));
        }

        menu.add(new MenuColumnRemove(this, name));
        menu.addSeparator();
        menu.add(new MenuReset(this, editor));
    }

    /** {@link ActionHandler} interface. */
    public TestTableModel getTableModel() {
        return tableModel;
    }

    /** {@link ActionHandler} interface. */
    public void initTableModel(int rows) {
        this.tableModel = TestTableModel.createTestTableModel(rows);
        // >>special difference with Java 6
        // instead of table.setModel(tableModel);
        tableSorter.setTableModel(tableModel);
    }

    /** {@link ActionHandler} interface. */
    public JMenu getFilterMenu() {
        return filtersMenu;
    }

    /** {@link ActionHandler} interface. */
    public JTable getTable() {
        return table;
    }

    /** {@link ActionHandler} interface. */
    public JFrame getJFrame() {
        return this;
    }

    /** {@link ActionHandler} interface. */
    public TableFilterHeader getFilterHeader() {
        return filterHeader;
    }

    /** {@link ActionHandler} interface. */
    public void updateEnabledFlag() {
        allEnabled.setSelected(filterHeader.isEnabled());
    }


    /** {@link ActionHandler} interface. */
    public void updateFiltersInfo() {
        TableColumnModel model = table.getColumnModel();
        int n = model.getColumnCount();
        while (n-- > 0) {
            TableColumn tc = model.getColumn(n);
            updateFilterInfo(filterHeader.getFilterEditor(tc.getModelIndex()),
                (String) tc.getHeaderValue());
        }
    }

    /** {@link ActionHandler} interface. */
    public void updateFilterInfo(IFilterEditor editor, String columnName) {
        JMenu menu = (JMenu) getMenu(filtersMenu, columnName, false);
        ((JCheckBoxMenuItem) getMenu(menu, MenuAutoCompletion.NAME, false))
        .setSelected(editor.isAutoCompletion());
        ((JCheckBoxMenuItem) getMenu(menu, MenuEditable.NAME, false))
            .setSelected(editor.isEditable());
        ((JCheckBoxMenuItem) getMenu(menu, MenuEnabled.NAME, false))
            .setSelected(editor.getFilter().isEnabled());
        ((JCheckBoxMenuItem) getMenu(menu, MenuIgnoreCase.NAME, false))
            .setSelected(editor.isIgnoreCase());
        ((JCheckBoxMenuItem) getMenu(menu, MenuInstantFiltering.NAME, false))
            .setSelected(editor.isInstantFiltering());

        JMenu autoChoicesMenu = (JMenu) getMenu(menu, MenuAutoChoices.NAME,
                false);
        ((JRadioButtonMenuItem) getMenu(autoChoicesMenu,
                editor.getAutoChoices().toString().toLowerCase(), false))
            .setSelected(true);

        JMenu historyMenu = (JMenu) getMenu(menu, MenuMaxHistory.NAME, false);
        JRadioButtonMenuItem item = ((JRadioButtonMenuItem) getMenu(historyMenu,
                    String.valueOf(editor.getMaxHistory()), false));
        if (item != null) {
            item.setSelected(true);
        }
    }

    /** Creates / removed a submenu with the given name. */
    JMenuItem getMenu(JMenu menu, String name, boolean remove) {
        int pos = menu.getItemCount();
        while (pos-- > 0) {
            JMenuItem item = menu.getItem(pos);
            if ((item != null) && item.getText().equals(name)) {
                if (remove) {
                    menu.remove(pos);
                }

                return item;
            }
        }

        if (remove) {
            return null;
        }

        JMenu ret = new JMenu(name);
        menu.add(ret);

        return ret;
    }

    public final static void init(int modelRows) {
        TableFilterExample frame = new TableFilterExample(modelRows);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        if (START_LARGE_MODEL){
        	frame.setSize(1200, frame.getSize().height);
        }
        frame.setVisible(true);    	
    }
    
    public final static void main(String args[]) {
        FilterSettings.autoChoices = AutoChoices.ENABLED;
        init(DEFAULT_MODEL_ROWS);
    }

}
