package com.dglozano.escale.db.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.dglozano.escale.web.dto.DietDTO;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(foreignKeys = @ForeignKey(entity = Patient.class,
        parentColumns = "id",
        childColumns = "userId",
        onDelete = CASCADE))
public class Diet {

    @PrimaryKey
    @NonNull
    private String id;
    private Long userId;
    private String fileName;
    private Date startDate;
    private long size;
    private FileStatus fileStatus;

    public Diet() {

    }

    @Ignore
    public Diet(DietDTO dietDTO, Long userId) {
        this.id = dietDTO.getUuid();
        this.startDate = dietDTO.getStartDate();
        this.size = dietDTO.getSize();
        this.fileName = dietDTO.getFileName();
        this.userId = userId;
        this.fileStatus = FileStatus.NOT_DOWNLOADED;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public FileStatus getFileStatus() {
        return fileStatus;
    }

    public void setFileStatus(FileStatus fileStatus) {
        this.fileStatus = fileStatus;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public enum FileStatus {
        @SerializedName("1")
        DOWNLOADING,
        @SerializedName("2")
        DOWNLOADED,
        @SerializedName("3")
        NOT_DOWNLOADED
    }
}
