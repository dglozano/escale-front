package com.dglozano.escale.db.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.dglozano.escale.util.Constants;
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
    private int downloadId;

    public Diet() {

    }

    @Ignore
    public Diet(Long patientId, @NonNull String uuid, String fileName, Date date, Long size) {
        this.id = uuid;
        this.fileName = fileName;
        this.startDate = date;
        this.size = size;
        this.userId = patientId;
        this.fileStatus = FileStatus.NOT_DOWNLOADED;
        this.downloadId = -1;
    }

    @Ignore
    public Diet(DietDTO dietDTO, Long userId) {
        this(userId, dietDTO.getUuid(), dietDTO.getFileName(), dietDTO.getStartDate(), dietDTO.getSize());
    }

    public int getDownloadId() {
        return downloadId;
    }

    public void setDownloadId(int downloadId) {
        this.downloadId = downloadId;
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
        if(fileStatus.equals(FileStatus.NOT_DOWNLOADED))
            this.downloadId = -1;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getDownloadUri() {
        return Constants.BASE_HEROKU_URL + "diets/download/" + getId();
    }

    public String getLocalFileName() {
        return this.id + this.fileName;
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
