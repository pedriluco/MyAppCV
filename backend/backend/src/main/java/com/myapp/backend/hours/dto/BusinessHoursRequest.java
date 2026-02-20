package com.myapp.backend.hours.dto;

import java.util.List;

public class BusinessHoursRequest {
    public List<Item> items;

    public static class Item {
        public Integer dayOfWeek;
        public String openTime;
        public String closeTime;
        public Boolean closed;
    }
}