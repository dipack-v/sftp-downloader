package com.demo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class SftpDownloader {

    public static void main(String args[]) throws IOException {
        List<String> hosts = Arrays.asList("your-remote-host#your-file-name", "your-remote-host#your-file-name");
        Map<String, String> hostMap = hosts.stream().map(s -> s.split("#")).collect(Collectors.toMap(s -> s[0], s -> s[1]));

        for (Map.Entry<String, String> entry : hostMap.entrySet()) {
            JSch jsch = new JSch();
            Session session = null;
            try {
                System.out.print("Trying to download " + entry.getValue() + " from " + entry.getKey());

                session = jsch.getSession("oralog", entry.getKey(), 22);
                session.setConfig("StrictHostKeyChecking", "no");
                session.setPassword("oralog");
                session.connect();

                ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
                sftpChannel.connect();

                InputStream stream = sftpChannel.get("/your/remote/file/path/" + entry.getValue());

                try {
                    File targetFile = new File("D:/logs/" + entry.getValue());
                    FileUtils.copyInputStreamToFile(stream, targetFile);

                } catch (IOException io) {
                    System.out.println("Exception 1 " + io.getMessage());

                } catch (Exception e) {
                    System.out.println("Exception 2 " + e.getMessage());
                }

                sftpChannel.exit();
                session.disconnect();
                System.out.println(" >>>>>>>> Completed !");
            } catch (JSchException e) {
                e.printStackTrace();
            } catch (SftpException e) {
                e.printStackTrace();
            }
        }
    }
}