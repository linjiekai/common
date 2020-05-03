package com.xfhl.common.api.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.support.NullValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.io.IOException;


@Configuration
public class RedisConfig {

    @Bean("redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate redisTemplate = new StringRedisTemplate(redisConnectionFactory);
        GenericJackson2JsonRedisSerializerEx genericJackson2JsonRedisSerializerEx = new GenericJackson2JsonRedisSerializerEx();
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        redisTemplate.setValueSerializer(genericJackson2JsonRedisSerializerEx);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setHashValueSerializer(genericJackson2JsonRedisSerializerEx);
        redisTemplate.setDefaultSerializer(genericJackson2JsonRedisSerializerEx);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    /**
     * 支付事务
     * @param redisConnectionFactory
     * @return
     */
    @Bean(name = "redisTemplateTransaction")
    public RedisTemplate<String, Object> redisTemplateTransaction(
            RedisConnectionFactory redisConnectionFactory) {

        RedisTemplate redisTemplate = new StringRedisTemplate(redisConnectionFactory);

        GenericJackson2JsonRedisSerializerEx genericJackson2JsonRedisSerializerEx = new GenericJackson2JsonRedisSerializerEx();
        redisTemplate.setValueSerializer(genericJackson2JsonRedisSerializerEx);
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(genericJackson2JsonRedisSerializerEx);
        redisTemplate.setDefaultSerializer(genericJackson2JsonRedisSerializerEx);
        redisTemplate.afterPropertiesSet();
        redisTemplate.setEnableTransactionSupport(true);
        return redisTemplate;
    }

    @Bean
    @ConditionalOnMissingBean(StringRedisTemplate.class)
    public StringRedisTemplate stringRedisTemplate(
            RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    class GenericJackson2JsonRedisSerializerEx implements RedisSerializer<Object> {

        protected GenericJackson2JsonRedisSerializer serializer = null;

        public GenericJackson2JsonRedisSerializerEx() {
            ObjectMapper om = new ObjectMapper();
            om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            om.registerModule(new JavaTimeModule());
            om.registerModule(new Jdk8Module());
            om.registerModule((new SimpleModule())
                    .addSerializer(new NullValueSerializer()));
            om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
            om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            this.serializer = new GenericJackson2JsonRedisSerializer(om);
        }

        public GenericJackson2JsonRedisSerializerEx(ObjectMapper om) {
            this.serializer = new GenericJackson2JsonRedisSerializer(om);
        }

        @Override
        public byte[] serialize(Object o) throws SerializationException {
            return serializer.serialize(o);
        }

        @Override
        public Object deserialize(byte[] bytes) throws SerializationException {
            return serializer.deserialize(bytes);
        }

        protected class NullValueSerializer extends StdSerializer<NullValue> {
            private static final long serialVersionUID = 1999052150548658807L;
            private final String classIdentifier="@class";

            NullValueSerializer() {
                super(NullValue.class);
            }

            public void serialize(NullValue value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                jgen.writeStartObject();
                jgen.writeStringField(this.classIdentifier, NullValue.class.getName());
                jgen.writeEndObject();
            }
        }
    }
}
