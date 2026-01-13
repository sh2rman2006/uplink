package tech.sh2rman.coreservice.integration.minio.model;

import tech.sh2rman.coreservice.domain.chat.model.AttachmentType;

import java.util.Set;


public enum AttachmentMime {
    IMAGE(Set.of("image/png", "image/jpeg", "image/jpg", "image/gif", "image/webp")),
    VIDEO(Set.of("video/mp4", "video/webm", "video/quicktime")),
    AUDIO(Set.of("audio/mpeg", "audio/mp3", "audio/ogg", "audio/wav", "audio/webm", "audio/aac", "audio/flac")),
    FILE(Set.of()),
    UNKNOWN(Set.of());

    private final Set<String> allowed;

    AttachmentMime(Set<String> allowed) {
        this.allowed = allowed;
    }

    public static String normalize(String mimeType) {
        if (mimeType == null) return null;
        String mt = mimeType.trim().toLowerCase();
        return mt.isEmpty() ? null : mt;
    }

    public static AttachmentMime classify(String mimeType) {
        String mt = normalize(mimeType);
        if (mt == null) return UNKNOWN;

        if (mt.startsWith("image/")) return IMAGE;
        if (mt.startsWith("video/")) return VIDEO;
        if (mt.startsWith("audio/")) return AUDIO;

        if (mt.contains("/")) return FILE;
        return UNKNOWN;
    }

    public boolean isAllowed(String mimeType) {
        String mt = normalize(mimeType);
        if (mt == null) return false;
        if (this == FILE) return true;
        if (this == UNKNOWN) return false;
        return allowed.contains(mt) || mt.startsWith(prefixOf(this));
    }

    private static String prefixOf(AttachmentMime m) {
        return switch (m) {
            case IMAGE -> "image/";
            case VIDEO -> "video/";
            case AUDIO -> "audio/";
            default -> "";
        };
    }

    public static AttachmentType classifyFileAttachmentType(
            String contentType,
            String originalFilename
    ) {
        String ct = normalize(contentType);
        String name = originalFilename == null ? "" : originalFilename.toLowerCase();

        // Архивы
        if ((ct != null && (
                ct.equals("application/zip") ||
                        ct.equals("application/x-zip-compressed") ||
                        ct.equals("application/x-7z-compressed") ||
                        ct.equals("application/x-rar-compressed") ||
                        ct.equals("application/vnd.rar") ||
                        ct.equals("application/gzip") ||
                        ct.equals("application/x-tar")
        )) ||
                name.endsWith(".zip") ||
                name.endsWith(".rar") ||
                name.endsWith(".7z") ||
                name.endsWith(".tar") ||
                name.endsWith(".gz") ||
                name.endsWith(".tgz")
        ) {
            return AttachmentType.ARCHIVE;
        }

        if ((ct != null && (
                ct.equals("application/pdf") ||
                        ct.equals("application/msword") ||
                        ct.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
                        ct.equals("application/vnd.ms-excel") ||
                        ct.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ||
                        ct.equals("application/vnd.ms-powerpoint") ||
                        ct.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation") ||
                        ct.equals("text/plain") ||
                        ct.equals("text/markdown") ||
                        ct.startsWith("text/")
        ))) {
            return AttachmentType.DOCUMENT;
        }

        return AttachmentType.OTHER;
    }
}

