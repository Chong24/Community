package com.nowcoder.community;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Deque;
import java.util.LinkedList;

@SpringBootTest
class CommunityApplicationTests {

	@Test
	void contextLoads() {
		Deque<Integer> deque = new LinkedList<>();
		Deque<Integer> deque1 = new LinkedList<>();

		deque.add(1);
		deque1.add(1);

		System.out.println(deque.peek() == deque1.peekFirst());
	}

}
