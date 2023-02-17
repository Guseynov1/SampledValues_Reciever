package gui.other;

import lombok.Data;

@Data
public class NHMISignal{

    private final String name;
    private final Attribute<?> dataX, dataY;

    public NHMISignal(String name, Attribute<?> data) { this.name = name; this.dataX = null; this.dataY = data; }
    public NHMISignal(String name, Attribute<?> dataX, Attribute<?> dataY) { this.name = name; this.dataX = dataX; this.dataY = dataY; }

}
