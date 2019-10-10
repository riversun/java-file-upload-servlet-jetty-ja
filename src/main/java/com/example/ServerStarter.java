package com.example;

import javax.servlet.MultipartConfigElement;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * サーバー起動コード
 */
public class ServerStarter {

    public static void main(String[] args) {

        final int MAX_UPLOAD_SIZE_BYTES = 10 * 1024 * 1024;
        final int PORT = 8081;

        final ServletContextHandler servletHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletHandler.setMaxFormContentSize(MAX_UPLOAD_SIZE_BYTES);

        // アップロード処理用サーブレット
        final ServletHolder shUpload = servletHandler.addServlet(UploadServlet.class, "/upload");
        shUpload.setAsyncSupported(true);

        shUpload.getRegistration().setMultipartConfig(
                // String location, long maxFileSize,long maxRequestSize, int fileSizeThreshold
                new MultipartConfigElement("c:/temp", MAX_UPLOAD_SIZE_BYTES, MAX_UPLOAD_SIZE_BYTES * 2, (int) (MAX_UPLOAD_SIZE_BYTES / 2)));

        // 静的ページの設定
        final ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setResourceBase(System.getProperty("user.dir") + "/htdocs");
        resourceHandler.setDirectoriesListed(false);
        resourceHandler.setWelcomeFiles(new String[] { "index.html" });
        resourceHandler.setCacheControl("no-store,no-cache,must-revalidate");

        final HandlerList handlerList = new HandlerList();
        handlerList.addHandler(resourceHandler);
        handlerList.addHandler(servletHandler);

        final Server jettyServer = new Server();
        jettyServer.setHandler(handlerList);

        final HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setSendServerVersion(false);

        final HttpConnectionFactory httpConnFactory = new HttpConnectionFactory(httpConfig);
        final ServerConnector httpConnector = new ServerConnector(jettyServer, httpConnFactory);
        httpConnector.setPort(PORT);
        jettyServer.setConnectors(new Connector[] { httpConnector });

        try {
            System.out.println("サーバー開始 ポート:" + PORT);
            jettyServer.start();
            jettyServer.join();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
