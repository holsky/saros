package de.fu_berlin.inf.dpp.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.concurrent.watchdog.ConsistencyWatchdogClient;
import de.fu_berlin.inf.dpp.concurrent.watchdog.IsInconsistentObservable;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.project.AbstractSessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.util.Util;
import de.fu_berlin.inf.dpp.util.ValueChangeListener;

@Component(module = "action")
public class ConsistencyAction extends Action {

    private static Logger log = Logger.getLogger(ConsistencyAction.class);

    protected SessionManager sessionManager;

    protected ConsistencyWatchdogClient watchdogClient;

    protected IsInconsistentObservable inconsistentObservable;

    public ConsistencyAction(ConsistencyWatchdogClient watchdogClient,
        SessionManager sessionManager,
        IsInconsistentObservable inconsistentObservable) {

        setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
            .getImageDescriptor(ISharedImages.IMG_OBJS_WARN_TSK));
        setToolTipText("No inconsistencies");
        this.watchdogClient = watchdogClient;
        this.sessionManager = sessionManager;
        this.inconsistentObservable = inconsistentObservable;

        sessionManager.addSessionListener(new AbstractSessionListener() {
            @Override
            public void sessionStarted(ISharedProject session) {
                setSharedProject(session);
            }

            @Override
            public void sessionEnded(ISharedProject session) {
                setSharedProject(null);
            }
        });

        setSharedProject(sessionManager.getSharedProject());
    }

    protected ISharedProject sharedProject;

    private void setSharedProject(ISharedProject newSharedProject) {

        // Unregister from previous project
        if (sharedProject != null) {
            inconsistentObservable.remove(isConsistencyListener);
        }

        sharedProject = newSharedProject;

        // Register to new project
        if (sharedProject != null) {
            inconsistentObservable.addAndNotify(isConsistencyListener);
        } else {
            setEnabled(false);
        }
    }

    ValueChangeListener<Boolean> isConsistencyListener = new ValueChangeListener<Boolean>() {

        public void setValue(Boolean newValue) {

            if (sharedProject.isHost() && newValue == true) {
                log.warn("No inconsistency should ever be reported"
                    + " to the host");
                return;
            }
            log.debug("Inconsistency indicator goes: "
                + (newValue ? "on" : "off"));
            setEnabled(newValue);

            if (newValue) {
                final Set<SPath> paths = new HashSet<SPath>(watchdogClient
                    .getPathsWithWrongChecksums());

                Util.runSafeSWTAsync(log, new Runnable() {
                    public void run() {

                        // set tooltip
                        setToolTipText("Inconsistency Detected in file/s "
                            + Util.toOSString(paths));

                        // TODO Balloon is too aggressive at the moment, when
                        // the host is slow in sending changes (for instance
                        // when refactoring)

                        // show balloon notification
                        /*
                         * BalloonNotification.showNotification(
                         * ((ToolBarManager) toolBar).getControl(),
                         * "Inconsistency Detected!",
                         * "Inconsistencies detected in: " +
                         * pathsOfInconsistencies, 5000);
                         */
                    }
                });

            } else {
                setToolTipText("No inconsistencies");
            }
        }

    };

    @Override
    public void run() {
        Util.runSafeSWTAsync(log, new Runnable() {

            public void run() {
                log.debug("User activated CW recovery.");

                Shell dialogShell = EditorAPI.getShell();
                if (dialogShell == null)
                    dialogShell = new Shell();

                ProgressMonitorDialog dialog = new ProgressMonitorDialog(
                    dialogShell);
                try {
                    dialog.run(true, true, new IRunnableWithProgress() {
                        public void run(IProgressMonitor monitor)
                            throws InterruptedException {

                            SubMonitor progress = SubMonitor.convert(monitor);
                            progress.beginTask("Performing recovery...", 100);
                            watchdogClient.runRecovery(progress.newChild(100));
                            monitor.done();
                        }
                    });
                } catch (InvocationTargetException e) {
                    log.error("Exception not expected here.", e);
                } catch (InterruptedException e) {
                    log.error("Exception not expected here.", e);
                }
            }
        });
    }
}
