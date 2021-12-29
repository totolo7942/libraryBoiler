import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.List;
import java.util.Optional;

public class JmhExample {
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JmhExample.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @Benchmark //JDK1.8 뿐이 지원하지 않는것 IntelliJ Plugin JMH l;;
    @BenchmarkMode(Mode.SampleTime)
    public static int sumInt() throws RunnerException {
        List<Integer> items = List.of(1,2,3,4,5,6,7);
        return items.stream()
                .reduce(0, Integer::sum);
    }

}
