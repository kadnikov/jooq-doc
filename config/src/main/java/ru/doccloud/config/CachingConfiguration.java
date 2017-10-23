package ru.doccloud.config;

import java.util.Arrays;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.support.SimpleCacheManager;
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
        networkConfig.addAddress("hazelcast");
        networkConfig.setConnectionAttemptLimit(5);
        networkConfig.setConnectionAttemptPeriod(10000);
        networkConfig.setConnectionTimeout(5000);
        try {
	        HazelcastInstance client = HazelcastClient.newHazelcastClient(config);
	        return new HazelcastCacheManager(client); 
        }catch (IllegalStateException e) {
        	SimpleCacheManager cacheManager = new SimpleCacheManager();
            cacheManager.setCaches(Arrays.asList(new ConcurrentMapCache("docsByType"), new ConcurrentMapCache("userByLoginAndPwd")));
            return cacheManager;
		}
    } 
    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return null;
    }
	@Override
	public CacheErrorHandler errorHandler() {
	     return new CacheErrorHandler() {
	            @Override
	            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
	                System.out.println("cache get error");
	            }

	            @Override
	            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
	                System.out.println("cache put error");
	            }

	            @Override
	            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
	                System.out.println("cache evict error");
	            }

	            @Override
	            public void handleCacheClearError(RuntimeException exception, Cache cache) {
	                System.out.println("cache clear error");
	            }
	     };
	}
    
}
