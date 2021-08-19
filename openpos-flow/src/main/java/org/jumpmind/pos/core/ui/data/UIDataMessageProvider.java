package org.jumpmind.pos.core.ui.data;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Random;

@Data
public abstract class UIDataMessageProvider<T> {

    private String providerKey;
    @Getter
    @Setter
    private int seriesId = new Random().nextInt();
    private boolean newSeries;

    abstract public List<T> getNextDataChunk();
    abstract public void reset();
}
