package com.arenarpg.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arenarpg.backend.dto.PersonagemDTO;
import com.arenarpg.backend.repository.PersonagemCatalogo;

@RestController
@RequestMapping("/api/personagens")
public class PersonagemController {
    private final PersonagemCatalogo personagemCatalogo;

    public PersonagemController(PersonagemCatalogo personagemCatalogo) {
        this.personagemCatalogo = personagemCatalogo;
    }

    @GetMapping
    public List<PersonagemDTO> listarPersonagens() {
        return personagemCatalogo.listar();
    }

    @GetMapping("/{tipo}")
    public PersonagemDTO buscarPorTipo(@PathVariable String tipo) {
        return personagemCatalogo.buscarPorTipo(tipo);
    }
}
