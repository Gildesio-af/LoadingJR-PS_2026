package com.loandingjr.chat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor") // É exatamente esse nome que o aviso estava procurando!
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2); // Quantas threads ficam sempre ativas
        executor.setMaxPoolSize(5);  // Máximo de threads simultâneas se o servidor lotar
        executor.setQueueCapacity(50); // Se passarem de 5 requisições juntas, até 50 ficam na fila de espera
        executor.setThreadNamePrefix("IA-Async-"); // O nome bonitinho que vai aparecer no seu log
        executor.initialize();
        return executor;
    }
}
