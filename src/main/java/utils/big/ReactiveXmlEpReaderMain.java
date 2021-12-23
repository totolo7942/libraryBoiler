package utils.big;

import org.apache.commons.lang3.time.StopWatch;
import utils.big.reader.XmlFileContextHandler;
import utils.big.reader.defUtils.EpOperation;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReactiveXmlEpReaderMain {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ReactiveXmlEpReaderMain.class);

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {

        warmUpCleanDirectoryFiles();

        Stream<Path> files = readFileLists();

        List<Path> fNames = files.distinct().collect(Collectors.toList());
//        Flowable.fromIterable(fNames)
//                .subscribeOn(Schedulers.newThread())
//                .blockingSubscribe( data -> {
//                    System.out.println(Thread.currentThread().getId() + " " + data.toFile().getName());
////                            new XmlFileContextHandler().read(data.toFile().getAbsolutePath(), true, EpOperation.PARSING);
//                        }
//                );

        StopWatch stopWatch = new StopWatch();
        stopWatch.reset();
        stopWatch.start();

//        Flowable.fromIterable(fNames)
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(Schedulers.newThread())
//                .subscribe(s -> new XmlFileContextHandler().read(s.toFile().getAbsolutePath(), true, EpOperation.PARSING));


        List<CompletableFuture<String>> futures = new ArrayList<>();
        for(Path p : fNames) {
            CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        new XmlFileContextHandler().read(p.toFile().getAbsolutePath(), true, EpOperation.PARSING);
                    } catch (IOException | InterruptedException | XMLStreamException e) {
                        e.printStackTrace();
                    }
                    return null;
                });
            futures.add(completableFuture);
        };

        futures.forEach(CompletableFuture::join);

        stopWatch.stop();
        System.out.println("ALL DONE : " + stopWatch);
    }

    public static <T> CompletableFuture<List<T>> allOf(List<CompletableFuture<T>> futuresList) {
        CompletableFuture<Void> allFuturesResult = CompletableFuture.allOf(new CompletableFuture[futuresList.size()]);
        return allFuturesResult.thenApplyAsync(v ->
                futuresList.stream().map(CompletableFuture::join).collect(Collectors.<T>toList())
        );
    }

    private static final String TARGET_DIRE="/Users/a1101381/naver_data/parse";
    private static void warmUpCleanDirectoryFiles() throws IOException {
        Path path = Path.of(TARGET_DIRE);
        Stream<Path> fstream = Files.list(path);
        fstream.forEach( r -> {
            try {
                System.out.println("warmup : delete file " + r);
                Files.delete(r);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        Files.createDirectories(path);
    }

    private static Stream<Path> readFileLists() throws IOException {
        Path path = Path.of("/Users/a1101381/naver_data/ndata/");
        return Files.list(path);
    }

//    public static void main(String[] args) {
//        System.out.println("# start : " + TimeUtil.getCurrentTimeFormatted());
//        Flowable.interval(300L, TimeUnit.MILLISECONDS)
//                .doOnNext(data -> Logger.log("#inverval doOnNext()", data))
//                .onBackpressureBuffer(
//                        2,
//                        () -> Logger.log("overflow!"),
//                        BackpressureOverflowStrategy.DROP_LATEST)
//                .doOnNext(data -> Logger.log("#onBackpressureBuffer doOnNext()", data))
//                .observeOn(Schedulers.computation(), false, 1)
//                .subscribe(
//                        data -> {
//                            TimeUtil.sleep(1000L);
//                            Logger.log(LogType.ON_NEXT, data);
//                        },
//                        error -> Logger.log(LogType.ON_ERROR, error)
//                );
//
//        TimeUtil.sleep(2800L);
//    }
}
