package net.paurf3.raspberrypitools.models.connection;


import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;
import java.util.Properties;

/**
 * Created by pau on 26/01/16.
 */
public class Ssh {
    private JSch jsch;
    private String username;
    private String password;
    private String host;
    private int port;
    private Session session;


    public Ssh(String username, String password, String host, int port) {
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
    }

    /**
     * @param username
     * @param password
     * @param host
     * @param port
     * @return
     */
    public void connection(String username, String password, String host, int port) throws JSchException {

        jsch = new JSch();
        session = null;

        session = jsch.getSession(username, host, port);
        session.setPassword(password);

        Properties prop = new Properties();
        prop.put("StrictHostKeyChecking", "no");
        session.setConfig(prop);

        session.connect();
    }


    public boolean disconnect() {
        if (session != null) {
            session.disconnect();
            return true;
        } else {
            return true;
        }
    }

    public ChannelSftp openSftpChannel() throws JSchException {
        return (ChannelSftp) session.openChannel("sftp");
    }

    /**
     * @param command
     * @return String
     */
    public String execCommand(String command) throws JSchException {
        String output;

        try {
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            channel.setOutputStream(stream);

            channel.setCommand(command);
            channel.connect();
            java.lang.Thread.sleep(500);
            channel.disconnect();

            output = stream.toString();
            return output;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getUserAndHostPrompt() throws JSchException {
        String user;
        String host;
        String outputArray[];
        String output;

        try {
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            channel.setOutputStream(stream);

            channel.setCommand("whoami && hostname");
            channel.connect();
            java.lang.Thread.sleep(500);
            channel.disconnect();

            outputArray = stream.toString().split(System.getProperty("line.separator"));

            user = outputArray[0];
            host = outputArray[1];

            if (user.equals("root")) {
                output = user + "@" + host + ":~#";
            } else {
                output = user + "@" + host + ":~$";
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }

        return output;
    }




    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
