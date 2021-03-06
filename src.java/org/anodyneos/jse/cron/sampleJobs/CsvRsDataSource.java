package org.anodyneos.jse.cron.sampleJobs;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.activation.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author jvas
 */
public class CsvRsDataSource implements DataSource {

    private ResultSet rs;
    private String name;
    private List<String> cols;
    private boolean closeRs;
    private String mimeType;
    private String charset;
    private String compression = CsvRsInputStream.NO_COMPRESSION;

    private boolean alreadyUsed = false;

    public static final Log log = LogFactory.getLog(CsvRsDataSource.class);

    public CsvRsDataSource(ResultSet rs, boolean closeRs, String name, String mimeType, String charset,
            String compression, List<String> cols) {
        this.rs = rs;
        //this.query = query;
        this.name = name;
        this.cols = cols;
        this.closeRs=closeRs;
        this.mimeType = mimeType;
        this.charset = charset;
        this.compression = compression;
    }

    @Override
    public String getContentType() {
        String contentType;
        if (CsvRsInputStream.GZIP_COMPRESSION.equals(compression)) {
            return "application/x-gzip";
        } else if (CsvRsInputStream.ZIP_COMPRESSION.equals(compression)) {
            return "application/zip";
        } else {
            if (null == mimeType && null == charset) {
                contentType = "text/plain";
            } else if (null == charset) {
                contentType = mimeType;
            } else {
                contentType = mimeType + "; charset=" + charset;
            }
        }
        return contentType;
    }

    @Override
    public String getName() {
        if (CsvRsInputStream.GZIP_COMPRESSION.equals(compression)) {
            return name + ".gz";
        } else if (CsvRsInputStream.ZIP_COMPRESSION.equals(compression)) {
            return name + ".zip";
        } else {
            return name;
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("getInputStream() called.");
        }
        if (alreadyUsed) {
            throw new IllegalStateException("Sorry, can only call getInputStream() once for this datasource");
        }
        alreadyUsed = true;

        try {
            CsvRsInputStream is = new CsvRsInputStream(rs, cols, closeRs, charset, compression, name);
            return new BufferedInputStream(is, 8192);
        } catch (SQLException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException("Readonly DataSource");
    }

}
