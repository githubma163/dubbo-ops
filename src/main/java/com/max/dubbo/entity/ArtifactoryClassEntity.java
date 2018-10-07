package com.max.dubbo.entity;

/**
 * 
 * @author githubma
 * @date 2018年3月16日 上午10:12:18
 *
 */
public class ArtifactoryClassEntity {

	private String name;
	private String archiveName;
	private Long modifiedDate;
	private String downloadLink;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getArchiveName() {
		return archiveName;
	}

	public void setArchiveName(String archiveName) {
		this.archiveName = archiveName;
	}

	public Long getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Long modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public String getDownloadLink() {
		return downloadLink;
	}

	public void setDownloadLink(String downloadLink) {
		this.downloadLink = downloadLink;
	}

}
