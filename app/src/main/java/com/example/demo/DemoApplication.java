package com.example.demo;

import com.example.demo.handler.ex1.BasicHandler;
import com.example.demo.handler.ex2.MaxFrequencyHandler;
import com.example.demo.handler.ex3.SchemaHandler;
import com.example.demo.handler.ex4.DeltaDeliveryHandler;
import com.example.demo.handler.ex5.PositionProtocolHandler;
import com.example.demo.handler.ex6.BandwidthControlHandler;
import com.example.demo.handler.ex7.BandwidthLimitPerUserHandler;
import com.exchange.IPricingClient;
import com.exchange.impl.RandomPriceGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeFailureException;
import org.springframework.web.socket.server.HandshakeHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.standard.TomcatRequestUpgradeStrategy;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.util.Map;

@Configuration
@EnableWebSocket
@SpringBootApplication
public class DemoApplication implements WebSocketConfigurer {

	@Autowired
	private IPricingClient client;

	@Autowired
	private BasicHandler basic;

    @Autowired
    private MaxFrequencyHandler maxFrequencyHandler;

    @Autowired
    private SchemaHandler schemaHandler;

    @Autowired
    private DeltaDeliveryHandler deltaDeliveryHandler;

    @Autowired
    private PositionProtocolHandler positionProtocolHandler;

    @Autowired
    private BandwidthControlHandler bandwidthControlHandler;

    @Autowired
    private BandwidthLimitPerUserHandler bandwidthLimitPerUserHandler;

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry r) {
        r.addHandler(basic, "/ws/basic").setAllowedOrigins("*");
		r.addHandler(maxFrequencyHandler,     "/ws/maxFrequency").setAllowedOrigins("*");
        r.addHandler(schemaHandler,           "/ws/schema").setAllowedOrigins("*");
        r.addHandler(deltaDeliveryHandler,   "/ws/deltaDelivery").setAllowedOrigins("*");
        r.addHandler(positionProtocolHandler, "/ws/position").setAllowedOrigins("*");
		r.addHandler(bandwidthControlHandler, "/ws/bandwidth").setAllowedOrigins("*");
        r.addHandler(bandwidthLimitPerUserHandler, "/ws/bandwidthPerUser").setAllowedOrigins("*");
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public IPricingClient client() {
		return new RandomPriceGenerator(10);
	}
}