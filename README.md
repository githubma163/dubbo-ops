1. dubbo-ops是参考阿里巴巴内部的hsf-ops开发，hsf在阿里内部广泛使用，hsf-ops是开发hsf服务必要的工具。但是开源的dubbo一直缺少这个工具，dubbo-ops主要目的是提升开发测试效率，使用对象一般是开发同学，测试同学。
2. dubbo-ops不同于dubbo官网的dubbo-ops，官网dubbo-ops的主要功能是dubbo-admin和dubbo-monitor，主要是dubbo服务的查询和监控。取这个名字完全是因为对标阿里内部的hsf-ops，操作页面基本和阿里内部的hsf-ops非常相似，只是功能上做了简化，部分功能做了优化。主要的优化点有
- 添加了provider的选择
- 添加了方法参数名称的显示（jdk1.8以上才支持）
- 添加了压测功能
- 添加了dubbo监控大盘（参考cat监控大盘）
3. dubbo-ops中主要使用的技术
- springboot
- java反射
- java类加载
- java编译技术
- dubbo泛化技术
- RestTemplate
- bootstrap和jquery
4. 安装说明
- 安装jdk，需要使用jdk1.8或者以上版本，因为有用到1.8相关的api，建议使用jdk1.8，因为实际的开发运行是基于jdk1.8
- 下载maven工程源码
- 修改项目中的配置文件，主要是端口配置（可以采用默认8080），环境配置（需要根据自己公司的zookeeper来配置，一般建议3个环境，开发环境，测试环境，生产环境），私服配置（需要配置自己公司maven或者artifactory地址，程序加载jar包时优先使用maven，maven中找不到时从artifactory中查询），项目日志（默认是/app/log/dubbo-ops.log）其他配置均可采用默认值
- 使用maven打包，打包成jar包格式，命令为maven clean package
- 启动jar，命令为nohup java -jar dubbo-ops.jar
5. 测试说明
- 选择环境
- 输入service名称（支持大小写模糊匹配）
- 选择测试方法
- 输入测试参数，可以选择指定的dubbo provider，执行后查看结果
- 执行成功后可以压测
6. 项目使用情况
- 目前该项目在阿里巴巴我所在的小组内使用广泛，得到很多同事的认可，提升了大家的开发效率。实际使用中有问题可以随时在github上留言，我会尽快答复。
7. 操作截图

![](https://oscimg.oschina.net/oscnet/e65ec11468d1110b8a57cdcf940386320f3.jpg)
![](https://oscimg.oschina.net/oscnet/00e17f46bf61f8829ed699d4a31daed2570.jpg)
![](https://oscimg.oschina.net/oscnet/59509a366456a06d59e4cf07b253b56243e.jpg)


