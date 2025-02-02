package com.knziha.plod.dictionarymodels;

import static com.knziha.plod.PlainUI.HttpRequestUtil.DO_NOT_VERIFY;
import static com.knziha.plod.db.LexicalDBHelper.FIELD_CREATE_TIME;
import static com.knziha.plod.db.LexicalDBHelper.FIELD_EDIT_TIME;
import static com.knziha.plod.db.LexicalDBHelper.TABLE_DATA_v2;
import static com.knziha.plod.plaindict.MainActivityUIBase.DarkModeIncantation;
import static com.knziha.plod.plaindict.MdictServerMobile.*;
import static com.knziha.plod.widgets.Tls12SocketFactory.enableTls12OnPreLollipop;
import static org.nanohttpd.protocols.http.response.Response.newChunkedResponse;

import android.animation.ObjectAnimator;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Pair;
import android.webkit.ValueCallback;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import androidx.appcompat.app.GlobalOptions;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.knziha.plod.ArrayList.SerializedLongArray;
import com.knziha.plod.db.LexicalDBHelper;
import com.knziha.plod.db.MdxDBHelper;
import com.knziha.plod.dictionary.Utils.AutoCloseInputStream;
import com.knziha.plod.dictionary.Utils.Flag;
import com.knziha.plod.dictionary.Utils.SU;
import com.knziha.plod.dictionary.Utils.SubStringKey;
import com.knziha.plod.dictionary.Utils.myCpr;
import com.knziha.plod.ebook.Utils.BU;
import com.knziha.plod.plaindict.BuildConfig;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.Toastable_Activity;
import com.knziha.plod.widgets.SSLSocketFactoryCompat;
import com.knziha.plod.widgets.ViewUtils;
import com.knziha.plod.widgets.WebResourceResponseCompat;
import com.knziha.plod.widgets.WebViewmy;
import com.knziha.rbtree.RBTree_additive;

import org.apache.commons.lang3.StringUtils;
import org.knziha.metaline.Metaline;
import org.knziha.metaline.StripMethods;
import org.nanohttpd.protocols.http.response.Status;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Cache;
import okhttp3.Dns;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/*
 Mdict point to online website.
 date:2019.11.28
 author:KnIfER
*/
public class PlainWeb extends DictionaryAdapter {
	/** The main url */
	String host;
	String hostName;
	String index;
	String jsCode;
	String onstart;
	String onload;
	/** The search sub url */
	String search;
	/** The search action js */
	String searchJs;
	String style;
	JSONArray stylex;
	String currentUrl;
	boolean abSearch;
	boolean excludeAll;
	public boolean hasExcludedUrl;
	public boolean hasExcludedResV4;
	public boolean fastGStaticFonts;
	public boolean canSaveResource;
	public boolean hasReroutedUrl;
	public boolean butReadonly;
	public boolean andEagerForParms;
	public boolean noImage;
	public boolean computerFace;
	public boolean forceText;
	//public boolean banJs;
	//public boolean reEnableJs;
	/** 缓存的关键词 */
	//ArrayList<String> searchKeys = new ArrayList<>();
	/** 缓存的关键词在历史记录中的索引 */
	SerializedLongArray searchKeyIds = new SerializedLongArray(100);
	/** Json中的入口点 */
	ArrayList<String> entrance = new ArrayList<>();
	/** 重定向超链接的点击 */
	ArrayList<String> routefrom = new ArrayList<>();
	ArrayList<String> routeto = new ArrayList<>();
	/** for excludeAll */
	HashSet<String> hosts=new HashSet<>();
	/** 排除资源文件，加速加载过程（目前仅对kitkat有效） */
	public String[] excludeResourcesV4;
	/** for SVG */
	public String[] svgKeywords;
	/** 兴趣文件后缀 */
	public String[] cacheExtensions;
	/** 无兴趣文件后缀(仅针对数据库) */
	public String[] cleanExtensions;
	private static final String[] default_cleanExtensions = new String[]{".jpg",".png",".gif",".mp4",".mp3",".m3u8",".json",".js",".css"};
	public Pattern keyPattern;
	private final Context context;
	private long mLastKeyId;
	private boolean mRecordsDirty;
	
	public Map<SubStringKey, String> jinkeSheaths;
	Object k3client;
	
	public boolean hasHosts() {
		return jinkeSheaths!=null && jinkeSheaths.size()>0;
	}
	public boolean shouldUseClientResponse(String url) {
		if(jinkeSheaths!=null) { //  && jinkeSheaths.size()>0
			return jinkeSheaths.containsKey(SubStringKey.new_hostKey(url));
		}
		return false;
	}
	void parseJinke(Context context) {
		String hosts = getField("hosts");
		if(hosts!=null) {
			if(jinkeSheaths==null) {
				jinkeSheaths = new HashMap<>();
			} else {
				jinkeSheaths.clear();
			}
			System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
			try (BufferedReader bufferedReader = new BufferedReader(new StringReader(hosts))){
				String ln;
				Pattern p = Pattern.compile("(.*)[ ]+(.*)");
				while ((ln=bufferedReader.readLine())!=null) {
					ln = ln.trim();
					if(ln.startsWith("#")) continue;
					Matcher m = p.matcher(ln);
					if(m.find()) {
						jinkeSheaths.put(SubStringKey.fast_hostKey(m.group(2)), m.group(1));
						//CMN.Log("jinke...", SubStringKey.fast_hostKey(m.group(2)), m.group(1));
					}
				}
			} catch (IOException e) {
				CMN.debug(e);
			}
			if(context instanceof MainActivityUIBase) {
				MainActivityUIBase a = (MainActivityUIBase) context;
				a.serverHostsHolder.add(this);
				if(a.serverHosts!=null) {
					a.serverHosts.putAll(jinkeSheaths);
				}
			}
		}
	}
	
	public static SSLSocketFactory NoSSLv3Factory;
	
	/**
	 * OkHttp在4.4及以下不支持TLS协议的解决方法
	 *  javax.net.ssl.SSLHandshakeException: javax.net.ssl.SSLProtocolException
	 * @param okhttpBuilder
	 * @return
	 */
	private synchronized static OkHttpClient.Builder setOkHttpSsl(OkHttpClient.Builder okhttpBuilder) {
		try {
			// 自定义一个信任所有证书的TrustManager，添加SSLSocketFactory的时候要用到
			final X509TrustManager trustAllCert =
					new X509TrustManager() {
						@Override
						public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
						}
						
						@Override
						public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
						}
						
						@Override
						public java.security.cert.X509Certificate[] getAcceptedIssuers() {
							return new java.security.cert.X509Certificate[]{};
						}
					};
			final SSLSocketFactory sslSocketFactory = new SSLSocketFactoryCompat(trustAllCert);
			okhttpBuilder.sslSocketFactory(sslSocketFactory, trustAllCert);
			return okhttpBuilder;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private OkHttpClient prepareKlient(File cacheDir, int cacheSize) {
		if (k3client!=null) return (OkHttpClient) k3client;
		if(cacheSize==-1)
			cacheSize = 10 * 1024 * 1024;
		Interceptor headerInterceptor = new Interceptor() {
			@Override
			public Response intercept(Chain chain) throws IOException {
				Request request = chain.request();
				Response response = chain.proceed(request);
				Response response1 = response.newBuilder()
						.removeHeader("Pragma")
						.removeHeader("Cache-Control")
						//cache for 30 days
						.header("Cache-Control", "max-age=" + 3600 * 24 * 30)
						.build();
				return response1;
			}
		};
//		ConnectionSpec spec1 = new ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
//				.tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_1, TlsVersion.TLS_1_0)
//				.cipherSuites(
//						CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
//						CipherSuite.TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256,
//						CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
//						CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA)
//				.build();
//		ConnectionSpec spec2 = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
//						.allEnabledTlsVersions()
//						.allEnabledCipherSuites()
//						.build();
//		ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
//				.tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_1, TlsVersion.TLS_1_0)
//				.cipherSuites(
//						CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
//						CipherSuite.TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256,
//						CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
//						CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA)
//				.build();
		OkHttpClient klient = enableTls12OnPreLollipop(
				new OkHttpClient.Builder()
				.connectTimeout(5, TimeUnit.SECONDS)
				.addNetworkInterceptor(headerInterceptor)
				//.protocols(Collections.singletonList(Protocol.HTTP_1_1))
				.cache(cacheDir==null ? null :
						new Cache(new File(cacheDir, "k3cache")
								, cacheSize)) // 配置缓存
				.dns(new Dns() {
					@Override
					public List<InetAddress> lookup(String hostname) throws UnknownHostException {
						String addr = jinkeSheaths.get(SubStringKey.new_hostKey(hostname));
						CMN.Log("lookup...", hostname, addr, InetAddress.getByName(addr));
						if (addr != null) {
							return Collections.singletonList(InetAddress.getByName(addr));
						}
						//else return Collections.singletonList(InetAddress.getByName(hostname));
						return Dns.SYSTEM.lookup(hostname);
					}
				})
//				.sslSocketFactory(NoSSLv3Factory)
//				.connectionSpecs(Arrays.asList(ConnectionSpec.CLEARTEXT, spec2, spec1))
				//.readTimeout(5, TimeUnit.SECONDS)
				//.setCache(getCache())
				//.certificatePinner(getPinnedCerts())
				//.setSslSocketFactory(NoSSLv3Factory)
				.hostnameVerifier(DO_NOT_VERIFY)
		)
				.build();
		return klient;
	}
	public Object getClientResponse(Context context, String url, String host, List<Pair> moders, Map<String, String> headers, boolean forServer) {
		//if(true) return null;
		//RequestQueue sRequestQueue = Volley.newRequestQueue(context, new HurlStack(null, ClientSSLSocketFactory.getSocketFactory(context)));
		try {
			if(headers==null) headers=new HashMap<>();
			for(String kI:headers.keySet()) {
				CMN.Log("headers::", kI, headers.get(kI));
			}
			//CMN.Log("host::", host);
			if(context==null) context=this.context;
			String acc = headers.get("Accept");
			//CMN.Log("SIR::Accept_", acc, acc.equals("*/*"));
			if(acc==null) {
				acc = "*/*;";
			}
			headers.put("Access-Control-Allow-Origin", "*");
			headers.put("Access-Control-Allow-Headers","Origin, X-Requested-With, Content-Type, Accept");
			InputStream input;
			String MIME;
			CMN.rt("转构开始……", url);
			if(true || moders!=null) {
				OkHttpClient klient = (OkHttpClient) k3client;
				if(klient==null) {
					klient = prepareKlient(context.getExternalFilesDir(null), -1);
				}
				Request.Builder k3request = new Request.Builder()
						.url(url)
						.header("Accept-Charset", "utf-8")
						.header("Access-Control-Allow-Origin", "*")
						.header("Access-Control-Allow-Headers","Origin, X-Requested-With, Content-Type, Accept")
						;
				for(String kI:headers.keySet()) {
					k3request.header(kI, headers.get(kI));
				}
				//k3request.method("GET", null);
				
				//int maxSale = 60 * 60 * 24 * 28; // tolerate 4-weeks sale
	//					if (!NetworkUtils.isConnected(a))
	//					k3request.removeHeader("Pragma")
	//							.cacheControl(new CacheControl.Builder()
	//									.maxAge(0, TimeUnit.SECONDS)
	//									.maxStale(365,TimeUnit.DAYS).build())
	//							.header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale);
				if(host!=null && !host.contains("mdbr")) k3request.header("Host", host);
				k3request.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.106 Safari/537.36");
				Response k3response = klient.newCall(k3request.build()).execute();
				MIME = k3response.header("content-type");
				//CMN.Log("重定向啦拉拉!!!", k3response.isRedirect(), url);
				CMN.Log("缓存啦拉拉!!!", k3response.cacheResponse(), k3response.cacheResponse()==k3response);
				if (moders!=null) {
					String raw = k3response.body().string();
					for (Pair p:moders) {
						p = (Pair) p.second;
						raw = raw.replace((String)p.first, (String)p.second);
					}
					//CMN.Log("修改了::", url);
					input = new ByteArrayInputStream(raw.getBytes());
					k3response.close();
				} else {
					input = k3response.body().byteStream();
				}
			}
			input = new AutoCloseInputStream(input);
			CMN.pt("转构完毕！！！", input.available(), MIME);
			if(TextUtils.isEmpty(MIME)) {
				MIME = acc;
			}
			int idx = MIME.indexOf(",");
			if(idx<0) {
				idx = MIME.indexOf(";");
			}
			if(idx>=0) {
				MIME = MIME.substring(0, idx);
			}
			if(forServer) {
				org.nanohttpd.protocols.http.response.Response ret = newChunkedResponse(Status.OK, MIME, input);
				ret.putHeaders(headers);
				return ret;
			} else {
				WebResourceResponse webResourceResponse;
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					webResourceResponse=new WebResourceResponse(MIME, "utf8", input);
					webResourceResponse.setResponseHeaders(headers);
				} else {
					webResourceResponse = new WebResourceResponseCompat(MIME, "utf8", input);
					((WebResourceResponseCompat)webResourceResponse).setResponseHeaders(headers);
				}
				//CMN.Log("百代春秋泽被万世");
				return webResourceResponse;
			}
		} catch (Exception e) {
			CMN.debug(url, e);
		}
		return null;
	}
	
	
	/**
	 if(!app.PLODKit) {
		 var e = document.createElement('style'), css = "mark{background:yellow}mark.current{background:orange}";
		 e.type = 'text/css';
		 e.class = '_PDict';
		 e.id = '_PDSTL';
		 document.head.appendChild(e);
		 if (e.styleSheet){
		 	e.styleSheet.cssText = css;
		 } else {
		 	e.appendChild(document.createTextNode(css));
		 }
	 	 app.PLODKit=1;
		function fun(url){
			var script=document.createElement('script');
			script.type="text/javascript";
			script.src=url;
			document.body.appendChild(script);
		}
		try{fun('//mdbr/SUBPAGE.js')}catch(e){app.loadJs(sid.get(),'SUBPAGE.js');}
	 }
	 */
	@Metaline()
	public static final String loadJs = StringUtils.EMPTY;
	
	/**
	 	//if(!window._fvwhl){
			var vi = document.getElementsByTagName('video');
			for(var i=0;i<vi.length;i++){if(!vi[i]._fvwhl){vi[i].addEventListener("webkitfullscreenchange", wrappedFscrFunc, true);vi[i]._fvwhl=1;}}
			function wrappedFscrFunc(e){
				//console.log('begin fullscreen!!!');
				var se = e.srcElement;
				if(se.webkitDisplayingFullscreen&&app) app.onRequestFView(se.videoWidth, se.videoHeight);
	 			return false;
			}
	 		window._fvwhl=1;
	 	//}
	 */
	@Metaline()
	public final static String projs="SUB PAGE";
	
	/**
		if (typeof String.prototype.startsWith != 'function') {
		 String.prototype.startsWith = function (prefix){
		  return this.slice(0, prefix.length) === prefix;
		 }
		}
	 */
	@Metaline()
	public final static String kitkatCompatJs="SUB PAGE";
	
	private JSONObject website;
	private ObjectAnimator progressProceed;
	private ObjectAnimator progressTransient;
	private boolean isListDirty;
	private String jsLoader;
	private boolean bNeedSave;
	private static SerializerFeature[] SerializerFormat = new SerializerFeature[]{SerializerFeature.PrettyFormat, SerializerFeature.MapSortField, SerializerFeature.QuoteFieldNames};
	boolean dataRead = false;
	private String name = "index";
	private String message;
	private boolean isTranslator;
	private boolean hasModifiers;
	private boolean bReplaceLetToVar;
	public String[] kikUrlPatterns;
	
	//构造
	public PlainWeb(File fn, MainActivityUIBase _a) throws IOException {
		super(fn, _a);
		//a=_a;
		//opt=a.opt;
		context = _a.getApplicationContext();
		_num_record_blocks=-1;
		//unwrapSuffix=false;
		
		//readInConfigs(a.UIProjects);
		
		parseJsonFile(_a);
		mType = DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_WEB;
		
		keyProviders.add(keyProvider_Entrances);
		keyProviders.add(keyProvider_Searches);
	}
	
	@Override
	public void setBooKID(long id) {
		super.setBooKID(id);
		if (!dataRead) {
			String kDumpName = "ws_"+getBooKID();
			try {
				LexicalDBHelper database = LexicalDBHelper.getInstance();
				if (database!=null && database.testDBV2) {
					String[] where = new String[]{kDumpName};
					Cursor cursor = database.getDB().rawQuery("select type,len,data from data where name=? limit 1", where);
					//CMN.Log("setBooKID::ReadData::", kDumpName, cursor.getCount());
					if (cursor.moveToNext()) {
						int type = cursor.getInt(0);
						int length = cursor.getInt(1);
						byte[] data = cursor.getBlob(2);
						if (type==0) {
							searchKeyIds.ensureCapacity(length);
							System.arraycopy(data, 0, searchKeyIds.getData(), 0, length);
						}
						if (type==2) { // zlib
							Inflater inf = new Inflater();
							inf.setInput(data, 0, data.length);
							searchKeyIds.ensureCapacity(length);
							inf.inflate(searchKeyIds.getData(), 0, length);
						}
						searchKeyIds.setData(null, length);
					}
					cursor.close();
				}
				dataRead = true;
			} catch (Exception e) {
				CMN.Log(e);
			}
		}
	}
	
	private String getString(String name, String defValue) {
		String value = website.getString(name);
		return value==null?defValue:value;
	}
	
	private void parseJsonFile(Context context) throws IOException {
		website = JSONObject.parseObject(ViewUtils.fileToString(context, f));
		String _host = website.getString("host");
		if(_host==null) _host=getRandomHost();
		if(_host.endsWith("/")) _host=_host.substring(0, _host.length()-1);
		host=_host;
		index = website.getString("index");
		name = getString("name", "index");
		if(index==null) index="";
		jsCode = website.getString("js");
		onstart = website.getString("onstart");
		onload = website.getString("onload");
		search = website.getString("search");
		searchJs = website.getString("searchJs");
		style = website.getString("style");
		stylex = website.getJSONArray("stylex");
		abSearch = search!=null&&search.startsWith("http");
		excludeAll = website.getBooleanValue("excludeAll");
		String _extensions  = website.getString("cacheRes");
		String _excludeRes  = website.getString("excludeRes");
		fastGStaticFonts  = website.getBooleanValue("fastFonts");
		butReadonly  = website.getBooleanValue("readRes");
		andEagerForParms  = website.getBooleanValue("php");
		String _exclude_db_save_extensions  = website.getString("CDROPT");
		noImage = website.getBooleanValue("noImage");
		forceText = website.getBooleanValue("forceText");
		computerFace = website.getBooleanValue("cpau");
		isTranslator = website.getBooleanValue("translator");
		hasModifiers = website.containsKey("modifiers");
		bReplaceLetToVar = website.containsKey("kikLetVar");
		if (bReplaceLetToVar) {
			String str = website.getString("kikLetVar");
			kikUrlPatterns = "true".equals(str)?new String[]{".js"}:str.split("\\|");
		}
		String svg = website.getString("svg");
		String _keyPattern = website.getString("keyPattern");
		if(_keyPattern!=null) {
			try {
				keyPattern = Pattern.compile(_keyPattern);
			} catch (Exception e) { /*CMN.Log(e);*/ }
		}
		/*CMN.Log("_keyPattern", _keyPattern, keyPattern);*/
		//banJs = json.getBooleanValue("banJs");
		//reEnableJs = json.getBooleanValue("reEnableJs");
		String _entrance = website.getString("entrance");
		if(!TextUtils.isEmpty(_entrance)){
			//read json defined entrances
			entrance = new ArrayList<>(Arrays.asList(_entrance.split("\n")));
			int sz = entrance.size();
			if (sz>0) {
				if(0==TextUtils.getTrimmedLength(entrance.get(sz-1))) entrance.remove(sz-1);
				if(sz>1 && 0==TextUtils.getTrimmedLength(entrance.get(0))) entrance.remove(0);
			}
			//todo read entrances that saved in db.
		}
		String _routes = website.getString("reroute");
		if(_routes!=null){
			hasReroutedUrl =true;
			String[] list = _routes.split("\n");
			int size = list.length;
			routefrom = new ArrayList<>(size);
			routeto = new ArrayList<>(size);
			for (int i = 0; i < size; i++) {
				String[] urls = list[i].split("\r");
				if (urls.length==2) {
					routefrom.add(urls[0]);
					routeto.add(urls[1]);
				}
			}
			CMN.Log("重定向", routefrom.size());
		}
		
		String testCode = website.getString("message");
		if (testCode!=null) {
			this.message = testCode;
			CMN.Log(context, testCode);
		}
//		if(bgColor==null) {
//			bgColor = CMN.GlobalPageBackground;
//		}
		
		if(excludeAll){
			hasExcludedUrl =true;
			hosts.add(host);
			for(String sI:entrance){
				int idx = sI.indexOf("://");
				idx++; if(idx>0) idx+=2;
				idx = sI.indexOf("/", idx);
				if(idx>0)
					sI = sI.substring(0, idx);
				//CMN.Log("hosts :: ", sI);
				hosts.add(sI);
			}
		}
		
//		bNeedCheckSavePathName=true;
		
		if(_excludeRes!=null){
			excludeResourcesV4 = _excludeRes.split("\\|");
			hasExcludedResV4=true;
			//InternalResourcePath = CachedPathSubToDBStorage("Caches");
		}
		
		if(_extensions!=null){
			cacheExtensions = _extensions.split("\\|");
			canSaveResource=true;
			//InternalResourcePath = CachedPathSubToDBStorage("Caches");
		}
		
		if(_exclude_db_save_extensions!=null){
			if(_exclude_db_save_extensions.equalsIgnoreCase("All")){
				cleanExtensions = default_cleanExtensions;
			} else {
				cleanExtensions = _exclude_db_save_extensions.split("\\|");
			}
		}
		
		if(svg!=null){
			svgKeywords = svg.split("\r");
		}
		
		int idx = host.indexOf("://");
		hostName = idx>0?host.substring(idx+3):host;
		idx = host.indexOf("/");
		if(idx>0) hostName = host.substring(0, idx);
		
		parseJinke(context);
		CMN.Log("jinkeSheaths::", jinkeSheaths);
		
	}
	
	//粗暴地排除
	public boolean shouldExcludeUrl(String url){
		if(hosts==null)
			return false;
		int idx = url.indexOf("://");
		idx++; if(idx>0) idx+=2;
		idx = url.indexOf("/", idx);
		if(idx>0)
			url = url.substring(0, idx);
		CMN.Log("shouldExcludeUrl :: ", url, !hosts.contains(url));
		return !hosts.contains(url);
	}
	
	public String shouldRerouteUrl(String url){
		if(routefrom !=null) {
			int size = routefrom.size();
			for (int i = 0; i < size; i++) {
				String uI = routefrom.get(i);
				if(url.startsWith(uI)) {
					String newurl = routeto.get(i);
					if(!newurl.startsWith(uI)) {
						return newurl + url.substring(uI.length());
					}
				}
			}
		}
		return null;
	}

/*	*//** 简便起见，直接将修改的配置存于HashMap *//*
	@Override
	public void readInConfigs() throws IOException {
		if(!check){
			firstFlag = website.getLongValue("MFF");
			if(website.containsKey("BG"))
				bgColor = website.getIntValue("BG");
			internalScaleLevel = website.getIntValue("TextZoom");

			lvPos = website.getIntValue("lvPos");
			lvClickPos = website.getIntValue("lvClk");
			lvPosOff = website.getIntValue("lvOff");

			initArgs = new int[]{website.getIntValue("argx"), website.getIntValue("argy")};
			webScale = website.getIntValue("args");
		}

//		if(opt.ChangedMap!=null){
//			Long newVal = opt.ChangedMap.remove(getPath());
//			if(newVal!=null && newVal!=firstFlag){
//				website.put("MFF", firstFlag = newVal);
//				CMN.Log("保存！！！"+this);
//				dumpViewStates();
//				refresh_eidt_kit(getContentEditable(), getEditingContents(), true);
//			}
//		}
	}*/

	/** 保存网站定义 */
//	@Override
//	public void dumpViewStates() {
//		try {
//			website.put("BG", bgColor);
//			website.put("TextZoom", internalScaleLevel);
//			website.put("lvPos", lvPos);
//			website.put("lvClk", lvClickPos);
//			website.put("lvOff", lvPosOff);
//			int ex=0,e=0;
//			if(viewsHolderReady && mWebView!=null) {
//				ex=mWebView.getScrollX();
//				e=mWebView.getScrollY();
//			}else if(initArgs!=null && initArgs.length==2){
//				ex=initArgs[0];
//				e=initArgs[1];
//			}
//			website.put("argx", ex);
//			website.put("argy", e);
//			website.put("args", webScale);
//			website.put("MFF", firstFlag);
//			if(entrance.size()>0)
//				website.put("entrance", StringUtils.join(entrance, '\n'));
//			if(cacheExtensions!=null)
//				website.put("cacheRes", StringUtils.join(cacheExtensions, '|'));
//			//if(cleanExtensions!=null)
//			//	website.put("CDROPT", StringUtils.join(cleanExtensions, '|'));
//
//			FileOutputStream fo = new FileOutputStream(f);
//			String v=website.toString();
//			v=v.replace("\\n", "\n");
//			fo.write(v.getBytes());
//			fo.flush();
//			fo.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	//Todo let's make dice
	String getRandomHost() {
		return "";
	}

//	@Override
	protected String getSaveUrl(WebViewmy mWebView) {
		String url=mWebView.getUrl();
		if(url==null) {
			url = currentUrl;
		}
		if (url!=null && url.startsWith(host))
			url = url.substring(host.length());
		return url;
	}

//	@Override
	protected void onPageSaved() {
		//super.onPageSaved();
		//a.notifyDictionaryDatabaseChanged(PlainWeb.this);
	}

//	@Override
	protected String getSaveNameForUrl(String finalUrl) {
		boolean needTrim=!((andEagerForParms&&!finalUrl.contains(".js"))||finalUrl.contains(".php"));//动态资源需要保留参数
		int start = 0;
		int end = needTrim?finalUrl.indexOf("?"):-1;
		if(end<0) end=finalUrl.length();
		String name=finalUrl.substring(start, end);
		try {
			name=URLDecoder.decode(name, "utf8");
		} catch (UnsupportedEncodingException ignored) { }
		if(!needTrim) name=name.replaceAll("[=?&|:*<>]", "_");
		if(name.length()>64){
			name=name.substring(0, 56)+"_"+name.length()+"_"+name.hashCode();
		}
		return name;
	}

	public long saveCurrentUrl(MdxDBHelper con, String url) {
		if(url!=null){
			con.enssureUrlTable();
			if(url.startsWith(host))
				url=url.substring(host.length());
			if(url.length()>0) {
				CMN.Log("saveCurrentUrl", url);
//				return con.putUrl(currentDisplaying, url);
			}
		}
		return -1;
	}

	public boolean containsCurrentUrl(MdxDBHelper con,String url) {
		if(url!=null) {
			con.enssureUrlTable();
			if(url.startsWith(host))
				url=url.substring(host.length());
			CMN.Log("containsCurrentUrl", url, con.containsUrl(url));
			return con.containsUrl(url);
		}
		return false;
	}

	public boolean removeCurrentUrl(MdxDBHelper con,String url) {
		con.enssureUrlTable();
		if(url.startsWith(host))
			url=url.substring(host.length());
		return con.removeUrl(url)!=-1;
	}

//	@Override
//	public String getSimplestInjection() {
//		return js;
//	}

	/*** Page constituents:<br/>
	 * 1. Index <br/>
	 * 2~n. Extra entrances [Optional].  Search History [Optional]. <br/>
	 * n~m. Book marks [not implemented]<br/>
	 * m~x. Page overrides [Optional]<br/>
	 */
	@Override
	public long getNumberEntries() {
		//long size =  1+entrance.size()+searchKeyIds.size();
		long size =  1;
		for (VirtualKeyProvider vkp:keyProviders) {
			size+=vkp.getSize();
		}
//		getCon(false);
//		if(con!=null && PageCursor==null){
//			con.enssurePageTable();
//			PageCursor = con.getDB().rawQuery("select * from t2 ", null);
//		}
//		if(PageCursor!=null){
//			size += PageCursor.getCount();
//		}
		return size;
	}
	
	public String getHost() {
		return host;
	}
	
	public String getDisplayName(String url) {
		url = url.replace(search.replace("%s", ""), "").replace(host, "");
		try {
			return URLDecoder.decode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return url;
		}
	}
	
	public boolean getDrawHighlightOnTop() {
		return website.getBooleanValue("drawHighlightOnTop");
	}
	
	// todo webview 版本
	public boolean getShouldReplaceLetToVar(String url) {
		if (bReplaceLetToVar && Build.VERSION.SDK_INT<21) {
			for (int i = 0; i < kikUrlPatterns.length; i++) {
				if (url.contains(kikUrlPatterns[i]))
					return true;
			}
		}
		return false;
	}
	
	public boolean shouldExcludedResV4(String url) {
		for (String key:excludeResourcesV4) {
			if (url.contains(key))
				return true;
		}
		return false;
	}
	
	public String getSearchUrl() {
		return search!=null?host+search:host;
	}
	
	public String getSyntheticWebPage() throws IOException {
		String synthesis;
		if(hasRemoteDebugServer && f.getPath().contains("ASSET")) {
			try {
				String p = f.getPath();
				SU.Log("getSyntheticWebPage asset path::", p, p.substring(p.indexOf("/", 5)));
				InputStream input = getRemoteServerRes(p.substring(p.indexOf("/", 5)), false);
				if(input!=null) {
					JSONObject json = JSONObject.parseObject(BU.StreamToString(input));
					synthesis = json.getString("synthesis");
					if(synthesis!=null) {
						return synthesis;
					}
				}
			} catch (IOException e) {
				CMN.Log(e);
			}
		}
		synthesis = getField("synthesis");
		if(synthesis!=null) {
			return synthesis;
		}
		return "当前模式（合并的多页面视图）暂不支持查看在线内容";
	}
	
	public String getSyntheticField(String field) throws IOException {
		return getSyntheticField(field, null);
	}
	
	public String getSyntheticField(String field, String def) throws IOException {
		String ret;
		if(hasRemoteDebugServer && f.getPath().contains("ASSET")) {
			try {
				String p = f.getPath();
				SU.Log("getSyntheticField asset path::", p, p.substring(p.indexOf("/", 5)));
				InputStream input = getRemoteServerRes(p.substring(p.indexOf("/", 5)), false);
				if(input!=null) {
					JSONObject json = JSONObject.parseObject(BU.StreamToString(input));
					ret = json.getString(field);
					if(ret!=null) {
						return ret;
					}
				}
			} catch (IOException e) {
				CMN.Log(e);
			}
		}
		ret = getField(field);
		if(ret!=null) {
			return ret;
		}
		return def;
	}
	
	@StripMethods(strip=!BuildConfig.isDebug, keys={"getRemoteServerRes"})
	public InputStream modifyRes(Context context, String url) {
		JSONArray mods = null;
		if(hasRemoteDebugServer && f.getPath().contains("ASSET")) {
			String p = f.getPath();
			SU.Log("getSyntheticWebPage asset path::", p, p.substring(p.indexOf("/", 5)));
			InputStream input = getRemoteServerRes(p.substring(p.indexOf("/", 5)), false);
			if(input!=null)
			try {
				JSONObject json = JSONObject.parseObject(BU.StreamToString(input));
				mods = json.getJSONArray("modifiers");
			} catch (Exception e) {
				CMN.debug(e);
			}
		}
		if(mods==null) {
			mods = website.getJSONArray("modifiers");
		}
		if(mods!=null) {
			for (int i = 0; i < mods.size(); i++) {
				String urlPrefix = mods.getJSONObject(i).getString("url");
				if(urlPrefix==null && Build.VERSION.SDK_INT<20)
					urlPrefix = mods.getJSONObject(i).getString("urlv4");
				if(urlPrefix!=null && url.startsWith(urlPrefix)) {
					try {
						if(url.contains("?"))  url = url.substring(0, url.indexOf("?"));
						File file = new File(f.getParent(), new File(url).getName());
						//CMN.Log("modifyRes::???:::", file.getName());
						InputStream ret = ViewUtils.fileToStream(context, file);
						//CMN.Log("modifyRes::", file.getPath());
						return ret;
					} catch (Exception e) {
						CMN.debug(url,"\n",e);
						return null;
					}
				}
			}
		}
		return null;
	}
	
	abstract class VirtualKeyProvider {
		abstract String getEntryAt(long position);
		abstract String getRecordAt(long position);
		abstract long getSize();
		abstract String getVirtualTextValidateJs(BookPresenter bookPresenter, WebViewmy mWebView, long position);
	}
	
	final ArrayList<VirtualKeyProvider> keyProviders = new ArrayList<>();
	final VirtualKeyProvider keyProvider_Entrances = new EntranceKeyProvider();
	final SearchKeyProvider keyProvider_Searches = new SearchKeyProvider();
	
	final static Pattern FieldReferencePattern = Pattern.compile("@\\{(.*?)\\}");
	
	class EntranceKeyProvider extends VirtualKeyProvider{
		String getEntryAt(long position) {
			if(position>=0 && position<entrance.size()){
				String url = entrance.get((int) position);
				String[] arr = url.split("\r");
				if(arr.length>1) {
					return arr[1];
				}
				return url;
			}
			return null;
		}
		
		@Override
		String getRecordAt(long position) {
			if(position>=0 && position<entrance.size()){
				String url = entrance.get((int) position);
				String[] arr = url.split("\r");
				if(arr.length>1) {
					url =  arr[0];
				}
				if (url.startsWith("js:")) {
					return url;
				} else {
					// 返回完整网址
					return WebxUrl(url);
				}
			}
			return null;
		}
		
		@Override
		long getSize() { return entrance.size(); }
		@Override
		String getVirtualTextValidateJs(BookPresenter bookPresenter, WebViewmy mWebView, long position) {
			if(position>=0 && position<entrance.size()){
				String url = getRecordAt(position);
				if (url.startsWith("js:")) {
					StringBuffer sb = new StringBuffer(url.length());
					Matcher m = FieldReferencePattern.matcher(url.substring(3));
					while(m.find()) {
						String field = m.group(1);
						m.appendReplacement(sb, String.valueOf(website.getString(field)));
					}
					m.appendTail(sb);
					int idx = sb.indexOf("%s");
					if(idx>0) {
						sb.replace(idx, idx+3, bookPresenter.GetAppSearchKey());
					}
					return sb.toString();
				} else {
					return TextUtils.equals(url, mWebView.getUrl())?"1":null;
				}
			}
			return null;
		}
	}
	
	class SearchKeyProvider extends VirtualKeyProvider{
		String getEntryAt(long position)
		{
			if(position>=0 && position<searchKeyIds.size())
			{
				LexicalDBHelper database = LexicalDBHelper.getInstance();
				if (database!=null && database.testDBV2 && !database.isClosed()) {
					long index = searchKeyIds.get((int) position);
					// todo 加一层缓存？
					try (Cursor cursor = database.getDB().rawQuery("select lex from history where id=? limit 1", new String[]{""+index})){
						if (cursor.moveToNext()) {
							return cursor.getString(0);
						}
					} catch (Exception e) {
						CMN.Log(e);
					}
				}
				return "无记录";
			}
			return null;
		}
		
		@Override
		String getRecordAt(long position) {
			// 返回搜索词
			return getEntryAt(position);
		}
		
		@Override long getSize() { return searchKeyIds.size(); }
		
		@Override
		String getVirtualTextValidateJs(BookPresenter bookPresenter, WebViewmy mWebView, long position)
		{
			if(searchJs!=null && position>=0 && position<searchKeyIds.size())
			{
				return searchJs.replace("%s", getEntryAt(position));
			}
			return null;
		}
	}
	
	@Override
	public String getEntryAt(long position) {
		if(position==0) return name;
		position-=1;
		try {
			String ret;
			for (VirtualKeyProvider vkp:keyProviders) {
				ret = vkp.getEntryAt(position);
				if (ret!=null) return ret;
				position -= vkp.getSize();
			}
			
//			if(entrance.size()>0){
//				if(position>=0 && position<entrance.size()){
//					String url = entrance.get(position);
//					String[] arr = url.split("\r");
//					if(arr.length>1) {
//						return arr[1];
//					}
//					return url;
//				}
//				position-=entrance.size();
//			}
			
//			if(position>=0 && position<searchKeyIds.size())
//			{
//				LexicalDBHelper database = LexicalDBHelper.getInstance();
//				if (database!=null && database.testDBV2 && !database.isClosed()) {
//					long index = searchKeyIds.get(position);
//					// todo 加一层缓存？
//					try (Cursor cursor = database.getDB().rawQuery("select lex from history where id=? limit 1", new String[]{""+index})){
//						if (cursor.moveToNext()) {
//							return cursor.getString(0);
//						}
//					} catch (Exception e) {
//						CMN.Log(e);
//					}
//				}
//				return searchKeyIds.get(position)+"=XDI（无记录）";
//			}
//			position-=searchKeyIds.size();
			
//			if(PageCursor!=null){
//				if(position>=0 && position<PageCursor.getCount()){
//					PageCursor.moveToPosition(position);
//					return PageCursor.getString(1);
//				}
//			}
		} catch (Exception e) {
			CMN.Log(e);
			return "Error!!!"+e;
		}
		return "Error!!!";
	}
	
	@Override
	public boolean hasVirtualIndex() {
		return true;
	}
	
	/** WEB模型直接在此处实现快速搜索（避免重新加载），返回的JS（基于searchJs）亦检验亦效应。 */
	@StripMethods(strip=!BuildConfig.isDebug, keys={"getSyntheticField"})
	@Override
	public String getVirtualTextValidateJs(Object presenter, WebViewmy mWebView, long position) {
		//mWebView.fromNet=true;
		BookPresenter bookPresenter = (BookPresenter) presenter;
		if (searchJs!=null && position==0)
		try {
			searchJs = getSyntheticField("searchJs");
		} catch (IOException ignored) { }
		if (position==0)
		{
			String searchKey = bookPresenter.GetSearchKey();
			if (searchKey!=null)
			{
				// 记录一部分搜索历史
				long id = bookPresenter.GetSearchKeyId(searchKey);
				if (id>=0 && mLastKeyId!=id) {
					searchKeyIds.remove(id);
					searchKeyIds.add(0, id);
					int maxKeyStore = 100;
					if (searchKeyIds.size()>maxKeyStore) {
						for (int del=searchKeyIds.size()-1,i = del+1-maxKeyStore; i >= 0; i--) {
							searchKeyIds.remove(del, null);
						}
					}
					mLastKeyId=id;
					mRecordsDirty=true;
				}
				// 更新列表
				bookPresenter.a.adaptermy.notifyDataSetChanged(); //todo
				//takeHistoryRecord();
				// 检验、应用搜索搜索词
				if (searchJs!=null)
				{
					return searchJs.replace("%s", searchKey);
				}
				return null;
			}
			// 同一本书籍
			return mWebView.getUrl()!=null && mWebView.getUrl().startsWith(host)?"1":null;
		} else {
			position -= 1;
		}
		
		String ret;
		for (VirtualKeyProvider vkp:keyProviders) {
			ret = vkp.getVirtualTextValidateJs(bookPresenter, mWebView, position);
			if (ret!=null) return ret;
			position -= vkp.getSize();
		}
		
		return null;
	}
	
	public void saveWebSearches(Toastable_Activity context, LexicalDBHelper database) {
		if (mRecordsDirty && database!=null && database.testDBV2)
		{
			try {
				String kDumpName = "ws_"+getBooKID();
				long now = CMN.now();
				ContentValues value = new ContentValues();
				
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				DeflaterOutputStream inf = new DeflaterOutputStream(out);
				inf.write(searchKeyIds.getData(),0, searchKeyIds.getDataLength());
				inf.finish();
				inf.close();
				CMN.Log("大小："+searchKeyIds.getDataLength()+"  压缩后大小："+out.size(), searchKeyIds.size());
				
				byte[] data = out.toByteArray();
				value.put("type", 2);
				value.put("data", data);
				value.put("len", searchKeyIds.getDataLength());
				
				String[] where = new String[]{kDumpName};
				Cursor cursor = database.getDB().rawQuery("select id,edit_count from data where name=? limit 1", where);
				long id = -1;
				int edit_count = 0;
				if (cursor.moveToNext()) {
					id = cursor.getLong(0);
					edit_count = cursor.getInt(1);
				}
				value.put(FIELD_EDIT_TIME, now);
				value.put("edit_count", ++edit_count);
				cursor.close();
				if (id>=0) {
					where[0] = ""+id;
					database.getDB().update(TABLE_DATA_v2, value, "id=?", where);
				} else {
					value.put("name", kDumpName);
					value.put(FIELD_CREATE_TIME, now);
					id = database.getDB().insert(TABLE_DATA_v2, null, value);
				}
				//CMN.Log("dumpRecords::", kDumpName, id);
			} catch (IOException e) {
				CMN.Log(e);
			}
			mRecordsDirty = false;
		}
	}
	
	
	/** 返回目标网址 */
	@Override
	public String getVirtualRecordAt(Object presenter, long position) throws IOException {
		//if(mWebView==null)
		//	mWebView=this.mWebView;
		//boolean resposibleForThisWeb = mWebView==this.mWebView;

		//if(resposibleForThisWeb||a.PeruseView!=null&&a.PeruseView.mWebView==mWebView)
		//	tintBackground(mWebView);
		String key = null;
		String url=null;
		BookPresenter bookPresenter = (BookPresenter) presenter;  // todo check instance of
		//CMN.Log("PlainWeb::getVirtualRecordAt::", position, bookPresenter.GetSearchKey(), search);
		if (position==0) {
			String searchKey = bookPresenter.GetSearchKey();
			if(searchKey!=null) {
				if (searchKey.startsWith("http")) // 简单处理
					url = searchKey;
				else if (search != null) {
					key = searchKey;
					// 清空搜索词
					bookPresenter.SetSearchKey(null);
				}
			}
		} else {
			position-=1;
			//key = getEntryAt(position);
			
			String ret;
			for (VirtualKeyProvider vkp:keyProviders) {
				ret = vkp.getRecordAt(position);
				if (ret!=null) {
					if (ret.startsWith("http")) // 简单处理
						url = ret;
					else
						key = ret;
					break;
				}
				position -= vkp.getSize();
			}
			
			
//			if(pos<searchKeys.size()){
//				if(search!=null){
//					key=searchKeys.get(pos);
//				}
//			}
//			pos-=searchKeys.size();
//			//CMN.Log(pos, entrance.size(), entrance.get(0));
//			if(key==null && entrance.size()>0){
//				if(pos>=0 && pos<entrance.size()){
//					url = entrance.get(pos);
//					String[] arr = url.split("\r");
//					if(arr.length>1){
//						url = arr[0];
//					}
//					if(!url.contains("://"))
//						url=host+url;
//				}
//			}
//			pos-=entrance.size();
//			if (url==null && PageCursor != null) {
//				if (pos>=0 && pos < PageCursor.getCount()) {
//					PageCursor.moveToPosition(pos);
//					url = PageCursor.getString(0);
//					//CMN.Log("数据库取出的数据：", URLDecoder.decode(url));
//					if(!url.contains("://"))
//						url=host+url;
//					if(resposibleForThisWeb)
//						toolbar_title.setText(PageCursor.getString(1));
//				}
//				pos -= PageCursor.getCount();
//			}
		}
		if(url==null) {
			if(key!=null && search!=null){
				if(search.contains("%s"))
					url=search.replaceAll("%s", key);
				else
					url=search+key;
				if(!abSearch)
					url=host+url;
//				if(resposibleForThisWeb)
//					toolbar_title.setText(key);
			} else {
				url=host+index;
				//bookPresenter.SetSearchKey(key);  // ??WTF
			}
		}
//		if(mWebView==this.mWebView) {
//			if(a.opt.getHideScroll1()&&resposibleForThisWeb)
//				a.mBar.setVisibility(View.GONE);
//			else {
//				a.mBar.setDelimiter("< >", mWebView);
//			}
//		}

//		if (bookPresenter!=null) {
			StringBuilder sb = AcquireStringBuffer(8196)
					.append("window.shzh=app.rcsp(sid.get());");
			if(GlobalOptions.isDark) sb.append(DarkModeIncantation);
			if(bookPresenter.getContentEditable() && bookPresenter.getEditingContents() && bookPresenter.mWebView!=bookPresenter.a.wordPopup.mWebView)
				sb.append(MainActivityUIBase.ce_on);
			if (style!=null || stylex!=null) {
				//CMN.Log("style::", style);
				int st = loadJs.indexOf("}\"");
				if (st>0) {
					sb.append(loadJs, 0, st+1);
					if (style!=null) sb.append(style);
					if (stylex!=null) {
						JSONObject sex;
						for (int i = 0; i < stylex.size(); i++) {
							sex = stylex.getJSONObject(i);
							String urlRegex = WebxUrl(sex.getString("urlx"));
							if (urlRegex!=null) {
								Pattern pattern = Pattern.compile(urlRegex);
								if (pattern.matcher(url).find()) {
									sb.append(sex.getString("value"));
								}
							}
						}
					}
					sb.append(loadJs, st+1, loadJs.length());
				} else {
					sb.append(loadJs);
				}
			}
			else {
				sb.append(loadJs);
			}
			jsLoader = sb.toString();
//		} else {
//			jsLoader = jsLoaderInJson;
//		}
		//if(GlobalOptions.debug)CMN.Log("加载网页::", url);
		return currentUrl=url;
	}
	
	/** 返回完整网址 */
	private String WebxUrl(String url) {
		if (url!=null) {
			if (url.startsWith("/")) {
				if (url.startsWith("//"))
					url = host+index+url.substring(index.endsWith("/")?2:1);
				else
					url = host+url;
			}
		}
		return url;
	}
	
	public void takeHistoryRecord(BookPresenter bookPresenter, String key) {
		/* 接管网页历史纪录 */
		//searchKeys.remove(key);
		//searchKeys.add(0, key);
		bookPresenter.a.adaptermy.notifyDataSetChanged();
	}
	
	//	@Override
//	public void renderContentAt(float initialScale, int SelfIdx, int frameAt, WebViewmy mWebView, int...position) {
//		//if(mWebView==null)
//		//	mWebView=this.mWebView;
//		//boolean resposibleForThisWeb = mWebView==this.mWebView;
//
//		//if(resposibleForThisWeb||a.PeruseView!=null&&a.PeruseView.mWebView==mWebView)
//		//	tintBackground(mWebView);
//
//		if(SelfIdx!=-1){
//			preloading(mWebView, frameAt, SelfIdx, position);
//			if(resposibleForThisWeb && mWebView.fromCombined==1  && PDICMainAppOptions.getTmpIsCollapsed(tmpIsFlag)){/* 自动折叠 */
//				mWebView.awaiting = true;
//				mWebView.setVisibility(View.GONE);
//				return;
//			}
//		}
//		mWebView.fromNet=true;
//		mWebView.clearHistory=false;
//		String key = null;
//		String url=null;
//		int pos=mWebView.currentRendring[0];
//		if(pos>0) {
//			pos-=1;
//			if(pos<searchKeys.size()){
//				if(search!=null){
//					key=searchKeys.get(pos);
//				}
//			}
//			pos-=searchKeys.size();
//			//CMN.Log(pos, entrance.size(), entrance.get(0));
//			if(key==null && entrance.size()>0){
//				if(pos>=0 && pos<entrance.size()){
//					url = entrance.get(pos);
//					String[] arr = url.split("\r");
//					if(arr.length>1){
//						url = arr[0];
//					}
//					if(!url.contains("://"))
//						url=host+url;
//				}
//			}
//			pos-=entrance.size();
//			if (url==null && PageCursor != null) {
//				if (pos>=0 && pos < PageCursor.getCount()) {
//					PageCursor.moveToPosition(pos);
//					url = PageCursor.getString(0);
//					//CMN.Log("数据库取出的数据：", URLDecoder.decode(url));
//					if(!url.contains("://"))
//						url=host+url;
//					if(resposibleForThisWeb)
//						toolbar_title.setText(PageCursor.getString(1));
//				}
//				pos -= PageCursor.getCount();
//			}
//		}
//		else {
////			if(searchKey!=null) {
////				if (searchKey.startsWith("http"))
////					url = searchKey;
////				else if (search != null) {
////					key = searchKey;
////					/* 接管网页历史纪录 */
////					searchKeys.remove(key);
////					searchKeys.add(0, key);
////					a.adaptermy.notifyDataSetChanged();
////					searchKey = null;
////				}
////			}
//		}
//		if(url==null){
//			if(key!=null && search!=null){
//				if(search.contains("%s"))
//					url=search.replaceAll("%s", key);
//				else
//					url=search+key;
//				if(!abSearch)
//					url=host+url;
////				if(resposibleForThisWeb)
////					toolbar_title.setText(key);
//			}else{
//				url=host+index;
//			}
//		}
////		if(mWebView==this.mWebView) {
////			if(a.opt.getHideScroll1()&&resposibleForThisWeb)
////				a.mBar.setVisibility(View.GONE);
////			else {
////				a.mBar.setDelimiter("< >", mWebView);
////			}
////		}
//
//		StringBuilder sb = AcquireStringBuffer(8196).append("app.rcsp=")
//				.append(MakeRCSP(opt)).append(";")
//				.append(loadJs);
//		if (jsLoader != null) sb.append(jsLoader);
//		if (onload != null) sb.append(onload);
//		if (onstart != null) sb.append(onstart);
//		if(GlobalOptions.isDark) sb.append(MainActivityUIBase.DarkModeIncantation);
//		if(getContentEditable() && getEditingContents() && mWebView!=a.popupWebView)
//			sb.append(MainActivityUIBase.ce_on);
//
//		jsCode = sb.toString();
//
//		mWebView.loadUrl(currentUrl=url);
//	}

//	@Override
//	public void initViewsHolder(MainActivityUIBase a) {
//		super.initViewsHolder(a);
//		mWebView.addJavascriptInterface(mWebBridge, "app");
//		View.OnClickListener voyager = v -> {
//			int isRecess = v.getId()==R.id.recess?-1:1;
//			if (mWebView.canGoBackOrForward(isRecess)) {
//				preloading(mWebView, -1, -1, null);
//				mWebView.goBackOrForward(isRecess);
//			}
//		};
//		mWebView.MutateBGInTitle();
//
//		recess.setOnClickListener(voyager);
//		forward.setOnClickListener(voyager);
//		setSaveIconLongClick(ic_save);
//		setWebLongClickListener(mWebView, a);
//		if(computerFace) {
//			mWebView.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.117 Safari/537.36");
//		}
//	}
	
	@Override
	public int[] getPageUtils(boolean extra) {
		return extra?new int[]{
				R.string.bmAdd
				,R.string.page_dakai
				,R.string.page_del
//				,R.string.page_fuzhi
//				,R.string.page_baocun
				,R.string.page_lianjie
//				,R.string.peruse_mode
		}:new int[]{
				R.string.page_yuan
				,R.string.refresh
				,R.string.page_rukou
				,R.string.page_nav
				,R.string.page_ucc
		};
	}
	
	@Override
	public boolean handlePageUtils(BookPresenter presenter, WebViewmy mWebView, int pos) {
		switch (pos) {
			case R.string.page_yuan:
				mWebView.setTag(R.id.save, false);
			case R.string.refresh:
				mWebView.reload();
			return true;
			case R.string.page_dakai:
				try {
					String url = mWebView.getUrl();
					Intent intent = new Intent();
					intent.setData(Uri.parse(url));
					intent.setAction(Intent.ACTION_VIEW);
					presenter.a.startActivity(intent);
				} catch (Exception e) { }
			return true;
			case R.string.page_rukou:
//					String _url = final_mWebView.getUrl();
//					if(_url!=null) {
//						EditText etNew = FU.buildStandardTopETDialog(a, false);
//						etNew.setText(final_mWebView.getTitle());
//						View btn_Done = ((View)etNew.getTag());
//						View btn_exec = ((View)btn_Done.getTag());
//						btn_Done.setTag(btn_exec.getTag());
//						btn_exec.setVisibility(View.GONE);
//						btn_Done.setOnClickListener(v1 -> {
//							String __url = _url;
//							String name = etNew.getText().toString();
//							if(name.length()>0) __url += "\r"+etNew.getText();
//							if(entrance==null) entrance=new ArrayList<>();
//							entrance.add(__url);
//							bNeedSave = true;
//							a.notifyDictionaryDatabaseChanged(PlainWeb.this);
//							((Dialog)v1.getTag()).dismiss();
//						});
//					}
			return true;
			case R.string.page_lianjie:
				presenter.a.FuzhiText(mWebView.getUrl());
			return true;
			case R.string.page_nav:
			return true;
			default:
			return false;
		}
	}
	
	//	@Override
//	public void unload() {
//		if(bNeedSave) { // 保存 json
//			if(entrance!=null) {
//				String _entrance = StringUtils.EMPTY;
//				int size = entrance.size();
//				if(size>0) {
//					StringBuilder sb = AcquireStringBuffer((int) (size*entrance.get(0).length()*1.5));
//					for (int i = 0; i < size; i++) {
//						sb.append(entrance.get(i));
//						if(i<size-1) {
//							sb.append("\n");
//						}
//					}
//					_entrance = sb.toString();
//				}
//				website.put("entrance", _entrance);
//			}
//
//			String webout = website.toString(SerializerFormat);
//
//			webout = webout.replaceAll("\t\"(.*)\":", " $1:").replace("\\n", "\n");
//
//			CMN.Log(webout);
//
//			BU.printFile(webout.getBytes(), f);
//
//			bNeedSave=false;
//		}
//		super.unload();
//	}
	
//	public void searchFor(String key) {
//		String url=host+index;
//		if(search!=null){
//			if(search.contains("%s"))
//				url+=search.replaceAll("%s", key);
//			else url=url+search+key;
//		}
//		mWebView.loadUrl(url);
//	}

//	public void preloading(WebViewmy mWebView, int frameAt, int SelfIdx, int[] position) {
//		currentDisplaying="";
//		if(frameAt!=-1 || SelfIdx!=-1){
//			mWebView.stopLoading();
//			mWebView.setTag(mWebView.SelfIdx=SelfIdx);
//			mWebView.frameAt=frameAt;
//			mWebView.currentRendring = position;
//			if(mWebView.wvclient!=a.myWebClient) {
//				mWebView.setWebChromeClient(a.myWebCClient);
//				mWebView.setWebViewClient(a.myWebClient);
//			}
//			if(mWebView.getTag(R.drawable.popup_background)!=f){
//				WebSettings settings = mWebView.getSettings();
//				if (Build.VERSION.SDK_INT >= 21) {
//					settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
//				}
//				settings.setBlockNetworkImage(noImage);
//				//settings.setJavaScriptEnabled(!banJs);
//				mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
//				mWebView.setTag(R.drawable.popup_background, f);
//			}
//		}
//		if(mWebView.fromCombined!=2)
//			a.checkW10_full();
//	}
	
	@Override
	public void Reload(Object context) {
		try {
			parseJsonFile(context instanceof Context?(Context)context:this.context);
		} catch (IOException e) {
			CMN.Log(e);
		}
	}
	
	public boolean hasCover() {
		return false;
	}

	public void onPageStarted(BookPresenter bookPresenter, WebView view, String url, boolean updateTitle) {
		WebViewmy mWebView = (WebViewmy) view;
		if (bookPresenter.getContentEditable()) {
			mWebView.bShouldOverridePageResource = true;
		}
		//if(updateTitle) mWebView.toolbar_title.setText(bookPresenter.currentDisplaying=view.getTitle());
		if(progressProceed !=null) {
			progressProceed.cancel();
		}
		if(mWebView.titleBar!=null) {
			mWebView.titleBar.getBackground().setLevel(1500);
			((LayerDrawable)mWebView.titleBar.getBackground()).getDrawable(1).setAlpha(255);
		}
		//onProgressChanged(mWebView, 5);
		
		currentUrl=view.getUrl();
//		if(jsLoader!=null || onstart!=null) {
//			String jsCode = "app.rcsp=" + bookPresenter.MakeRCSP(opt) + ";";
//			if (jsLoader != null) jsCode += jsLoader;
//			if (onstart != null) jsCode += onstart;
//			////view.evaluateJavascript(jsCode, null);
//		}
		//CMN.Log("onPageStarted\n\nonPageStarted -- ", url);CMN.rt();CMN.stst_add=0;
		if(GlobalOptions.isDark) mWebView.evaluateJavascript(DarkModeIncantation, null);
	}

	public void onProgressChanged(BookPresenter bookPresenter, WebViewmy  mWebView, int newProgress) {
		//CMN.debug("onProgressChanged", newProgress, mWebView.getProgress());
		if(mWebView.titleBar!=null) {
			Drawable d = mWebView.titleBar.getBackground();
			int start = d.getLevel();
			int end = newProgress*100;
			if(end<start) end=start+10;
			if(progressProceed!=null) {
				progressProceed.cancel();
				progressProceed.setIntValues(start, end);
			} else {
				progressProceed = ObjectAnimator.ofInt(d,"level", start, end);
				progressProceed.setDuration(100);
			}
			progressProceed.start();
		}
		if(GlobalOptions.isDark && newProgress>5) {
			if (Build.VERSION.SDK_INT>=21) { //todo webview版本 23 未测试
				mWebView.evaluateJavascript("document._pdkn||app.loadJs(sid.get(), 'dk.js')", null);
			} else {
				mWebView.evaluateJavascript(DarkModeIncantation, null);
			}
		}
		if(newProgress>89) {
			mWebView.wvclient.onPageFinished(mWebView, mWebView.getUrl());
		}
		else if(newProgress>=85) {
			//fadeOutProgressbar(bookPresenter, (WebViewmy) mWebView, newProgress>87);
			mWebView.postFinished();
		}
		if(mWebView.bShouldOverridePageResource && newProgress>=45) {
			mWebView.bShouldOverridePageResource = false;
		}
//		if(newProgress>=98){
//			CMN.debug("newProgress>=98", newProgress);
//			fadeOutProgressbar(bookPresenter, mWebView, newProgress>=99);
//		}
	}

	/** 接管进入黑暗模式、编辑模式 */
	@StripMethods(strip=!BuildConfig.isDebug, keys={"getSyntheticField"})
	public void onPageFinished(BookPresenter bookPresenter, WebViewmy mWebView, String url, boolean updateTitle) {
		CMN.debug("chromium", "web  - onPageFini_NWPshed", currentUrl);
		mWebView.removePostFinished();
		fadeOutProgressbar(bookPresenter, mWebView, updateTitle);
		currentUrl=mWebView.getUrl();
		if (Build.VERSION.SDK_INT<21) mWebView.evaluateJavascript(kitkatCompatJs, null);
		mWebView.evaluateJavascript(jsLoader, new ValueCallback<String>() {
			@Override
			public void onReceiveValue(String value) {
			}
		});
		if (jsCode != null)
		try {
			jsCode = getSyntheticField("js");
		} catch (Exception ignored) { }
		if (jsCode != null) {
			mWebView.evaluateJavascript(jsCode, null);
		}
		if (onload != null) mWebView.evaluateJavascript(onload, null);
		if (onstart != null) mWebView.evaluateJavascript(onstart, null);
		//mWebView.evaluateJavascript("window.loadJsCb=function(){erdo.init()};app.loadJs(sid.get(), 'erdo.js')", null);
	}

	private void fadeOutProgressbar(BookPresenter bookPresenter, WebViewmy mWebView, boolean updateTitle) {
		//if(updateTitle) mWebView.toolbar_title.setText(mWebView.word=bookPresenter.currentDisplaying=mWebView.getTitle());
		if(mWebView.titleBar!=null) {
			Drawable d = ((LayerDrawable) mWebView.titleBar.getBackground()).getDrawable(1);
			if(d.getAlpha()!=255) {
				return;
			}
			if(progressTransient!=null){
				progressTransient.cancel();
				progressTransient.setFloatValues(d.getAlpha(), 0);
			} else {
				progressTransient = ObjectAnimator.ofInt(d, "alpha", d.getAlpha(), 0);
				progressTransient.setDuration(200);
			}
			progressTransient.start();
		}
	}

	@Override
	public String getLexicalEntryAt(int position) {
		return "第"+position+"页";
	}

	@Override
	public String getEntryAt(long position, Flag mflag) {
		return getEntryAt(position);
	}

	@Override
	public int lookUp(String keyword, boolean isSrict) {
		return takeWord(keyword)?0:-100;
	}
	
	/** combined search page constituents :
	 * from key word [just one,
	 * 		and should be ( I. during combined search mode ) added to searchKeys after you've clicked that entry.
	 * 		or it's displaying contents directly and meanwhile 'enter' is typed.
	 * 		( II. during single search mode ) added to searchKeys after typing 'enter'.
	 * 		] ;
	 *
	 * from mimicking others' results [multiple. ]
	 * @return*/
	public int lookUpRange(String keyword, ArrayList<myCpr<String, Long>> rangReceiver, RBTree_additive treeBuilder, long SelfAtIdx, int theta, AtomicBoolean task) //多线程
	{
		if(takeWord(keyword)) {
			if(treeBuilder !=null)
				treeBuilder.insert(keyword, SelfAtIdx, (long) 0);
			else
				rangReceiver.add(new myCpr<>(keyword, (long) 0));
			isListDirty=true;
			return 1;
		}
		return 0;
	}

//	@Override
//	public String getRecordsAt(int... positions) throws IOException {
//		return "";
//	}
	
	@Override
	public InputStream getResourceByKey(String key) {
	 	//CMN.Log("getResourceByKey::", key);
		return null;
	}

	@Override
	public boolean hasMdd() {
		return true;
	}

//	@Override
//	protected InputStream mOpenInputStream(){
//		return null;
//	}

//	@Override
//	protected boolean StreamAvailable() {
//		return false;
//	}
	

//
//	@Override
//	public void flowerFindAllContents(String key, int selfAtIdx, AbsAdvancedSearchLogicLayer SearchLauncher) throws IOException {
//	}
//
//	@Override
//	public void flowerFindAllKeys(String key, int SelfAtIdx, AbsAdvancedSearchLogicLayer SearchLauncher) {
//	}

//	@Override
//	public CachedDirectory getInternalResourcePath(boolean create) {
//		if(create && InternalResourcePath!=null){
//			InternalResourcePath.mkdirs();
//		}
//		return InternalResourcePath;
//	}

//	@Override
//	public String getAboutString() {
//		return _Dictionary_fName+"<br>"
//				+ website.toString(SerializerFormat)
//					.replaceAll("\n\t\"(.*)\":", "<br>\t$1:").replace("\\n", "\n");
//	}

	/** Accept given key keyword or not. */
	public boolean takeWord(String key) {
		if(keyPattern!=null){
			return keyPattern.matcher(key).find();
		}
		return true;
	}
	
	public static class MyX509TrustManager implements X509TrustManager {
		@Override
		public void checkClientTrusted(X509Certificate certificates[], String authType) {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] ax509certificate,String s) {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return new java.security.cert.X509Certificate[] {};
		}
	}
	
	@Override
	public String getCharsetName() {
		return "online";
	}
	
	public boolean getIsTranslator() {
		return isTranslator;
	}
	
	public boolean getHasModifiers() {
		return hasModifiers;
	}
	
	@Override
	public String getField(String fieldName) {
		return website.getString(fieldName);
	}
	
	public boolean hasField(String fieldName) {
		return website.containsKey(fieldName);
	}
}