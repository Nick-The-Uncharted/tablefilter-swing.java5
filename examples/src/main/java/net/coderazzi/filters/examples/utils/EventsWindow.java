package net.coderazzi.filters.examples.utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import net.coderazzi.filters.gui.IFilterEditor;
import net.coderazzi.filters.gui.IFilterHeaderObserver;
import net.coderazzi.filters.gui.TableFilterHeader;


@SuppressWarnings("serial")
public class EventsWindow extends JDialog implements IFilterHeaderObserver,
    PropertyChangeListener {

    TableModel tableModel;
    private static final String EXCLUDED = "Excluded";
    private static final String CREATED = "Created";
    static String COLUMN_NAMES[] = {
            "Event", "Column", "{Content|Event}.type", "Value"
        };
    static Class<?> COLUMN_CLASSES[] = {
            String.class, String.class, String.class, String.class
        };

    class Event {
        String name;
        String column;
        String type;
        String content;
    }

    class TableModel extends AbstractTableModel {
        private List<Event> events = new ArrayList<Event>();

        public void addEvent(Event event) {
            events.add(event);
            fireTableRowsInserted(events.size() - 1, events.size() - 1);
        }

        public int getColumnCount() {
            return COLUMN_NAMES.length;
        }

        @Override public Class<?> getColumnClass(int columnIndex) {
            return COLUMN_CLASSES[columnIndex];
        }

        @Override public String getColumnName(int column) {
            return COLUMN_NAMES[column];
        }

        public int getRowCount() {
            return events.size();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            Event event = events.get(rowIndex);
            switch (columnIndex) {

            case 0:
                return event.name;

            case 1:
                return event.column;

            case 2:
                return event.type;

            case 3:
                return event.content;
            }

            return null;
        }
    }

    class Renderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable  table,
                Object  value,
                boolean isSelected,
                boolean hasFocus,
                int     row,
                int     column) {
            Object o = tableModel.getValueAt(row, 0);
            Color c;
            if (o.equals(CREATED)) {
                c = Color.GREEN;
            } else if (o.equals(EXCLUDED)) {
                c = Color.red;
            } else {
                c = Color.blue;
            }

            Component ret = super.getTableCellRendererComponent(table,
                    (value == null) ? "" : value, isSelected, hasFocus, row,
                    column);
            ret.setForeground(c);

            return ret;
        }
    }

    public EventsWindow(Frame parent, final TableFilterHeader header) {
        super(parent, "Filter Events", false);
        tableModel = new TableModel();

        JTable table = new JTable(tableModel);
        getContentPane().add(new JScrollPane(table));
        setSize(new Dimension(400, 300));
        header.addHeaderObserver(this);

        Renderer renderer = new Renderer();
        for (int i = 0; i < COLUMN_NAMES.length; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        addWindowListener(new WindowAdapter() {
                @Override public void windowClosing(WindowEvent e) {
                    header.removeHeaderObserver(EventsWindow.this);
                }
            });
        header.getParserModel().addPropertyChangeListener(this);
    }

    public void tableFilterEditorCreated(TableFilterHeader header,
                                         IFilterEditor     editor,
                                         TableColumn       tc) {
        Event event = new Event();
        event.name = CREATED;
        event.column = tc.getHeaderValue().toString();
        tableModel.addEvent(event);
    }

    public void tableFilterEditorExcluded(TableFilterHeader header,
                                          IFilterEditor     editor,
                                          TableColumn       tc) {
        Event event = new Event();
        event.name = EXCLUDED;
        event.column = tc.getHeaderValue().toString();
        tableModel.addEvent(event);
    }

    public void tableFilterUpdated(TableFilterHeader header,
                                   IFilterEditor     editor,
                                   TableColumn       tc) {
        Event event = new Event();
        event.name = "Updated";
        event.type = editor.getContent().getClass().getCanonicalName();
        event.content = editor.getContent().toString();
        event.column = tc.getHeaderValue().toString();
        tableModel.addEvent(event);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        Event event = new Event();
        event.name = "Global TextModel";
        event.type = evt.getPropertyName();

        Object value = evt.getNewValue();
        if (value instanceof Class) {
            event.column = "Class " + value.toString();
            event.content = null;
        } else {
            event.column = "*";
            event.content = evt.getNewValue().toString();
        }

        tableModel.addEvent(event);
    }

}
