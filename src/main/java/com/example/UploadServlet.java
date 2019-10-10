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
  public void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    // For CORS PreFlight Access for Ajax Upload request
    resp.addHeader("Access-Control-Allow-Origin", "*");
    resp.addHeader("Access-Control-Allow-Headers", "*");

  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    // For CORS PreFlight Access for Ajax Upload request
    resp.addHeader("Access-Control-Allow-Origin", "*");
    resp.addHeader("Access-Control-Allow-Headers", "*");

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