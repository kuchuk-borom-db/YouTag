package dev.kuku.youtagserver.auth.api.events;

import java.util.Map;

/**
 * @param userMap name,email,picture are the keys
 */
public record GotUserFromTokenEvent(Map<String, String> userMap) {
}
