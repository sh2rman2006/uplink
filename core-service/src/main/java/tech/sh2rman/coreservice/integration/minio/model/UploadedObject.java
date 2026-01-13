package tech.sh2rman.coreservice.integration.minio.model;

import tech.sh2rman.coreservice.domain.chat.model.AttachmentType;

public record UploadedObject(
        String objectKey,
        AttachmentType attachmentType
) {}
