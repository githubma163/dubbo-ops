package com.max.dubbo.entity;

import java.util.List;

/**
 * 
 * @author githubma
 * @date 2018年3月16日 上午10:05:25
 * 
 *       Artifactory私服按class搜索的结果
 *
 */
public class ArtifactoryClassResult {

	private List<ArtifactoryClassEntity> results;

	public List<ArtifactoryClassEntity> getResults() {
		return results;
	}

	public void setResults(List<ArtifactoryClassEntity> results) {
		this.results = results;
	}

}
