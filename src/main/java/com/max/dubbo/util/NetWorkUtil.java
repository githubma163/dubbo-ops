package com.max.dubbo.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;

import org.apache.commons.net.telnet.TelnetClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author githubma
 * @date 2018年6月1日 下午4:34:39
 *
 */
public class NetWorkUtil {

	static Logger logger = LoggerFactory.getLogger(NetWorkUtil.class);

	public static boolean ping(String ip) {
		int timeOut = 3000; // 超时应该在3钞以上
		boolean status;
		try {
			status = InetAddress.getByName(ip).isReachable(timeOut);
		} catch (Exception e) {
			logger.error("ping error:", e);
			return false;
		}
		return status;
	}

	public static boolean pingWithCmd(String ipOrHost) {
		String line = null;
		Process process = null;
		try {
			process = Runtime.getRuntime().exec("ping " + ipOrHost);
			BufferedReader buf = new BufferedReader(new InputStreamReader(process.getInputStream()));
			while ((line = buf.readLine()) != null) {
				logger.info(line);
				process.destroy();
				return true;
			}
		} catch (Exception e) {
			logger.error("pingWithCmd error:", e);
		} finally {
			if (null != process) {
				process.destroy();
			}
		}
		return false;
	}

	public static boolean telnet(String ipOrHost, int port) {
		TelnetClient telnet = new TelnetClient();
		try {
			telnet.setConnectTimeout(1000);
			telnet.connect(ipOrHost, port);
		} catch (IOException e) {
			logger.error("telnet error,ip:" + ipOrHost + ",port:" + port, e.getMessage());
			return false;
		} finally {
			try {
				telnet.disconnect();
			} catch (IOException e) {
				logger.error("telnet close error,ip:" + ipOrHost + ",port:" + port, e);
			}
		}
		return true;
	}

}
