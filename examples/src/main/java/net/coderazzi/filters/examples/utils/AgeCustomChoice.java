package net.coderazzi.filters.examples.utils;

import java.util.HashSet;
import java.util.Set;

import net.coderazzi.filters.artifacts.RowFilter;
import net.coderazzi.filters.gui.CustomChoice;
import net.coderazzi.filters.gui.IFilterEditor;


public class AgeCustomChoice extends CustomChoice {

    private static final long serialVersionUID = -4580882606646752756L;
    
    int min;
    int max;

    private AgeCustomChoice(String text, int min, int max, int precedence) {
        super(text, null, precedence);
        this.min = min;
        this.max = max;
    }

    @Override public RowFilter getFilter(IFilterEditor editor) {
        final int modelIndex = editor.getModelIndex();

        return new RowFilter() {
            @Override public boolean include(Entry entry) {
                Object value = entry.getValue(modelIndex);
                if (value instanceof Integer) {
                    int age = (Integer) value;

                    return (age >= min) && (age <= max);
                }

                return false;
            }
        };
    }

    public static Set<CustomChoice> getCustomChoices() {
        Set<CustomChoice> ret = new HashSet<CustomChoice>();
        ret.add(new AgeCustomChoice("below 20", 0, 19, DEFAULT_PRECEDENCE + 1));
        ret.add(new AgeCustomChoice("20-29", 20, 29, DEFAULT_PRECEDENCE + 2));
        ret.add(new AgeCustomChoice("30-35", 30, 34, DEFAULT_PRECEDENCE + 3));
        ret.add(new AgeCustomChoice("over 35", 35, 100,
                DEFAULT_PRECEDENCE + 4));

        return ret;
    }

}
