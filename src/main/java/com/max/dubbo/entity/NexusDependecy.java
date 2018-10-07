package com.max.dubbo.entity;

/**
 * 
 * @author githubma
 * @date 2018年3月5日 下午6:52:15  
 *
 */
public class NexusDependecy extends Dependency {

	private String latestSnapshotRepositoryId;// snapshot对应的repositry

	private String latestReleaseRepositoryId;// release对应的repositry，nexus通过gavr来唯一标识一个jar

	private String repositoryPath;// jar文件下载地址

	public String getLatestSnapshotRepositoryId() {
		return latestSnapshotRepositoryId;
	}

	public void setLatestSnapshotRepositoryId(String latestSnapshotRepositoryId) {
		this.latestSnapshotRepositoryId = latestSnapshotRepositoryId;
	}

	public String getLatestReleaseRepositoryId() {
		return latestReleaseRepositoryId;
	}

	public void setLatestReleaseRepositoryId(String latestReleaseRepositoryId) {
		this.latestReleaseRepositoryId = latestReleaseRepositoryId;
	}

	public String getRepositoryPath() {
		return repositoryPath;
	}

	public void setRepositoryPath(String repositoryPath) {
		this.repositoryPath = repositoryPath;
	}

}
