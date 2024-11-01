package dev.kuku.youtagserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class YouTagServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(YouTagServerApplication.class, args);
    }

}
//TODO Read Google client.json for secret stuff