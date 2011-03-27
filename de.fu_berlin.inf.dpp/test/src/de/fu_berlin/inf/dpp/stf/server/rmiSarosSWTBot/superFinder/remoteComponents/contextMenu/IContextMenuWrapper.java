package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IContextMenuWrapper extends Remote {
    public IShareWithC shareWith() throws RemoteException;

    public void open() throws RemoteException;

    public void openWith(String editorType) throws RemoteException;

    public void delete() throws RemoteException;

    public ITeamC team() throws RemoteException;

    public INewC newC() throws RemoteException;

    public IRefactorC refactor() throws RemoteException;

    public void copy() throws RemoteException;

    public void paste(String target) throws RemoteException;

    public boolean existsWithRegex(String name) throws RemoteException;

    public boolean exists(String name) throws RemoteException;

    /**
     * Delete all the projects existed in the package explorer view.
     * 
     * @throws RemoteException
     */
    // public void deleteAllProjects() throws RemoteException;

    // public void deleteAllItems() throws RemoteException;

    public List<String> getTextOfTreeItems() throws RemoteException;

}