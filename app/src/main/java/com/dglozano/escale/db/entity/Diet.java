package com.dglozano.escale.db.entity;

import com.dglozano.escale.util.Constants;
import com.dglozano.escale.util.MyFileUtils;
import com.dglozano.escale.web.dto.DietDTO;

import java.util.Date;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

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
    private MyFileUtils.FileStatus fileStatus;

    public Diet() {

    }

    @Ignore
    public Diet(Long patientId, @NonNull String uuid, String fileName, Date date, Long size) {
        this.id = uuid;
        this.fileName = fileName;
        this.startDate = date;
        this.size = size;
        this.userId = patientId;
        this.fileStatus = MyFileUtils.FileStatus.NOT_DOWNLOADED;
    }

    @Ignore
    public Diet(DietDTO dietDTO, Long userId) {
        this(userId, dietDTO.getUuid(), dietDTO.getFileName(), dietDTO.getStartDate(), dietDTO.getSize());
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

    public MyFileUtils.FileStatus getFileStatus() {
        return fileStatus;
    }

    public void setFileStatus(MyFileUtils.FileStatus fileStatus) {
        this.fileStatus = fileStatus;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getLocalFileName() {
        return this.id + this.fileName;
    }
}
