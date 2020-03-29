package com.demo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class SftpDownloader2 {
	static Session session = null;
	static ChannelSftp sftpChannel = null;

	public static void main(String args[]) throws IOException {
		String remotePath = DownloaderProps.getPropertyValue("remotePath");
		String remoteHost = DownloaderProps.getPropertyValue("remoteHost");
		String username = DownloaderProps.getPropertyValue("username");
		String password = DownloaderProps.getPropertyValue("password");

		String filename = "app2/Debug.log";

		try {
			long startTime = System.currentTimeMillis();
			connect(remoteHost, username, password);
			InputStream stream = sftpChannel.get(remotePath + filename);

			try {
				File targetFile = new File("C:/logs/" + filename);
				FileUtils.copyInputStreamToFile(stream, targetFile);

			} catch (IOException io) {
				System.out.println("Exception 1 " + io.getMessage());

			} catch (Exception e) {
				System.out.println("Exception 2 " + e.getMessage());
			}
			disconnect();
			long endTime = System.currentTimeMillis();
			System.out.println("That took " + (endTime - startTime) + " milliseconds");
			System.out.println(" >>>>>>>> Completed !");
		} catch (SftpException e) {
			e.printStackTrace();
		}

	}

	static void connect(String host, String username, String password) {
		JSch jsch = new JSch();
		try {
			System.out.print("Trying to download " + host);

			session = jsch.getSession(username, host, 22);
			session.setConfig("StrictHostKeyChecking", "no");
			session.setPassword(password);
			session.connect();

			sftpChannel = (ChannelSftp) session.openChannel("sftp");
			sftpChannel.connect();

		} catch (JSchException e) {
			e.printStackTrace();
		}
	}

	static void disconnect() {
		sftpChannel.exit();
		session.disconnect();
	}
}