package de.fu_berlin.inf.dpp.preferences;

import java.util.List;

import de.fu_berlin.inf.dpp.editor.colorstorage.UserColorID;

/**
 * IPreferences provide methods to get and set preferences. These Preferences
 * will be stored in an IDE specific way.
 * 
 * FIXME: This has to be split into a PreferenceStore / PreferenceUtils
 * implementation, otherwise every implementation for each IDE would need to
 * implement preferences that might not even be present or make sense.
 */
@Deprecated
public interface IPreferences {

    /**
     * Returns whether debug is enabled or not.
     * 
     * @return true if debug is enabled.
     */
    public boolean isDebugEnabled();

    /**
     * @return Saros's XMPP server DNS address.
     */
    public String getSarosXMPPServer();

    /**
     * @return the default server.<br/>
     *         Is never empty or null.
     */
    public String getDefaultServer();

    /**
     * Returns whether auto-connect is enabled or not.
     * 
     * @return true if auto-connect is enabled.
     */
    public boolean isAutoConnecting();

    /**
     * Returns whether port mapping is enabled or not by evaluating the stored
     * deviceID to be empty or not.
     * 
     * @return true if port mapping is enabled, false otherwise
     */
    public boolean isAutoPortmappingEnabled();

    /**
     * @return the Socks5 candidates for the Socks5 proxy
     */

    public List<String> getSocks5Candidates();

    /**
     * Returns whether the external address of the gateway should be used as a
     * Socks5 candidate or not.
     * 
     * @return true if external address of the gateway should be used as a
     *         Socks5 candidate, false otherwise
     */
    public boolean useExternalGatewayAddress();

    /**
     * Returns the device ID of the gateway to perform port mapping on.
     * 
     * @return Device ID of the gateway or empty String if disabled.
     */
    public String getAutoPortmappingGatewayID();

    /**
     * @return the last port of the auto port mapping.
     */
    public int getAutoPortmappingLastPort();

    /**
     * Returns the Skype user name or an empty string if none was specified.
     * 
     * @return the user name.for Skype or an empty string
     */
    public String getSkypeUserName();

    /**
     * Returns the port for SOCKS5 file transfer. If
     * {@link PreferenceConstants#USE_NEXT_PORTS_FOR_FILE_TRANSFER} is set, a
     * negative number is returned (smacks will try next free ports above this
     * number)
     * 
     * @return port for smacks configuration (negative if to try out ports
     *         above)
     */
    public int getFileTransferPort();

    /**
     * Returns whether force In-Band Bytestreams (IBB) transport is enabled or
     * not.
     * 
     * @return true if force In-Band Bytestreams (IBB) transport is enabled.
     */
    public boolean forceIBBTransport();

    /**
     * Returns whether concurrent undo is enabled or not.
     * 
     * @return true if concurrent undo is enabled.
     */
    public boolean isConcurrentUndoActivated();

    /**
     * Returns whether version control is enabled or not.
     * 
     * @return true if version control is enabled.
     */
    public boolean useVersionControl();

    /**
     * Sets the value of useVersionControl
     * 
     * @param value
     */
    public void setUseVersionControl(boolean value);

    /**
     * Returns whether local SOCKS5 proxy is enabled or not.
     * 
     * @return true if local SOCKS5 proxy is enabled.
     */
    public boolean isLocalSOCKS5ProxyEnabled();

    /**
     * @return Stun IP address.
     */
    public String getStunIP();

    /**
     * @return Stun Port.
     */
    public int getStunPort();

    /**
     * Returns the favorite color ID that should be used during a session.
     * 
     * @return the favorite color ID or {@value UserColorID#UNKNOWN} if no
     *         favorite color ID is available
     */
    public int getFavoriteColorID();

    /**
     * Returns the nickname that should be used in a session.
     * 
     * @return the nickname which may be empty if no nickname is available
     */
    public String getSessionNickname();

}
