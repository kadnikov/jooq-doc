package ru.doccloud.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.cache.HazelcastCacheManager;

@Configuration
@EnableCaching
public class CachingConfiguration extends CachingConfigurerSupport {
	@Bean(name = "springCM")
	@Override
    public CacheManager cacheManager() {
        ClientConfig config = new ClientConfig(); 
        ClientNetworkConfig networkConfig = config.getNetworkConfig();
        networkConfig.addAddress("doccloud.ru");
        networkConfig.setConnectionAttemptLimit(Integer.MAX_VALUE);
        networkConfig.setConnectionAttemptPeriod(10000);
        networkConfig.setConnectionTimeout(5000);
        
        HazelcastInstance client = HazelcastClient.newHazelcastClient(config);
        return new HazelcastCacheManager(client); 
    } 
    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return null;
    }
}
