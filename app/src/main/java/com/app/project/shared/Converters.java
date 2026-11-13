package com.app.project.shared;

import androidx.room.TypeConverter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

// Ensina o Room a gravar no SQLite tipos que ele não conhece.
public class Converters {

    // BigDecimal <-> texto: preserva todas as casas decimais (REAL/double perderia precisão).
    @TypeConverter
    public static String fromBigDecimal(BigDecimal value) {
        return value == null ? null : value.toPlainString();
    }

    @TypeConverter
    public static BigDecimal toBigDecimal(String value) {
        return value == null ? null : new BigDecimal(value);
    }

    // LocalDate <-> texto ISO ("2026-11-13"): legível e ordena corretamente como texto,
    // então ORDER BY, < e BETWEEN funcionam direto no SQL.
    @TypeConverter
    public static String fromLocalDate(LocalDate value) {
        return value == null ? null : value.toString();
    }

    @TypeConverter
    public static LocalDate toLocalDate(String value) {
        return value == null ? null : LocalDate.parse(value);
    }

    // Instant <-> milissegundos desde 1970 (UTC).
    @TypeConverter
    public static Long fromInstant(Instant value) {
        return value == null ? null : value.toEpochMilli();
    }

    @TypeConverter
    public static Instant toInstant(Long value) {
        return value == null ? null : Instant.ofEpochMilli(value);
    }
}
