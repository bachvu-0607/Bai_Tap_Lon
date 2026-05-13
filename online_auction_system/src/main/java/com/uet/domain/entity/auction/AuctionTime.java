package com.uet.domain.entity.auction;
import java.time.*;

import com.uet.domain.enums.AuctionStatus;
public class AuctionTime {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    public AuctionTime(LocalDateTime startTime, LocalDateTime endTime) {
        validate(startTime,endTime);
        this.startTime = startTime;
        this.endTime = endTime;
    }
    public void validate(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Thời gian bắt đầu và kết thúc không được null!");
        }
        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian bắt đầu!");
        }
        if (Duration.between(startTime, endTime).toMinutes() < 5) {
            throw new IllegalArgumentException("Thời gian đấu giá phải ít nhất 5 phút!");
        }
    }
    public void extendEndTime(long extraSeconds){
        this.endTime = this.endTime.plusSeconds(extraSeconds);
    }
    // Kiểm tra xem đấu giá đã bắt đầu chưa
    public boolean hasStarted(){
        return !LocalDateTime.now().isBefore(startTime);
    }
    // Kiểm tra xem đấu giá đã kết thúc chưa
    public boolean hasEnded(){
        return !LocalDateTime.now().isBefore(endTime);
    }
    public LocalDateTime getStartTime() {
        return startTime;
    }
    public LocalDateTime getEndTime() {
        return endTime;
    }
    // public synchronized void approveAuction(long delayMinutes){

    //     if (status != AuctionStatus.PENDING_APPROVAL) {
    //         throw new IllegalStateException("Chỉ có thể duyệt các đấu giá đang chờ duyệt!");
    //     }
    //     LocalDateTime newStart =LocalDateTime.now().plusMinutes(delayMinutes);

    //     this.auctionTime =new AuctionTime(newStart,auctionTime.getEndTime());

    //     this.status = AuctionStatus.OPEN;
    // }
}