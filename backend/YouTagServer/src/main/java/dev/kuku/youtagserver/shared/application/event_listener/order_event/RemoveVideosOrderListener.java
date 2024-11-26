package dev.kuku.youtagserver.shared.application.event_listener.order_event;

import dev.kuku.youtagserver.shared.api.events.RemoveVideosOrder;
import dev.kuku.youtagserver.shared.application.OrchestratorService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RemoveVideosOrderListener {
    final OrchestratorService orchestratorService;
    @Async
    @TransactionalEventListener
    void on(RemoveVideosOrder order) {
        log.debug("Remove videos {} event", order);
        orchestratorService.deleteSpecificVideos(order.invalidVideos());
    }
}
