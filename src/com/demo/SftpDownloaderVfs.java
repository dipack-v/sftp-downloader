package com.demo;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;

public class SftpDownloaderVfs {
	static final List<String> fileNames = Arrays.asList(DownloaderProps.getPropertyValues("fileNames"));
	static final String folderNamePrefix = "app";
	static final String[] fileNamesStartsWith = new String[] {
			// "Debug.log",
			// "UserEntry.log"
	};

	public static void main(String args[]) throws IOException {
		String remotePath = DownloaderProps.getPropertyValue("remotePath");
		String remoteHost = DownloaderProps.getPropertyValue("remoteHost");
		String username = DownloaderProps.getPropertyValue("username");
		String password = DownloaderProps.getPropertyValue("password");
		String localPath = System.getProperty("user.home") + "/Documents/qa8/";

		long startTime = System.currentTimeMillis();
		download(remoteHost, username, password, localPath, remotePath);
		long endTime = System.currentTimeMillis();
		
		System.out.println("That took " + (endTime - startTime) + " milliseconds");
	}

	/**
	 * Method to download the file from remote server location
	 * 
	 * @param hostName       HostName of the server
	 * @param username       UserName to login
	 * @param password       Password to login
	 * @param localFilePath  LocalFilePath. Should contain the entire local file
	 *                       path - Directory and Filename with \\ as separator
	 * @param remoteFilePath remoteFilePath. Should contain the entire remote file
	 *                       path - Directory and Filename with / as separator
	 */
	public static void download(String hostName, String username, String password, String localFilePath,
			String remoteFilePath) {
		StandardFileSystemManager manager = new StandardFileSystemManager();
		
		Function<FileObject, FileObject[]> findFiles = folder -> {
			try {
				return folder.findFiles(Selectors.SELECT_CHILDREN);
			} catch (FileSystemException e1) {
				throw new RuntimeException(e1);
			}
		};

		Predicate<FileObject> isFolder = folder -> {
			try {
				return folder.isFolder();
			} catch (FileSystemException e1) {
				throw new RuntimeException(e1);
			}
		};

		Predicate<FileObject> isFile = file -> {
			try {
				return file.isFile();
			} catch (FileSystemException e1) {
				throw new RuntimeException(e1);
			}
		};
		
		Predicate<FileObject> fileNameContains = file -> fileNames.contains(file.getName().getBaseName());

		Predicate<FileObject> folderNameStartsWith = folder -> StringUtils.startsWith(folder.getName().getBaseName(),
				folderNamePrefix);

		Predicate<FileObject> fileNameStartsWith = file -> StringUtils.startsWithAny(file.getName().getBaseName(),
				fileNamesStartsWith);

		Consumer<FileObject> downloadFile = file -> {
			try {
				String fileName = folderNamePrefix
						+ StringUtils.substringAfterLast(file.getName().getPath(), folderNamePrefix);
				FileObject localFile = manager.resolveFile(localFilePath + fileName);
				localFile.copyFrom(file, Selectors.SELECT_SELF);
				System.out.println(fileName + " >>>> Downloaded");

			} catch (FileSystemException e) {
				throw new RuntimeException(e);
			}
		};

		try {
			manager.init();

			// Create remote file object
			FileObject remoteRootFolder = manager.resolveFile(
					createConnectionString(hostName, username, password, remoteFilePath), createDefaultOptions());

			Arrays.stream(remoteRootFolder.findFiles(Selectors.SELECT_CHILDREN))
					.filter(isFolder.and(folderNameStartsWith)).map(findFiles).flatMap(files -> Arrays.stream(files))
					.filter(isFile.and(fileNameContains).or(fileNameStartsWith)).parallel().forEach(downloadFile);

		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			manager.close();
		}

	}

	/**
	 * Generates SFTP URL connection String
	 * 
	 * @param hostName       HostName of the server
	 * @param username       UserName to login
	 * @param password       Password to login
	 * @param remoteFilePath remoteFilePath. Should contain the entire remote file
	 *                       path - Directory and Filename with / as separator
	 * @return concatenated SFTP URL string
	 * @throws URISyntaxException
	 */
	public static String createConnectionString(String hostName, String username, String password,
			String remoteFilePath) throws URISyntaxException {
		URI uri = new URI("sftp", username + ":" + password, hostName, -1, remoteFilePath, null, null);
		System.out.println("URL>>>>>>>" + uri.toString());
		return uri.toString();
	}

	/**
	 * Method to setup default SFTP config
	 * 
	 * @return the FileSystemOptions object containing the specified configuration
	 *         options
	 * @throws FileSystemException
	 */
	public static FileSystemOptions createDefaultOptions() throws FileSystemException {
		// Create SFTP options
		FileSystemOptions opts = new FileSystemOptions();

		// SSH Key checking
		SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");

		/*
		 * Using the following line will cause VFS to choose File System's Root as VFS's
		 * root. If I wanted to use User's home as VFS's root then set 2nd method
		 * parameter to "true"
		 */
		// Root directory set to user home
		SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, false);

		// Timeout is count by Milliseconds
		SftpFileSystemConfigBuilder.getInstance().setSessionTimeoutMillis(opts, 10000);

		return opts;
	}

}