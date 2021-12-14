package utils.big.reader.defUtils;

public enum ByteTypes {
    KILO_BYTE(1024), MEGA_BYTE(1024 * 1024), GIGA_BYTE(1024 * 1024 * 1024);

    final long value;
    ByteTypes(int v) {
        this.value = v;
    }

    public long toByteValue(int defByte) {
        return this.value * defByte;
    }
}
