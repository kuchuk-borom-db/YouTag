package dev.kuku.youtagserver;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

public class ModulithTests {
    static ApplicationModules modules = ApplicationModules.of(YouTagServerApplication.class);

    @Test
    void modulithTest() {
        modules.verify();
    }
}
