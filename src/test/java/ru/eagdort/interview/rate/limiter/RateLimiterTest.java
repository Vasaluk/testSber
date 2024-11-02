package ru.eagdort.interview.rate.limiter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;


/**
 * TODO Проверьте, что ваш лимитер корректно работает.
 * <p>
 * Предполагается, что есть какое-то заданное количество потоков-обработчиков, которые выполняют задачу {@link PrintTask}:
 * <p>
 * Если не пользоваться лимитером, то каждый поток будет выводить на экран по 10 записей в секунду
 * <p>
 * Требуется ограничить общий вывод с помощью оконного лимитера,
 * чтобы количество записей на экране не превышало заданное лимитером.
 * <p>
 * Для этого реализуйте {@link RateLimiter}.accept() в классе {@link WindowRateLimiter}
 */
class RateLimiterTest {

    private static final int rate = 5;

    private static final RateLimiter limiter = new WindowRateLimiter(rate);

    private final int nTasks = 10;

    private final ExecutorService executorService  = Executors.newFixedThreadPool(nTasks);

    private final LocalTime startTime = LocalTime.now();
    private final LocalTime finishTime = startTime.plus(5, ChronoUnit.SECONDS);
    private static int delta;

    @AfterEach
    void tearDown() {
        executorService.shutdownNow();
    }


    //TODO Напишите проверку, что вывод в каждую секунду не превышает заданное число операций (rate).
    @Test
    void shouldLimitNumberOfOutputEventsPerSecondToRateValueWhenWindowRateLimiterIsApplied() throws InterruptedException {

        //временные рамки для теста: 5 секунд
//        LocalTime startTime = LocalTime.now();
//        LocalTime finishTime = startTime.plus(5, ChronoUnit.SECONDS);

        System.out.println("start at " + startTime);
        System.out.println("finish at " + finishTime);
        System.out.println("--------------------");

        //Запускаем задания, каждое в своем потоке
        IntStream.range(0, nTasks)
                .mapToObj(i -> UUID.randomUUID().toString())
                .map(PrintTask::create)
                .map(Executors::callable)
                .forEach(executorService::submit);

        //Данный блок кода помогает визуализировать, сколько операций было выполнено в секунду
        while (LocalTime.now().isBefore(finishTime)) {
            Thread.sleep(1000);

            delta = LocalTime.now().getSecond() - startTime.getSecond();
            System.out.println("--------------------------  [Seconds passed: " + delta + "] -----------------------------");
        }

        //Your assertions...
        // потестить консоль к сожалению уже не могу(устала), полагаю через System.out

    }

    //TODO Напишите тест для случая rate = 0
    @Test
    void shouldBlockAllOutputEventsWhenRateEqualsZero() {

    }

    /**
     * Задание по выводу на экран заданной строки каждые 100 млсек (10 раз в секунду)
     * TODO модифицируйте данный класс под требования задачи
     */
    static class PrintTask implements Runnable {

        private final String name;

        private PrintTask(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            int attempt = 0;

            LocalTime startTime = LocalTime.now();

            while (!Thread.currentThread().isInterrupted()) {
                attempt++;

                try {
                    Thread.sleep(100);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

                long fromStart = Duration.between(startTime, LocalTime.now()).toMillis();
                if (limiter.accept(delta)) {
                    System.out.println(name + ": Print attempt number: " + attempt + ". Time passed after start [ms]: " + fromStart);
                }
            }
        }

        static PrintTask create(String name) {
            return new PrintTask("Task: " + name);
        }
    }
}
