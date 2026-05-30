package com.uet.domain.entity.auction;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import com.uet.domain.entity.item.Electronics;
import com.uet.domain.entity.user.Bidder;
import com.uet.domain.entity.user.Seller;
import com.uet.domain.enums.AuctionStatus;

class AuctionConcurrencyTest {

    private Auction runningAuction() {
        Electronics item = new Electronics("I1", "Laptop", 100);
        Seller seller = new Seller("S1", "C1", "Seller", "090", "pw", "HN");
        Auction auction = new Auction("A1", item, seller,
                LocalDateTime.now().minusMinutes(1),
                LocalDateTime.now().plusMinutes(10), 1);
        auction.updateStatus();
        return auction;
    }

    private Bidder bidder(String id, double balance) throws Exception {
        Bidder b = new Bidder(id, "C" + id, "Bidder " + id, "09" + id, "pw", "HN");
        b.deposit(balance);
        return b;
    }

    /**
     * Kiểm tra tính an toàn khi nhiều Bidder cùng đặt giá đồng thời.
     * Kỳ vọng: chỉ đúng một người thắng, không xảy ra lost update.
     */
    @Test
    void concurrentBids_OnlyOneWinner_NoLostUpdate() throws Exception {
        Auction auction = runningAuction();
        int threadCount = 10;
        List<Bidder> bidders = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            bidders.add(bidder("B" + i, 10_000));
        }

        CountDownLatch startLatch = new CountDownLatch(1); // chờ tất cả thread sẵn sàng
        CountDownLatch doneLatch  = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);

        // Mỗi bidder đặt giá tại một mức khác nhau (101..110)
        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            pool.submit(() -> {
                try {
                    startLatch.await(); // đợi tín hiệu xuất phát đồng thời
                    auction.placeBid(bidders.get(idx), 101 + idx);
                    successCount.incrementAndGet();
                } catch (Exception ignored) {
                    // Các bid thấp hơn bị từ chối là hành vi đúng
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // phát tín hiệu xuất phát
        doneLatch.await();      // đợi tất cả thread xong
        pool.shutdown();

        // Phải có ít nhất 1 bid thành công và chính xác chỉ 1 người thắng
        assertTrue(successCount.get() >= 1);
        assertNotNull(auction.getWinner());

        // currentMaxPrice phải khớp với giá của người thắng
        double expectedPrice = 101 + bidders.indexOf(auction.getWinner());
        assertEquals(expectedPrice, auction.getCurrentMaxPrice());
    }

    /**
     * Kiểm tra tổng lockedBalance không bao giờ vượt quá balance gốc
     * sau nhiều vòng đặt giá liên tiếp từ nhiều thread.
     */
    @Test
    void concurrentBids_LockedBalance_NeverExceedsTotalBalance() throws Exception {
        Auction auction = runningAuction();
        int threadCount = 5;
        List<Bidder> bidders = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            bidders.add(bidder("B" + i, 5_000));
        }

        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch done  = new CountDownLatch(threadCount);
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            pool.submit(() -> {
                try {
                    latch.await();
                    // Mỗi bidder thử đặt nhiều mức giá liên tiếp
                    for (int round = 0; round < 3; round++) {
                        try {
                            auction.placeBid(bidders.get(idx), 200 + idx * 10 + round);
                        } catch (Exception ignored) {}
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        latch.countDown();
        done.await();
        pool.shutdown();

        // Với mỗi bidder: lockedBalance phải nằm trong khoảng [0, balance]
        for (Bidder b : bidders) {
            assertTrue(b.getLockedBalance() >= 0,
                    b.getId() + " lockedBalance âm: " + b.getLockedBalance());
            assertTrue(b.getLockedBalance() <= b.getBalance(),
                    b.getId() + " lockedBalance vượt balance");
        }

        // Phiên vẫn phải RUNNING (không bị hỏng trạng thái)
        assertEquals(AuctionStatus.RUNNING, auction.getStatus());
    }
}
