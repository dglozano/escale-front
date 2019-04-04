package com.dglozano.escale.web.dto;

public class ProfileImageDTO {

    private String uuid;
    private String fileName;
    private String fileType;
    private long size;

    public ProfileImageDTO() {
    }

    public ProfileImageDTO(String uuid, String fileName, String fileType, long size) {
        this.uuid = uuid;
        this.fileName = fileName;
        this.fileType = fileType;
        this.size = size;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
