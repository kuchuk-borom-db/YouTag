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
//TODO Move service to exposed domain and dependencies between objects should be resolved using common DTO
/*
 *
 * Spring modulith is awesome!
 * It allows you to build modular monolith application with few inter module dependencies
 * The next best thing is it's persistent event system that allows you to handle event driven application without having to run a separate messaging server
 */