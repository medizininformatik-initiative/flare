package de.medizininformatikinitiative.flare.rest;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class IdGenerator {

    public UUID generateRandom() {
        return UUID.randomUUID();
    }
}
