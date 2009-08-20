package de.fu_berlin.inf.dpp.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.XMPPChatReceiver;
import de.fu_berlin.inf.dpp.net.internal.XMPPChatTransmitter;
import de.fu_berlin.inf.dpp.net.internal.XStreamExtensionProvider;
import de.fu_berlin.inf.dpp.net.internal.XStreamExtensionProvider.XStreamIQPacket;

/**
 * Component for figuring out whether two Saros plug-in instances with known
 * Bundle Version strings are compatible.
 * 
 * This class does not use a {@link Comparator#compare(Object, Object)}, because
 * results might not be symmetrical (we only note whether current Version is A
 * is compatible with older versions, but not whether the older versions from
 * their perspective are compatible with us) and transitive (if Version A is too
 * old for B, Version B too old for C, then A might be still OK for C).
 */
@Component(module = "misc")
public class VersionManager {

    private static final Logger log = Logger.getLogger(VersionManager.class
        .getName());

    /**
     * Data Object for sending version information
     */
    public static class VersionInfo {

        public Version version;

        public Compatibility compatibility;

    }

    /**
     * Enumeration to describe whether a local version is compatible with a
     * remote one.
     */
    public enum Compatibility {
        /**
         * Versions are (probably) compatible
         */
        OK,
        /**
         * The local version is (probably) too old to work with the remote
         * version.
         * 
         * The user should be told to upgrade
         */
        TOO_OLD,
        /**
         * The local version is (probably) too new to work with the remote
         * version.
         * 
         * The user should be told to tell the peer to update.
         */
        TOO_NEW;

        /**
         * Given a result from {@link Comparator#compare(Object, Object)} will
         * return the associated Compatibility object
         */
        public static Compatibility valueOf(int comparison) {
            switch (Integer.signum(comparison)) {
            case -1:
                return TOO_OLD;
            case 0:
                return OK;
            case 1:
            default:
                return TOO_NEW;
            }
        }
    }

    /**
     * The compatibilityChart should contain for each version the list of all
     * versions which should be compatible with the given one. If no entry
     * exists for the version run by a user, the VersionManager will only return
     * {@link Compatibility#OK} if and only if the version information are
     * {@link Version#equals(Object)} to each other.
     */
    public static Map<Version, List<Version>> compatibilityChart = new HashMap<Version, List<Version>>();

    /**
     * Initialize the compatibility map.
     * 
     * For each version, all older versions that are compatible should be added
     * in order of release date.
     * 
     * For the first version which is too old the commit which broke
     * compatibility should be listed.
     */
    static {

        /**
         * <Add new version here>
         */

        /**
         * Version 9.8.21
         */
        compatibilityChart.put(new Version("9.8.21"), Arrays.asList(
            new Version("9.8.21"), new Version("9.8.21.DEVEL")));

        /**
         * Version 9.8.21.DEVEL
         * 
         * No longer compatible with 9.7.31 since r.1576 changed serialization
         * of Activities
         */
        compatibilityChart.put(new Version("9.8.21.DEVEL"), Arrays
            .asList(new Version("9.8.21.DEVEL")));
    }

    /**
     * @Inject
     */
    protected Bundle bundle;

    /**
     * @Inject
     */
    protected Saros saros;

    /**
     * @Inject
     */
    protected XMPPChatTransmitter transmitter;

    protected XStreamExtensionProvider<VersionInfo> versionProvider = new XStreamExtensionProvider<VersionInfo>(
        "sarosVersion", VersionInfo.class, Version.class, Compatibility.class);

    public VersionManager(Bundle bundle, final Saros saros,
        final XMPPChatReceiver receiver, XMPPChatTransmitter transmitter) {

        this.bundle = bundle;
        this.saros = saros;
        this.transmitter = transmitter;

        receiver.addPacketListener(new PacketListener() {
            public void processPacket(Packet packet) {
                @SuppressWarnings("unchecked")
                XStreamIQPacket<VersionInfo> iq = (XStreamIQPacket<VersionInfo>) packet;

                if (iq.getType() != IQ.Type.GET)
                    return;

                VersionInfo remote = iq.getPayload();

                VersionInfo local = new VersionInfo();
                local.version = getVersion();
                local.compatibility = determineCompatibility(local.version,
                    remote.version);

                IQ reply = versionProvider.createIQ(local);
                reply.setType(IQ.Type.RESULT);
                reply.setPacketID(iq.getPacketID());
                reply.setTo(iq.getFrom());
                saros.getConnection().sendPacket(reply);
            }
        }, versionProvider.getIQFilter());
    }

    /**
     * Will query the given user for his Version and whether s/he thinks that
     * her/his remote version is compatible with our local version.
     * 
     * The resulting VersionInfo represents what the remote peer knows about
     * compatibility with our version.
     * 
     * If the resulting {@link VersionInfo#compatibility} is TOO_NEW (meaning
     * the remote version is too new), then the remote peer does not know
     * whether his version is compatible with ours. We must then check by
     * ourselves.
     * 
     * @blocking If the request times out (7,5s) or an error occurs null is
     *           returned.
     */
    public VersionInfo queryVersion(JID rqJID) {
        VersionInfo versionInfo = new VersionInfo();
        versionInfo.version = getVersion();
        return transmitter.sendQuery(rqJID, versionProvider, versionInfo, 7500);
    }

    /**
     * Returns the Version of the locally running Saros plugin
     */
    public Version getVersion() {
        return Util.getBundleVersion(bundle);
    }

    /**
     * Will compare the two given Versions for compatibility. The result
     * indicates whether the localVersion passed first is compatible with the
     * remoteVersion.
     */
    public Compatibility determineCompatibility(Version localVersion,
        Version remoteVersion) {

        // If localVersion is older than remote version, then we cannot know
        // whether we are compatible
        if (localVersion.compareTo(remoteVersion) < 0)
            return Compatibility.TOO_OLD;

        List<Version> compatibleVersions = compatibilityChart.get(localVersion);
        if (compatibleVersions == null) {
            log.error("VersionManager does not know about current version."
                + " The release manager must have slept:" + localVersion);

            // Fallback to comparing versions directly
            return Compatibility.valueOf(localVersion.compareTo(remoteVersion));
        }

        if (compatibleVersions.contains(remoteVersion))
            return Compatibility.OK;
        else
            return Compatibility.TOO_OLD;
    }

    /**
     * If the remote version is newer than the local one, the remote
     * compatibility comparison result will be returned.
     * 
     * @return A {@link VersionInfo} object with the remote
     *         {@link VersionInfo#version} and {@link VersionInfo#compatibility}
     *         info or <code>null</code> if could not get version information
     *         from the peer (this probably means the other person is TOO_OLD)
     * 
     *         The return value describes whether the local version is
     *         compatible with the peer's one (e.g.
     *         {@link Compatibility#TOO_OLD} means that the local version is too
     *         old)
     * 
     * @blocking This method may take some time (up to 7,5s) if the peer is not
     *           responding.
     */
    public VersionInfo determineCompatibility(JID peer) {

        VersionInfo remoteVersionInfo = queryVersion(peer);

        /*
         * FIXME Our caller should be able to distinguish whether the query
         * failed or it is an IM client which sends back the message
         */

        if (remoteVersionInfo == null)
            return null; // No answer from peer

        if (remoteVersionInfo.compatibility == null)
            return null; /*
                          * Peer does not understand our query and just sends it
                          * back to us. IMs like Pidgin do this.
                          */

        VersionInfo result = new VersionInfo();
        result.version = remoteVersionInfo.version;

        Compatibility localComp = determineCompatibility(getVersion(),
            remoteVersionInfo.version);
        Compatibility remoteComp = remoteVersionInfo.compatibility;

        if (localComp == Compatibility.TOO_OLD) {
            // Our version is older than the peer's one, let's trust his info
            if (remoteComp == Compatibility.OK) {
                // only if he tell's us that it is okay, return OK
                result.compatibility = Compatibility.OK;
            } else {
                // otherwise we are too old
                result.compatibility = Compatibility.TOO_OLD;
            }
        } else {
            // We are newer, thus we can just use our compatibility result
            result.compatibility = localComp;
        }
        return result;
    }

    /**
     * Given the bundle version string of a remote Saros instance, will return
     * whether the local version is compatible with the given instance.
     * 
     * @blocking This is a long running operation
     */
    public Compatibility determineCompatibility(String remoteVersionString) {

        Version remoteVersion = Util.parseBundleVersion(remoteVersionString);
        Version localVersion = Util.getBundleVersion(bundle);

        return determineCompatibility(localVersion, remoteVersion);
    }
}
