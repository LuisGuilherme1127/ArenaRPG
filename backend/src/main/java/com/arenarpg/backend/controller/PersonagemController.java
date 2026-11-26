package com.arenarpg.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arenarpg.backend.dto.PersonagemDTO;
import com.arenarpg.backend.service.PersonagemService;

@RestController
@RequestMapping("/api/personagens")
public class PersonagemController {
    private final PersonagemService personagemService;

    public PersonagemController(PersonagemService personagemService) {
        this.personagemService = personagemService;
    }

    @GetMapping
    public List<PersonagemDTO> listarPersonagens() {
        return personagemService.listarPersonagens();
    }

    @GetMapping("/{tipo}")
    public PersonagemDTO buscarPorTipo(@PathVariable String tipo) {
        return personagemService.buscarPorTipo(tipo);
    }
}
