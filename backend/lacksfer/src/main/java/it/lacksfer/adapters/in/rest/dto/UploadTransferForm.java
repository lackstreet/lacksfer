package it.lacksfer.adapters.in.rest.dto;

import lombok.Getter;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.time.Instant;

public class UploadTransferForm {
    @RestForm("file")
    public FileUpload file;
    @RestForm("expiresAt")
    public Instant expiresAt;
}
