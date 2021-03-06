package de.fu_berlin.inf.dpp;

import java.util.Arrays;

import org.picocontainer.MutablePicoContainer;

import de.fu_berlin.inf.dpp.account.XMPPAccountLocator;
import de.fu_berlin.inf.dpp.ui.core_services.AccountCoreService;
import de.fu_berlin.inf.dpp.ui.browser_functions.ContactListCoreService;
import de.fu_berlin.inf.dpp.ui.manager.ContactListManager;
import de.fu_berlin.inf.dpp.ui.view_parts.AddAccountWizard;
import de.fu_berlin.inf.dpp.ui.view_parts.AddContactWizard;
import de.fu_berlin.inf.dpp.ui.view_parts.SarosMainPage;

/**
 * This is the HTML UI core factory for Saros. All components that are created
 * by this factory <b>must</b> be working on any platform the application is
 * running on.
 */
public class SarosHTMLUIContextFactory extends AbstractSarosContextFactory {

    @Override
    public void createComponents(MutablePicoContainer container) {

        Component[] components = new Component[] {
            Component.create(XMPPAccountLocator.class),
            Component.create(SarosMainPage.class),
            Component.create(AddAccountWizard.class),
            Component.create(AddContactWizard.class),
            Component.create(ContactListCoreService.class),
            Component.create(AccountCoreService.class),
            Component.create(ContactListManager.class) };

        for (Component component : Arrays.asList(components)) {
            container.addComponent(component.getBindKey(),
                component.getImplementation());
        }
    }
}
