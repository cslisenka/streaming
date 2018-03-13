package com.example.demo;

import com.example.demo.handler.ex1.BasicHandler;
import com.example.demo.handler.ex2.MaxFrequencyHandler;
import com.example.demo.handler.ex3.SchemaHandler;
import com.example.demo.handler.ex4.SnapshotUpdateHandler;
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
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

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
    private SnapshotUpdateHandler snapshotUpdateHandler;

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
	    r.addHandler(basic,                   "/ws/basic").setAllowedOrigins("*");
		r.addHandler(maxFrequencyHandler,     "/ws/maxFrequency").setAllowedOrigins("*");
        r.addHandler(schemaHandler,           "/ws/schema").setAllowedOrigins("*");
        r.addHandler(snapshotUpdateHandler,   "/ws/snapshotUpdate").setAllowedOrigins("*");
        r.addHandler(positionProtocolHandler, "/ws/position").setAllowedOrigins("*");
		r.addHandler(bandwidthControlHandler, "/ws/bandwidth").setAllowedOrigins("*");
        r.addHandler(bandwidthLimitPerUserHandler, "/ws/bandwidthPerUser").setAllowedOrigins("*");
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public IPricingClient client() {
		return new RandomPriceGenerator(10);
	}
}