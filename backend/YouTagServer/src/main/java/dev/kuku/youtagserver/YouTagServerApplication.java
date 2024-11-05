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
//TODO Authentication service or per service internal function needs to check if is allowed function to check if user is allowed to do something
/*
 *
 * Spring modulith is awesome!
 * It allows you to build modular monolith application with few inter module dependencies
 * The next best thing is it's persistent event system that allows you to handle event driven application without having to run a separate messaging server
 */