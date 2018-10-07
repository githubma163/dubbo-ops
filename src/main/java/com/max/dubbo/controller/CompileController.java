package com.max.dubbo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.max.dubbo.constant.Constant;

/**
 * 
 * @author githubma
 * @date 2018年5月18日 上午11:21:03
 *
 */
@RestController
public class CompileController {

	@RequestMapping(value = "/compile/enable")
	@ResponseBody
	public Object pressureTestEnable() {
		Constant.DYNAMIC_COMPILE_SWITCH = Constant.SWITCH_ON;
		return true;
	}

	@RequestMapping(value = "/compile/disable")
	@ResponseBody
	public Object pressureTestDisable() {
		Constant.DYNAMIC_COMPILE_SWITCH = Constant.SWITCH_OFF;
		return true;
	}

}
