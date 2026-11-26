
package com.arenarpg.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {

    @GetMapping("/api/status")
    public String verificarStatus() {
        return "Arena RPG - Backend Java funcionando!";
    }
}
