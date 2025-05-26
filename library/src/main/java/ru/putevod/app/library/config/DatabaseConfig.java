package ru.putevod.app.library.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.sql.DataSource;

@Configuration
@EnableScheduling
@Slf4j
public class DatabaseConfig {

    private final DataSource dataSource;

    public DatabaseConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logConnectionPoolInfo() {
        if (dataSource instanceof HikariDataSource hikariDataSource) {
            log.info("HikariCP Pool initialized - Max Pool Size: {}, Min Idle: {}", 
                    hikariDataSource.getMaximumPoolSize(), 
                    hikariDataSource.getMinimumIdle());
        }
    }

    @Scheduled(fixedRate = 60000) 
    public void logConnectionPoolStats() {
        if (dataSource instanceof HikariDataSource hikariDataSource) {
            log.debug("HikariCP Stats - Active: {}, Idle: {}, Total: {}, Waiting: {}", 
                    hikariDataSource.getHikariPoolMXBean().getActiveConnections(),
                    hikariDataSource.getHikariPoolMXBean().getIdleConnections(),
                    hikariDataSource.getHikariPoolMXBean().getTotalConnections(),
                    hikariDataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
        }
    }
} 