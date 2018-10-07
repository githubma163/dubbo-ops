package com.max.dubbo.entity;

/**
 * 
 * @author githubma
 * @date 2018年3月16日 上午10:12:18
 *
 */
public class ArtifactoryGavcEntity {

	private String name;
	private Long modifiedDate;
	private String downloadLink;
	private String classifier;// 分类，source的代表是源码包

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public String getClassifier() {
		return classifier;
	}

	public void setClassifier(String classifier) {
		this.classifier = classifier;
	}

}
