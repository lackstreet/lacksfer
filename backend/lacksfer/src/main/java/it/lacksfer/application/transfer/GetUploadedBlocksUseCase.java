package it.lacksfer.application.transfer;

import it.lacksfer.application.transfer.block.BlockIdCodec;
import it.lacksfer.application.transfer.result.GetUploadedBlocksResult;
import it.lacksfer.domain.exception.TransferExpiredException;
import it.lacksfer.domain.exception.TransferNotFoundException;
import it.lacksfer.domain.transfer.Transfer;
import it.lacksfer.ports.out.FileStoragePort;
import it.lacksfer.ports.out.TransferRepositoryPort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class GetUploadedBlocksUseCase {
    private final TransferRepositoryPort transferRepositoryPort;
    private final FileStoragePort fileStoragePort;
    private final BlockIdCodec blockIdCodec;

    public GetUploadedBlocksUseCase(TransferRepositoryPort transferRepositoryPort, FileStoragePort fileStoragePort, BlockIdCodec blockIdCodec) {
        this.transferRepositoryPort = transferRepositoryPort;
        this.fileStoragePort = fileStoragePort;
        this.blockIdCodec = blockIdCodec;
    }



    public GetUploadedBlocksResult execute(UUID transferId) {

        if (transferId == null) {
            throw new IllegalArgumentException("transferId is required");
        }

        Transfer transfer = transferRepositoryPort.findTransferById(transferId).orElseThrow(
                () -> new TransferNotFoundException("Transfer not found")
        );

        if (transfer.isExpired()) {
            throw new TransferExpiredException("Transfer expired");
        }

        if (transfer.isReady()) {
            throw new IllegalStateException("Transfer is already ready");
        }

        List<Integer> uploadedBlockIndexes = fileStoragePort.listUncommittedBlockIds(transfer.getBlobName())
                .stream()
                .map(blockIdCodec::decode)
                .sorted()
                .toList();

        return new GetUploadedBlocksResult(
                transfer.getId(),
                uploadedBlockIndexes
        );
    }
}
