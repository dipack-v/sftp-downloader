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
		String remotePath = "<your-remote-path>";
		String filename = "Debug.log";
		String host = "<hostname>";
		String username = "<username>";
		String password = "<password>";

		try {
			connect(host, username, password);
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