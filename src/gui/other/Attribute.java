package gui.other;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Attribute<T> {
    private T value;
}
