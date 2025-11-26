package com.zhp.flea_market;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class FleaMarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(FleaMarketApplication.class, args);
    }

}
