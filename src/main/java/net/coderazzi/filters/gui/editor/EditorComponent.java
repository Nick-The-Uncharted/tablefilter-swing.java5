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

package net.coderazzi.filters.gui.editor;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.text.Format;
import java.text.ParseException;

import java.util.Comparator;
import java.util.regex.Pattern;

import javax.swing.CellRendererPane;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.plaf.TextUI;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import net.coderazzi.filters.IParser;
import net.coderazzi.filters.IParser.InstantFilter;
import net.coderazzi.filters.artifacts.RowFilter;
import net.coderazzi.filters.gui.CustomChoice;
import net.coderazzi.filters.gui.Look;


/**
 * <p>Component representing the filter editor itself, where the user can enter
 * the filter text (if not rendered) and is displayed the current filter
 * choice.</p>
 *
 * <p>The underlying component is a {@link JTextField}, even when the content is
 * rendered.</p>
 */
class EditorComponent extends JTextField {

    private static final long serialVersionUID = -2196080442586435546L;

    private Controller controller;
    private boolean focus;
    boolean instantFiltering;
    boolean autoCompletion;
    FilterEditor filterEditor;
    PopupComponent popup;
    static final Pattern newLinePattern = Pattern.compile("[\n\r\t\f]");

    public EditorComponent(FilterEditor   editor,
                           PopupComponent popupComponent) {
        super(15); // created with 15 columns
        this.filterEditor = editor;
        this.popup = popupComponent;
        this.controller = new EditableTextController();
    }

    @Override public void setUI(TextUI ui) {
        super.setUI(ui);
        // whatever the LookAndFeel, display no border
        setBorder(null);
    }

    @Override protected void paintComponent(Graphics g) {
        controller.paintComponent(g);
    }

    @Override public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (controller != null) {
            setCaretPosition(0);
            moveCaretPosition(0);
            updateLook();
            setFocusable(enabled);
        }
    }

    /** Updates the current look. */
    public void updateLook() {
        controller.updateLook();
    }

    /**
     * Returns the filter associated to the current content.<br>
     * Always invoked after {@link #consolidateFilter()}
     */
    public RowFilter getFilter() {
        return controller.getFilter();
    }

    /** Returns the definition associated to the current editor. */
    public Object getContent() {
        return controller.getContent();
    }

    /** Sets the editor content. */
    public void setContent(Object content) {
        controller.setContent(content);
    }

    /** Requests an update on the text parser used by the editor. */
    public void updateParser() {
        if (controller instanceof TextController) {
            ((TextController) controller).setParser(
                filterEditor.createParser());
        }
    }

    /** Requests the parser to escape choices, which can be null. */
    public IChoicesParser getChoicesParser() {
        return (controller instanceof IChoicesParser)?
        		(IChoicesParser)controller : null;
    }

    /** Returns the editable flag. */
    public boolean isEditableContent() {
        return controller instanceof EditableTextController;
    }

    /** Sets the instant filtering flag. */
    public void setInstantFiltering(boolean enable) {
        this.instantFiltering = enable;
    }

    /** Returns the instant filtering flag. */
    public boolean isInstantFiltering() {
        return instantFiltering;
    }

    /** Sets the auto completion flag. */
    public void setAutoCompletion(boolean enable) {
        this.autoCompletion = enable;
    }

    /** Returns the auto completion flag. */
    public boolean isAutoCompletion() {
        return autoCompletion;
    }

    /** Sets the text mode and editable flag. */
    public void setTextMode(boolean editable) {
        if (controller != null) {
            if (editable && (controller instanceof EditableTextController)) {
                return;
            }

            if (!editable
                    && (controller instanceof NonEditableTextController)) {
                return;
            }

            controller.detach();
        }

        if (editable) {
            controller = new EditableTextController();
        } else {
            controller = new NonEditableTextController();
        }

        updateParser();
    }

    /** Sets the render mode. */
    public void setRenderMode() {
        if (controller != null) {
            if (controller instanceof RenderedController) {
                return;
            }

            controller.detach();
        }

        controller = new RenderedController();
        filterEditor.filterUpdated(null);
    }

    /** Returns true if the content is valid. */
    public boolean isValidContent() {
        return controller.isValidContent();
    }

    /**
     * Consolidates the current filter, usually done only when the user presses
     * the ENTER keys or the editor loses the focus.
     */
    public void consolidateFilter() {
        controller.consolidateFilter();
    }

    /** Informs that the editor has received the focus. */
    public void focusMoved(boolean gained) {
        focus = gained;
        controller.focusMoved(gained);
        if (gained) {
            // select all text
            setCaretPosition(0);
            moveCaretPosition(getText().length());
        }
    }

    /** Returns true if the focus is on this editor. */
    public boolean isFocused() {
        return focus;
    }

    Look prepareComponentLook(CustomChoice cc) {
        return popup.getFilterRenderer()
                .prepareComponentLook(this, isFocused(), cc);
    }

    void superPaintComponent(Graphics g) {
        super.paintComponent(g);
    }


    /** The JTextField is controlled via this interface. */
    private interface Controller {

        /**
         * Called to replace the basic {@link
         * JTextField#paintComponents(Graphics)} functionality.
         */
        void paintComponent(Graphics g);

        /** Detaches the controller, not to be used again. */
        void detach();

        /** @see  EditorComponent#setContent(Object) */
        void setContent(Object content);

        /** @see  EditorComponent#getContent() */
        Object getContent();

        /** @see  EditorComponent#isValidContent() */
        boolean isValidContent();

        /** @see  EditorComponent#getFilter() */
        RowFilter getFilter();

        /** @see  EditorComponent#consolidateFilter() */
        void consolidateFilter();

        /** @see  EditorComponent#updateLook() */
        void updateLook();

        /** @see  EditorComponent#focusMoved(boolean) */
        void focusMoved(boolean gained);
    }


    /** Controller interface to handle editors with content rendered. */
    private class RenderedController extends MouseAdapter
        implements Controller {

        private Object content = CustomChoice.MATCH_ALL;
        private CellRendererPane painter = new CellRendererPane();
        RowFilter filter;
        Object cachedContent = content;

        RenderedController() {
            addMouseListener(this);
            setEditable(false);
        }

        public void paintComponent(Graphics g) {
            Component c = popup.getFilterRenderer()
                    .getCellRendererComponent(content, getWidth(), isFocused());
            painter.paintComponent(g, c, EditorComponent.this, 0, 0, getWidth(),
                getHeight());
        }

        public void detach() {
            removeMouseListener(this);
        }

        public void setContent(Object content) {
            this.content = content;
            repaint();
            consolidateFilter();
        }

        public Object getContent() {
            return content;
        }

        public boolean isValidContent() {
            return true;
        }

        public RowFilter getFilter() {
            return filter;
        }

        public void consolidateFilter() {
            Object currentContent = getContent();
            if (currentContent != cachedContent) {
                cachedContent = currentContent;
                if (cachedContent instanceof CustomChoice) {
                    filter = ((CustomChoice) cachedContent).getFilter(
                            filterEditor);
                } else {
                    filter = new RowFilter() {
                        @Override public boolean include(
                                RowFilter.Entry entry) {
                            Object val = entry.getValue(
                                    filterEditor.getModelIndex());

                            return (val == null) ? (cachedContent == null)
                                                 : val.equals(cachedContent);
                        }
                    };
                }

                filterEditor.filterUpdated(filter);
            }
        }

        public void updateLook() {
            prepareComponentLook(null);
        }

        public void focusMoved(boolean gained) {
            repaint();
        }

        /** @see  MouseAdapter#mouseClicked(MouseEvent) */
        @Override public void mouseClicked(MouseEvent e) {
            if (isEnabled()) {
                filterEditor.triggerPopup(filterEditor);
            }
        }

    }


    /** Parent class of controllers with text enabled edition. */
    private abstract class TextController 
    	implements Controller, CaretListener, IChoicesParser {

        protected IParser textParser;
        // userUpdate is true when the content is being updated internally,
        // not due to programmed actions (setContent / setText)
        protected boolean userUpdate = true;
        // the content, which not necessarily matches the current text
        private Object content;
        // the filter associated to the content variable
        private RowFilter filter;
        private boolean error;
        private boolean useCustomDecoration;

        TextController() {
            setEditable(true);
            setText(CustomChoice.MATCH_ALL.toString());
            addCaretListener(this);
        }

        /**
         * Sets the parser used in the filter. Note This controller is not
         * functional until this parser is set
         */
        public void setParser(IParser textParser) {
            this.textParser = textParser;
            if (isEnabled()) {
                updateFilter();
            }
        }

        public void paintComponent(Graphics g) {
            superPaintComponent(g);
            if (useCustomDecoration && (content instanceof CustomChoice)) {
                filterEditor.getLook()
                    .getCustomChoiceDecorator()
                    .decorateComponent((CustomChoice) content, filterEditor,
                        isFocused(), EditorComponent.this, g);
            }
        }

        public void detach() {
            removeCaretListener(this);
        }

        public void setContent(Object content) {
            String text;
            ChoiceMatch match = new ChoiceMatch();
            if (content instanceof CustomChoice) {
                // never escape custom choices
                text = ((CustomChoice) content).toString();
                match.content = content;
            } else {
                if (content instanceof String) {
                    text = (String) content;
                } else {
                    Format fmt = filterEditor.getFormat();
                    text = (fmt == null) ? content.toString()
                                         : fmt.format(content);
                }

                match.content = text;
            }

            match.exact = true; // avoid interpretation
            setEditorText(text);
            updateFilter(text, match, false);
            activateCustomDecoration();
        }

        public Object getContent() {
            if (!instantFiltering) {
                // in this case, the content is not always updated,
                // try an update now, if needed
                String ret = getText();
                if (!ret.equals(content.toString())) {
                    return ret;
                }
            }

            return content;
        }

        public boolean isValidContent() {
            return !error;
        }

        public void consolidateFilter() {
            String text = getText();
            String content = this.content.toString();
            if (!text.equals(content)) {
	            if (instantFiltering) {
	                // with instant filtering, the filter could be the instant
	                // expression (normally the test + '*'). If this is the case,
	                // show it
                    consolidateInstantFilter(text, content);
	            } else {
	                updateFilter();
	            }
            }
            // remove now any selection and try to activate custom decoration
            getCaret().setDot(getCaret().getDot());
            activateCustomDecoration();
        }

        public RowFilter getFilter() {
            return filter;
        }

        public void updateLook() {
            CustomChoice cc =
                (useCustomDecoration && (content instanceof CustomChoice))
                ? (CustomChoice) content : null;
            Look look = prepareComponentLook(cc);
            if (isEnabled() && error) {
                Color foreground = look.getErrorForeground();
                if (foreground != getForeground()) {
                    setForeground(foreground);
                }
            }

            Color selection = look.getTextSelection();
            if (getSelectionColor() != selection) {
                setSelectionColor(selection);
            }
        }

        public void focusMoved(boolean gained) {
            updateLook();
        }


        /** @see  CaretListener#caretUpdate(CaretEvent) */
        public void caretUpdate(CaretEvent e) {
            // if the user moves the cursor on the editor, the focus passes
            // automatically back to the editor (from the popup)
            if (isEnabled()) {
                popup.setPopupFocused(false);
                deactivateCustomDecoration();
            }
        }

        /** Reports that the current content is wrong. */
        protected void setError(boolean error) {
            if (this.error != error) {
                this.error = error;
                if (isEnabled()) {
                    updateLook();
                }
            }
        }

        /** Returns the best match for a given hint. */
        protected ChoiceMatch getBestMatch(String hint) {
            ChoiceMatch ret = popup.selectBestMatch(hint, false);
            popup.setPopupFocused(false);

            return ret;
        }

        /** Returns an exact match for a given hint. */
        protected ChoiceMatch getExactMatch(String hint) {
            ChoiceMatch ret = popup.selectBestMatch(hint, true);
            popup.setPopupFocused(false);

            return ret;
        }

        /**
         * Activates, if possible, the custom decoration, that is, if the
         * content is a CustomChoice and has an associated icon.
         */
        private boolean activateCustomDecoration() {
            boolean ret = false;
            if (!useCustomDecoration && (content instanceof CustomChoice)) {
                useCustomDecoration = true;
                updateLook();
                repaint();
                ret = true;
            }

            return ret;
        }

        /** Deactivates the custom decoration. */
        protected void deactivateCustomDecoration() {
            if (useCustomDecoration) {
                useCustomDecoration = false;
                updateLook();
                repaint();
            }
        }

        protected void updateFilter() {
            updateFilter(null, null, false);
        }

        /**
         * Updates the filter and content variables, propagating the filter.
         *
         * @param  text        the current content; if null, is retrieved from
         *                     the text field
         * @param  match       the popup match for the given text. If null, is
         *                     retrieved from the text
         * @param  userUpdate  true if the update is due to some user input
         */
        protected void updateFilter(String      text,
                                    ChoiceMatch match,
                                    boolean     userUpdate) {
            RowFilter currentFilter = filter;
            boolean error = false;
            if (text == null) {
                match = null;
                text = getText();
            }

            if (match == null) {
                match = getBestMatch(text);
            }
            // perform actions in a try/catch due to text parsing exceptions

            try {
                if (match.exact) {
                    content = match.content;
                    if (match.content instanceof CustomChoice) {
                        filter = ((CustomChoice) content).getFilter(
                                filterEditor);
                    } else {
                        filter = textParser.parseText(parseEscape(text));
                    }
                } else if (instantFiltering && userUpdate) {
                	// time to try the parseInstantText, if needed
                    filter = textParser.parseText(parseEscape(text));
                    if (filterEditor.attemptFilterUpdate(filter)) {
                        content = text;
                    } else {
                        InstantFilter iFilter = textParser.parseInstantText(
                                parseEscape(text));
                        content = iFilter.expression;
                        filter = iFilter.filter;
                    }
                } else {
                    filter = textParser.parseText(parseEscape(text));
                    content = text;
                }
            } catch (ParseException pex) {
                filter = null;
                content = text;
                error = true;
                match = null;
            }

            setError(error);
            if (filter != currentFilter) {
                if (userUpdate) {
                    // in this case, the filter is only propagated if it does
                    // not filter all rows out. If it would, just set the
                    // warning color -unset it otherwise-
                    filterEditor.attemptFilterUpdate(filter);
                } else {
                    filterEditor.filterUpdated(filter);
                }
            }
        }

        /** Sets the editor text, as a programmed action (userUpdate=false). */
        protected void setEditorText(String text) {
            userUpdate = false;
            setText(text);
            userUpdate = true;
        }

        /**
         * Method called when consolidating a filter instant, if the text and
         * the filter content do not match.
         */
        abstract protected void consolidateInstantFilter(String text,
                                                         String content);

        /** Method called to handle parse text before invoking the parser. */
        abstract protected String parseEscape(String text);
    }


    /** TextController for editable content. */
    private class EditableTextController extends TextController {

        EditableTextController() {
            ((AbstractDocument) getDocument()).setDocumentFilter(
                new ControllerDocumentFilter());
        }

        @Override public void detach() {
            super.detach();
            ((AbstractDocument) getDocument()).setDocumentFilter(null);
        }

        public String escapeChoice(String s) {
        	return textParser.escape(textParser.stripHtml(s));
        }

        @Override protected String parseEscape(String text) {
            // content on editable fields is always escaped, so there is
            // no need to escape it again
            return text;
        }

        @Override protected void consolidateInstantFilter(String text,
                                                          String content) {
            // content is the real filter match on use, so set it
            setEditorText(content);
        }

        /**
         * DocumentFilter instance to handle any user's input, in order to react
         * to text changes and to also provide autocompletion.
         */
        class ControllerDocumentFilter extends DocumentFilter {

            @Override public void insertString(FilterBypass fb,
                                               int          offset,
                                               String       string,
                                               AttributeSet attr) {
                // we never use it, we never invoke Document.insertString
                // note that normal (non programmatically) editing only invokes
                // replace/remove
            }

            @Override public void replace(FilterBypass fb,
                                          int          offset,
                                          int          length,
                                          String       text,
                                          AttributeSet attrs)
                                   throws BadLocationException {
                int moveCaretLeft = 0;
                boolean singleCharacter = text.length() == 1;
                // avoid new lines, etc, see
                // http://code.google.com/p/tablefilter-swing/issues/detail?id=13
                text = newLinePattern.matcher(text).replaceAll(" ");
                if (autoCompletion && userUpdate && singleCharacter) {
                    String now = getText();
                    // autocompletion is only triggered if the user inputs
                    // a character at the end of the current text
                    if (now.length() == (offset + length)) {
                        String begin = now.substring(0, offset) + text;
                        String completion = popup.getCompletion(begin);
                        text += completion;
                        moveCaretLeft = completion.length();
                    }
                }

                super.replace(fb, offset, length, text, attrs);
                editorUpdated();
                // the 'completion' part remains selected, for easily removal
                if (moveCaretLeft > 0) {
                    int caret = getDocument().getLength();
                    setCaretPosition(caret);
                    moveCaretPosition(caret - moveCaretLeft);
                }
            }

            @Override public void remove(FilterBypass fb,
                                         int          offset,
                                         int          length)
                                  throws BadLocationException {
                // special case if the removal is due to BACK SPACE
                if ((offset > 0) && (offset == getCaretPosition())) {
                    AWTEvent ev = EventQueue.getCurrentEvent();
                    if ((ev instanceof KeyEvent)
                            && (((KeyEvent) ev).getKeyCode()
                                == KeyEvent.VK_BACK_SPACE)) {
                        --offset;
                        ++length;
                        setCaretPosition(offset);
                    }
                }

                super.remove(fb, offset, length);
                editorUpdated();
            }

            /** handles any editor update, if userUpdate is true. */
            private void editorUpdated() {
                // this action is only taken when the user is entering text,
                // not when is programmatically set (setText)
                if (userUpdate) {
                    deactivateCustomDecoration();
                    setError(false);

                    String text = getText();
                    // the best match is anyway obtained to select the proper
                    // choice on the popup
                    if (instantFiltering || popup.isVisible()) {
                        ChoiceMatch match = getBestMatch(text);
                        if (instantFiltering) {
                            updateFilter(text, match, true);
                        }
                    }
                }
            }
        }
    }


    /** TextController for non editable content. */
    private class NonEditableTextController extends TextController {

        NonEditableTextController() {
            ((AbstractDocument) getDocument()).setDocumentFilter(
                new ControllerDocumentFilter());
        }

        @Override public void detach() {
            super.detach();
            ((AbstractDocument) getDocument()).setDocumentFilter(null);
        }

        public String escapeChoice(String s) {
        	return textParser.stripHtml(s);
        }

        @Override protected String parseEscape(String text) {
            // choices are not escaped, escape them now therefore
            return textParser.escape(text);
        }

        @Override protected void consolidateInstantFilter(String text,
                                                          String content) {

            // in this case, the text does not represent the real filter for
            // example, if the user entered 'Ha' the text could be 'Harold', but
            // the current filter 'Ha*'. Update therefore the filter
            updateFilter(text, null, false);
        }

        /**
         * DocumentFilter instance to handle any user's input, ensuring that the
         * text always match any of the available choices.
         */
        class ControllerDocumentFilter extends DocumentFilter {

            @Override public void insertString(FilterBypass fb,
                                               int          offset,
                                               String       string,
                                               AttributeSet attr) {
                // we never use it, we never invoke Document.insertString
                // note that normal (non programmatically) editing only invokes
                // replace/remove
            }

            @Override public void replace(FilterBypass fb,
                                          int          offset,
                                          int          length,
                                          String       text,
                                          AttributeSet attrs)
                                   throws BadLocationException {
                if (!userUpdate) {
                    // content set from outside, go with it
                    super.replace(fb, offset, length, text, attrs);
                    return;
                }

                String buffer = getText();
                String newContentBegin = buffer.substring(0, offset) + text;
                String newContent = newContentBegin
                        + buffer.substring(offset + length);
                ChoiceMatch match = getBestMatch(newContent);
                String proposal = null;
                if (match.exact) {
                    proposal = match.content.toString();
                } else {
                    // why this part? Imagine having text "se|cond" with the
                    // cursor at "|". Nothing is selected. if the user presses
                    // now 'c', the code above would imply getting "seccond",
                    // which is probably wrong, so we try now to get a proposal
                    // starting at 'sec' ['sec|ond']
                    ChoiceMatch match2 = getExactMatch(newContentBegin);
                    if (match2.exact) {
                        match = match2;
                        proposal = match.content.toString();
                    } else if (match.content == null) {
                        return;
                    } else {
                        proposal = match.content.toString();
                        // on text content, the string comparator cannot
                        // be null
                        if ((proposal.length() < newContentBegin.length())
                                || (0
                                    != popup.getStringComparator().compare(
                                        newContentBegin,
                                        proposal.substring(0,
                                            newContentBegin.length())))) {
                            return;
                        }
                    }
                }

                int caret = 1
                        + Math.min(getCaret().getDot(), getCaret().getMark());

                super.replace(fb, 0, buffer.length(), proposal, attrs);

                int len = proposal.length();
                setCaretPosition(len);
                moveCaretPosition(Math.min(len, caret));
                deactivateCustomDecoration();

                if (instantFiltering) {
                    match.exact = true;
                    updateFilter(proposal, match, true);
                }
            }

            @Override public void remove(FilterBypass fb,
                                         int          offset,
                                         int          length)
                                  throws BadLocationException {
                int caret = getCaret().getDot();
                int mark = getCaret().getMark();
                String buffer = getText();
                String newContent = buffer.substring(0, offset)
                        + buffer.substring(offset + length);
                ChoiceMatch match = getBestMatch(newContent);
                if (match.content == null) {
                    return;
                }

                String proposal = match.content.toString();
                // on text content, this comparator cannot be null
                Comparator<String> comparator = popup.getStringComparator();
                if (!match.exact
                        || (0 != comparator.compare(newContent, proposal))) {
                    if (
                        ChoiceMatch.getMatchingLength(proposal, newContent,
                                comparator)
                            <= ChoiceMatch.getMatchingLength(buffer, newContent,
                                comparator)) {
                        proposal = buffer;
                    }
                }

                // special case if the removal is due to BACK SPACE
                AWTEvent ev = EventQueue.getCurrentEvent();
                if ((ev instanceof KeyEvent)
                        && (((KeyEvent) ev).getKeyCode()
                            == KeyEvent.VK_BACK_SPACE)) {
                    if (caret > mark) {
                        caret = mark;
                    } else if (buffer == proposal) {
                        --caret;
                    } else if (caret == mark) {
                        caret = offset;
                    }
                }

                if ((0 == caret) && (buffer == proposal)) {
                    // remove all text in this case
                    match.content = CustomChoice.MATCH_ALL;
                    proposal = match.content.toString();
                }

                if (buffer != proposal) {
                    super.replace(fb, 0, buffer.length(), proposal, null);
                }

                int len = proposal.length();
                setCaretPosition(len);
                moveCaretPosition(Math.min(len, caret));
                deactivateCustomDecoration();

                if (userUpdate && instantFiltering && (proposal != buffer)) {
                    match.exact = true;
                    updateFilter(proposal, match, true);
                }
            }
        }
    }

}
