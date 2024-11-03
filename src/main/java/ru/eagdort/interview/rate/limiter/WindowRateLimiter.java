package ru.eagdort.interview.rate.limiter;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO Необходимо реализовать оконный лимитер, который:
 * <p>
 * 1. способен пропускать заданное (rate) количество TPS (операций в секунду)
 * <p>
 * 2. способен не пропускать операции при превышении rate, т.е. меджу двумя моментами времени,
 * * отстоящими друг от друга на секунду, не должно быть больше операций, чем rate
 * <p>
 * 3. должен работать в многопоточном режиме
 */
public class WindowRateLimiter implements RateLimiter {

    private final int rate;
    private final Map<Integer, Integer> mapCounts = new HashMap<>();

    public WindowRateLimiter(int rate) {
        this.rate = rate;
    }

    @Override
    public synchronized boolean accept(int currentSecond) {
        if (rate == 0) {
            return false; // Ничего не пропускаем
        }

        if (mapCounts.containsKey(currentSecond)) {
            mapCounts.put(currentSecond, mapCounts.get(currentSecond) + 1);
        } else {
            mapCounts.put(currentSecond, 0);
        }

        return mapCounts.get(currentSecond) < rate;
    }

}
