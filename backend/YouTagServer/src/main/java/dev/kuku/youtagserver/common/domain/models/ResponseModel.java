package dev.kuku.youtagserver.common.domain.models;

public record ResponseModel<T>(T data, String msg){}
