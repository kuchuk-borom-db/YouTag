package dev.kuku.youtagserver.junction.domain;

import lombok.*;
import org.springframework.stereotype.Service;

@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
@Getter
@Service
public class JunctionId {
    String userId;
    String videoId;
}
