package de.fu_berlin.inf.dpp.ui.widgets.chat.parts;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.widgets.SimpleRoundedComposite;
import de.fu_berlin.inf.dpp.ui.widgets.chat.events.ChatClearedEvent;
import de.fu_berlin.inf.dpp.ui.widgets.chat.events.IChatDisplayListener;
import de.fu_berlin.inf.dpp.ui.widgets.chat.items.ChatLine;
import de.fu_berlin.inf.dpp.ui.widgets.chat.items.ChatLinePartnerChangeSeparator;
import de.fu_berlin.inf.dpp.ui.widgets.chat.items.ChatLineSeparator;

/**
 * This control displays a chat conversation between n users
 * 
 * @author bkahlert
 */
public final class SkypeStyleChatDisplay extends ScrolledComposite {

    private final List<IChatDisplayListener> chatDisplayListeners = new CopyOnWriteArrayList<IChatDisplayListener>();

    private Composite contentComposite;
    private Composite optionsComposite;

    private JID lastUser;

    public SkypeStyleChatDisplay(Composite parent, int style,
        Color backgroundColor) {
        super(parent, style);

        contentComposite = new Composite(this, SWT.NONE);
        contentComposite.setBackgroundMode(SWT.INHERIT_DEFAULT);
        setContent(contentComposite);
        setExpandHorizontal(true);
        setExpandVertical(true);
        getVerticalBar().setIncrement(50);

        // Focus content composite on activation to enable scrolling.
        addListener(SWT.Activate, new Listener() {
            @Override
            public void handleEvent(Event e) {
                contentComposite.setFocus();
            }
        });

        setBackgroundMode(SWT.INHERIT_DEFAULT);
        contentComposite.setBackground(backgroundColor);

        /*
         * NO LAYOUT needed, because ScrolledComposite sets it's own
         * automatically
         */
        GridLayout gridLayout = new GridLayout(1, false);
        contentComposite.setLayout(gridLayout);

        /*
         * Scroll to bottom if resized
         */
        addListener(SWT.Resize, new Listener() {
            @Override
            public void handleEvent(Event event) {
                SkypeStyleChatDisplay.this.refresh();
            }
        });
    }

    /**
     * Adds a line of options to modify the chat to the end of the
     * {@link SkypeStyleChatDisplay} and removes an eventually existing option
     * bar.
     */
    private void createOptionComposite() {
        if (optionsComposite != null)
            optionsComposite.dispose();

        optionsComposite = new Composite(contentComposite, SWT.NONE);

        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        optionsComposite.setLayoutData(gridData);

        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        optionsComposite.setLayout(gridLayout);

        final Button clearButton = new Button(optionsComposite, SWT.PUSH);
        clearButton.setText(Messages.ChatDisplay_clear);
        clearButton.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                clear();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // NOP
            }
        });
        clearButton.setLayoutData(new GridData(SWT.END, SWT.END, true, true));
    }

    /**
     * Displays a new line containing the supplied message and a separator line
     * if necessary.
     * 
     * @param jid
     *            JID of the sender who composed the message
     * @param color
     *            the color to be used to mark the user
     * @param message
     *            composed by the sender
     * @param receivedOn
     *            the date the message was received
     */
    public void addChatLine(JID jid, String displayName, Color color,
        String message, Date receivedOn) {
        /*
         * Sender line
         */
        if (lastUser != null && lastUser.equals(jid)) { // same user
            ChatLineSeparator chatLineSeparator = new ChatLineSeparator(
                contentComposite, displayName, color, receivedOn);
            chatLineSeparator.setLayoutData(new GridData(SWT.FILL,
                SWT.BEGINNING, true, false));
            chatLineSeparator.setData(jid);
        } else { // new / different user
            ChatLinePartnerChangeSeparator chatPartnerChangeLine = new ChatLinePartnerChangeSeparator(
                contentComposite, displayName, color, receivedOn);
            chatPartnerChangeLine.setLayoutData(new GridData(SWT.FILL,
                SWT.BEGINNING, true, false));
            chatPartnerChangeLine.setData(jid);
        }

        /*
         * Message line
         */
        ChatLine chatLine = new ChatLine(contentComposite, message);
        GridData chatLineGridData = new GridData(SWT.FILL, SWT.BEGINNING, true,
            false);
        chatLineGridData.horizontalIndent = SimpleRoundedComposite.MARGIN_WIDTH;
        chatLine.setLayoutData(chatLineGridData);

        /*
         * Reposition the clear option to the end
         */
        createOptionComposite();

        refresh();

        lastUser = jid;
    }

    /**
     * Updates the color of the chat line separators for a specific JID.
     * 
     * @param jid
     *            JID whose color should be updated
     * @param color
     *            the new color
     */
    public void updateColor(JID jid, Color color) {

        for (Control control : contentComposite.getChildren()) {
            if (!jid.equals(control.getData()))
                continue;

            if (control instanceof ChatLineSeparator
                || control instanceof ChatLinePartnerChangeSeparator)
                control.setBackground(color);
        }
    }

    /**
     * Updates the display name of the chat line separators for a specific JID.
     * 
     * @param jid
     *            the JID whose display name should be updated
     * @param displayName
     *            the new display name
     */
    public void updateDisplayName(JID jid, String displayName) {
        for (Control control : contentComposite.getChildren()) {
            if (!jid.equals(control.getData()))
                continue;

            if (control instanceof ChatLinePartnerChangeSeparator) {
                ChatLinePartnerChangeSeparator separator = (ChatLinePartnerChangeSeparator) control;
                separator.setUsername(displayName);
            }
        }

    }

    /**
     * Computes the ideal render widths of non-{@link ChatLine}s and returns the
     * maximum.
     * 
     * @return the maximum ideal render width of all non-{@link ChatLine}s
     */
    private int computeMaxNonChatLineWidth() {
        int maxNonChatLineWidth = 0;
        for (Control chatItem : contentComposite.getChildren()) {
            if (!(chatItem instanceof ChatLine)) {
                int currentNonChatLineWidth = chatItem.computeSize(SWT.DEFAULT,
                    SWT.DEFAULT).x;
                maxNonChatLineWidth = Math.max(currentNonChatLineWidth,
                    maxNonChatLineWidth);
            }
        }
        return maxNonChatLineWidth;
    }

    /**
     * Layouts the current contents, updates the scroll bar minimum size and
     * scrolls to the bottom.
     */
    public void refresh() {
        /*
         * Layout makes the added controls visible
         */
        contentComposite.layout();

        int verticalBarWidth = (this.getVerticalBar() != null) ? this
            .getVerticalBar().getSize().x : 0;

        int widthHint = Math.max(computeMaxNonChatLineWidth()
            + verticalBarWidth,
            SkypeStyleChatDisplay.this.getClientArea().width);

        final Point neededSize = contentComposite.computeSize(widthHint,
            SWT.DEFAULT);

        SkypeStyleChatDisplay.this.setMinSize(neededSize);
        SkypeStyleChatDisplay.this.setOrigin(0, neededSize.y);
    }

    /**
     * Clears the {@link SkypeStyleChatDisplay}
     */
    public void clear() {
        for (Control chatItem : contentComposite.getChildren()) {
            chatItem.dispose();
        }

        refresh();
        lastUser = null;

        notifyChatCleared();
    }

    /**
     * Adds a {@link IChatDisplayListener}
     * 
     * @param chatListener
     */
    public void addChatDisplayListener(IChatDisplayListener chatListener) {
        chatDisplayListeners.add(chatListener);
    }

    /**
     * Removes a {@link IChatDisplayListener}
     * 
     * @param chatListener
     */
    public void removeChatListener(IChatDisplayListener chatListener) {
        chatDisplayListeners.remove(chatListener);
    }

    /**
     * Notify all {@link IChatDisplayListener}s about a cleared chat
     */
    public void notifyChatCleared() {
        for (IChatDisplayListener chatListener : chatDisplayListeners) {
            chatListener.chatCleared(new ChatClearedEvent(this));
        }
    }
}
