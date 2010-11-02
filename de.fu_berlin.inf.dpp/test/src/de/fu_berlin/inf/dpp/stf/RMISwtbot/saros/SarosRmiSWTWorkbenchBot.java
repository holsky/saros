package de.fu_berlin.inf.dpp.stf.RMISwtbot.saros;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.RMISwtbot.eclipse.RmiSWTWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.swtbot.saros.finder.SarosSWTBot;
import de.fu_berlin.inf.dpp.stf.swtbot.saros.finder.widgets.SarosSWTBotChatInput;
import de.fu_berlin.inf.dpp.ui.RosterView;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * SarosRmiSWTWorkbenchBot controls Eclipse Saros from the GUI perspective. It
 * exports {@link ISarosState} via RMI. You should not use this within tests.
 * Have a look at {@link Musician} if you want to write tests.
 */
public class SarosRmiSWTWorkbenchBot extends RmiSWTWorkbenchBot implements
    ISarosRmiSWTWorkbenchBot {
    private static final transient Logger log = Logger
        .getLogger(SarosRmiSWTWorkbenchBot.class);

    public final static transient String TEMPDIR = System
        .getProperty("java.io.tmpdir");

    private static transient SarosRmiSWTWorkbenchBot self;

    /** RMI exported Saros state object */
    private ISarosState state;

    /** SarosRmiSWTWorkbenchBot is a singleton */
    public static SarosRmiSWTWorkbenchBot getInstance() {
        if (delegate != null && self != null)
            return self;

        SarosSWTBot swtwbb = new SarosSWTBot();
        self = new SarosRmiSWTWorkbenchBot(swtwbb);
        return self;
    }

    /** RmiSWTWorkbenchBot is a singleton, but inheritance is possible */
    protected SarosRmiSWTWorkbenchBot(SarosSWTBot bot) {
        super(bot);
    }

    /**
     * Export given state object by given name on our local RMI Registry.
     */
    public void exportState(SarosState state, String exportName) {
        try {
            this.state = (ISarosState) UnicastRemoteObject.exportObject(state,
                0);
            addShutdownHook(exportName);
            registry.bind(exportName, this.state);
        } catch (RemoteException e) {
            log.error("Could not export stat object.", e);
        } catch (AlreadyBoundException e) {
            log.error(
                "Could not bind stat object, because it is bound already.", e);
        }
    }

    /*******************************************************************************
     * 
     * popup window page
     * 
     *******************************************************************************/

    public void confirmProblemOccurredWindow(String plainJID)
        throws RemoteException {
        waitUntilShellActive("Problem Occurred");
        delegate.text().getText().matches("*." + plainJID + ".*");
        waitUntilButtonEnabled(SarosConstant.BUTTON_OK);
        delegate.button(SarosConstant.BUTTON_OK).click();
    }

    public void confirmNewContactWindow(String plainJID) throws RemoteException {
        waitUntilShellActive(SarosConstant.SHELL_TITLE_NEW_CONTACT);
        delegate.textWithLabel(SarosConstant.TEXT_LABEL_JABBER_ID).setText(
            plainJID);
        waitUntilButtonEnabled(SarosConstant.BUTTON_FINISH);
        delegate.button(SarosConstant.BUTTON_FINISH).click();
    }

    public void comfirmInvitationWindow(String inviteeJID)
        throws RemoteException {
        waitUntilShellActive("Invitation");
        confirmWindowWithCheckBox("Invitation", SarosConstant.BUTTON_FINISH,
            inviteeJID);
    }

    public void confirmRequestOfSubscriptionReceivedWindow()
        throws RemoteException {
        waitUntilShellActive(SarosConstant.SHELL_TITLE_REQUEST_OF_SUBSCRIPTION_RECEIVED);
        confirmWindow(
            SarosConstant.SHELL_TITLE_REQUEST_OF_SUBSCRIPTION_RECEIVED,
            SarosConstant.BUTTON_OK);
    }

    public void confirmInvitationWindow(String... invitees)
        throws RemoteException {
        windowObject
            .activateShellWithText(SarosConstant.SHELL_TITLE_INVITATION);
        confirmWindowWithCheckBox(SarosConstant.SHELL_TITLE_INVITATION,
            SarosConstant.BUTTON_FINISH, invitees);
    }

    public void confirmSessionInvitationWizard(String inviter,
        String projectname) throws RemoteException {
        waitUntilShellActive(SarosConstant.SHELL_TITLE_SESSION_INVITATION);
        confirmSessionInvitationWindowStep1();
        confirmSessionInvitationWindowStep2UsingNewproject(projectname);
    }

    public void confirmSessionInvitationWizardUsingExistProject(String inviter,
        String projectName) throws RemoteException {
        // waitUntilShellActive(SarosConstant.SHELL_TITLE_SESSION_INVITATION);
        confirmSessionInvitationWindowStep1();
        confirmSessionInvitationWindowStep2UsingExistProject(projectName);
    }

    public void confirmSessionInvitationWizardUsingExistProjectWithCancelLocalChange(
        String inviter, String projectName) throws RemoteException {
        // waitUntilShellActive(SarosConstant.SHELL_TITLE_SESSION_INVITATION);
        confirmSessionInvitationWindowStep1();
        confirmSessionInvitationWindowStep2UsingExistProjectWithCancelLocalChange(projectName);
    }

    public void confirmSessionInvitationWizardUsingExistProjectWithCopy(
        String inviter, String projectName) throws RemoteException {
        // waitUntilShellActive(SarosConstant.SHELL_TITLE_SESSION_INVITATION);
        confirmSessionInvitationWindowStep1();
        confirmSessionInvitationWindowStep2UsingExistProjectWithCopy(projectName);
    }

    public void confirmCreateNewUserAccountWindow(String server,
        String username, String password) throws RemoteException {
        try {
            windowObject.activateShellWithText("Create New User Account");
            delegate.textWithLabel("Jabber Server").setText(server);
            delegate.textWithLabel("Username").setText(username);
            delegate.textWithLabel("Password").setText(password);
            delegate.textWithLabel("Repeat Password").setText(password);
            delegate.button(SarosConstant.BUTTON_FINISH).click();
        } catch (WidgetNotFoundException e) {
            log.error("widget not found while accountBySarosMenu", e);
        }
    }

    /**
     * First step: invitee acknowledge session to given inviter
     * 
     * This method captures two screenshots as side effect.
     */
    public void confirmSessionInvitationWindowStep1() throws RemoteException {
        // if (!isTextWithLabelEqualWithText(SarosConstant.TEXT_LABEL_INVITER,
        // inviter))
        // log.warn("inviter does not match: " + inviter);
        captureScreenshot(TEMPDIR + "/acknowledge_project1.png");
        waitUntilButtonEnabled(SarosConstant.BUTTON_NEXT);
        delegate.button(SarosConstant.BUTTON_NEXT).click();
        captureScreenshot(TEMPDIR + "/acknowledge_project2.png");
        waitUntilButtonEnabled(SarosConstant.BUTTON_FINISH);
    }

    /**
     * Second step: invitee acknowledge a new project
     * 
     * This method captures two screenshots as side effect.
     */
    public void confirmSessionInvitationWindowStep2UsingNewproject(
        String projectName) throws RemoteException {
        delegate.radio(SarosConstant.RADIO_LABEL_CREATE_NEW_PROJECT).click();
        captureScreenshot(TEMPDIR + "/acknowledge_project3.png");
        delegate.button(SarosConstant.BUTTON_FINISH).click();
        captureScreenshot(TEMPDIR + "/acknowledge_project4.png");
        waitUntilShellCloses(delegate
            .shell(SarosConstant.SHELL_TITLE_SESSION_INVITATION));
    }

    public void confirmSessionInvitationWindowStep2UsingExistProject(
        String projectName) throws RemoteException {
        delegate.radio("Use existing project").click();
        delegate.button("Browse").click();
        confirmWindowWithTree("Folder Selection", SarosConstant.BUTTON_OK,
            projectName);
        delegate.button(SarosConstant.BUTTON_FINISH).click();

        confirmWindow("Warning: Local changes will be deleted",
            SarosConstant.BUTTON_YES);
        if (isShellActive("Save Resource"))
            confirmWindow("Save Resource", SarosConstant.BUTTON_YES);
        waitUntilShellCloses(delegate
            .shell(SarosConstant.SHELL_TITLE_SESSION_INVITATION));
    }

    public void confirmSessionInvitationWindowStep2UsingExistProjectWithCancelLocalChange(
        String projectName) throws RemoteException {
        delegate.radio("Use existing project").click();
        delegate.button("Browse").click();
        confirmWindowWithTree("Folder Selection", SarosConstant.BUTTON_OK,
            projectName);
        delegate.button(SarosConstant.BUTTON_FINISH).click();
        confirmWindow("Warning: Local changes will be deleted",
            SarosConstant.BUTTON_NO);
    }

    public void confirmSessionInvitationWindowStep2UsingExistProjectWithCopy(
        String projectName) throws RemoteException {
        delegate.radio("Use existing project").click();
        delegate.button("Browse").click();
        confirmWindowWithTree("Folder Selection", SarosConstant.BUTTON_OK,
            projectName);
        delegate.checkBox(
            "Create copy for working distributed. New project name:").click();
        delegate.button(SarosConstant.BUTTON_FINISH).click();
        waitUntilShellCloses(delegate
            .shell(SarosConstant.SHELL_TITLE_SESSION_INVITATION));
    }

    /**
     * Fill up the configuration wizard with title "Saros Configuration".
     */

    public void confirmSarosConfigurationWizard(String xmppServer, String jid,
        String password) throws RemoteException {
        windowObject
            .activateShellWithText(SarosConstant.SAROS_CONFI_SHELL_TITLE);
        delegate.textWithLabel(SarosConstant.TEXT_LABEL_JABBER_SERVER).setText(
            xmppServer);
        delegate.sleep(sleepTime);
        delegate.textWithLabel(SarosConstant.TEXT_LABEL_USER_NAME).setText(jid);
        delegate.sleep(sleepTime);
        delegate.textWithLabel(SarosConstant.TEXT_LABEL_PASSWORD).setText(
            password);
        delegate.textWithLabel("Confirm:").setText(password);
        delegate.button(SarosConstant.BUTTON_FINISH).click();

        // while (delegate.button("Next >").isEnabled()) {
        // delegate.button("Next >").click();
        // log.debug("click Next > Button.");
        // delegate.sleep(sleepTime);
        // }
        //
        // if (delegate.button(SarosConstant.BUTTON_FINISH).isEnabled()) {
        // delegate.button(SarosConstant.BUTTON_FINISH).click();
        // return;
        // } else {
        // System.out.println("can't click finish button");
        // }
        // throw new NotImplementedException(
        // "only set text fields and click Finish is implemented.");
    }

    // public void addNewContact(String name) throws RemoteException {
    // if (!isRosterViewOpen())
    // addSarosSessionView();
    // clickToolbarButtonWithTooltipInViewWithTitle(
    // SarosConstant.VIEW_TITLE_ROSTER,
    // SarosConstant.TOOL_TIP_TEXT_ADD_A_NEW_CONTACT);
    // activateShellWithText(SarosConstant.SHELL_TITLE_NEW_CONTACT);
    // setTextWithLabel(SarosConstant.TEXT_LABEL_JABBER_ID, name);
    // waitUntilButtonEnabled(SarosConstant.BUTTON_FINISH);
    // clickButton(SarosConstant.BUTTON_FINISH);
    // delegate.sleep(sleepTime);
    //
    // // // server respond with failure code 503, service unavailable, add
    // // // contact anyway
    // // try {
    // // delegate.shell("Contact look-up failed").activate();
    // // delegate.button("Yes").click();
    // // } catch (WidgetNotFoundException e) {
    // // // ignore, server responds
    // // }
    // }

    /*******************************************************************************
     * 
     * Roster view page
     * 
     *******************************************************************************/
    public void openRosterView() throws RemoteException {
        viewObject.showViewById(SarosConstant.ID_ROSTER_VIEW);
    }

    public boolean isRosterViewOpen() throws RemoteException {
        return viewObject.isViewOpen(SarosConstant.VIEW_TITLE_ROSTER);
    }

    public void activateRosterView() throws RemoteException {
        viewObject.activateViewWithTitle(SarosConstant.VIEW_TITLE_ROSTER);
    }

    public void closeRosterView() throws RemoteException {
        viewObject.hideViewById(SarosConstant.ID_ROSTER_VIEW);
    }

    public void xmppDisconnect() throws RemoteException {
        if (isConnectedByXMPP()) {
            clickTBDisconnectInRosterView();
            waitUntilDisConnected();
            // sleep(200);
        }
    }

    public SWTBotTreeItem selectBuddy(String contact) throws RemoteException {
        return viewObject.selectTreeWithLabelsInView(
            SarosConstant.VIEW_TITLE_ROSTER, "Buddies", contact);
    }

    public boolean isBuddyExist(String contact) {
        SWTBotTree tree = viewObject
            .getTreeInView(SarosConstant.VIEW_TITLE_ROSTER);
        return treeObject.isTreeItemWithMatchTextExist(tree,
            SarosConstant.BUDDIES, contact + ".*");
    }

    public boolean isConnectedByXmppGuiCheck() throws RemoteException {
        try {
            openRosterView();
            activateRosterView();
            SWTBotToolbarButton toolbarButton = viewObject
                .getToolbarButtonWithTooltipInView(
                    SarosConstant.VIEW_TITLE_ROSTER,
                    SarosConstant.TOOL_TIP_TEXT_DISCONNECT);
            return (toolbarButton != null && toolbarButton.isVisible());
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }

    /**
     * This method returns true if {@link SarosState} and the GUI
     * {@link RosterView} having the connected state.
     */
    public boolean isConnectedByXMPP() throws RemoteException {
        return state.isConnectedByXMPP() && isConnectedByXmppGuiCheck();
    }

    public void clickTBAddANewContactInRosterView() throws RemoteException {
        openRosterView();
        activateRosterView();
        viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_ROSTER,
            SarosConstant.TOOL_TIP_TEXT_ADD_A_NEW_CONTACT);
    }

    /**
     * Roster must be open
     */
    public void clickTBConnectInRosterView() throws RemoteException {
        openRosterView();
        activateRosterView();
        viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_ROSTER,
            SarosConstant.TOOL_TIP_TEXT_CONNECT);
    }

    /**
     * Roster must be open
     */
    public boolean clickTBDisconnectInRosterView() throws RemoteException {
        openRosterView();
        activateRosterView();
        return viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_ROSTER,
            SarosConstant.TOOL_TIP_TEXT_DISCONNECT) != null;
    }

    /*******************************************************************************
     * 
     * Share project session view page
     * 
     *******************************************************************************/

    public void activateSharedSessionView() throws RemoteException {
        viewObject
            .activateViewWithTitle(SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION);
    }

    public void giveDriverRole(String inviteeJID) throws RemoteException {
        openSharedSessionView();
        activateSharedSessionView();
        viewObject.clickContextMenuOfTableInView(
            BotConfiguration.NAME_SESSION_VIEW, inviteeJID,
            SarosConstant.CONTEXT_MENU_GIVE_DRIVER_ROLE);
    }

    public void giveExclusiveDriverRole(String inviteePlainJID)
        throws RemoteException {
        openSharedSessionView();
        activateSharedSessionView();
        viewObject.clickContextMenuOfTableInView(
            BotConfiguration.NAME_SESSION_VIEW, inviteePlainJID,
            SarosConstant.CONTEXT_MENU_GIVE_EXCLUSIVE_DRIVER_ROLE);
        waitUntilShellCloses("Progress Information");
    }

    public boolean isSharedSessionViewOpen() throws RemoteException {
        return viewObject
            .isViewOpen(SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION);
    }

    /**
     * "Shared Project Session" View must be open
     */
    public boolean isInSession() throws RemoteException {
        viewObject.activateViewWithTitle("Shared Project Session");
        return delegate.viewByTitle("Shared Project Session")
            .toolbarButton("Leave the session").isEnabled();
    }

    public boolean isContactInSessionView(String Contact)
        throws RemoteException {
        activateSharedSessionView();
        SWTBotTable table = viewObject
            .getTableInView(SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION);
        for (int i = 0; i < table.rowCount(); i++) {
            if (table.getTableItem(i).getText().matches(".*" + Contact + ".*"))
                return true;
        }
        return false;
    }

    public boolean isFollowing() throws RemoteException {
        return state.isFollowing();
    }

    public void openSharedSessionView() throws RemoteException {
        viewObject.showViewById("de.fu_berlin.inf.dpp.ui.SessionView");
    }

    public void closeSessionView() throws RemoteException {
        viewObject.hideViewById("de.fu_berlin.inf.dpp.ui.SessionView");
    }

    public void clickTBShareYourScreenWithSelectedUserInSPSView()
        throws RemoteException {
        openSharedSessionView();
        activateSharedSessionView();
        viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
            SarosConstant.TOOL_TIP_TEXT_SHARE_SCREEN_WITH_USER);
    }

    public void clickTBStopSessionWithUserInSPSView(String name)
        throws RemoteException {
        // selectTableItemWithLabelInViewWithTitle(
        // SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION, name);
        openSharedSessionView();
        activateSharedSessionView();
        viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
            SarosConstant.TOOL_TIP_TEXT_STOP_SESSION_WITH_USER + " " + name);
    }

    public void clickTBSendAFileToSelectedUserInSPSView(String inviteeJID)
        throws RemoteException {
        openSharedSessionView();
        activateSharedSessionView();
        viewObject.selectTableItemWithLabelInView(
            SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION, inviteeJID);
        viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
            SarosConstant.TOOL_TIP_TEXT_SEND_FILE_TO_SELECTED_USER);
    }

    public void clickTBOpenInvitationInterfaceInSPSView()
        throws RemoteException {
        openSharedSessionView();
        activateSharedSessionView();
        viewObject.clickToolbarPushButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
            SarosConstant.TOOL_TIP_TEXT_OPEN_INVITATION_INTERFACE);
    }

    public void clickTBStartAVoIPSessionInSPSView() throws RemoteException {
        openSharedSessionView();
        activateSharedSessionView();
        viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
            SarosConstant.TOOL_TIP_TEXT_START_VOIP_SESSION);
    }

    public void clickTBNoInconsistenciesInSPSView() throws RemoteException {
        openSharedSessionView();
        activateSharedSessionView();
        viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
            SarosConstant.TOOL_TIP_TEXT_NO_INCONSISTENCIES);
    }

    public void clickTBRemoveAllRriverRolesInSPSView() throws RemoteException {
        openSharedSessionView();
        activateSharedSessionView();
        viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
            SarosConstant.TOOL_TIP_TEXT_REMOVE_ALL_DRIVER_ROLES);
    }

    public void clickTBEnableDisableFollowModeInSPSView()
        throws RemoteException {
        openSharedSessionView();
        activateSharedSessionView();
        viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
            SarosConstant.TOOL_TIP_TEXT_ENABLE_DISABLE_FOLLOW_MODE);
    }

    public void clickTBLeaveTheSessionInSPSView() throws RemoteException {
        openSharedSessionView();
        activateSharedSessionView();
        viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
            SarosConstant.TOOL_TIP_TEXT_LEAVE_THE_SESSION);
    }

    public void clickCMJumpToPositionOfSelectedUserInSPSView(
        String participantJID, String sufix) throws RemoteException {
        openSharedSessionView();
        activateSharedSessionView();
        viewObject.clickContextMenuOfTableInView(
            BotConfiguration.NAME_SESSION_VIEW, participantJID + sufix,
            SarosConstant.CONTEXT_MENU_JUMP_TO_POSITION_SELECTED_USER);
    }

    public void clickCMStopFollowingThisUserInSPSView(ISarosState state, JID jid)
        throws RemoteException {
        openSharedSessionView();
        activateSharedSessionView();
        if (state.isDriver(jid))
            viewObject.clickContextMenuOfTableInView(
                BotConfiguration.NAME_SESSION_VIEW,
                jid.getBase() + " (Driver)",
                SarosConstant.CONTEXT_MENU_STOP_FOLLOWING_THIS_USER);
        else
            viewObject.clickContextMenuOfTableInView(
                BotConfiguration.NAME_SESSION_VIEW, jid.getBase() + "",
                SarosConstant.CONTEXT_MENU_STOP_FOLLOWING_THIS_USER);

    }

    public void clickCMgiveExclusiveDriverRoleInSPSView(String inviteeJID)
        throws RemoteException {
        openSharedSessionView();
        activateSharedSessionView();
        viewObject.clickContextMenuOfTableInView(
            BotConfiguration.NAME_SESSION_VIEW, inviteeJID,
            SarosConstant.CONTEXT_MENU_REMOVE_DRIVER_ROLE);
    }

    public void clickCMRemoveDriverRoleInSPSView(String inviteeJID)
        throws RemoteException {
        openSharedSessionView();
        activateSharedSessionView();
        viewObject.clickContextMenuOfTableInView(
            BotConfiguration.NAME_SESSION_VIEW, inviteeJID,
            SarosConstant.CONTEXT_MENU_REMOVE_DRIVER_ROLE);
    }

    /*******************************************************************************
     * 
     * Saros Package explorer page
     * 
     *******************************************************************************/

    /**
     * This method captures two screenshots as side effect.
     */
    public void clickCMShareProjectInPEView(String projectName)
        throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        String[] nodes = { projectName };
        String[] matchTexts = mainObject.changeToRegex(nodes);

        viewObject.clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Saros",
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT);
        // viewObject.clickContextMenuOfTreeInView(
        // SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER,
        // SarosConstant.CONTEXT_MENU_SHARE_PROJECT, projectName);

    }

    public void clickCMShareprojectWithVCSSupportInPEView(String projectName)
        throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        String[] nodes = { projectName };
        String[] matchTexts = mainObject.changeToRegex(nodes);
        viewObject.clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Saros",
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT_WITH_VCS);
    }

    public void clickCMShareProjectParticallyInPEView(String projectName)
        throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        String[] nodes = { projectName };
        String[] matchTexts = mainObject.changeToRegex(nodes);
        viewObject.clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Saros",
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT_PARTIALLY);
        // viewObject.clickContextMenuOfTableInView(
        // SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER,
        // SarosConstant.CONTEXT_MENU_SHARE_PROJECT_PARTIALLY, projectName);
    }

    public void clickCMAddToSessionInPEView(String projectName)
        throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        String[] nodes = { projectName };
        String[] matchTexts = mainObject.changeToRegex(nodes);
        viewObject.clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Saros",
            SarosConstant.CONTEXT_MENU_ADD_TO_SESSION);

        // viewObject.clickContextMenuOfTableInView(
        // SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER,
        // SarosConstant.CONTEXT_MENU_ADD_TO_SESSION, projectName);
    }

    /*******************************************************************************
     * 
     * Remote screen view page
     * 
     *******************************************************************************/
    public void activateRemoteScreenView() throws RemoteException {
        viewObject
            .activateViewWithTitle(SarosConstant.VIEW_TITLE_REMOTE_SCREEN);
    }

    public boolean isRemoteScreenViewOpen() throws RemoteException {
        return viewObject.isViewOpen(SarosConstant.VIEW_TITLE_REMOTE_SCREEN);
    }

    public void openRemoteScreenView() throws RemoteException {
        viewObject
            .showViewById("de.fu_berlin.inf.dpp.videosharing.player.VideoPlayerView");
    }

    public void closeRemoteScreenView() throws RemoteException {
        viewObject
            .hideViewById("de.fu_berlin.inf.dpp.videosharing.player.VideoPlayerView");
    }

    public void clickTBChangeModeOfImageSourceInRSView() throws RemoteException {
        openRemoteScreenView();
        activateRemoteScreenView();
        viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_REMOTE_SCREEN,
            SarosConstant.TOOL_TIP_TEXT_CHANGE_MODE_IMAGE_SOURCE);
    }

    public void clickTBStopRunningSessionInRSView() throws RemoteException {
        openRemoteScreenView();
        activateRemoteScreenView();
        viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_REMOTE_SCREEN,
            SarosConstant.TOOL_TIP_TEXT_STOP_RUNNING_SESSION);
    }

    public void clickTBResumeInRSView() throws RemoteException {
        openRemoteScreenView();
        activateRemoteScreenView();
        viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_REMOTE_SCREEN,
            SarosConstant.TOOL_TIP_TEXT_RESUME);
    }

    public void clickTBPauseInRSView() throws RemoteException {
        openRemoteScreenView();
        activateRemoteScreenView();
        viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_REMOTE_SCREEN,
            SarosConstant.TOOL_TIP_TEXT_PAUSE);
    }

    /*******************************************************************************
     * 
     * Chat view page
     * 
     *******************************************************************************/
    public void activateChatView() throws RemoteException {
        viewObject.activateViewWithTitle(SarosConstant.VIEW_TITLE_CHAT_VIEW);
    }

    public void openChatView() throws RemoteException {
        viewObject.showViewById(SarosConstant.ID_CHAT_VIEW);
    }

    public void closeChatView() throws RemoteException {
        viewObject.hideViewById(SarosConstant.ID_CHAT_VIEW);
    }

    public boolean isChatViewOpen() throws RemoteException {
        return viewObject.isViewOpen(SarosConstant.VIEW_TITLE_CHAT_VIEW);
    }

    public void sendChatMessage(String message) throws RemoteException {
        if (!isChatViewOpen())
            openChatView();
        activateChatView();
        SarosSWTBotChatInput chatInput = delegate.chatInput();
        chatInput.setText(message);
        delegate.text();
        log.debug("inerted message in chat view: " + chatInput.getText());
        chatInput.pressEnterKey();
    }

    public String getUserNameOnChatLinePartnerChangeSeparator()
        throws RemoteException {
        if (!isChatViewOpen())
            openChatView();
        activateChatView();
        log.debug("user name of the first chat line partner change separator: "
            + delegate.chatLinePartnerChangeSeparator().getPlainID());
        return delegate.chatLinePartnerChangeSeparator().getPlainID();
    }

    public String getUserNameOnChatLinePartnerChangeSeparator(int index)
        throws RemoteException {
        if (!isChatViewOpen())
            openChatView();
        activateChatView();
        log.debug("user name of the chat line partner change separator with the index"
            + index
            + ": "
            + delegate.chatLinePartnerChangeSeparator(index).getPlainID());
        return delegate.chatLinePartnerChangeSeparator(index).getPlainID();
    }

    public String getUserNameOnChatLinePartnerChangeSeparator(String plainID)
        throws RemoteException {
        if (!isChatViewOpen())
            openChatView();
        activateChatView();
        log.debug("user name of the chat line partner change separator with the plainID "
            + plainID
            + ": "
            + delegate.chatLinePartnerChangeSeparator(plainID).getPlainID());
        return delegate.chatLinePartnerChangeSeparator(plainID).getPlainID();
    }

    public String getTextOfChatLine() throws RemoteException {
        if (!isChatViewOpen())
            openChatView();
        activateChatView();
        log.debug("text of the first chat line: "
            + delegate.chatLine().getText());
        return delegate.chatLine().getText();
    }

    public String getTextOfChatLine(int index) throws RemoteException {
        if (!isChatViewOpen())
            openChatView();
        activateChatView();
        log.debug("text of the chat line with the index " + index + ": "
            + delegate.chatLine(index).getText());
        return delegate.chatLine(index).getText();
    }

    public String getTextOfLastChatLine() throws RemoteException {
        if (!isChatViewOpen())
            openChatView();
        activateChatView();
        log.debug("text of the last chat line: "
            + delegate.lastChatLine().getText());
        return delegate.lastChatLine().getText();
    }

    public String getTextOfChatLine(String regex) throws RemoteException {
        if (!isChatViewOpen())
            openChatView();
        activateChatView();
        log.debug("text of the chat line with the specifed regex: "
            + delegate.chatLine(regex).getText());
        return delegate.chatLine(regex).getText();
    }

    public boolean compareChatMessage(String jid, String message)
        throws RemoteException {
        if (!isChatViewOpen())
            openChatView();
        activateChatView();
        log.debug("chatLine: " + delegate.lastChatLine());
        // log.debug("text of the lastChatLine: "
        // + delegate.lastChatLine().widget.getText());
        log.debug("text of the lastChatLine: "
            + delegate.lastChatLine().getText());
        String text = delegate.lastChatLine().getText();
        return text.equals(message);

        // return Comperator.compareStrings(jid, message, text);
    }

    // public boolean isContactOnline(String contact) {
    // throw new NotImplementedException(
    // "Can not be implemented, because no information is visible by swtbot. Enhance information with a tooltip or toher stuff.");
    // }

    // /**
    // * Returns true if the given jid was found in Shared Project Session View.
    // */
    // public boolean isInSharedProject(String jid) {
    // SWTBotView sessionView = delegate.viewByTitle("Shared Project Session");
    // SWTBot bot = sessionView.bot();
    //
    // try {
    // SWTBotTable table = bot.table();
    // SWTBotTableItem item = table.getTableItem(jid);
    // return item != null;
    // } catch (WidgetNotFoundException e) {
    // return false;
    // }
    // }

    /*******************************************************************************
     * 
     * frequently used components
     * 
     *******************************************************************************/

    public void leaveSessionByPeer() throws RemoteException {
        // Need to check for isDriver before leaving.
        clickTBLeaveTheSessionInSPSView();
        confirmWindow(SarosConstant.SHELL_TITLE_CONFIRM_LEAVING_SESSION,
            SarosConstant.BUTTON_YES);
        waitUntilSessionCloses();
    }

    public void leaveSessionByHost() throws RemoteException {
        clickTBLeaveTheSessionInSPSView();
        Util.runSafeAsync(log, new Runnable() {
            public void run() {
                try {
                    confirmWindow("Confirm Closing Session",
                        SarosConstant.BUTTON_YES);
                } catch (RemoteException e) {
                    // no popup
                }
            }
        });
        if (isShellActive("Confirm Closing Session"))
            confirmWindow("Confirm Closing Session", SarosConstant.BUTTON_YES);
        waitUntilSessionCloses();
    }

    public void followUser(ISarosState stateOfFollowedUser,
        JID JIDOfFollowedUser) throws RemoteException {
        openSharedSessionView();
        activateSharedSessionView();
        if (stateOfFollowedUser.isDriver(JIDOfFollowedUser))
            viewObject.clickContextMenuOfTableInView(
                BotConfiguration.NAME_SESSION_VIEW, JIDOfFollowedUser.getBase()
                    + " (Driver)", SarosConstant.CONTEXT_MENU_FOLLOW_THIS_USER);

        else
            viewObject.clickContextMenuOfTableInView(
                BotConfiguration.NAME_SESSION_VIEW, JIDOfFollowedUser.getBase()
                    + "", SarosConstant.CONTEXT_MENU_FOLLOW_THIS_USER);
    }

    public void stopFollowUser(ISarosState stateOfFollowedUser,
        JID JIDOfFollowedUser) throws RemoteException {
        openSharedSessionView();
        activateSharedSessionView();
        if (stateOfFollowedUser.isDriver(JIDOfFollowedUser))
            viewObject.clickContextMenuOfTableInView(
                BotConfiguration.NAME_SESSION_VIEW, JIDOfFollowedUser.getBase()
                    + " (Driver)",
                SarosConstant.CONTEXT_MENU_STOP_FOLLOWING_THIS_USER);

        else
            viewObject.clickContextMenuOfTableInView(
                BotConfiguration.NAME_SESSION_VIEW, JIDOfFollowedUser.getBase()
                    + "", SarosConstant.CONTEXT_MENU_STOP_FOLLOWING_THIS_USER);
    }

    public void clickShareProjectWith(String projectName,
        String shareProjectWith) throws RemoteException {
        if (shareProjectWith.equals(SarosConstant.CONTEXT_MENU_SHARE_PROJECT)) {
            clickCMShareProjectInPEView(projectName);
        } else if (shareProjectWith
            .equals(SarosConstant.CONTEXT_MENU_SHARE_PROJECT_WITH_VCS))
            clickCMShareprojectWithVCSSupportInPEView(projectName);
        else if (shareProjectWith
            .equals(SarosConstant.CONTEXT_MENU_SHARE_PROJECT_PARTIALLY))
            clickCMShareProjectParticallyInPEView(projectName);
        else
            clickCMAddToSessionInPEView(projectName);
    }

    public void confirmSessionUsingNewOrExistProject(
        ISarosRmiSWTWorkbenchBot inviteeBot, JID inviterJID,
        String projectName, int typeOfSharingProject) throws RemoteException {
        inviteeBot
            .waitUntilShellActive(SarosConstant.SHELL_TITLE_SESSION_INVITATION);
        switch (typeOfSharingProject) {
        case SarosConstant.CREATE_NEW_PROJECT:
            inviteeBot.confirmSessionInvitationWizard(inviterJID.getBase(),
                projectName);
            break;
        case SarosConstant.USE_EXISTING_PROJECT:
            inviteeBot.confirmSessionInvitationWizardUsingExistProject(
                inviterJID.getBase(), projectName);
            break;
        case SarosConstant.USE_EXISTING_PROJECT_WITH_CANCEL_LOCAL_CHANGE:
            inviteeBot
                .confirmSessionInvitationWizardUsingExistProjectWithCancelLocalChange(
                    inviterJID.getBase(), projectName);
            break;
        case SarosConstant.USE_EXISTING_PROJECT_WITH_COPY:
            inviteeBot.confirmSessionInvitationWizardUsingExistProjectWithCopy(
                inviterJID.getBase(), projectName);
            break;
        default:
            break;
        }
    }

    public void shareScreenWithUser(ISarosState respondentState,
        JID respondentJID) throws RemoteException {
        openRemoteScreenView();
        if (respondentState.isDriver(respondentJID)) {
            viewObject.selectTableItemWithLabelInView(
                SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
                respondentJID.getBase() + " (Driver)");

        } else {
            viewObject.selectTableItemWithLabelInView(
                SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
                respondentJID.getBase());
        }
        clickTBShareYourScreenWithSelectedUserInSPSView();
    }

    public void xmppConnect(JID jid, String password) throws RemoteException {
        log.trace("connectedByXMPP");
        boolean connectedByXMPP = isConnectedByXMPP();
        if (!connectedByXMPP) {
            log.trace("clickTBConnectInRosterView");
            clickTBConnectInRosterView();
            sleep(100);// wait a bit to check if shell pops up
            log.trace("isShellActive");
            boolean shellActive = isShellActive(SarosConstant.SAROS_CONFI_SHELL_TITLE);
            if (shellActive) {
                log.trace("confirmSarosConfigurationWindow");
                confirmSarosConfigurationWizard(jid.getDomain(), jid.getName(),
                    password);
            }
            waitUntilConnected();
        }
    }

    public void creatNewAccount(JID jid, String password)
        throws RemoteException {
        getEclipseShell().activate().setFocus();
        menuObject.clickMenuWithTexts("Saros", "Create Account");
        confirmCreateNewUserAccountWindow(jid.getDomain(), jid.getName(),
            password);
    }

    public void openSarosViews() throws RemoteException {
        openRosterView();
        openSharedSessionView();
        openChatView();
        openRemoteScreenView();
    }

    public boolean hasContactWith(JID jid) throws RemoteException {
        return state.hasContactWith(jid) && isBuddyExist(jid.getBase());
    }

    public void renameContact(String contact, String newName)
        throws RemoteException {
        SWTBotTree tree = delegate.viewByTitle(SarosConstant.VIEW_TITLE_ROSTER)
            .bot().tree();
        SWTBotTreeItem item = treeObject.getTreeItemWithMatchText(tree,
            SarosConstant.BUDDIES + ".*", contact + ".*");
        item.contextMenu("Rename...").click();
        waitUntilShellActive("Set new nickname");
        delegate.text(contact).setText(newName);
        delegate.button(SarosConstant.BUTTON_OK).click();
    }

    /**
     * Remove given contact from Roster, if contact was added before.
     */
    public void deleteContact(JID jid, ISarosRmiSWTWorkbenchBot participant)
        throws RemoteException {
        if (!hasContactWith(jid))
            return;
        try {
            viewObject.clickContextMenuOfTreeInView(
                SarosConstant.VIEW_TITLE_ROSTER,
                SarosConstant.CONTEXT_MENU_DELETE, SarosConstant.BUDDIES,
                jid.getBase());
            waitUntilShellActive(SarosConstant.SHELL_TITLE_CONFIRM_DELETE);
            confirmWindow(SarosConstant.SHELL_TITLE_CONFIRM_DELETE,
                SarosConstant.BUTTON_YES);
            participant
                .waitUntilShellActive(SarosConstant.SHELL_TITLE_REMOVAL_OF_SUBSCRIPTION);
            participant.confirmWindow(
                SarosConstant.SHELL_TITLE_REMOVAL_OF_SUBSCRIPTION,
                SarosConstant.BUTTON_OK);

        } catch (WidgetNotFoundException e) {
            log.info("Contact not found: " + jid.getBase(), e);
        }
    }

    // /**
    // * Create a {@link ISarosSession} using context menu off the given project
    // * on package explorer view.
    // */
    // public void clickProjectContextMenu(String projectName,
    // String nameOfContextMenu) throws RemoteException {
    // SWTBotView view = delegate.viewByTitle("Package Explorer");
    // SWTBotTree tree = view.bot().tree().select(projectName);
    // SWTBotTreeItem item = tree.getTreeItem(projectName).select();
    // SWTBotMenu menu = item.contextMenu(nameOfContextMenu);
    // menu.click();
    // }

    public void shareProject(String projectName, List<String> inviteeJIDS)
        throws RemoteException {
        clickCMShareProjectInPEView(projectName);
        waitUntilShellActive(SarosConstant.SHELL_TITLE_INVITATION);
        captureScreenshot(TEMPDIR + "/shareProjectStepParallel1.png");
        tableObject.selectCheckBoxsInTable(inviteeJIDS);
        captureScreenshot(TEMPDIR + "/shareProjectStepParallel2.png");
        waitUntilButtonEnabled(SarosConstant.BUTTON_FINISH);
        delegate.button(SarosConstant.BUTTON_FINISH).click();
    }

    // protected SWTBotToolbarButton getXmppDisconnectButton() {
    // for (SWTBotToolbarButton toolbarButton : delegate.viewByTitle("Roster")
    // .getToolbarButtons()) {
    // if (toolbarButton.getToolTipText().matches("Disconnect.*")) {
    // return toolbarButton;
    // }
    //
    // }
    //
    // return null;
    // }

    public void addContact(JID jid, ISarosRmiSWTWorkbenchBot participant)
        throws RemoteException {
        if (!hasContactWith(jid)) {
            openRosterView();
            activateRosterView();
            clickTBAddANewContactInRosterView();
            waitUntilShellActive(SarosConstant.SHELL_TITLE_NEW_CONTACT);
            // activateShellWithText(SarosConstant.SHELL_TITLE_NEW_CONTACT);
            delegate.textWithLabel(SarosConstant.TEXT_LABEL_JABBER_ID).setText(
                jid.getBase());
            waitUntilButtonEnabled(SarosConstant.BUTTON_FINISH);
            delegate.button(SarosConstant.BUTTON_FINISH).click();
            participant.confirmRequestOfSubscriptionReceivedWindow();
            confirmRequestOfSubscriptionReceivedWindow();
        }

    }

    /*******************************************************************************
     * 
     * waitUntil
     * 
     *******************************************************************************/
    public void waitUntilFollowed(String plainJID) throws RemoteException {
        wUntilObject
            .waitUntil(SarosConditions.isFollowingUser(state, plainJID));
    }

    public void waitUntilGetChatMessage(String jid, String message)
        throws RemoteException {
        wUntilObject.waitUntil(SarosConditions.isChatMessageExist(this, jid,
            message));
    }

    public void waitUntilConnected() {
        wUntilObject.waitUntil(SarosConditions.isConnect(delegate));
    }

    public void waitUntilDisConnected() {
        wUntilObject.waitUntil(SarosConditions.isDisConnected(delegate));
    }

    public void waitUntilSessionCloses() throws RemoteException {
        log.info("wait begin " + System.currentTimeMillis());
        wUntilObject.waitUntil(SarosConditions.isSessionClosed(state));
        log.info("wait end " + System.currentTimeMillis());
    }

    /**
     * For some tests a host need to invite many peers concurrently and some
     * operations should not be performed if the invitation processes aren't
     * finished yet. In this case, you can use this method to guarantee, that
     * host wait so long until all the invitation Processes are finished.
     */
    public void waitUntilNoInvitationProgress() throws RemoteException {
        openProgressView();
        activateProgressView();
        delegate.waitUntil(SarosConditions.existNoInvitationProgress(delegate),
            100000);
    }

    public void waitUntilSessionClosedBy(ISarosState state)
        throws RemoteException {
        wUntilObject.waitUntil(SarosConditions.isSessionClosed(state));
        delegate.sleep(sleepTime);
    }

    public void waitUntilAllPeersLeaveSession(List<JID> jids)
        throws RemoteException {
        wUntilObject.waitUntil(SarosConditions.existNoParticipant(state, jids));
    }

    public void waitUntilSessionOpen() throws RemoteException {
        wUntilObject.waitUntil(SarosConditions.isInSession(state));
    }

    public void waitUntilSessionOpenBy(ISarosState state)
        throws RemoteException {
        wUntilObject.waitUntil(SarosConditions.isInSession(state));
    }

    /******************************/
    public void setTextInJavaEditorWithSave(String contentPath,
        String projectName, String packageName, String className)
        throws RemoteException {
        String contents = state.getContents(contentPath);
        // activateEclipseShell();

        openClass(projectName, packageName, className);
        activateJavaEditor(className);
        SWTBotEditor editor;
        editor = delegate.editorByTitle(className + ".java");
        SWTBotEclipseEditor e = editor.toTextEditor();

        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                final IWorkbench wb = PlatformUI.getWorkbench();
                final IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
                log.debug("shell name: " + win.getShell().getText());
                win.getShell().forceActive();
                win.getShell().forceFocus();
            }
        });
        e.setFocus();
        e.setText(contents);
        e.typeText("hallo wie geht es dir ");
        e.pressShortcut(Keystrokes.LF);
        e.typeText("mir geht es gut!");
        delegate.sleep(2000);

        delegate.sleep(2000);

        e.save();
        // editorObject.setTextinEditorWithSave(contents, className + ".java");
    }

    public void setTextInEditorWithSave(String contentPath, String... filePath)
        throws RemoteException {
        String contents = state.getContents(contentPath);
        String fileName = filePath[filePath.length - 1];
        openFile(filePath);
        activateEditor(fileName);
        editorObject.setTextinEditorWithSave(contents, fileName);
    }

    public void setTextInJavaEditorWithoutSave(String contentPath,
        String projectName, String packageName, String className)
        throws RemoteException {
        String contents = state.getContents(contentPath);
        openClass(projectName, packageName, className);
        activateJavaEditor(className);
        editorObject.setTextinEditorWithoutSave(contents, className + ".java");
    }

    public void typeTextInJavaEditor(String contentPath, String projectName,
        String packageName, String className) throws RemoteException {
        String contents = state.getContents(contentPath);
        activateEclipseShell();
        openClass(projectName, packageName, className);
        activateJavaEditor(className);
        editorObject.typeTextInEditor(contents, className + ".java");
    }

    /*******************************************************************************
     * 
     * saros main page
     * 
     *******************************************************************************/

    public void resetSaros() throws RemoteException {
        xmppDisconnect();
        deleteAllProjects();
    }

    /**
     * remove the progress. ie. Click the gray clubs delete icon.
     */
    public void removeProgress() throws RemoteException {
        openProgressView();
        activateProgressView();
        SWTBotView view = delegate.viewByTitle("Progress");
        view.setFocus();
        SWTBot bot = view.bot();
        SWTBotToolbarButton b = bot.toolbarButton();
        b.click();
    }

    public void invitateUser(String inviteeJID) throws RemoteException {
        clickTBOpenInvitationInterfaceInSPSView();
        comfirmInvitationWindow(inviteeJID);
    }

    /**
     * end the invitation process. ie. Click the red stop icon in Progress view.
     */
    public void cancelInvitation() throws RemoteException {
        openProgressView();
        activateProgressView();
        SWTBotView view = delegate.viewByTitle("Progress");
        view.setFocus();
        SWTBot bot = view.bot();
        SWTBotToolbarButton b = bot.toolbarButton();
        b.click();
    }

    public void cancelInvitation(int index) throws RemoteException {
        openProgressView();
        activateProgressView();
        SWTBotView view = delegate.viewByTitle("Progress");
        view.toolbarButton("Remove All Finished Operations").click();
        view.setFocus();
        SWTBot bot = view.bot();
        SWTBotToolbarButton b = bot.toolbarButton(index);
        b.click();
    }

    public void cancelInivtationInSessionInvitationWindow()
        throws RemoteException {
        SWTBotShell shell = delegate.activeShell();
        shell.bot().toolbarButton().click();
    }

    public boolean isProgressViewOpen() throws RemoteException {
        return viewObject.isViewOpen("Progress");
    }

    public void confirmInvitationCancelledWindow() throws RemoteException {
        SWTBotShell shell = delegate.shell("Invitation Cancelled");
        shell.activate().setFocus();
        SWTBotButton button = shell.bot().button();
        button.click();
    }

    public boolean isToolbarNoInconsistenciesEnabled() throws RemoteException {
        openSharedSessionView();
        activateSharedSessionView();
        return viewObject.isToolbarInViewEnabled(
            SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
            SarosConstant.TOOL_TIP_TEXT_NO_INCONSISTENCIES);
    }
}