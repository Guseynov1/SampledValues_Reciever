package gui.other;

import lombok.Data;

@Data
public class Attribute<T> {

    private T value;

    public Attribute(T value) {  this.value = value;  }

}
