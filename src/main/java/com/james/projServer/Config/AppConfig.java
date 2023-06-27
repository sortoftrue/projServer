package com.james.projServer.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

@Configuration
public class AppConfig {
    
    // @Value("${DO_STORAGE_KEY}")
   private String accessKey = "DO004YNQCV8DM2KUHBMK"; 

//    @Value("${DO_STORAGE_SECRETKEY}")
   private String secretKey ="mBckTWmh9kWjR+YWDmmdCnyqTmroKKbmqHfh4KJg7d8"; 

//    @Value("${DO_STORAGE_ENDPOINT}")
//    private String endPoint = "https://james.sgp1.digitaloceanspaces.com"; 

//    @Value("${DO_STORAGE_ENDPOINT_REGION}")
//    private String endPointRegion;

    @Bean
    public AmazonS3 createS3Client(){
        BasicAWSCredentials cred =
                new BasicAWSCredentials(accessKey, secretKey);
        EndpointConfiguration ep = 
                new EndpointConfiguration("sgp1.digitaloceanspaces.com", "sgp1");

        return AmazonS3ClientBuilder.standard()
            .withEndpointConfiguration(ep)
            .withCredentials(new AWSStaticCredentialsProvider(cred))
            .build();
   }

   //<<<<<<<<<<<<<<<<<<<<<REDIS STUFF>>>>>>>>>>>>>>>>>>>>>

   @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Value("${spring.redis.username}")
    private String redisUsername;

    @Value("${spring.redis.password}")
    private String redisPassword;

    @Bean @Primary
    public RedisTemplate<String, String> createRedisTemplate() {

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(
                redisHost, redisPort);
        config.setDatabase(0);

            config.setUsername(redisUsername);
            config.setPassword(redisPassword);

        // System.out.println(redisPort + redisHost + redisUsername + redisPassword);

        JedisClientConfiguration jedisClient = JedisClientConfiguration
                .builder().build();
        JedisConnectionFactory jedisFac = new JedisConnectionFactory(config, jedisClient);
        jedisFac.afterPropertiesSet();

        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisFac);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());

        return template;
    };



}
