package org.jumpmind.pos.print;

import lombok.Data;

import java.util.Map;

@Data
public class PrinterConfig {
    Map<String, Object> settings;
    String styleSheet;
    boolean pingEnabled = true;
}
