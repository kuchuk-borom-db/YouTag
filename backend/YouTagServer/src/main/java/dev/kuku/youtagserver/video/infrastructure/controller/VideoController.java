package dev.kuku.youtagserver.video.infrastructure.controller;

import dev.kuku.youtagserver.shared.exceptions.ResponseException;
import dev.kuku.youtagserver.shared.models.ResponseModel;
import dev.kuku.youtagserver.video.application.services.VideoCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/authenticated/video")
class VideoController {
    final VideoCommandHandler commandHandler;

    @GetMapping("/{id}")
    ResponseEntity<ResponseModel<Map<String, String>>> getVideoInfo(@PathVariable("id") String videoId) throws ResponseException {
        var info = commandHandler.getVideoInfo(videoId);
        //Creating a map so that we can skip sending updated info.
        var map = new HashMap<String, String>();
        map.put("id", info.id());
        map.put("title", info.title());
        map.put("description", info.description());
        map.put("thumbnail", info.thumbnail());
        return ResponseEntity.ok(new ResponseModel<>(map, ""));
    }
}
