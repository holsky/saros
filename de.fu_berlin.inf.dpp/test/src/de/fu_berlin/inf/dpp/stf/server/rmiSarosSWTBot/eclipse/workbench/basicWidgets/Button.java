package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipseComponent;

public interface Button extends EclipseComponent {

    /**********************************************
     * 
     * basic widget: {@link SWTBotButton}.
     * 
     **********************************************/

    /**
     * clicks the button specified with the given mnemonicText.
     * 
     * @param mnemonicText
     *            the mnemonicText on the widget, e.g. Button "Finish" in a
     *            dialog.
     */
    public void clickButton(String mnemonicText) throws RemoteException;

    public void clickButtonAndWait(String mnemonicText) throws RemoteException;

    /**
     * clicks the button specified with the given tooltip.
     * 
     * @param tooltip
     *            the tooltip on the widget,
     * @throws RemoteException
     */
    public void clickButtonWithTooltip(String tooltip) throws RemoteException;

    public void clickButtonInGroup(String groupTitle) throws RemoteException;

    public void clickButtonInGroup(String mnemonicText, String inGroup)
        throws RemoteException;

    public void selectCComboBox(int indexOfCComboBox, int indexOfSelection)
        throws RemoteException;

    public void clickCheckBox(String mnemonicText) throws RemoteException;

    /**
     * 
     * @param mnemonicText
     *            the mnemonicText on the widget, e.g. Button "Finish" in a
     *            dialog.
     * @return<tt>true</tt>, if the given button is enabled.
     * @throws RemoteException
     */
    public boolean isButtonEnabled(String mnemonicText) throws RemoteException;

    public boolean existsButtonInGroup(String mnemonicText, String inGroup)
        throws RemoteException;

    /**
     * 
     * @param tooltip
     *            the tooltip on the widget,
     * @return<tt>true</tt>, if the button specified with the given tooltip is
     *                       enabled.
     * @throws RemoteException
     */
    public boolean isButtonWithTooltipEnabled(String tooltip)
        throws RemoteException;

    /**
     * Waits until the button is enabled.
     * 
     * @param mnemonicText
     *            the mnemonicText on the widget.
     */
    public void waitUntilButtonEnabled(String mnemonicText)
        throws RemoteException;

    /**
     * Waits until the button is enabled.
     * 
     * @param tooltipText
     *            the tooltip on the widget.
     */
    public void waitUnitButtonWithTooltipIsEnabled(String tooltipText)
        throws RemoteException;

}