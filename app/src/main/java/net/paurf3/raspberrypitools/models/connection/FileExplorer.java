package net.paurf3.raspberrypitools.models.connection;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import net.paurf3.raspberrypitools.activities.MainActivity;

import java.util.Vector;

/**
 * Created by pau on 12/02/16.
 */
public class FileExplorer {

    ChannelSftp channelSftp;

    private String absoluteCurrentPath;

    public FileExplorer() throws JSchException, SftpException {

        this.channelSftp = MainActivity.ssh.openSftpChannel();
        this.channelSftp.connect();
        this.absoluteCurrentPath = channelSftp.pwd();
        this.channelSftp.cd(absoluteCurrentPath);
    }

    public void goUpperDirectory() {
        try {
            channelSftp.cd("..");
            this.absoluteCurrentPath = channelSftp.pwd();
        } catch (SftpException e) {
            try {
                this.channelSftp = MainActivity.ssh.openSftpChannel();
                this.channelSftp.connect();
            } catch (JSchException e1) {
                e1.printStackTrace();
            }

        }
    }

    public void changeDir(String relativePath) {

        try {
            channelSftp.cd(relativePath);
            this.absoluteCurrentPath = channelSftp.pwd();
        } catch (SftpException e) {
            try {
                this.channelSftp = MainActivity.ssh.openSftpChannel();
                this.channelSftp.connect();
            } catch (JSchException e1) {
                e1.printStackTrace();
            }
        }
    }

    public Vector<ChannelSftp.LsEntry> lsDirectory() {
        try {
            Vector<ChannelSftp.LsEntry> elementsList;
            elementsList = this.channelSftp.ls(absoluteCurrentPath);
            return elementsList;
        } catch (SftpException e) {
            try {
                this.channelSftp = MainActivity.ssh.openSftpChannel();
                this.channelSftp.connect();
            } catch (JSchException e1) {
                e1.printStackTrace();
                return null;
            }
            return null;
        }
    }



    public void disconnectChannelSftp() {
        this.channelSftp.disconnect();
    }


    public String getAbsoluteCurrentPath() {
        return absoluteCurrentPath;
    }

}
