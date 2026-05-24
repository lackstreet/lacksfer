package it.lacksfer.adapters.out.storage;

import it.lacksfer.ports.out.BlobNameGeneratorPort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class UuidBlobNameGeneratorAdapter implements BlobNameGeneratorPort {
    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
