package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.invitation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.ConfigTester;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

/**
 * Tests for the initial synchronization of the SVN state during the invitation.<br>
 * <br>
 * Bob doesn't have a project, Alice has an SVN project, AND<br>
 * <br>
 * Alice has one resource updated to another revision<br>
 * Alice has one resource switched to another URL<br>
 * Alice has one resource switched and updated<br>
 * Alice has one resource modified<br>
 * TODO Alice has one resource added (not promoted)<br>
 * TODO Alice has one resource added (promoted)<br>
 * TODO Alice has one resource removed<br>
 * <br>
 * use existing:<br>
 * TODO Alice has an SVN project, Bob has the non-SVN project (connect during
 * invitation)<br>
 * TODO Alice has a non-SVN project, Bob has the SVN project (disconnect during
 * invitation)<br>
 * TODO Alice has an SVN project, one resource switched and updated, Bob has the
 * unmodified project<br>
 * TODO Alice has an SVN project, one resource switched and updated, Bob has the
 * managed project with other resources switched/updated/deleted.<br>
 */
public class TestSVNStateInitialization extends STFTest {

    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Write Access)</li>
     * <li>Bob (Read-Only Access)</li>
     * <li>Alice has the project {@link STFTest#SVN_PROJECT_COPY}, which is
     * checked out from SVN:<br>
     * repository: {@link STFTest#SVN_REPOSITORY_URL}<br>
     * path: {@link STFTest#SVN_PROJECT_PATH}
     * </ol>
     * 
     * @throws RemoteException
     */
    @BeforeClass
    public static void initMusicians() throws RemoteException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB);
        setUpWorkbench();
        setUpSaros();
        if (!alice.fileM.existsProjectNoGUI(SVN_PROJECT_COPY)) {
            alice.fileM.newJavaProject(SVN_PROJECT_COPY);
            alice.team.shareProjectWithSVNUsingSpecifiedFolderName(
                VIEW_PACKAGE_EXPLORER, SVN_PROJECT_COPY, SVN_REPOSITORY_URL,
                SVN_PROJECT_PATH);
        }
    }

    @AfterClass
    public static void resetSaros() throws RemoteException {
        resetSaros(bob);
        if (ConfigTester.DEVELOPMODE) {
            if (alice.sarosSessionV.isInSession())
                alice.sarosSessionV.leaveTheSessionByHost();
            // don't delete SVN_PROJECT_COPY
        } else {
            resetSaros(alice);
        }
    }

    /**
     * Preconditions:
     * <ol>
     * <li>Alice copied {@link STFTest#SVN_PROJECT_COPY} to
     * {@link STFTest#SVN_PROJECT}.</li>
     * </ol>
     * Only SVN_PROJECT is used in the tests. Copying from SVN_PROJECT_COPY is
     * faster than checking out the project for every test.
     * 
     * @throws RemoteException
     */
    @Before
    public void setUp() throws RemoteException {
        alice.pEV.selectProject(SVN_PROJECT_COPY);
        alice.editM.copyProject(SVN_PROJECT);
        assertTrue(alice.fileM.existsProjectNoGUI(SVN_PROJECT));
        assertTrue(alice.team.isProjectManagedBySVN(SVN_PROJECT));
        assertTrue(alice.fileM.existsFileNoGUI(SVN_CLS1_FULL_PATH));
    }

    @After
    public void tearDown() throws RemoteException, InterruptedException {
        leaveSessionHostFirst();

        if (alice.fileM.existsProjectNoGUI(SVN_PROJECT))
            alice.editM.deleteProjectNoGUI(SVN_PROJECT);
        bob.editM.deleteAllProjects(VIEW_PACKAGE_EXPLORER);
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice shares project SVN_PROJECT with Bob.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>Bob's copy of SVN_PROJECT is managed by SVN.</li>
     * </ol>
     * 
     * @throws RemoteException
     * 
     */
    @Test
    public void testSimpleCheckout() throws RemoteException {
        buildSessionSequentially(VIEW_PACKAGE_EXPLORER, SVN_PROJECT,
            TypeOfShareProject.SHARE_PROJECT, TypeOfCreateProject.NEW_PROJECT,
            alice, bob);
        alice.sarosSessionV.waitUntilIsInviteeInSession(bob.sarosSessionV);
        assertTrue(bob.team.isProjectManagedBySVN(SVN_PROJECT));

        assertTrue(alice.sarosSessionV.hasWriteAccessNoGUI());
        assertTrue(alice.sarosSessionV.isParticipantNoGUI(bob.jid));
        assertTrue(bob.sarosSessionV.hasWriteAccessNoGUI());
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice updates {@link STFTest#SVN_CLS1} to revision
     * {@link STFTest#SVN_CLS1_REV1}.</li>
     * <li>Alice shares project {@link STFTest#SVN_PROJECT} with Bob.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>Bob's copy of {@link STFTest#SVN_PROJECT} is managed by SVN.</li>
     * <li>Bob's copy of {@link STFTest#SVN_CLS1} has revision
     * {@link STFTest#SVN_CLS1_REV1}.</li>
     * </ol>
     * 
     * @throws RemoteException
     * 
     */
    @Test
    public void testCheckoutWithUpdate() throws RemoteException {
        alice.team.updateClass(VIEW_PACKAGE_EXPLORER, SVN_PROJECT, SVN_PKG,
            SVN_CLS1, SVN_CLS1_REV1);
        assertEquals(SVN_CLS1_REV1, alice.team.getRevision(SVN_CLS1_FULL_PATH));
        buildSessionSequentially(VIEW_PACKAGE_EXPLORER, SVN_PROJECT,
            TypeOfShareProject.SHARE_PROJECT, TypeOfCreateProject.NEW_PROJECT,
            alice, bob);
        alice.sarosSessionV.waitUntilIsInviteeInSession(bob.sarosSessionV);

        assertTrue(bob.team.isProjectManagedBySVN(SVN_PROJECT));
        assertEquals(SVN_CLS1_REV1, bob.team.getRevision(SVN_CLS1_FULL_PATH));
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice switches {@link STFTest#SVN_CLS1} to
     * {@link STFTest#SVN_CLS1_SWITCHED_URL}.</li>
     * <li>Alice shares project {@link STFTest#SVN_PROJECT} with Bob.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>Bob's copy of {@link STFTest#SVN_PROJECT} is managed by SVN.</li>
     * <li>Bob's copy of {@link STFTest#SVN_CLS1} is switched to
     * {@link STFTest#SVN_CLS1_SWITCHED_URL}.</li>
     * </ol>
     * 
     * @throws RemoteException
     * 
     */
    @Test
    public void testCheckoutWithSwitch() throws RemoteException {
        alice.team.switchResource(SVN_CLS1_FULL_PATH, SVN_CLS1_SWITCHED_URL);
        assertEquals(SVN_CLS1_SWITCHED_URL,
            alice.team.getURLOfRemoteResource(SVN_CLS1_FULL_PATH));
        buildSessionSequentially(VIEW_PACKAGE_EXPLORER, SVN_PROJECT,
            TypeOfShareProject.SHARE_PROJECT, TypeOfCreateProject.NEW_PROJECT,
            alice, bob);
        alice.sarosSessionV.waitUntilIsInviteeInSession(bob.sarosSessionV);
        bob.sarosSessionV.waitUntilIsInSession();

        assertTrue(bob.team.isProjectManagedBySVN(SVN_PROJECT));
        assertEquals(SVN_CLS1_SWITCHED_URL,
            bob.team.getURLOfRemoteResource(SVN_CLS1_FULL_PATH));
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice switches {@link STFTest#SVN_CLS1} to
     * {@link STFTest#SVN_CLS1_SWITCHED_URL}@{@link STFTest#SVN_CLS1_REV3}.</li>
     * <li>Alice shares project {@link STFTest#SVN_PROJECT} with Bob.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>Bob's copy of {@link STFTest#SVN_PROJECT} is managed by SVN.</li>
     * <li>Bob's copy of {@link STFTest#SVN_CLS1} is switched to
     * {@link STFTest#SVN_CLS1_SWITCHED_URL}@{@link STFTest#SVN_CLS1_REV3}.</li>
     * </ol>
     * 
     * @throws RemoteException
     * 
     */
    @Test
    public void testCheckoutWithSwitch2() throws RemoteException {
        alice.team.switchResource(SVN_CLS1_FULL_PATH, SVN_CLS1_SWITCHED_URL,
            SVN_CLS1_REV3);
        assertEquals(SVN_CLS1_SWITCHED_URL,
            alice.team.getURLOfRemoteResource(SVN_CLS1_FULL_PATH));
        assertEquals(SVN_CLS1_REV3, alice.team.getRevision(SVN_CLS1_FULL_PATH));
        buildSessionSequentially(VIEW_PACKAGE_EXPLORER, SVN_PROJECT,
            TypeOfShareProject.SHARE_PROJECT, TypeOfCreateProject.NEW_PROJECT,
            alice, bob);
        alice.sarosSessionV.waitUntilIsInviteeInSession(bob.sarosSessionV);
        bob.sarosSessionV.waitUntilIsInSession();

        assertTrue(bob.team.isProjectManagedBySVN(SVN_PROJECT));
        assertEquals(SVN_CLS1_SWITCHED_URL,
            bob.team.getURLOfRemoteResource(SVN_CLS1_FULL_PATH));
        assertEquals(SVN_CLS1_REV3, bob.team.getRevision(SVN_CLS1_FULL_PATH));
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice modifies her working copy by changing the content of the file
     * {@link STFTest#SVN_CLS1} to {@link STFTest#CP1}.</li>
     * <li>Alice shares project {@link STFTest#SVN_PROJECT} with Bob.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>Bob's copy of {@link STFTest#SVN_PROJECT} is managed by SVN.</li>
     * <li>Bob's copy of {@link STFTest#SVN_CLS1} has the same content as
     * Alice's copy.</li>
     * </ol>
     * 
     * @throws RemoteException
     * 
     */
    @Test
    public void testCheckoutWithModification() throws RemoteException {
        assertTrue(alice.fileM.existsClassNoGUI(SVN_PROJECT, SVN_PKG, SVN_CLS1));
        String cls1_content_before = alice.editor.getTextOfJavaEditor(
            SVN_PROJECT, SVN_PKG, SVN_CLS1);
        alice.editor.setTextInJavaEditorWithSave(CP1, SVN_PROJECT, SVN_PKG,
            SVN_CLS1);
        String cls1_content_after = alice.editor.getTextOfJavaEditor(
            SVN_PROJECT, SVN_PKG, SVN_CLS1);
        assertFalse(cls1_content_after.equals(cls1_content_before));

        buildSessionSequentially(VIEW_PACKAGE_EXPLORER, SVN_PROJECT,
            TypeOfShareProject.SHARE_PROJECT, TypeOfCreateProject.NEW_PROJECT,
            alice, bob);
        alice.sarosSessionV.waitUntilIsInviteeInSession(bob.sarosSessionV);
        bob.sarosSessionV.waitUntilIsInSession();

        assertTrue(bob.team.isProjectManagedBySVN(SVN_PROJECT));
        assertEquals(cls1_content_after,
            bob.editor.getTextOfJavaEditor(SVN_PROJECT, SVN_PKG, SVN_CLS1));
    }

}