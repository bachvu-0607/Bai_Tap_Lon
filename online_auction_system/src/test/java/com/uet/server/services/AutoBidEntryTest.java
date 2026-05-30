package com.uet.server.services;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.PriorityQueue;

import com.uet.domain.entity.user.Bidder;

class AutoBidEntryTest {

    private Bidder makeBidder(String id) throws Exception {
        Bidder b = new Bidder(id, "C" + id, "Bidder " + id, "090" + id, "pw", "HN");
        b.deposit(10_000);
        return b;
    }

    // PriorityQueue phải ưu tiên entry có maxBid cao nhất lên đầu
    @Test
    void priorityQueue_HigherMaxBid_ComesFirst() throws Exception {
        Bidder b1 = makeBidder("1");
        Bidder b2 = makeBidder("2");

        AutoBidEntry low  = new AutoBidEntry(b1, 300, 10);
        AutoBidEntry high = new AutoBidEntry(b2, 500, 10);

        PriorityQueue<AutoBidEntry> queue = new PriorityQueue<>();
        queue.add(low);
        queue.add(high);

        // Entry có maxBid = 500 phải đứng đầu hàng đợi
        assertEquals(500, queue.peek().getMaxBid());
    }

    // Khi hai entry có maxBid bằng nhau, người đăng ký trước phải được ưu tiên
    @Test
    void priorityQueue_SameMaxBid_EarlierRegistrationComesFirst() throws Exception {
        Bidder b1 = makeBidder("1");
        Bidder b2 = makeBidder("2");

        AutoBidEntry first  = new AutoBidEntry(b1, 500, 10); // đăng ký trước
        Thread.sleep(5); // đảm bảo registeredAt khác nhau
        AutoBidEntry second = new AutoBidEntry(b2, 500, 10); // đăng ký sau

        PriorityQueue<AutoBidEntry> queue = new PriorityQueue<>();
        queue.add(second);
        queue.add(first);

        // b1 đăng ký trước → phải được poll trước
        assertEquals(b1.getId(), queue.poll().getBidderId());
        assertEquals(b2.getId(), queue.poll().getBidderId());
    }

    // Kiểm tra các getter trả về đúng giá trị khởi tạo
    @Test
    void getters_ReturnCorrectValues() throws Exception {
        Bidder b = makeBidder("X");
        AutoBidEntry entry = new AutoBidEntry(b, 750, 25);

        assertEquals(b.getId(), entry.getBidderId());
        assertEquals(b, entry.getBidder());
        assertEquals(750, entry.getMaxBid());
        assertEquals(25, entry.getIncrement());
        assertNotNull(entry.getRegisteredAt());
    }

    // Khi chỉ có một entry trong queue, poll() phải trả về đúng entry đó
    @Test
    void priorityQueue_SingleEntry_PollReturnsThatEntry() throws Exception {
        Bidder b = makeBidder("Z");
        AutoBidEntry entry = new AutoBidEntry(b, 200, 5);

        PriorityQueue<AutoBidEntry> queue = new PriorityQueue<>();
        queue.add(entry);

        assertSame(entry, queue.poll());
        assertTrue(queue.isEmpty());
    }
}
