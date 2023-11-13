package com.edison42.karate.grpcurl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public class GrpcurlClient {

    private final static String GRPCURL_CMD_PATH = "grpcurl";
    private final static String GPRC_URL_FORMAT = "%s:%d";
    private final String GPRC_URL;
    private final DefaultExecutor exec;
    ByteArrayOutputStream susStream;
    ByteArrayOutputStream errStream;

    boolean noTls = false;

    public GrpcurlClient(String host, int port) {
        this(host, port, false);
    }
    public GrpcurlClient(String host, int port, boolean noTls) {
        GPRC_URL = String.format(GPRC_URL_FORMAT, host, port);

        susStream = new ByteArrayOutputStream();
        errStream = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(susStream, errStream);
        exec = new DefaultExecutor();

        exec.setStreamHandler(streamHandler);

        this.noTls = noTls;
    }

    private CommandLine GetGrpcurlCmd() {
        CommandLine cmd = CommandLine.parse(GRPCURL_CMD_PATH);
        if (noTls) {
            cmd.addArgument("-plaintext");
        }
        return cmd;
    }

    public Collection<String> list() {
        CommandLine cmd = GetGrpcurlCmd();
        cmd.addArgument(GPRC_URL);
        cmd.addArgument("list");

        String result = invokeCmd(cmd);

        return Arrays.asList(result.split("\n"));
    }

    public Map<String, Object> call(String method, Map<String, Object> payload) {
        CommandLine cmd = GetGrpcurlCmd();
        cmd.addArgument("-d");
        cmd.addArgument(new Gson().toJson(payload), false);
        cmd.addArgument(GPRC_URL);
        cmd.addArgument(method);

        String result = invokeCmd(cmd);
        return new Gson().fromJson(result, new TypeToken<Map<String, Object>>(){}.getType());
    }

    private String invokeCmd(CommandLine cmd) {
        try {
            System.out.println(cmd.toString());
            susStream.reset();
            errStream.reset();

            int resultCode = exec.execute(cmd);
            if (resultCode == 0) {
                return susStream.toString(StandardCharsets.UTF_8);
            } else {
                throw new GrpcurlException(errStream.toString(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            throw new GrpcurlException(errStream.toString(StandardCharsets.UTF_8));
        }
    }
}
