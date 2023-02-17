package gui.other;

/**
 * @description Выборка для построения графика ХУ
 */

import lombok.AllArgsConstructor;
import lombok.Data;

/** Пара значений X и Y */
@Data
@AllArgsConstructor
public class NHMIPoint<X, Y> {
    private final X value1;
    private final Y value2;
}
