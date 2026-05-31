# ADR 0001 - Direct Blob Upload

## Context
Lacksfer currently uploads files with a single direct PUT to blob Storage.
This works for small and medium files. However, it is not the right model for very large files (target 70GB files upload simultaneously)
## Decision
Use Azure Block Blob upload flow:

1. create a pending transfer
2. generate a write SAS URL
3. split the file into fixed-size blocks
4. upload each block with `Put Block`
5. commit all uploaded blocks with `Put Block List`
6. mark the transfer as ready

## Consequences

- block size: 8 MiB
- parallel uploads: 3
- block id format: base64 encoded zero-padded index
- resume storage: browser local storage first, IndexedDB later

## Future Improvements

- configurable block size
- IndexedDB resume state
- checksum validation
- cancel upload
- direct download SAS