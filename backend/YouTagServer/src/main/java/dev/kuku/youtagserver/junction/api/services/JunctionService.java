package dev.kuku.youtagserver.junction.api.services;

import dev.kuku.youtagserver.junction.api.dtos.JunctionDTO;
import dev.kuku.youtagserver.junction.api.exceptions.JunctionDTOHasNullValues;
import dev.kuku.youtagserver.junction.domain.Junction;
import dev.kuku.youtagserver.shared.application.Service;

import java.util.List;

public interface JunctionService extends Service<Junction, JunctionDTO> {
    void addVideosWithTags(String userId, List<String> videos, List<String> tags) throws JunctionDTOHasNullValues;

    void deleteAllVideosAndTags(String userId) throws JunctionDTOHasNullValues;

    void deleteTagsFromAllVideos(String userId, List<String> tags) throws JunctionDTOHasNullValues;

    void deleteTagsFromVideos(String userId, List<String> videos, List<String> tags) throws JunctionDTOHasNullValues;

    void deleteVideosFromUser(String userId, List<String> videoIds);

    List<JunctionDTO> getAllJunctionOfUser(String userId, int skip, int limit) throws JunctionDTOHasNullValues;

    List<JunctionDTO> getAllVideosWithTags(String userId, List<String> list, int skip, int limit) throws JunctionDTOHasNullValues;

    List<JunctionDTO> getVideosOfUser(String userId, List<String> videos, int skip, int limit) throws JunctionDTOHasNullValues;
}
