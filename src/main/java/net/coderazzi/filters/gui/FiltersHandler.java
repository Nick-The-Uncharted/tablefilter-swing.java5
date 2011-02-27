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

package net.coderazzi.filters.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.table.TableModel;

import net.coderazzi.filters.AndFilter;
import net.coderazzi.filters.IFilter;
import net.coderazzi.filters.IFilterObserver;
import net.coderazzi.filters.artifacts.ITableModelFilter;
import net.coderazzi.filters.artifacts.RowFilter;
import net.coderazzi.filters.artifacts.TableModelFilter;
import net.coderazzi.filters.gui.editor.FilterEditor;


/**
 * <p>FiltersHandler represents a {@link RowFilter} instance that can be
 * attached to a {@link javax.swing.JTable} to compose dynamically the outcome
 * of one or more filter editors. As such, it is a dynamic filter, which updates
 * the table when there are changes in any of the composed sub filters.</p>
 *
 * <p>Users have, after version 3.2, no direct use for this class</p>
 *
 * <p>In Java 5, the {@link javax.swing.JTable} has no sorting or filtering
 * capabilities, so the implementation of the TableFilter is slightly different
 * in Java 5 and 6. When the table is attached, it should contain already the
 * model to filter, and the model itself should be an instance of {@link
 * ITableModelFilter}. If this is not the case, the TableFilter automatically
 * creates a (@link ITableModelFilter} and attaches it to the table.</p>
 *
 * <p>It is important, therefore, not to override afterwards the model in the
 * table, or, if this is done, it is needed to invoke {@link
 * FiltersHandler#setModel(TableModel)} on the TableFilter.</p>
 *
 * @author  Luis M Pena - lu@coderazzi.net
 */
public class FiltersHandler extends AndFilter
    implements PropertyChangeListener {

    /**
     * sendNotifications is used internally as a semaphore to disable
     * temporarily notifications to the filter observers. Notifications are only
     * sent to the observers when this variable is non negative.
     */
    int sendNotifications = 0;

    /**
     * pendingNotifications keeps track of notifications to be sent to the
     * observers, but were discarded because the variable sendNotifications was
     * negative.
     */
    private boolean pendingNotifications;

    /** The autoChoices mode.* */
    private AutoChoices autoChoices = FilterSettings.autoChoices;

    /** All the editors, mapped by their filter position. */
    private Map<Integer, FilterEditor> editors =
        new HashMap<Integer, FilterEditor>();

    /** The associated table. */
    private JTable table;

    /** Instance to handle choices (choices) on each FilterEditor. */
    private ChoicesHandler choicesHandler = new NonAdaptiveChoicesHandler(this);

    /** The associated filter model. */
    private IParserModel parserModel;

    /** Only constructor. */
    FiltersHandler() {

        // create an observer instance to notify the associated table when there
        // are filter changes.
        addFilterObserver(new IFilterObserver() {
                public void filterUpdated(IFilter obs) {
                    notifyUpdatedFilter();
                }
            });
    }

    /**
     * Method to set the associated table. If the table had not defined its own
     * {@link javax.swing.RowSorter}, the default one is automatically created.
     */
    public void setTable(JTable table) {
        choicesHandler.setInterrupted(true);
        if (this.table != null) {
            TableModel tm = this.table.getModel();
            if (tm instanceof ITableModelFilter) {
                this.table.setModel(((ITableModelFilter) tm).getModel());
            }
        }

        this.table = table;
    }

    /** Returns the associated table. */
    public JTable getTable() {
        return table;
    }

    public void setParserModel(IParserModel parserModel) {
        if ((parserModel != null) && (parserModel != this.parserModel)) {
            if (this.parserModel != null) {
                this.parserModel.removePropertyChangeListener(this);
            }

            this.parserModel = parserModel;
            this.parserModel.addPropertyChangeListener(this);
            enableNotifications(false);
            for (FilterEditor editor : editors.values()) {
                editor.resetFilter();
            }

            enableNotifications(true);
        }

        this.parserModel = parserModel;
    }

    public IParserModel getParserModel() {
        return parserModel;
    }

    /**
     * {@link PropertyChangeListener} interface, for changes on {@link
     * IParserModel}.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        Class target;
        boolean formatChange = false;
        if (evt.getPropertyName() == IParserModel.IGNORE_CASE_PROPERTY) {
            target = null;
        } else {
            if (evt.getPropertyName() == IParserModel.FORMAT_PROPERTY) {
                formatChange = true;
            } else if (evt.getPropertyName()
                    != IParserModel.COMPARATOR_PROPERTY) {
                return;
            }

            Object cl = evt.getNewValue();
            if (cl instanceof Class) {
                target = (Class) cl;
            } else {
                return;
            }
        }

        enableNotifications(false);
        for (FilterEditor editor : editors.values()) {
            if (target == null) {
                editor.setIgnoreCase(parserModel.isIgnoreCase());
            } else if (editor.getModelClass() == target) {
                if (formatChange) {
                    editor.setFormat(parserModel.getFormat(target));
                } else {
                    editor.setComparator(parserModel.getComparator(target));
                }
            }
        }

        enableNotifications(true);
    }

    @Override public void setEnabled(boolean enabled) {
        enableNotifications(false);
        super.setEnabled(enabled);
        enableNotifications(true);
    }

    /** Sets/unsets the auto choices flag. */
    public void setAutoChoices(AutoChoices mode) {
        if (mode != autoChoices) {
            enableNotifications(false);
            this.autoChoices = mode;
            for (FilterEditor editor : editors.values()) {
                // after this call, the editor will request its choices
                editor.setAutoChoices(mode);
            }

            enableNotifications(true);
        }
    }

    /** Returns the auto choices mode. */
    public AutoChoices getAutoChoices() {
        return autoChoices;
    }

    @Override public void addFilter(IFilter... filtersToAdd) {
        choicesHandler.filterOperation(true);
        super.addFilter(filtersToAdd);
        choicesHandler.filterOperation(false);
    }

    @Override public void removeFilter(IFilter... filtersToRemove) {
        choicesHandler.filterOperation(true);
        super.removeFilter(filtersToRemove);
        choicesHandler.filterOperation(false);
    }

    /** Adds a new filter editor. */
    public void addFilterEditor(FilterEditor editor) {
        super.addFilter(editor.getFilter());
        editors.put(editor.getModelIndex(), editor);
        editor.setAutoChoices(autoChoices);
    }

    /** Removes an existing editor. */
    public void removeFilterEditor(FilterEditor editor) {
        super.removeFilter(editor.getFilter());
        editors.remove(editor.getModelIndex());
    }

    /**
     * Method invoked by the FilterEditor when its autoChoices mode OR user
     * choices change; in return, it will set the proper choices on the
     * specified editor.
     */
    public void updateEditorChoices(FilterEditor editor) {
        if (editors.containsValue(editor) && isEnabled()) {
            choicesHandler.editorUpdated(editor);
        }
    }

    @Override public void filterUpdated(IFilter filter) {
        boolean wasEnabled = isEnabled();
        boolean filterWasDisabled = isDisabled(filter);
        choicesHandler.filterUpdated(filter);
        super.filterUpdated(filter);
        if (filterWasDisabled && filter.isEnabled()) {
            choicesHandler.filterEnabled(filter);
        } else if (wasEnabled && !isEnabled()) {
            choicesHandler.allFiltersDisabled();
        }
    }

    /** Internal method to set/update the filtering. */
    public void updateTableFilter() {
        pendingNotifications = false;
        if (table != null) {
            TableModel model = table.getModel();
            if (model != null) {
                getRowFilter(model).setRowFilter(isEnabled() ? this : null);
            }
        }
    }

    public Collection<FilterEditor> getEditors() {
        return editors.values();
    }

    public FilterEditor getEditor(int column) {
        return editors.get(column);
    }

    /**
     * <p>Temporarily enable/disable notifications to the observers, including
     * the registered {@link javax.swing.JTable}.</p>
     *
     * <p>Multiple calls to this method can be issued, but the caller must
     * ensure that there are as many calls with true parameter as with false
     * parameter, as the notifications are only re-enabled when the zero balance
     * is reached.</p>
     */
    public void enableNotifications(boolean enable) {
        sendNotifications += enable ? 1 : -1;
        if (enable) {
            if (sendNotifications == 0) {

                // adding/removing filter editors is not done as separate
                // processes when the user sets/changes the TableModel, all
                // columns are removed and then recreated. At the beginning of
                // the process, there are calls to
                // enableNotifications(false)/(true) than only balance out when
                // the whole model is setup. We use the same mechanism whenever
                // it would be needed to recreate the adaptive support or
                // because it could be more efficient doing so.
                if (choicesHandler.setInterrupted(false)
                        || pendingNotifications) {
                    updateTableFilter();
                }
            }
        } else if (choicesHandler.setInterrupted(true)) {
            updateTableFilter();
        }
    }

    /**
     * Internal method to send a notification to the observers, verifying first
     * if the notifications are currently enabled.
     */
    void notifyUpdatedFilter() {
        if (sendNotifications < 0) {
            pendingNotifications = true;
        } else {
            updateTableFilter();
        }
    }

    /**
     * Returns the row filter associated to the current table, creating a
     * default one if none.
     */
    private ITableModelFilter getRowFilter(TableModel tableModel) {
        if (tableModel instanceof ITableModelFilter) {
            return (TableModelFilter) tableModel;
        }

        TableModelFilter modelFilter = new TableModelFilter(tableModel);
        table.setModel(modelFilter);

        return modelFilter;
    }

}
