package dev.kuku.youtagserver.user.infrastructure.controller;

import dev.kuku.youtagserver.shared.models.ResponseModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/authenticated/user")
class UserController {

    @GetMapping("/")
    public ResponseEntity<ResponseModel<String>> test() {
        return ResponseEntity.ok(new ResponseModel<>("WELCOME", ""));
    }

}
