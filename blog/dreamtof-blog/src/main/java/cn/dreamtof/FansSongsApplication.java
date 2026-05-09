package cn.dreamtof;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"cn.dreamtof"})
@MapperScan("cn.dreamtof.*.infrastructure.persistence.mapper")
public class FansSongsApplication {

    public static void main(String[] args) {
        // 3. 【日志黑洞修复】启动前强制劫持 Tomcat/Spring 的底层 JUL 日志，导流到我们的自定义引擎
//        SLF4JBridgeHandler.removeHandlersForRootLogger();
//        SLF4JBridgeHandler.install();

        SpringApplication.run(FansSongsApplication.class, args);

        System.out.println("(♥◠‿◠)ﾉﾞ  FansSongs 服务启动成功   ლ(´ڡ`ლ)ﾞ  ");
    }

}
