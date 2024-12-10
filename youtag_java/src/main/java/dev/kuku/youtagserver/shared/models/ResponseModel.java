package dev.kuku.youtagserver.shared.models;

public record ResponseModel<T>(T data, String msg) {
    public static <J> ResponseModel<J> build(J data, String msg) {
        return new ResponseModel<>(data, msg);
    }
}
