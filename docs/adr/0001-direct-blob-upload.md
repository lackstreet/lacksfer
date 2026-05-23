# ADR 0001 - Direct Blob Upload

## Context
The current MVP uppload file through the Quarkus backend
This is simple and work for small files but it does not scale for large files because of the size the backend has to processes and store the file in memory and this is not scalable for large files create a bottle leak in the backend
The project goal includes supporting large file transfers, initially targeting files up to 50 GB.
## Decision
Large file uploads will not be proxied through the backend.
The backend will create a transfer, store its metadata, and return a url for azure blob storage.
The frontend will upload the clob direcly on the blob storage
After the upload completes, the frontend will call the backend to mark the transfer as ready.
## Consequences
The backend will handle authorization, metadata, transfer state, expiration, and dowload token generation.
Blob storage will be used for file storage.
Transfer state will be stored in a database.
## Current MVP limitation
we can't upload files larger than 100 mb
## Target flow
1. Frontend sends file metadata to the backend.
2. Backend creates a pending transfer.
3. Backend returns a signed upload URL.
4. Frontend uploads directly to Blob Storage.
5. Frontend calls backend to complete the transfer.
6. Backend marks the transfer as ready.
7. Download uses the existing download token flow, later upgraded to signed download URLs.