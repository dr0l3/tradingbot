package model.signals;

import model.Selector;

import java.time.temporal.ChronoUnit;

public class TrendSignal {
    private ChronoUnit timeUnit;
    private Integer timeAmount;
    private Double percentage;
    private Selector selector;
}
