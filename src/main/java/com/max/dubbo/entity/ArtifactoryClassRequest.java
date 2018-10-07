package com.max.dubbo.entity;

/**
 * 
 * @author githubma
 * @date 2018年3月16日 上午10:28:17
 *
 */
public class ArtifactoryClassRequest {

	private String search = "class";

	private String name;

	private Boolean searchClassOnly = true;

	public String getSearch() {
		return search;
	}

	public void setSearch(String search) {
		this.search = search;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getSearchClassOnly() {
		return searchClassOnly;
	}

	public void setSearchClassOnly(Boolean searchClassOnly) {
		this.searchClassOnly = searchClassOnly;
	}

}
