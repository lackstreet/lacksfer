package it.lacksfer.application.transfer.result;

import it.lacksfer.adapters.out.storage.AzuriteFileStorageAdapter;
import it.lacksfer.application.transfer.block.BlockIdCodec;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class GetUploadedBlocksUseCase {
    private final AzuriteFileStorageAdapter azuriteFileStorageAdapter;

    public GetUploadedBlocksUseCase(AzuriteFileStorageAdapter azuriteFileStorageAdapter) {
        this.azuriteFileStorageAdapter = azuriteFileStorageAdapter;
    }

    public List<String> execute(String transferId) {

        return List.of();
    }
}
