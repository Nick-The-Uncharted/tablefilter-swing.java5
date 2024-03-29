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

package net.coderazzi.filters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import net.coderazzi.filters.artifacts.RowFilter;


/**
 * Commodity class implementing the interface {@link
 * net.coderazzi.filters.IFilter} on a {@link RowFilter}.
 *
 * @author  Luis M Pena - lu@coderazzi.net
 */
abstract public class Filter extends RowFilter implements IFilter {

    /** The set of currently subscribed observers. */
    private Set<IFilterObserver> filterObservers =
        new HashSet<IFilterObserver>();

    /** The enabled state. */
    private boolean enabled = true;

    /** @see  IFilter#isEnabled() */
    public boolean isEnabled() {
        return enabled;
    }

    /** @see  IFilter#setEnabled(boolean) */
    public void setEnabled(boolean enable) {
        if (enable != this.enabled) {
            this.enabled = enable;
            reportFilterUpdatedToObservers();
        }
    }

    /** @see  IFilter#addFilterObserver(IFilterObserver) */
    public void addFilterObserver(IFilterObserver observer) {
        filterObservers.add(observer);
    }

    /** @see  IFilter#removeFilterObserver(IFilterObserver) */
    public void removeFilterObserver(IFilterObserver observer) {
        filterObservers.remove(observer);
    }

    /** Returns all the registered {@link IFilterObserver} instances. */
    public Set<IFilterObserver> getFilterObservers() {
        return new HashSet<IFilterObserver>(filterObservers);
    }

    /**
     * Method to be called by subclasses to report to the observers that the
     * filter has changed.
     */
    public void reportFilterUpdatedToObservers() {
        for (IFilterObserver obs
                : new ArrayList<IFilterObserver>(filterObservers)) {
            obs.filterUpdated(this);
        }
    }
}
