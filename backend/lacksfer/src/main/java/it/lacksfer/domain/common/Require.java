package it.lacksfer.domain.common;

public final class Require {
    public static void notBlank(String value, String fieldName){
        if(value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is null or blank");
        }
    }
    public static void notNull(Object value, String fieldName) {
        if(value == null) {
            throw new IllegalArgumentException(fieldName + " is null");
        }
    }

    public static void positive(long value, String fieldName) {
        if(value <= 0) {
            throw new IllegalArgumentException(fieldName + " is not a valid positive number");
        }
    }

}
