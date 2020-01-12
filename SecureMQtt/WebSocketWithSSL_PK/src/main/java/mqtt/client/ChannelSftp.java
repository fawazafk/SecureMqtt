package mqtt.client;


import com.jcraft.jsch.*;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.ftp.FtpFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Selector;

public class ChannelSftp {
    private String remoteHost = "192.168.10.10";
    public String username = "";
    public String password = "";

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static String localDir,remoteFile="/sftpuser/pub";

        public void copyPubFile() throws IOException {
            FileSystemOptions opts = new FileSystemOptions();

//            SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");
            SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, false);
//            FtpFileSystemConfigBuilder.getInstance().setPassiveMode(opts, false);


            FileSystemManager manager = VFS.getManager();

            FileObject local = manager.resolveFile( System.getProperty("user.dir")+"/" +"pub");
//            System.getProperty("user.dir") + "/" + localDir + "vfsFile.txt");
            String sftpStr="sftp://" + username + ":" + password + "@" + remoteHost + "/" + remoteFile;
            FileObject remote = manager.resolveFile(
                    sftpStr,opts);
//            "sftp://" + username + ":" + password + "@" + remoteHost + "/" + remoteFile);

            local.copyFrom(remote, Selectors.SELECT_SELF);


            local.close();
            remote.close();

    }
    }




