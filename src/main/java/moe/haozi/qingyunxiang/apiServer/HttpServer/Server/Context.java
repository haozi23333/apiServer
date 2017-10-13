package moe.haozi.qingyunxiang.apiServer.HttpServer.Server;

import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class Context {
    public HttpExchange httpExchange;
    public HashMap<String, String> Query = new HashMap<>();
    public LinkedHashMap<String, String> Param = new LinkedHashMap<>();
    public URI url() {
        return this.httpExchange.getRequestURI();
    }
    public Context.HttpMethod method = HttpMethod.GET;
    public String rawReqBody = "";

    public static enum HttpMethod {
        GET,
        POST,
        DELETE,
        PUT,
    }

    public Context(HttpExchange httpExchange) {
        this.httpExchange = httpExchange;
        this.parseConext();
    }

    public Context statu(int code) throws IOException {
        this.httpExchange.sendResponseHeaders(code, 0);
        return  this;
    }

    public Context ContentType(String type) {
        this.httpExchange.getResponseHeaders().set("Content-Type", type);
        return this;

    }

    public void close() {
        this.httpExchange.close();
    }

    public Context write(int b) throws IOException {
        this.httpExchange.getResponseBody().write(b);
        return this;
    }
    public Context write(byte b[]) throws IOException {
        this.httpExchange.getResponseBody().write(b);
        return this;
    }
    public Context write(byte b[], int off, int len) throws IOException {
        this.httpExchange.getResponseBody().write(b, off, len);
        return this;
    }

    public void parseConext() {
        this.parseMethod()
                .parseQuery()
                .parseParma();
    }

    public Context parseMethod() {
        switch (this.httpExchange.getRequestMethod()) {
            case "GET":
                this.method = HttpMethod.GET;
                break;
            case "POST":
                this.method = HttpMethod.POST;
                break;
        }
        return this;
    }

    public Context parseQuery() {
        String querys = this.httpExchange.getRequestURI().getQuery();
        if (querys == null) {
            return this;
        }
        for(String query : querys.split("&")) {
            String[] kv = query.split("=");
            if (kv.length == 2 ) {
                try {
                    this.Query.put(decode(kv[0]), decode(kv[1]));
                } catch (Exception e) {
                    System.out.println("[Route -> Parma 转码错误无法将 ]" + kv[0]  + "转换编码为 -> " +  getEncoding());
                    e.printStackTrace();
                }
            }
        }
        return this;
    }

    public Context parseParma() {
        try {

            InputStreamReader inputStreamReader = new InputStreamReader(httpExchange.getRequestBody(), getEncoding());
            StringBuffer postbody = new StringBuffer();
            String line = "";
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            while ((line = bufferedReader.readLine()) != null) {
                postbody.append(line);
            }
            this.rawReqBody = postbody.toString();

            // 解析Body 拿值

        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    public String decode(String encodeString) throws UnsupportedEncodingException {
        return URLDecoder.decode(encodeString, getEncoding());
    }

    public String getEncoding() {
//        return  System.getProperty("file.encoding");
        return  "UTF-8";
    }
}
