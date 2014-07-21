package com.example.httpdemo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

/**
 * 
 * 
 * Description：基于Apache HttpClient的封装，支持HTTP GET、POST请求，支持多文件上传
 *
 */
public class ApacheHttpUtil {

	//设置URLConnection的连接超时时间
	private final static int CONNET_TIMEOUT = 5 * 1000;
	//设置URLConnection的读取超时时间
	private final static int READ_TIMEOUT = 5 * 1000;
	
	/**
	 * HTTP GET请求
	 * @param url
	 * 		请求链接
	 * @return
	 * 		HTTP GET请求结果
	 */
	public static String get(String url) {
		return get(url, null);
	}
	
	/**
	 * HTTP GET请求
	 * @param url
	 * 		请求链接
	 * @param params
	 * 		HTTP GET请求的QueryString封装map集合
	 * @return
	 * 		HTTP GET请求结果
	 */
	public static String get(String url, Map<String, String> params) {
		try {
			String realUrl = generateUrl(url, params);
			HttpClient client = getNewHttpClient();
			HttpGet getMethod = new HttpGet(realUrl);
			HttpResponse response = client.execute(getMethod);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				StringBuilder builder = new StringBuilder();
				BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				for (String s = reader.readLine(); s != null; s = reader.readLine()) {
					builder.append(s);
				}
				String result = builder.toString();
				return result;
			} 
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * HTTP POST请求
	 * @param url
	 * 		请求链接
	 * @param params
	 * 		HTTP POST请求body的封装map集合
	 * @return
	 * 		
	 */
	public static String post(String url, Map<String, String> params) {
		try {
			HttpClient client = getNewHttpClient();
			HttpPost postMethod = new HttpPost(url);
			List<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
			if (params != null && params.size() > 0) {
				Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
				while (iterator.hasNext()) {
					Entry<String, String> param = iterator.next();
					String key = param.getKey();
					String value = param.getValue();
					BasicNameValuePair pair = new BasicNameValuePair(key, value);
					pairs.add(pair);
				}
				postMethod.setEntity(new UrlEncodedFormEntity(pairs, HTTP.UTF_8));
			}
			HttpResponse response = client.execute(postMethod);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				return EntityUtils.toString(response.getEntity());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
 
	/**
	 * HTTP POST请求上传文件，支持多文件上传
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
    public String post(String url, Map<String, String> params, Map<String, String> files) throws IOException {
		HttpPost post = new HttpPost(url);
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, CONNET_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParams, READ_TIMEOUT);
		HttpClient client = new DefaultHttpClient(httpParams);
		MultipartEntity entity = new MultipartEntity();
		if (params != null && !params.isEmpty()) {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				entity.addPart(entry.getKey(), new StringBody(entry.getValue()));
			}
		}
		
		if (files != null && !files.isEmpty()) {
			for (Map.Entry<String, String> entry : files.entrySet()) {
				String key = entry.getKey();
				String filePth = entry.getValue();
				entity.addPart(key, new FileBody(new File(filePth)));
			}
		}
		post.setEntity(entity);
		HttpResponse response = client.execute(post);
		StringBuffer sb = new StringBuffer();
		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			HttpEntity result = response.getEntity();
			if (result != null) {
				InputStream is = result.getContent();
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				String tempLine;
				while ((tempLine = br.readLine()) != null) {
					sb.append(tempLine);
				}
			}
		}
		post.abort();
		return sb.toString();
    }
 
    /**
     * 获取HttpClient
     * @return
     */
	private static HttpClient getNewHttpClient() {
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);
			SSLSocketFactory sf = new SSLSocketFactory(trustStore);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
			HttpConnectionParams.setConnectionTimeout(params, CONNET_TIMEOUT);
			HttpConnectionParams.setSoTimeout(params, READ_TIMEOUT);

			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			registry.register(new Scheme("https", sf, 443));
			ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
			return new DefaultHttpClient(ccm, params);
		} catch (Exception e) {
			return new DefaultHttpClient();
		}
	}
	
	/**
	 * 
	 * @param url
	 * 		HTTP GET请求的基础链接
	 * @param params
	 * 		HTTP GET请求参数列表
	 * @return
	 * 		拼接后url链接
	 */
	private static String generateUrl(String url, Map<String, String> params) {
		StringBuilder urlBuilder = new StringBuilder(url);
		if (null != params) {
			urlBuilder.append("?");
			Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, String> param = iterator.next();
				String key = param.getKey();
				String value = param.getValue();
				urlBuilder.append(key).append('=').append(value);
				if (iterator.hasNext()) {
					urlBuilder.append('&');
				}
			}
		}
		return urlBuilder.toString();
	}
}
