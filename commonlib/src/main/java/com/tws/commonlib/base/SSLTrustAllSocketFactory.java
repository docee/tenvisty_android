package com.tws.commonlib.base;

import android.util.Log;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.net.Socket;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SSLTrustAllSocketFactory extends SSLSocketFactory {

    private static final String TAG = "SSLTrustAllSocketFactory";
    private SSLContext mCtx;

    public class SSLTrustAllManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1)
                throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1)
                throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

    }

    public SSLTrustAllSocketFactory(KeyStore truststore)
            throws Throwable {
        super(truststore);
        try {
            mCtx = SSLContext.getInstance("TLS");
            mCtx.init(null, new TrustManager[] { new SSLTrustAllManager() },
                    null);
            setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        } catch (Exception ex) {
        }
    }

    @Override
    public Socket createSocket(Socket socket, String host,
                               int port, boolean autoClose)
            throws IOException {
        return mCtx.getSocketFactory().createSocket(socket, host, port, autoClose);
    }

    @Override
    public Socket createSocket() throws IOException {
        return mCtx.getSocketFactory().createSocket();
    }

    public static SSLSocketFactory getSocketFactory() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            SSLSocketFactory factory = new SSLTrustAllSocketFactory(trustStore);
            return factory;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
    public static HttpClient getClient() {
    	try{

            BasicHttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
            HttpProtocolParams.setUseExpectContinue(params, true);

            SchemeRegistry schReg = new SchemeRegistry();
            schReg.register(new Scheme("https", SSLTrustAllSocketFactory.getSocketFactory(), 443));
            schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

            ClientConnectionManager connMgr = new ThreadSafeClientConnManager(params, schReg);
            return new DefaultHttpClient(connMgr, params);
    	}
    	catch(Exception e){
    		 return new DefaultHttpClient();
    	}
    }


}