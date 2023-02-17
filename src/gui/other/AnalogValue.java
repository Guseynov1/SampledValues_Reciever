package gui.other;

import lombok.Data;

@Data
public class AnalogValue {
    private Attribute<Float> f = new Attribute<>(0f);

}
