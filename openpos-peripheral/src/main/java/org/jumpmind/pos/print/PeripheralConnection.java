package org.jumpmind.pos.print;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PeripheralConnection {

    OutputStream out;  // write to printer.
    InputStream in;  // read status, etc. from printer.
    Object rawConnection;

    public void close() {
        try {
            in.close();
            out.close();
        } catch (IOException e) {
            log.info("Failed to close peripheral connection", e);
        }
    }

}
