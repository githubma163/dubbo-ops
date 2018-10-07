package com.max.dubbo.entity;

import java.util.List;

/**
 * 
 * @author githubma
 * @date 2018年3月16日 上午10:05:25
 * 
 *       Artifactory私服按gavc搜索的结果
 *
 */
public class ArtifactoryGavcResult {

	private List<ArtifactoryGavcEntity> results;

	public List<ArtifactoryGavcEntity> getResults() {
		return results;
	}

	public void setResults(List<ArtifactoryGavcEntity> results) {
		this.results = results;
	}

}
