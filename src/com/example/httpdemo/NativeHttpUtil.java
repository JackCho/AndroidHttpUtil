package com.example.httpdemo;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.http.HttpStatus;

public class NativeHttpUtil {

	//设置URLConnection的连接超时时间
	private final static int CONNET_TIMEOUT = 5 * 1000;
	//设置URLConnection的读取超时时间
	private final static int READ_TIMEOUT = 5 * 1000;
	//设置请求参数的字符编码格式
	private final static String QUERY_ENCODING = "UTF-8";
	//设置返回请求结果的字符编码格式
	private final static String ENCODING = "GBK";
	
	/**
	 * 
	 * @param url
	 * 		请求链接
	 * @return
	 * 		HTTP GET请求结果
	 */
	public static String get(String url) {
		return get(url, null);
	} 
	
	/**
	 * 
	 * @param url
	 * 		请求链接
	 * @param params
	 * 		HTTP GET请求的QueryString封装map集合
	 * @return
	 * 		HTTP GET请求结果
	 */
	public static String get(String url, Map<String, String> params) {
		InputStream is = null;
		try {
			StringBuffer queryString = null;
			if (params != null && params.size() > 0) {
				queryString = new StringBuffer("?");
				queryString = joinParam(params, queryString);
			}
			if (queryString != null) {
				url = url + URLEncoder.encode(queryString.toString(), QUERY_ENCODING);
			}
			URL u = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) u.openConnection();
			conn.setUseCaches(false);
			conn.setConnectTimeout(CONNET_TIMEOUT);
			conn.setReadTimeout(READ_TIMEOUT);
			conn.setRequestMethod("GET");
			if (conn.getResponseCode() == HttpStatus.SC_OK) {
				is = conn.getInputStream();
				return StreamUtil.readStreamToString(is, ENCODING);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	/**
	 * 
	 * @param url
	 * 		请求链接
	 * @param params
	 * 		HTTP POST请求body的封装map集合
	 * @return
	 * 		
	 */
	public static String post(String url, Map<String, String> params) {
		if (params == null || params.size() == 0) {
			return null;
		}
		OutputStream os = null;
		InputStream is = null;
		StringBuffer body = new StringBuffer();
		body = joinParam(params, body);
		byte[] data = body.toString().getBytes();
		try {
			URL u = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) u.openConnection();
			conn.setConnectTimeout(CONNET_TIMEOUT);
			conn.setReadTimeout(READ_TIMEOUT);
			conn.setRequestMethod("POST");
			// 请求头, 必须设置
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("Content-Length", String.valueOf(data.length));
			// post请求必须允许输出
			conn.setDoOutput(true);
			// 向服务器写出数据
			os = conn.getOutputStream();
			os.write(data);
			if (conn.getResponseCode() == HttpStatus.SC_OK) {
				is = conn.getInputStream();
				return StreamUtil.readStreamToString(is, ENCODING);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param url
	 * 		请求链接
	 * @param params
	 * 		HTTP POST请求文本参数map集合
	 * @param files
	 * 		HTTP POST请求文件参数map集合
	 * @return
	 * 		HTTP POST请求结果
	 * @throws IOException
	 */
	 public static String post(String url, Map<String, String> params, Map<String, File> files) throws IOException {
	        String BOUNDARY = UUID.randomUUID().toString();
	        String PREFIX = "--", LINEND = "\r\n";
	        String MULTIPART_FROM_DATA = "multipart/form-data";

	        URL uri = new URL(url);
	        HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
	        // 缓存的最长时间
	        conn.setReadTimeout(READ_TIMEOUT); 
	        // 允许输入
	        conn.setDoInput(true);
	        // 允许输出
	        conn.setDoOutput(true);
	        // 不允许使用缓存
	        conn.setUseCaches(false);
	        conn.setRequestMethod("POST");
	        conn.setRequestProperty("connection", "keep-alive");
	        conn.setRequestProperty("Charsert", "UTF-8");
	        conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA + ";boundary=" + BOUNDARY);

	        // 首先组拼文本类型的参数
	        StringBuilder sb = new StringBuilder();
	        for (Map.Entry<String, String> entry : params.entrySet()) {
	            sb.append(PREFIX);
	            sb.append(BOUNDARY);
	            sb.append(LINEND);
	            sb.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + LINEND);
	            sb.append("Content-Type: text/plain; charset=" + ENCODING + LINEND);
	            sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
	            sb.append(LINEND);
	            sb.append(entry.getValue());
	            sb.append(LINEND);
	        }

	        DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
	        outStream.write(sb.toString().getBytes());
	        // 发送文件数据
	        if (files != null)
	            for (Map.Entry<String, File> file : files.entrySet()) {
	                StringBuilder sb1 = new StringBuilder();
	                sb1.append(PREFIX);
	                sb1.append(BOUNDARY);
	                sb1.append(LINEND);
	                sb1.append("Content-Disposition: form-data; name=\"uploadfile\"; filename=\""
	                        + file.getValue().getName() + "\"" + LINEND);
	                sb1.append("Content-Type: application/octet-stream; charset=" + ENCODING + LINEND);
	                sb1.append(LINEND);
	                outStream.write(sb1.toString().getBytes());

	                InputStream is = new FileInputStream(file.getValue());
	                byte[] buffer = new byte[1024];
	                int len = 0;
	                while ((len = is.read(buffer)) != -1) {
	                    outStream.write(buffer, 0, len);
	                }
	                is.close();
	                outStream.write(LINEND.getBytes());
	            }

	        // 请求结束标志
	        byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
	        outStream.write(end_data);
	        outStream.flush();
	        StringBuilder sb2 = new StringBuilder();
	        if (conn.getResponseCode() == HttpStatus.SC_OK) {
	        	InputStream in = conn.getInputStream();
	            int ch;
	            while ((ch = in.read()) != -1) {
	                sb2.append((char) ch);
	            }
	        }
	        outStream.close();
	        conn.disconnect();
	        return sb2.toString();
    }

	 /**
	  * 
	  * @param params
	  * @param queryString
	  * @return
	  * 	返回拼接后的StringBuffer
	  */
	private static StringBuffer joinParam(Map<String, String> params, StringBuffer queryString) {
		Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, String> param = iterator.next();
			String key = param.getKey();
			String value = param.getValue();
			queryString.append(key).append('=').append(value);
			if (iterator.hasNext()) {
				queryString.append('&');
			}
		}
		return queryString;
	} 
}
