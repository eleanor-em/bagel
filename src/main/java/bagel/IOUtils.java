package bagel;

import java.util.zip.InflaterInputStream;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

class IOUtils {
    private IOUtils() {
    }

    static ByteBuffer stringToByteBuffer(MemoryStack stack, String string) {
        ByteBuffer buffer = stack.malloc(string.length());
        byte[] bytes = string.getBytes();
        for (byte b : bytes) {
            buffer.put(b);
        }
        buffer.rewind();
        return buffer;
    }
    static ByteBuffer fileToByteBuffer(String filename) throws IOException {
        Path path = Paths.get(filename);
        ByteBuffer buffer;
        try (SeekableByteChannel fc = Files.newByteChannel(path)) {
            buffer = BufferUtils.createByteBuffer((int) fc.size() + 1);
            while (fc.read(buffer) != -1);
        }
        buffer.flip();
        return buffer;
    }

    static String readResource(String filename) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try (InputStream is = cl.getResourceAsStream(filename)) {
            if (is == null) {
                throw new BagelError("Missing resource file `" + filename + "`");
            }
            try (Scanner s = new Scanner(is)) {
                // Delimit by EOF
                s.useDelimiter("\\Z");
                return s.next();
            }
        } catch (IOException e) {
            throw new BagelError("Exception while loading resource `" + filename + "`: "
                                 + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public static byte[] gzipDecompress(byte[] compressed) throws IOException {
        try (GZIPInputStream decompressor = new GZIPInputStream(new ByteArrayInputStream(compressed));
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            int read;
            byte[] data = new byte[1024];
            while ((read = decompressor.read(data, 0, data.length)) > 0) {
                buffer.write(data, 0, read);
            }
            buffer.flush();
            return buffer.toByteArray();
        }
    }

    public static byte[] zlibDecompress(byte[] compressed) throws IOException{
        InflaterInputStream inStream = new InflaterInputStream(new ByteArrayInputStream( compressed));
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        int readByte;
        byte[] buf = new byte[1024];
        while((readByte = inStream.read(buf)) != -1) {
            outStream.write(buf, 0, readByte);
        }
        byte[] ret = outStream.toByteArray();
        outStream.close();
        return ret;
    }
}
