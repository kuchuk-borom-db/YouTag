package dev.kuku.youtagserver.video.application;

import dev.kuku.youtagserver.user.domain.exception.InvalidEmailException;

public interface VideoServiceInternal {
    boolean addVideoForUser(String video, String userEmail) throws InvalidEmailException;
}
