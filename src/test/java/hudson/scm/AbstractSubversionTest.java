package hudson.scm;

import hudson.ClassicPluginStrategy;
import hudson.Launcher.LocalLauncher;
import hudson.Proc;
import hudson.scm.SubversionSCM.DescriptorImpl;
import hudson.util.StreamTaskListener;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;

import org.jvnet.hudson.test.HudsonHomeLoader.CopyExisting;
import org.jvnet.hudson.test.HudsonTestCase;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

/**
 * Base class for Subversion related tests.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class AbstractSubversionTest extends HudsonTestCase {

    protected DescriptorImpl descriptor;
    protected String kind = ISVNAuthenticationManager.PASSWORD;

    @Override
    protected void setUp() throws Exception {
        //Enable classic plugin strategy, because some extensions are duplicated with default strategy
        System.setProperty("hudson.PluginStrategy", "hudson.ClassicPluginStrategy");
        super.setUp();
        descriptor = hudson.getDescriptorByType(DescriptorImpl.class);
    }

    protected Process runSvnServe(URL zip) throws Exception {
        return runSvnServe(new CopyExisting(zip).allocate());
    }

    /**
     * Runs svnserve to serve the specified directory as a subversion
     * repository.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    protected Process runSvnServe(File repo) throws IOException, InterruptedException {
        try {
            ProcessBuilder pb = new ProcessBuilder("svnserve", "--help");
            Process p = pb.start();
            p.waitFor();
        } catch (IOException e) {
            // if we fail to launch svnserve, skip the test
            return null;
        }

        Socket s = null;
        try {
            s = new Socket("localhost", 3690);

            // Reaching this point implies that port 3690 received a response.
            return null;
        } catch (IOException e) {
            // Failed to receive any reposnse to port 3690. That means port is available.
        } finally {
            if (s != null) {
                s.close();
            }
        }

        ProcessBuilder pb = new ProcessBuilder("svnserve", "-d", "--foreground", "-r", repo.getAbsolutePath());
        pb.directory(repo);
        Process p = pb.start();
        return p;
    }

    protected ISVNAuthenticationManager createInMemoryManager() {
        char[] password = null;
        ISVNAuthenticationManager m = SVNWCUtil.createDefaultAuthenticationManager(hudson.root, null, password, false);
        m.setAuthenticationProvider(descriptor.createAuthenticationProvider(null));
        return m;
    }

    static {
        ClassicPluginStrategy.useAntClassLoader = true;
    }
}
