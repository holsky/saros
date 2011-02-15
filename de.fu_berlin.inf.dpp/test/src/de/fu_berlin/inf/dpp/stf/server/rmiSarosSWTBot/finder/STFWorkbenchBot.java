package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder;

import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.hamcrest.Matcher;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotEditor;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotPerspective;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotView;

public interface STFWorkbenchBot extends STFBot {
    public STFBotView view(String viewTitle) throws RemoteException;

    /**
     * @return the title list of all the views which are opened currently.
     * @see SWTWorkbenchBot#views()
     */
    public List<String> getTitlesOfOpenedViews() throws RemoteException;

    public boolean isViewOpen(String title) throws RemoteException;

    /**
     * open the given view specified with the viewId.
     * 
     * @param viewId
     *            the id of the view, which you want to open.
     */
    public void openViewById(String viewId) throws RemoteException;

    /**
     * Shortcut for perspective(withPerspectiveLabel(label))
     * 
     * @param label
     *            the "human readable" label for the perspective
     * @return a perspective with the specified <code>label</code>
     * @see #perspective(Matcher)
     * @see WidgetMatcherFactory#withPerspectiveLabel(Matcher)
     */
    public STFBotPerspective perspectiveByLabel(String label)
        throws RemoteException;

    public STFBotPerspective perspectiveById(String id) throws RemoteException;

    /**
     * Shortcut for view(withPartId(id))
     * 
     * @param id
     *            the view id
     * @return the view with the specified id
     * @see WidgetMatcherFactory#withPartId(String)
     */
    public STFBotView viewById(String id) throws RemoteException;

    /**
     * Returns the active workbench view part
     * 
     * @return the active view, if any
     * @throws WidgetNotFoundException
     *             if there is no active view
     */
    public STFBotView activeView() throws RemoteException;

    /**
     * Shortcut for editor(withPartName(title))
     * 
     * @param fileName
     *            the the filename on the editor tab
     * @return the editor with the specified title
     * @see #editor(Matcher)
     */
    public STFBotEditor editor(String fileName) throws RemoteException;

    /**
     * Shortcut for editor(withPartId(id))
     * 
     * @param id
     *            the the id on the editor tab
     * @return the editor with the specified title
     * @see #editor(Matcher)
     */
    public STFBotEditor editorById(String id) throws RemoteException;

    public boolean isEditorOpen(String fileName) throws RemoteException;

    /**
     * Returns the active workbench editor part
     * 
     * @return the active editor, if any
     * @throws WidgetNotFoundException
     *             if there is no active view
     */
    public STFBotEditor activeEditor() throws RemoteException;

    /**
     * @return the active perspective in the active workbench page
     */
    public STFBotPerspective activePerspective() throws RemoteException;

    /**
     * Does a <em>best effort</em> to reset the workbench. This method attempts
     * to:
     * <ul>
     * <li>close all non-workbench windows</li>
     * <li>save and close all open editors</li>
     * <li>reset the <em>active</em> perspective</li>
     * <li>switch to the default perspective for the workbench</li>
     * <li>reset the <em>default</em> perspective for the workbench</li>
     * <ul>
     */
    public void resetWorkbench() throws RemoteException;

    /**
     * Returns the default perspective as defined in the WorkbenchAdvisor of the
     * application.
     */
    public STFBotPerspective defaultPerspective() throws RemoteException;

    public void closeAllEditors() throws RemoteException;

    public void saveAllEditors() throws RemoteException;

    public void resetActivePerspective() throws RemoteException;

    public void closeAllShells() throws RemoteException;

    public void waitUntilEditorOpen(final String title) throws RemoteException;

    public void waitUntilEditorClosed(final String title)
        throws RemoteException;
}
