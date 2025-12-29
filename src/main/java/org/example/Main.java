package org.example;

import org.example.utils.SnowflakeIdGenerator;

public class Main {
    public static void main(String[] args) {
        SnowflakeIdGenerator snow = new SnowflakeIdGenerator();
        for(int i = 1; i <= 1000; i ++) {
            System.out.println(snow.nextId());
        }
    }
}