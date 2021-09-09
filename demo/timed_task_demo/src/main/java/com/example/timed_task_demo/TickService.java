package com.example.timed_task_demo;

import org.springframework.stereotype.Service;

@Service
public class TickService {
    public void tick() {
        System.out.println("学习使我快乐");
    }

    public long getDelay() {
        return 1000;
    }
}
