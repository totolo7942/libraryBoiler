package utils.big.reader.defUtils;

import lombok.Getter;

@Getter
public class EpCreateFileUtils {

    private final int fileSequence;

    public EpCreateFileUtils(EpCreateFileUtilsBuilder builder) {
        this.fileSequence = builder.fileSequence;
    }

    public static EpCreateFileUtilsBuilder builder(){
        return new EpCreateFileUtilsBuilder();
    }

    @Override
    public String toString() {
        return "EpCreateFileUtils{" +
                "fileSequence=" + fileSequence +
                '}';
    }

    public static class EpCreateFileUtilsBuilder {
        private int fileSequence;

        EpCreateFileUtilsBuilder(){}

        public EpCreateFileUtilsBuilder fileSeq(int extFileName) {
            this.fileSequence = fileSequence;
            return this;
        }

        public EpCreateFileUtils build() {
            return new EpCreateFileUtils(this);
        }
    }
}
