package com.max.dubbo.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * 
 * @author githubma
 * @date 2018年3月5日 下午6:54:24
 *
 */
public class RegUtil {

    public static Boolean checkSocket(String socket) {
        if (StringUtils.isBlank(socket)) {
            return false;
        }
        String regex = "[0-9:.]{10,30}";// 数字，冒号，点号,简单判断下
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(socket);
        return matcher.matches();
    }

    public static Boolean checkEnglish(String content) {
        if (StringUtils.isBlank(content)) {
            return false;
        }
        String regex = "[a-zA-Z]+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        return matcher.matches();
    }

    public static String extractPackageName(String content) {
        if (StringUtils.isBlank(content)) {
            return "";
        }
        String regex = "[a-zA-Z.]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            sb.append(matcher.group());
        }
        return sb.toString();
    }

}
