package dev.kuku.youtagserver.shared.api.services;

import dev.kuku.youtagserver.shared.exceptions.ResponseException;

//TODO : Use common dto exception
public interface Service<Entity, Dto> {
    Dto toDto(Entity e) throws ResponseException;
}
