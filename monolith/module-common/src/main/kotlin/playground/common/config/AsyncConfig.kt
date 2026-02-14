package playground.common.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@Configuration
@EnableAsync
class AsyncConfig {

    @Bean(name = ["eventExecutor"])
    fun eventExecutor(): Executor {
        return ThreadPoolTaskExecutor().apply {
            corePoolSize = 50
            maxPoolSize = 100
            queueCapacity = 200
            setThreadNamePrefix("event-async-")
            setWaitForTasksToCompleteOnShutdown(true)
            setAwaitTerminationSeconds(60)
            initialize()
        }
    }
}
