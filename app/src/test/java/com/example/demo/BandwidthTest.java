package com.example.demo;

import com.exchange.IPricingListener;
import com.exchange.impl.RandomPriceGenerator;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

import static com.example.demo.MessageUtil.toLowerCase;

public class BandwidthTest {

    public static void main(String[] args) throws InterruptedException, IOException {
        RandomPriceGenerator gen = new RandomPriceGenerator(1);
        BasicListener basic = new BasicListener();
        SnapshotUpdateListener snapshotUpdate = new SnapshotUpdateListener();
        PositionProtocolListener position = new PositionProtocolListener();
        gen.addListener(basic);
        gen.addListener(snapshotUpdate);
        gen.addListener(position);

        gen.subscribe("STOCK_AAPL");

        gen.start();
        TimeUnit.SECONDS.sleep(10);
        gen.shutdown();

        basic.flush();
        snapshotUpdate.flush();
        position.flush();
    }

    static class PositionProtocolListener extends SnapshotUpdateListener {

        private final List<String> schema = new ArrayList<>();
        {
            schema.add("symbol");
            schema.add("seqnum");
            schema.add("timestamp");
            schema.add("last");
            schema.add("bid");
            schema.add("ask");
            schema.add("bidsize");
            schema.add("asksize");
            schema.add("last");
        }

        @Override
        protected void send(Map<String, String> data) {
            writer.write(MessageUtil.toPositionBased(data, schema));
        }

        @Override
        public void flush() throws IOException {
            writer.flush(new File("target/position.txt"));
        }
    }

    static class SnapshotUpdateListener implements IPricingListener {

        private Map<String, Map<String, String>> snapshots = new ConcurrentHashMap<>();
        protected MyBufferedWriter writer = new MyBufferedWriter();

        @Override
        public void onData(String symbol, Map<String, String> data) {
            toLowerCase(data);

            Map<String, String> snapshot = snapshots.putIfAbsent(symbol, data);
            if (snapshot == null) {
                data.put("symbol", symbol);
                send(data);
            } else {
                Map<String, String> toSend = new HashMap<>();
                snapshot.forEach((k, v) -> {
                    if (!data.containsKey(k) || !data.get(k).equals(v)) {
                        toSend.put(k, v);
                    }
                });

                snapshot.putAll(data);
                toSend.put("symbol", symbol);
                send(toSend);
            }
        }

        protected void send(Map<String, String> data) {
            writer.write(MessageUtil.toJson(data));
        }

        public void flush() throws IOException {
            writer.flush(new File("target/snapshotUpdate.txt"));
        }
    }

    static class BasicListener implements IPricingListener {

        private MyBufferedWriter writer = new MyBufferedWriter();

        @Override
        public void onData(String symbol, Map<String, String> data) {
            data.put("symbol", symbol);
            toLowerCase(data);
            writer.write(MessageUtil.toJson(data));
        }

        public void flush() throws IOException {
            writer.flush(new File("target/basic.txt"));
        }
    }

    static class MyBufferedWriter {

        private Queue<String> buffer = new ConcurrentLinkedDeque<>();

        public void write(String data) {
            buffer.add(data);
        }

        public void flush(File path) throws IOException {
            if (path.exists()) {
                path.delete();
            }

            path.createNewFile();
            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(path)));
            buffer.forEach(s -> writer.println(s));
            writer.flush();
        }
    }
}