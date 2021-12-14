package utils.big.reader.defUtils;

/**
 * @author a1101381
 */

public enum ByteTypes {
    KILO_BYTE(1024), MEGA_BYTE(1024 * 1024), GIGA_BYTE(1024 * 1024 * 1024), TERA_BYTE(1024L * 1024 * 1024 * 1024);

    private final long value;
    ByteTypes(long v) {
        this.value = v;
    }

    public long toByteValue(long defByte) {
        return this.value * defByte;
    }

    public long toValue() {
        return this.value;
    }
}
