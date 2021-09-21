package com.example.timed_task_demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.text.DateFormat;
import java.util.Date;

@EnableAsync
@EnableScheduling
@SpringBootApplication
public class TimedTaskDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(TimedTaskDemoApplication.class, args);
    }

    //@Scheduled(fixedRate = 30*1000)
//    @Scheduled(cron = "0/10 * 7-22 * * ?")
    @Async("taskExecutor")
    public void relax_plan() {
        System.out.println(Thread.currentThread());
        System.out.println("人类需要休息，除非我不做人了" + DateFormat.getDateTimeInstance().format(new Date()));
    }

    @Scheduled(fixedRate = 10*1000)
//    @Scheduled(cron = "0 0/2 7-22 * * ?")
    public void learn_plan() {
        System.out.println("今天学习了吗？人类是有极限的，JOJO" + DateFormat.getDateTimeInstance().format(new Date()));
    }
}
