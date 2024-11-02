package dev.kuku.youtagserver.common.models;

public record ResponseModel<T>(T data, String msg){}
