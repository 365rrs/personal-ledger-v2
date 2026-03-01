package com.ledger;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 个人账本系统启动类
 *
 * @author personal-ledger
 * @version 1.0
 * @date 2025-01-13
 */
@Slf4j
@SpringBootApplication
@MapperScan("com.ledger.mapper")
public class LedgerApplication {

    public static void main(String[] args) throws UnknownHostException {
        ConfigurableApplicationContext application = SpringApplication.run(LedgerApplication.class, args);
        Environment env = application.getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = env.getProperty("server.port");
        String contextPath = env.getProperty("server.servlet.context-path", "");
        
        log.info("\n----------------------------------------------------------\n\t" +
                "Application '{}' is running! Access URLs:\n\t" +
                "Local: \t\thttp://localhost:{}{}\n\t" +
                "External: \thttp://{}:{}{}\n\t" +
                "Swagger UI: \thttp://localhost:{}{}/swagger-ui.html\n" +
                "----------------------------------------------------------",
                env.getProperty("spring.application.name"),
                port, contextPath,
                ip, port, contextPath,
                port, contextPath);
    }
}
