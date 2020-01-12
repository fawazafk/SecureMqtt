package com.fwz.tcpsubpk;


import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;

import java.io.IOException;

public class ChannelSftp {
    private String remoteHost = "192.168.10.10";
    public String username = "sftpuser";
    public String password = "123456";

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




