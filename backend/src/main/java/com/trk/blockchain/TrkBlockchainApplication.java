package com.trk.blockchain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TrkBlockchainApplication {
    public static void main(String[] args) {
        SpringApplication.run(TrkBlockchainApplication.class, args);
    }
}
