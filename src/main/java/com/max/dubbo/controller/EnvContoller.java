package com.max.dubbo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.max.dubbo.constant.Constant;
import com.max.dubbo.entity.Environment;

/**
 * 
 * @author githubma
 * @date 2018年4月3日 上午10:42:39
 *
 */
@RestController
@RequestMapping("/env")
public class EnvContoller {

	@RequestMapping("/list")
	public List<Environment> getAllEnv() {
		List<Environment> environmentList = Constant.ENVIRONMENT_LIST;
		return environmentList;
	}

}
