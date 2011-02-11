package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.permissionsAndFollowmode;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.rmi.RemoteException;

import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestFollowMode extends STFTest {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException,
        InterruptedException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB);
        setUpWorkbench();
        setUpSaros();
        setUpSessionWithAJavaProjectAndAClass(alice, bob);
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        resetFollowModeSequentially(bob, alice);
    }

    /**
     * TODO: It exists still some bugs in saros by granting write access, so you
     * may get exception by perform this test.
     * 
     * @throws IOException
     * @throws CoreException
     */
    @Test
    public void testBobFollowAlice() throws IOException, CoreException {
        alice.editor.setTextInJavaEditorWithSave(CP1, PROJECT1, PKG1, CLS1);
        bob.sarosSessionV.followThisBuddy(alice.jid);
        bob.editor.waitUntilJavaEditorActive(CLS1);
        assertTrue(bob.sarosSessionV.isInFollowModeNoGUI());
        assertTrue(bob.editor.isJavaEditorActive(CLS1));

        String clsContentOfAlice = alice.editor.getClassContent(PROJECT1, PKG1,
            CLS1);

        bob.editor.waitUntilClassContentsSame(PROJECT1, PKG1, CLS1,
            clsContentOfAlice);
        String clsContentOfBob = bob.editor.getClassContent(PROJECT1, PKG1,
            CLS1);
        assertTrue(clsContentOfBob.equals(clsContentOfAlice));

        alice.fileM.newClass(PROJECT1, PKG1, CLS2);
        bob.editor.waitUntilJavaEditorActive(CLS2);
        assertTrue(bob.editor.isJavaEditorActive(CLS2));

        /*
         * After new release 10.10.28 all read-only users are automatically in
         * follow mode(are the read-only users really in follow mode???) when
         * host give someone exclusive write access. So the following line have
         * to comment out, otherwise you should get WidgetNotFoundException.
         */
        // alice.sessionV.followThisUser(bob.state);
        bob.editor.activateJavaEditor(CLS1);
        alice.editor.waitUntilJavaEditorActive(CLS1);
        assertTrue(alice.sarosSessionV.isInFollowModeNoGUI());
        assertTrue(alice.editor.isJavaEditorActive(CLS1));

        bob.sarosSessionV.followThisBuddy(alice.jid);
        alice.fileM.newClass(PROJECT1, PKG1, CLS3);
        alice.editor.waitUntilJavaEditorActive(CLS3);
        alice.editor.setTextInJavaEditorWithSave(CP3, PROJECT1, PKG1, CLS3);
        alice.editor.setBreakPoint(13, PROJECT1, PKG1, CLS3);
        // alice.debugJavaFile(BotConfiguration.PROJECTNAME,
        // BotConfiguration.PACKAGENAME, BotConfiguration.CLASSNAME3);
        // bob.waitUntilJavaEditorActive(BotConfiguration.CLASSNAME3);
        // assertFalse(bob.isDebugPerspectiveActive());
        // alice.openJavaPerspective();
        // bob.sleep(1000);
        // int lineFromAlice = alice.getJavaCursorLinePosition(
        // BotConfiguration.PROJECTNAME, BotConfiguration.PACKAGENAME,
        // BotConfiguration.CLASSNAME3);
        // int lineFromBob = bob.getJavaCursorLinePosition(
        // BotConfiguration.PROJECTNAME, BotConfiguration.PACKAGENAME,
        // BotConfiguration.CLASSNAME3);
        // assertEquals(lineFromAlice, lineFromBob);
        // alice.waitUntilShellActive("Confirm Perspective Switch");
        // assertTrue(alice.isShellActive("Confirm Perspective Switch"));
    }
}