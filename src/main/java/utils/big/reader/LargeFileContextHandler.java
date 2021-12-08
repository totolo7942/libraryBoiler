package utils.big.reader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    public void read(String fileName) throws IOException, InterruptedException {
        buffer = ByteBuffer.allocate(BLOCK_SIZE);

        Path path = Paths.get(fileName);
        channel = AsynchronousFileChannel.open(path);
        channel.read(buffer, position, done, this);

        System.err.println("AsyncFileChannel I/O 진행 중에는 다른 작업도 할 수 있지롱");
        System.err.println("그동안 그리스에도 다녀오고");
        System.err.println("크로아티아에도 갔다오자");

        done.take();

        System.out.println("============================== Done");
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
        System.out.println(" " + position + " >" +Charset.defaultCharset().decode(buffer));
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

