package dev.kuku.youtagserver.junction.api.events;

import dev.kuku.youtagserver.junction.api.dtos.JunctionDTO;

import java.util.List;

public record JunctionDeletedEvent(List<JunctionDTO> deletedDtos) {
}
