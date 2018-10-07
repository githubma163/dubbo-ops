package com.max.dubbo.entity;

/**
 * 
 * @author githubma
 * @date 2018年3月16日 上午10:28:17
 *
 */
public class ArtifactoryGavcRequest {

	private String search = "gavc";

	private String groupID;

	private String artifactID;

	private String version;

	public String getSearch() {
		return search;
	}

	public void setSearch(String search) {
		this.search = search;
	}

	public String getGroupID() {
		return groupID;
	}

	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}

	public String getArtifactID() {
		return artifactID;
	}

	public void setArtifactID(String artifactID) {
		this.artifactID = artifactID;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

}
