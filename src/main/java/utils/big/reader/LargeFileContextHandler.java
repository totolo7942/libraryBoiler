package utils.big.reader;

import org.apache.commons.lang3.time.StopWatch;
import utils.big.reader.entity.NaverModelBO;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author a1101381
 */
public class LargeFileContextHandler implements CompletionHandler<Integer, BlockingQueue<Boolean>> {

    final int BLOCK_SIZE =1024;

    private ByteBuffer buffer;
    private AsynchronousFileChannel channel;
    private long position = 0;
    BlockingQueue<Boolean> done = new ArrayBlockingQueue<>(1);
    private StringBuilder builder = new StringBuilder();

    public void read(String fileName) throws IOException, InterruptedException, JAXBException, XMLStreamException {
        Path path = Paths.get(fileName);
//
        StopWatch stopWatch = new StopWatch();
        NaverEpParser epParser = new NaverEpParser();
        stopWatch.reset();
        stopWatch.start();

        List<NaverModelBO> modelBOList = epParser.staxParser(path);
        stopWatch.stop();
        System.out.println("Stax Processing : " + modelBOList.size());
        System.out.println("Stax parser : " + stopWatch);


        //async file reader
//        buffer = ByteBuffer.allocate(BLOCK_SIZE);
//        channel = AsynchronousFileChannel.open(path);
//        channel.read(buffer, position, done, this);
//        done.take();

    }

    public void legacyRead(String fileName) throws JAXBException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.reset();
        stopWatch.start();

        NaverEpParser epParser = new NaverEpParser();
        epParser.lineParser(fileName);

        stopWatch.stop();
        System.out.println("legacyRead parser :"+ stopWatch);

    }

    @Override
    public void completed(Integer result, BlockingQueue<Boolean> attachment) {
        if( result < 0 ) {
            try {
                attachment.put(true);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        position += result;
        buffer.flip();
        buffer.mark();

//        System.out.println(" " + position + " >" +Charset.defaultCharset().decode(buffer).toString());
        builder.append(Charset.defaultCharset().decode(buffer));

        buffer.clear();

        channel.read(buffer,position, done, this);
    }

    @Override
    public void failed(Throwable exc, BlockingQueue<Boolean> attachment) {
        try {
            done.put(false);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            closeAsyncFileChannel(channel);
        }
    }

    private void closeAsyncFileChannel(AsynchronousFileChannel asyncFileChannel) {
        if (asyncFileChannel != null && asyncFileChannel.isOpen()) {
            try {
                asyncFileChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }
}

