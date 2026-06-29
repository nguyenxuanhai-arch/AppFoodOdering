package com.example.android_lab.utils;

import java.util.HashMap;
import java.util.Map;

public class StatusMapper {
    private static final Map<String, String> enToVi = new HashMap<>();
    private static final Map<String, String> viToEn = new HashMap<>();

    static {
        enToVi.put("pending", "Chờ xác nhận");
        enToVi.put("processing", "Đang xử lý");
        enToVi.put("completed", "Đã hoàn thành");
        enToVi.put("cancelled", "Đã hủy");

        // Tạo map ngược
        for (Map.Entry<String, String> entry : enToVi.entrySet()) {
            viToEn.put(entry.getValue(), entry.getKey());
        }
    }

    public static String toVietnamese(String statusEn) {
        return enToVi.getOrDefault(statusEn, statusEn);
    }

    public static String toEnglish(String statusVi) {
        return viToEn.getOrDefault(statusVi, statusVi);
    }
}
