package com.knziha.plod.dictionarymodels;

import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionary.Utils.SU;
import com.knziha.plod.plaindict.BasicAdapter;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.WebViewListHandler;
import com.knziha.plod.widgets.ViewUtils;
import com.knziha.plod.widgets.WebViewmy;
import com.knziha.rbtree.additiveMyCpr1;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Recorder rendering search results as : LinearLayout {WebView, WebView, ... }  */
public class resultRecorderCombined extends resultRecorderDiscrete {
	private List<additiveMyCpr1> data;
	int firstItemIdx;
	public View scrollTarget;

	public List<additiveMyCpr1> list(){return data;}
	private List<BookPresenter> md;
	
	public resultRecorderCombined(MainActivityUIBase a, List<additiveMyCpr1> data_, List<BookPresenter> md_){
		super(a);
		data=data_;
		md=md_;
	}
	
	public boolean FindFirstIdx(String key, AsyncTask task) {
		int cc=0;
		for (int i = 0; i < data.size(); i++) {
			if(cc++>350) { if(task.isCancelled()) return false; cc=0; }
			if(data.get(i).key.regionMatches(true, 0, key, 0, key.length())) {
				firstItemIdx = i;
				break;
			}
		}
		return true;
	}
	
	private int RemapPos(int pos) {
		if(firstItemIdx>0) {
			pos = (pos+firstItemIdx)%data.size();
		}
		return pos;
	}
	
	@Override
	public ArrayList<Long> getBooksAt(ArrayList<Long> books, int pos) {
		ArrayList<Long> data = getRecordAt(pos);
		if(books==null) books=new ArrayList<>(data.size()/2);
		else {books.clear();books.ensureCapacity(data.size()/2);}
		long last=-1;
		for(int i=0;i<data.size();i+=2) {
			if(last!=data.get(i))
				books.add(last=data.get(i));
		}
		return books;
	}

	@Override
	public long getOneDictAt(int pos) {
		return getRecordAt(pos).get(0);
	}

	@Override
	public boolean checkAllWebs(MainActivityUIBase a, ArrayList<BookPresenter> md) {
		ArrayList<Long> data = getRecordAt(0);
		allWebs=true;
		for(int i=0;i<data.size();i+=2) {
			long toFind=data.get(i);
			if (a.getBookById(toFind).getType()!=DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_WEB) {
				allWebs=false;
				break;
			}
		}
		return allWebs;
	}

	@Override
	public CharSequence getResAt(MainActivityUIBase a, long pos) {
		if(data==null || pos<0 || pos>data.size()-1)
			return "!!! Error: code 1";
		if(firstItemIdx>0) pos = RemapPos((int) pos);
		List<Long> l = ((List<Long>) data.get((int) pos).value); //todo
		bookId = l.get(0);
		int sz=l.size()/2;
		count = sz>1?String.format("%02d", sz):null;
		return data.get((int) pos).key;
	};

	public boolean scrolled=false, toHighLight;
	public int LHGEIGHT;
	
	@Override
	public void renderContentAt(long pos, final MainActivityUIBase a, BasicAdapter ADA, WebViewListHandler weblistHandler){
		CMN.Log("renderContentAt::", pos, weblistHandler.bMergeFrames, weblistHandler.bMergingFrames, weblistHandler.getChildCount());
		scrollTarget=null;
		final ScrollView sv = (ScrollView) weblistHandler.getScrollView();
		toHighLight=a.hasCurrentPageKey();
		if(toHighLight || expectedPos==0)
			sv.scrollTo(0, 0);
		else{
			sv.scrollTo(0, expectedPos);
		}
		additiveMyCpr1 jointResult;
		if(pos==-2) {
			if(weblistHandler.bMergingFrames) {
				jointResult = weblistHandler.getMergedFrame().jointResult;
			} else {
				jointResult = weblistHandler.jointResult;
			}
		} else {
			if(firstItemIdx>0) pos = RemapPos((int) pos);
			jointResult = data.get((int) pos);
		}
		
		if(jointResult==null) {
			a.showT("ERROR "+pos+" "+weblistHandler.bMergingFrames+" "+weblistHandler.bMergeFrames);
			return;
		}
		List<Long> vals = (List<Long>) jointResult.value;
		
		boolean bUseMergedUrl = weblistHandler.bMergeFrames;
		
		if(!bUseMergedUrl) {
			a.showT("未更新？"+CMN.Log(weblistHandler.jointResult==jointResult, weblistHandler.getChildCount()==weblistHandler.frames.size()
			, weblistHandler.getChildCount(),weblistHandler.frames.size()));
		}
		if(bUseMergedUrl && weblistHandler.getMergedFrame().jointResult==jointResult
			|| !bUseMergedUrl && weblistHandler.jointResult==jointResult && weblistHandler.getChildCount()==weblistHandler.frames.size()) {
			weblistHandler.initMergedFrame(bUseMergedUrl, weblistHandler.bShowingInPopup, bUseMergedUrl);
			a.showT("未更新！");
			return;
		}
		
		if(!bUseMergedUrl) {
			// todo remove adaptively .
			//weblistHandler.removeAllViews();
		}
		
		//if(false)
		//yyy
		a.awaiting = true;
		//weblistHandler.installLayoutScrollListener(this);
		
		ArrayList<Long> valsTmp = new ArrayList<>();
		int valueCount=0;
		boolean checkReadEntry = a.opt.getAutoReadEntry();
		boolean bNeedExpand=true;
		ViewGroup webholder = weblistHandler;
		long toFind;
		View expTbView = null;
		StringBuilder mergedUrl = null;
		weblistHandler.frames.clear();
		for(int i=0;i<vals.size();i+=2){
			valsTmp.clear();
			toFind=vals.get(i);
			while(i<vals.size() && toFind==vals.get(i)) {
				valsTmp.add(vals.get(i+1));
				i+=2;
			}
			i-=2;
			
			BookPresenter presenter = a.getBookById(toFind);
			
			if(presenter==a.EmptyBook) continue;
			
			weblistHandler.frames.add(toFind);
			
			if(!bUseMergedUrl) {
				long[] p = new long[valsTmp.size()];
				for(int i1 = 0;i1<valsTmp.size();i1++){
					p[i1] = valsTmp.get(i1);
				}
				//if(Build.VERSION.SDK_INT>=22)...// because kitkat's webview is not that adaptive for content height
				presenter.initViewsHolder(a);
				ViewGroup rl = presenter.rl;
				WebViewmy mWebView = presenter.mWebView;
				int frameAt=valueCount;
				//if(rl.getParent()!=a.weblistHandler.getViewGroup())
				{
					ViewUtils.removeView(rl);
					frameAt=Math.min(frameAt, webholder.getChildCount());
					webholder.addView(rl,frameAt);
				}
				//else
				//	a.showT("yes: "+mdtmp.getPath());
				//mdtmp.vll=vll;

				/*//for debug usage
				if(mdtmp.mWebView.getLayerType()!=View.LAYER_TYPE_NONE)
					mdtmp.mWebView.setLayerType(View.LAYER_TYPE_NONE, null);
				*/
				//mdtmp.mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
				mWebView.setTag(R.id.toolbar_action5, i==0&&toHighLight?false:null);
				mWebView.fromCombined=1;
				if(presenter.getType()==DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_WEB)
				{
					presenter.SetSearchKey(jointResult.key);
				}
				//CMN.debug("combining_search_result.renderContentAt::", frameAt);
				presenter.renderContentAt(-1, BookPresenter.RENDERFLAG_NEW, frameAt,null, p);
				if(!mWebView.awaiting){
					bNeedExpand=false;
					if(checkReadEntry){
						presenter.mWebView.bRequestedSoundPlayback=true;
						checkReadEntry=false;
					}
				} else if(bNeedExpand && !presenter.getNeedsAutoFolding(mWebView.frameAt)) {
					expTbView = mWebView.toolbar_title;
					bNeedExpand = false;
				}
				mWebView.fromCombined=1;
			}
			else {
				if(mergedUrl==null)
					mergedUrl = new StringBuilder("http://MdbR.com/MERGE.jsp?q=")
						.append(SU.encode(jointResult.key)).append("&exp=");
				else mergedUrl.append("-");
				mergedUrl.append("d");
				IU.NumberToText_SIXTWO_LE(presenter.getId(), mergedUrl);
				for (Long val:valsTmp) {
					mergedUrl.append("_");
					IU.NumberToText_SIXTWO_LE(val, mergedUrl);
				}
			}
			valueCount++;
		}
		weblistHandler.initMergedFrame(bUseMergedUrl, weblistHandler.bShowingInPopup, bUseMergedUrl);
		if(bUseMergedUrl) {
			WebViewmy mWebView = weblistHandler.mMergedFrame;
			CMN.debug("mergedUrl::", mergedUrl);
			mWebView.loadUrl(mergedUrl.toString());
//			mWebView.loadUrl("https://en.m.wiktionary.org/wiki/Wiktionary:Word_of_the_day/Archive/2016/September");
			a.RecalibrateWebScrollbar(mWebView);
			mWebView.jointResult=jointResult;
		}
		else {
			if(bNeedExpand && PDICMainAppOptions.getEnsureAtLeatOneExpandedPage()){
				//expTbView = webholder.findViewById(R.id.toolbar_title); //yyy!!!
				expTbView = weblistHandler.getViewGroup().findViewById(R.id.toolbar_title); //yyy!!!
			}
			if (expTbView != null) {
				expTbView.performClick();
			}
			weblistHandler.jointResult=jointResult;
		}
		a.RecalibrateWebScrollbar(null);
	}

	@Override
	public ArrayList<Long> getRecordAt(int pos) {
		if(firstItemIdx>0) pos = RemapPos(pos);
		return (ArrayList<Long>) data.get(pos).value;
	}

	@Override
	public int size(){
		if(data==null)
			return 0;
		else
		return data.size();
	};
	
	@Override
	public void shutUp() {
		data.clear();
		firstItemIdx = 0;
	}

	public void scrollTo(View _scrollTarget, MainActivityUIBase a) {
		scrollTarget=_scrollTarget;
		a.weblistHandler.NotifyScrollingTo(this);
		scrolled=false;
	}
}
