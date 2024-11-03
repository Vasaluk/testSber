package ru.eagdort.interview.rate.limiter;

public interface RateLimiter {

    /**
     *  Вызов данного метода предполагается перед вызовом кода,
     *  работу которого необходимо ограничивать во времени
     *
     * @return boolean должен вернуть true, если лимитер "пропускает" операцию
     */
    boolean accept(int currentSecond);
}
