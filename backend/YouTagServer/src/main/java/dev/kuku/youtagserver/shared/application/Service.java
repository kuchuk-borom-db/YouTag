package dev.kuku.youtagserver.shared.application;

import dev.kuku.youtagserver.junction.api.exceptions.JunctionDTOHasNullValues;
import dev.kuku.youtagserver.video.api.exceptions.VideoDTOHasNullValues;

public interface Service<Entity, Dto> {
    Dto toDto(Entity e) throws VideoDTOHasNullValues, JunctionDTOHasNullValues;
}
