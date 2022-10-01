package gui.other;


public class NHMISignal{

    private final String name;
    private final Attribute<?> dataX, dataY;

    public NHMISignal(String name, Attribute<?> data) { this.name = name; this.dataX = null; this.dataY = data; }
    public NHMISignal(String name, Attribute<?> dataX, Attribute<?> dataY) { this.name = name; this.dataX = dataX; this.dataY = dataY; }

    public String getName() { return name; }
    public Attribute<?> getDataX() { return dataX; }
    public Attribute<?> getDataY() { return dataY; }
}
