package ru.eagdort.interview.rate.limiter;

import java.util.concurrent.TimeUnit;

/**
 * TODO Необходимо реализовать оконный лимитер, который:
 * <p>
 * 1. способен пропускать заданное (rate) количество TPS (операций в секунду)
 * <p>
 * 2. не пропускать операции при превышении rate, т.е. меджу двумя моментами времени,
 * * отстоящими друг от друга на секунду, не должно быть больше операций, чем rate
 * <p>
 * 3. должен работать в многопоточном режиме
 */
public class WindowRateLimiter implements RateLimiter {

    private final int rate;

    public WindowRateLimiter(int rate) {
        this.rate = rate;
    }

    @Override
    public boolean accept() {

        if (rate == 0) {
            return false; // Ничего не пропускаем
        }

        //.. Уour code
        return true;
    }

}
