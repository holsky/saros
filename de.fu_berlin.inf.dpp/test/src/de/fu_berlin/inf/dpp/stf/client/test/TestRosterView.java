package de.fu_berlin.inf.dpp.stf.client.test;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.Musician;

public class TestRosterView {
    private static final Logger log = Logger
        .getLogger(TestBasicSarosElements.class);
    private static Musician alice = InitMusician.newAlice();
    private static Musician bob = InitMusician.newBob();

    @AfterClass
    public static void afterClass() throws RemoteException {
        alice.bot.resetSaros();
        bob.bot.resetSaros();
    }

    @After
    public void cleanup() throws RemoteException {
        alice.bot.resetWorkbench();
        bob.bot.resetWorkbench();
    }

    @Test
    public void testBuddy() throws RemoteException {
        assertTrue(alice.bot.hasContactWith(bob.jid));
    }
}