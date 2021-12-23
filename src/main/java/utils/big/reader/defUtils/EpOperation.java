package utils.big.reader.defUtils;

public enum EpOperation {
    PARSING(1), DIVIDER(2);

    private final long value;
    EpOperation(long v) {
        this.value = v;
    }

    public long toValue() {
        return this.value;
    }

}
