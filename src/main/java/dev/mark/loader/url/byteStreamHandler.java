package dev.mark.loader.url;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Map;

public class byteStreamHandler extends URLStreamHandler {
    private final Map<String, byte[]> data;

    public byteStreamHandler(Map<String, byte[]> data) {
        this.data = data;
    }

    @Override
    protected URLConnection openConnection(URL url) {
        byte[] bytes = data.get(url.getPath());
        return bytes != null ? new ByteConnection(url, bytes) : null;
    }

    private static class ByteConnection extends URLConnection {
        private final byte[] bytes;

        ByteConnection(URL url, byte[] bytes) {
            super(url);
            this.bytes = bytes;
        }

        @Override
        public void connect() {}

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(bytes);
        }
    }
}