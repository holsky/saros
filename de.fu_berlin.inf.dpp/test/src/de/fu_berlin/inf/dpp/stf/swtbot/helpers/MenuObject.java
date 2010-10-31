package de.fu_berlin.inf.dpp.stf.swtbot.helpers;

import org.apache.log4j.Logger;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;

import de.fu_berlin.inf.dpp.stf.RMISwtbot.eclipse.RmiSWTWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.swtbot.saros.finder.SarosSWTBot;

public class MenuObject {
    private static final transient Logger log = Logger
        .getLogger(MenuObject.class);
    private RmiSWTWorkbenchBot rmiBot;
    private WaitUntilObject wUntil;
    private static SarosSWTBot bot = new SarosSWTBot();

    public MenuObject(RmiSWTWorkbenchBot rmiBot) {
        this.rmiBot = rmiBot;
        this.wUntil = rmiBot.wUntilObject;
    }

    public void clickMenuWithTexts(String... texts) {
        SWTBotMenu selectedmenu = null;
        for (String text : texts) {
            try {
                if (selectedmenu == null) {
                    selectedmenu = bot.menu(text);
                } else {
                    selectedmenu = selectedmenu.menu(text);
                }
            } catch (WidgetNotFoundException e) {
                log.error("menu \"" + text + "\" not found!");
                throw e;
            }
        }
        if (selectedmenu != null)
            selectedmenu.click();
    }
}
