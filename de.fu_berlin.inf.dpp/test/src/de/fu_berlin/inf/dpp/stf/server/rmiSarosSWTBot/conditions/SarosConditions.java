package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions;

import java.util.List;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotEditor;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTable;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.ChatViewImp;

/**
 * This is a factory class to create some conditions provided with STF.
 */
public class SarosConditions extends Conditions {

    public static ICondition isConnect(List<SWTBotToolbarButton> buttons,
        String tooltipText) {
        return new IsConnect(buttons, tooltipText);
    }

    public static ICondition isDisConnected(List<SWTBotToolbarButton> buttons,
        String tooltipText) {
        return new IsDisConnected(buttons, tooltipText);
    }

    public static ICondition ShellActive(SWTBotShell shell) {
        return new IsShellActive(shell);
    }

    public static ICondition isShellOpen(SWTWorkbenchBot bot, String title) {
        return new IsShellOpen(bot, title);
    }

    public static ICondition existTableItem(STFBotTable table,
        String tableItemName) {
        return new ExistsTableItem(table, tableItemName);
    }

    public static ICondition ExistContextMenuOfTableItem(STFBotTable table,
        String itemName, String contextName) {
        return new ExistsContextMenuOfTableItem(table, itemName, contextName);
    }

    /**********************************************
     * 
     * conditions for editor
     * 
     **********************************************/
    // public static ICondition isEditorOpen(STFBotEditor editor, String name) {
    // return new IsEditorOpen(editor, name);
    // }
    //
    // public static ICondition isEditorActive(STFBotEditor editor, String name)
    // {
    // return new IsEditorActive(editor, name);
    // }
    //
    // public static ICondition isEditorClosed(STFBotEditor editor, String name)
    // {
    // return new IsEditorClosed(editor, name);
    // }

    public static ICondition isShellClosed(SWTBotShell shell) {
        return new IsShellClosed(shell);
    }

    public static ICondition isViewActive(SWTWorkbenchBot bot, String name) {
        return new IsViewActive(bot, name);
    }

    public static ICondition isFileContentsSame(STFBotEditor state,
        String otherClassContent, String... fileNodes) {
        return new IsFileContentsSame(state, otherClassContent, fileNodes);
    }

    public static ICondition isNotInSVN(String projectName) {
        return new IsNotInSVN(projectName);
    }

    public static ICondition isInSVN(String projectName) {
        return new IsInSVN(projectName);
    }

    public static ICondition isResourceExist(String resourcePath) {
        return new ExistsResource(resourcePath);
    }

    public static ICondition isResourceNotExist(String resourcePath) {
        return new ExistsNoResource(resourcePath);
    }

    public static ICondition isChatMessageExist(ChatViewImp chatV, String jid,
        String message) {
        return new ExistsChatMessage(chatV, jid, message);
    }

    public static ICondition isRevisionSame(String fullPath, String revisionID) {
        return new IsRevisionSame(fullPath, revisionID);
    }

    public static ICondition isUrlSame(String fullPath, String url) {
        return new IsUrlSame(fullPath, url);
    }
}
