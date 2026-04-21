package com.piedrazul.msnotifications;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MsNotificationsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsNotificationsApplication.class, args);
    }
}
