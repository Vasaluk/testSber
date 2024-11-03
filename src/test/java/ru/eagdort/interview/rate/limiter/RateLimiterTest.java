package ru.eagdort.interview.rate.limiter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    private static int delta;


    //TODO Напишите проверку, что вывод в каждую секунду не превышает заданное число операций (rate).
    @Test
    void shouldLimitNumberOfOutputEventsPerSecondToRateValueWhenWindowRateLimiterIsApplied() throws InterruptedException {
        //перенаправляет вывод в поток байт
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        //временные рамки для теста: 5 секунд
        LocalTime startTime = LocalTime.now();
        LocalTime finishTime = startTime.plus(5, ChronoUnit.SECONDS);

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

        executorService.shutdownNow();

        //Your assertions...
        for (int number : numbersEventsPerSecond(outputStream.toString())) {
            Assertions.assertTrue(number <= rate);
        }
    }

    /**
     * @return List возвращает кол-во операций в секундных промежутках
     */
    private List<Integer> numbersEventsPerSecond(String text) {
        String regex = "(--------------------------.*?)(?=--------------------------|$)";

        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);

        List<Integer> numbersEventsPerSecond = new ArrayList<>();

        while (matcher.find()) {
            String block = matcher.group();
            String[] lines = block.split("\n");

            int lineCount = 0;
            for (String line : lines) {
                if (line.startsWith("Task: ")) {
                    lineCount++;
                }
            }
            numbersEventsPerSecond.add(lineCount);
        }

        return numbersEventsPerSecond;
    }

    //TODO Напишите тест для случая rate = 0
    //Не потребуется - предыдущий тест покрывает этот случай
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
