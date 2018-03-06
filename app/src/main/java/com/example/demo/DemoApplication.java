package com.example.demo;

import com.example.demo.handler.*;
import com.example.demo.protocol.JSONProtocol;
import com.example.demo.protocol.PositionBasedProtocol;
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
    private MaxFrequencyHandler maxFrequencyHandler;

    @Autowired
    private SchemaHandler schemaHandler;

    @Autowired
    private SnapshotUpdateHandler snapshotUpdateHandler;

    @Autowired
    private PositionProtocolHandler positionProtocolHandler;

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry r) {
	    r.addHandler(new BasicHandler(client), "/ws/basic").setAllowedOrigins("*");
		r.addHandler(maxFrequencyHandler, "/ws/maxFrequency").setAllowedOrigins("*");
        r.addHandler(schemaHandler, "/ws/schema").setAllowedOrigins("*");
        r.addHandler(snapshotUpdateHandler, "/ws/snapshotUpdate").setAllowedOrigins("*");
        r.addHandler(positionProtocolHandler, "/ws/position").setAllowedOrigins("*");
        // TODO bandwidthControl
//		r.addHandler(bandwidthControlHandler, "/ws/bandwidth").setAllowedOrigins("*");
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public IPricingClient client() {
		return new RandomPriceGenerator(10);
	}
}