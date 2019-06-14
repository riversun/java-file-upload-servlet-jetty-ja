# Overview

## Java Servletでファイルアップロード処理する方法

It is licensed under [MIT](https://opensource.org/licenses/MIT).

![image](https://user-images.githubusercontent.com/11747460/59484931-ca3d9d00-8eae-11e9-806c-951fe1701862.png)


### Code

**Java**

```java
package com.example;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

/**
 * マルチパートのPOSTでファイルのアップロードを受け取るサーブレット
 * 
 * @author Tom.Misawa(riversun.org@gmail.com)
 *
 */
@SuppressWarnings("serial")
public class UploadServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // 簡易的なログ
        final StringBuilder log = new StringBuilder();
        log.append("----サーバーで受信----").append("\n");

        try {

            final Collection<Part> parts = req.getParts();

            for (Part part : parts) {

                final String contentType = part.getContentType();
                final String paramName = part.getName();

                if (contentType == null) {
                    // テキストパートの処理
                    // (contentTypeがnullなら、ファイルではない)

                    final String paramValue = req.getParameter(paramName);
                    log.append("paramName=").append(paramName).append(" paramValue=").append(paramValue).append("\n");

                } else {

                    // ファイルパートの処理
                    final String fileName = part.getSubmittedFileName();

                    final long fileSize = part.getSize();
                    log.append("paramName=").append(paramName).append(" contentType=").append(contentType)
                            .append(" fileName=").append(fileName).append(" fileSize=").append(fileSize).append("\n");

                    if (fileSize > 0) {

                        // アップロードされたファイルを保存する（その１ Part#writeメソッドをつかう）
                        // MultipartConfigElementで指定されたディレクトリに保存される
                        // part.write(fileName);

                        // アップロードされたファイルを保存する（その２ ストリームをつかう）
                        // byte[] data = new byte[(int) fileSize];
                        // part.getInputStream().read(data);
                        // Files.copy(part.getInputStream(), new File("c:/temp/" + fileName).toPath());
                    }
                }
            }

            System.out.println(log.toString());

            resp.setStatus(HttpServletResponse.SC_OK);

            // ログをJSONにして、レスポンスを返す
            final PrintWriter out = resp.getWriter();
            out.println("{\"msg\":\"" + log.toString().replace("\n", "\\n") + "\"}");
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
```

```java
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
        final int PORT = 8080;

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

```

**HTML/JavaScript**

```HTML
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <title>ファイルアップロードサンプル</title>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css">

    <style>
        /** ファイルアップロードパーツのスタイル **/
        .custom-file-input:lang(ja) ~ .custom-file-label::after {
            content: "選択";
        }

        .custom-file {
            overflow: hidden;
        }

        .custom-file-label {
            white-space: nowrap;
        }
    </style>

</head>

<body class="bg-light">

<div class="container">
    <div class="py-5 text-center">
        <h2>ファイルアップロード</h2>
        <p class="lead">ファイルアップロードサーブレットのサンプルです。
            ファイルとその他のパラメータをマルチパートで送信します。<br>送信処理はJavaScriptで行います。</p>
    </div>

    <!-- アップロードフォーム -->
    <div class="row">
        <div class="col-md-12">
            <h4 class="d-flex justify-content-between align-items-center mb-3">
                <span class="text-muted">フォーム</span>
            </h4>

            <form id="myform" class="card p-2">
                <div class="form-group">
                    <label for="myusername">ユーザー名</label>
                    <div class="input-group mb-2">
                        <div class="input-group-prepend">
                            <div class="input-group-text">@</div>
                        </div>
                        <input type="text" class="form-control" name="myusername" id="myusername" placeholder="ユーザー名">
                    </div>
                </div>
                <div class="form-group">
                    <label for="myfile1">画像１</label>
                    <div class="custom-file">
                        <input type="file" class="custom-file-input" id="myfile1" name="myfile1"
                               aria-describedby="fileHelp" accept="image/png, image/jpeg">
                        <label class="custom-file-label" for="myfile1">ファイルを選択してください</label>
                        <small id="fileHelp" class="form-text text-muted">画像ファイルを選択する</small>
                    </div>
                </div>
                <div class="form-group">
                    <label for="myfile2">画像２</label>
                    <div class="custom-file">
                        <input type="file" class="custom-file-input" id="myfile2" name="myfile2"
                               aria-describedby="fileHelp2" accept="image/png, image/jpeg">
                        <label class="custom-file-label" for="myfile2">ファイルを選択してください</label>
                        <small id="fileHelp2" class="form-text text-muted">２つめの画像ファイルを選択する</small>
                    </div>
                </div>
                <div class="form-group">
                    <label for="mycomment">ひとことコメントを入力</label>
                    <input type="text" class="form-control" name="mycomment" id="mycomment" placeholder="ひとことどうぞ">
                </div>

                <button type="button" id="send-button" class="btn btn-primary mb-2">送信</button>
            </form>
        </div>
    </div>
</div>

<!-- アップロード結果表示用ダイアログ-->
<div class="modal fade" id="modal-dialog" tabindex="-1" role="dialog" aria-labelledby="resultModalLabel"
     aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="resultModalLabel">アップロード結果</h5>
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <pre id="myresult" class="card-text">
                </pre>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">OK</button>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript"
        src="https://cdn.jsdelivr.net/npm/bootstrap.native@2.0.15/dist/bootstrap-native-v4.min.js"></script>

<script type="text/javascript">

    const sendButton = document.querySelector('#send-button');
    sendButton.addEventListener('click', evt => {
        doUpload();
    });

    //ファイルアップロードパーツの処理

    //vanillaで書くと↓
    document.querySelectorAll('.custom-file-input').forEach(ele => {
        ele.addEventListener('change', (evt) => {
            const target = evt.target;
            const fileName = target.value.split('\\').pop();
            const labelEle = target.parentNode.querySelector('.custom-file-label');
            labelEle.classList.add('selected');
            labelEle.innerHTML = fileName;
        });
    });

    //jqueryつかうなら↓
    // $(".custom-file-input").on("change", function() {
    //     var fileName = $(this).val().split("\\").pop();
    //     $(this).siblings(".custom-file-label").addClass("selected").html(fileName);
    // });

    //Formアップロード処理
    function doUpload() {

        const formEle = document.querySelector('#myform');
        const formData = new FormData(formEle);

        const xhr = new XMLHttpRequest();

        xhr.timeout = 30 * 1000;//タイムアウト30秒
        xhr.addEventListener('load', evt => {

            if (xhr.status == 200) {
                console.log(xhr.responseText);
                const data = JSON.parse(xhr.response);
                handleResult(data);
            } else {
                console.log(xhr.statusText);
            }
        });

        xhr.addEventListener('timeout', evt => {
            console.error(evt);
        });

        xhr.addEventListener('error', evt => {
            console.error(evt);
        });

        xhr.open('post', 'upload', true);
        xhr.send(formData);
    }

    function handleResult(data) {
        const info = document.querySelector('#myresult');
        info.innerHTML = data.msg;
        const ele = document.querySelector('#modal-dialog');
        var modal = new Modal(ele);
        modal.show();
    }
</script>
</body>
</html>
```
