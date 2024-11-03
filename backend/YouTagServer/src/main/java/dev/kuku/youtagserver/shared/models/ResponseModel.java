package dev.kuku.youtagserver.shared.models;

public record ResponseModel<T>(T data, String msg){}
