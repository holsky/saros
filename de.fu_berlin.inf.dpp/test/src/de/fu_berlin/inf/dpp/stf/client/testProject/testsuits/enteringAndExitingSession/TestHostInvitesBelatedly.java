package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.enteringAndExitingSession;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.RemoteException;

import org.eclipse.core.runtime.CoreException;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestHostInvitesBelatedly extends STFTest {

    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Write Access)</li>
     * <li>Bob (Read-Only Access)</li>
     * <li>Carl (Read-Only Access)</li>
     * <li>All read-only users enable follow mode</li>
     * </ol>
     * 
     * @throws AccessException
     * @throws RemoteException
     * @throws InterruptedException
     */

    @BeforeClass
    public static void runBeforeClass() throws RemoteException,
        InterruptedException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB, TypeOfTester.CARL);
        setUpWorkbench();
        setUpSaros();
        alice.fileM.newJavaProjectWithClasses(PROJECT1, PKG1, CLS1, CLS2);
        bob.fileM.newJavaProjectWithClasses(PROJECT1, PKG1, CLS1, CLS2);

        /*
         * alice build session only with carl and is followed by carl.
         */
        buildSessionConcurrently(VIEW_PACKAGE_EXPLORER, PROJECT1,
            TypeOfShareProject.SHARE_PROJECT, TypeOfCreateProject.NEW_PROJECT,
            alice, carl);
        setFollowMode(alice, carl);
    }

    /**
     * Steps:
     * 
     * 1. alice edits the file CLS but don't saves it.
     * 
     * 2. bob edits the file CLS in the project currently used and saves it.
     * 
     * 3. alice edits the file CLS2 but don't saves it.
     * 
     * 4. bob edits the file CLS2 in the project currently used and don't saves
     * it.
     * 
     * 5. alice invites bob.
     * 
     * 6. The question about the changed files at bob is answered with YES.
     * 
     * 
     * Expected Results:
     * 
     * 7. bob has the same project like host.
     * 
     * FIXME: There are some bugs, if bob's editors are not closed, bob has the
     * different project like host.
     * 
     * @throws CoreException
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testFollowModeByOpenClassbyAlice() throws IOException,
        CoreException, InterruptedException {
        alice.openC.openClass(VIEW_PACKAGE_EXPLORER, PROJECT1, PKG1, CLS1);
        alice.bot().editor(CLS1_SUFFIX).setTextWithoutSave(CP1);
        String dirtyContent1ByAlice = alice.bot().editor(CLS1_SUFFIX).getText();

        bob.openC.openClass(VIEW_PACKAGE_EXPLORER, PROJECT1, PKG1, CLS1);
        bob.bot().editor(CLS1_SUFFIX).setTextAndSave(CP1_CHANGE);

        alice.openC.openClass(VIEW_PACKAGE_EXPLORER, PROJECT1, PKG1, CLS2);
        alice.bot().editor(CLS2_SUFFIX).setTextWithoutSave(CP2);
        String dirtyContent2ByAlice = alice.bot().editor(CLS2_SUFFIX).getText();

        bob.openC.openClass(VIEW_PACKAGE_EXPLORER, PROJECT1, PKG1, CLS2);
        bob.editor.setTextWithoutSave(CP2_CHANGE);
        // bob.editor.closeJavaEditorWithSave(CLS1);
        // bob.editor.closeJavaEditorWithSave(CLS2);

        inviteBuddies(PROJECT1, TypeOfCreateProject.EXIST_PROJECT, alice, bob);

        bob.bot().editor(CLS1_SUFFIX)
            .waitUntilContentSame(dirtyContent1ByAlice);
        bob.bot().editor(CLS2_SUFFIX)
            .waitUntilContentSame(dirtyContent2ByAlice);

        String CLSContentOfAlice = alice.bot().editor(CLS1_SUFFIX).getText();
        String CLS2ContentOfAlice = alice.bot().editor(CLS2_SUFFIX).getText();

        String CLSContentOfBob = bob.bot().editor(CLS1_SUFFIX).getText();
        String CLS2ContentOfBob = bob.bot().editor(CLS2_SUFFIX).getText();

        assertEquals(CLSContentOfAlice, CLSContentOfBob);
        assertEquals(CLS2ContentOfAlice, CLS2ContentOfBob);
    }
}
