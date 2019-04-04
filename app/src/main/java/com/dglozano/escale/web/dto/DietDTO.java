package com.dglozano.escale.web.dto;

import java.util.Date;

public class DietDTO {

    private String uuid;
    private String fileName;
    private String fileType;
    private Date startDate;
    private long size;

    public DietDTO() {
    }

    public DietDTO(String uuid, String fileName, String fileType, Date startDate, long size) {
        this.uuid = uuid;
        this.fileName = fileName;
        this.fileType = fileType;
        this.startDate = startDate;
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

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
