package io.sonocoin.sdk.Types;

import android.annotation.SuppressLint;
import android.provider.BaseColumns;

/**
 * Created by aantonov on 31.08.2017.
 */
public class Connection implements Comparable<Connection> {
    @SuppressWarnings("FieldCanBeLocal")
    private int id;
    private String addr;
    private boolean ssl;
    private int port;
    private String url;

    public Connection(int id, String addr, int port, boolean ssl, String url) {
        if ((port == 80) && ssl) {
            // Workaround
            port = 443;
        }
        this.id = id;
        this.addr = addr;
        this.port = port;
        this.ssl = ssl;
        this.url = url;
    }

    @Override
    public int compareTo(Connection o) {
        return addr.compareTo(o.addr);
    }

    /**
     * Table description (DDL) for SQLite
     */
    public static abstract class ConnectionEntry implements BaseColumns {
        public static final String TABLE_NAME = "connection";
        public static final String COLUMN_NAME_NULLABLE = null;
        public static final String COLUMN_NAME_ADDR = "addr";
        public static final String COLUMN_NAME_SSL = "ssl";
        public static final String COLUMN_NAME_PORT = "port";
        public static final String COLUMN_NAME_URL = "url";
    }

    public String getAddr() {
        return this.addr;
    }

    public String getUrl() {
        return this.url;
    }

    public int getPort() {
        return this.port;
    }

    public boolean getSSL() {
        return this.ssl;
    }

    public String _fullUrl = null;

    @SuppressLint("DefaultLocale")
    public String getFullUrl() {
        if (_fullUrl != null) {
            return _fullUrl;
        }
        String postfix = "";
        {
            int defaultPort = ssl ? 443 : 80;
            if (defaultPort != port) {
                postfix = String.format(":%d", port);
            }
        }
        _fullUrl = String.format("%s%s", url, postfix);
        return _fullUrl;
    }
}
