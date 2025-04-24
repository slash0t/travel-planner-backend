package ru.putevod.app.travelplannerapp;

import org.springframework.boot.SpringApplication;

public class TestTravelPlannerAppApplication {

    public static void main(String[] args) {
        SpringApplication.from(TravelPlannerAppApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
