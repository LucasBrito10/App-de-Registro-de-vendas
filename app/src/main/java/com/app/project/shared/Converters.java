package com.app.project.shared;

import androidx.room.TypeConverter;
import java.math.BigDecimal;

public class Converters {
    @TypeConverter
    public static String fromBigDecimal(BigDecimal value) {
        return value == null ? null : value.toPlainString();
    }

    @TypeConverter
    public static BigDecimal toBigDecimal(String value) {
        return value == null ? null : new BigDecimal(value);
    }
}