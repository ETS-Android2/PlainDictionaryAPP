package com.knziha.plod.plaindict;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.os.Message;
import android.provider.Settings;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;

import com.bumptech.glide.load.engine.cache.DiskCache;
import com.google.android.material.appbar.AppBarLayout;
import com.knziha.filepicker.view.FilePickerDialog;
import com.knziha.filepicker.view.WindowChangeHandler;
import com.knziha.plod.PlainUI.AppUIProject;
import com.knziha.plod.PlainUI.MenuGrid;
import com.knziha.plod.PlainUI.PlainAppPanel;
import com.knziha.plod.PlainUI.SearchToolsMenu;
import com.knziha.plod.PlainUI.WeakReferenceHelper;
import com.knziha.plod.db.SearchUI;
import com.knziha.plod.dictionary.SearchResultBean;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionary.Utils.SU;
import com.knziha.plod.dictionary.mdict;
import com.knziha.plod.dictionarymanager.files.BooleanSingleton;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.DictionaryAdapter;
import com.knziha.plod.dictionarymodels.PlainWeb;
import com.knziha.plod.dictionarymodels.resultRecorderScattered;
import com.knziha.plod.plaindict.databinding.ActivityMainBinding;
import com.knziha.plod.searchtasks.AsyncTaskWrapper;
import com.knziha.plod.searchtasks.BuildIndexTask;
import com.knziha.plod.searchtasks.FullSearchTask;
import com.knziha.plod.searchtasks.FuzzySearchTask;
import com.knziha.plod.searchtasks.VerbatimSearchTask;
import com.knziha.plod.widgets.AdvancedNestScrollListview;
import com.knziha.plod.widgets.AdvancedNestScrollView;
import com.knziha.plod.widgets.AdvancedNestScrollWebView;
import com.knziha.plod.widgets.BottomNavigationBehavior;
import com.knziha.plod.widgets.CheckableImageView;
import com.knziha.plod.widgets.FlowTextView;
import com.knziha.plod.widgets.HeightProvider;
import com.knziha.plod.widgets.PageSlide;
import com.knziha.plod.widgets.ListViewmy;
import com.knziha.plod.widgets.NoSSLv3SocketFactory;
import com.knziha.plod.widgets.NoScrollViewPager;
import com.knziha.plod.widgets.OnScrollChangedListener;
import com.knziha.plod.widgets.ScreenListener;
import com.knziha.plod.widgets.ViewUtils;
import com.knziha.plod.widgets.WebViewmy;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static com.knziha.plod.PlainUI.AppUIProject.RebuildBottombarIcons;
import static com.knziha.plod.dictionary.SearchResultBean.SEARCHENGINETYPE_PLAIN;
import static com.knziha.plod.dictionary.SearchResultBean.SEARCHENGINETYPE_REGEX;
import static com.knziha.plod.dictionary.SearchResultBean.SEARCHENGINETYPE_WILDCARD;
import static com.knziha.plod.dictionary.SearchResultBean.SEARCHTYPE_SEARCHINNAMES;
import static com.knziha.plod.dictionary.SearchResultBean.SEARCHTYPE_SEARCHINTEXTS;
import static com.knziha.plod.dictionarymodels.DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_WEB;
import static com.knziha.plod.plaindict.CMN.GlobalPageBackground;
import static com.knziha.plod.plaindict.DeckListAdapter.SelectionMode_peruseview;
import static com.knziha.plod.plaindict.PDICMainAppOptions.PLAIN_TARGET_FLOAT_SEARCH;
import static com.knziha.plod.plaindict.PDICMainAppOptions.PLAIN_TARGET_INPAGE_SEARCH;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

/**
 * 主程序 - 单实例<br/>
 * Our single instanced Main Interface.<br/>
 * Created by KnIfER on 2018.
 */
@SuppressLint({"SetTextI18n", "ClickableViewAccessibility","PrivateApi","DiscouragedPrivateApi","ResourceType"})
public class PDICMainActivity extends MainActivityUIBase implements OnClickListener, OnLongClickListener, OnMenuItemClickListener{
	public String textToSetOnFocus;
	public static int taskCounter = 0;
	public Timer timer;
	public int currentSearchingDictIdx;
	public TextView dvTitle;
	public SeekBar dvSeekbar;
	public TextView dvProgressFrac;
	public TextView dvDictFrac;
	public TextView dvResultN;

	public ListViewmy mlv1;
	public ListViewmy mlv2;

	public String lastFuzzyKeyword;
	public String lastFullKeyword;
	public String lastKeyword;
	
	ActionBarDrawerToggle mDrawerToggle;
	
	private MyHandler mHandle;
	public AsyncTaskWrapper<String, Object, String> mAsyncTask;
	private LinearLayout weblist;
	
	public ActivityMainBinding UIData;
	
	/** 定制底栏一：<br/>
	 * 选择词典1 选择分组2 词条搜索3 全文搜索4 进入收藏5 进入历史6 <br/>
	 * 退离程序7 打开侧栏8 随机词条9 上一词典10 下一词典11 调整亮度12 定制底栏13 定制颜色14 管理词典15 进入设置16<br/>*/
	public final static int[] BottombarBtnIcons = new int[]{
			R.drawable.book_list,
			R.drawable.book_bundle,
			R.drawable.fuzzy_search,
			R.drawable.full_search,
			R.drawable.favoriteg,
			R.drawable.customize_bars,
			R.drawable.ic_menu_24dp,
			R.drawable.historyg,
			R.drawable.ic_exit_app,
			R.drawable.ic_menu_drawer_24dp,
			R.drawable.ic_shuffle_black_24dp,
			R.drawable.ic_prv_dict_chevron,
			R.drawable.ic_nxt_dict_chevron,
			R.drawable.ic_brightness_low_black_24dp,
			R.drawable.ic_swich_landscape_orientation,
			R.drawable.ic_options_toolbox_small,
			R.drawable.book_bundle2,
			R.drawable.ic_settings_black_24dp,
			R.drawable.ic_keyboard_show_24,
	};
	public final ImageView[] BottombarBtns = new ImageView[BottombarBtnIcons.length];
	
	public AppUIProject bottombar_project;
	private static int LauncherInstanceCount;
	private EnchanterReceiver locationReceiver;
	private int barSzBot;
	private float barSzRatio;
	
	@Override
	public void onConfigurationChanged(@NonNull Configuration newConfig) {
		//CMN.Log("onConfigurationChanged",mConfiguration==newConfig, dm==getResources().getDisplayMetrics(), !isLocalesEqual(mConfiguration, newConfig));
		if(!systemIntialized) return;
		if(!isLocalesEqual(mConfiguration, newConfig) && "".equals(opt.getLocale())){
			recreate();
			super.onConfigurationChanged(newConfig);
			return;
		}
		super.onConfigurationChanged(newConfig);
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		if(mConfiguration.orientation!=newConfig.orientation) {
			if(root.getTag()!=null) {
				MarginLayoutParams lp = (MarginLayoutParams) root.getLayoutParams();
				int mT=DockerMarginT;
				int mB=DockerMarginB;
				DockerMarginT=DockerMarginL;
				DockerMarginB=DockerMarginR;
				DockerMarginL=mT;
				DockerMarginR=mB;
				lp.leftMargin = DockerMarginL;
				lp.rightMargin = DockerMarginR;
				lp.topMargin = 0;
				lp.bottomMargin = DockerMarginB;
				root.setLayoutParams(lp);
			}
			actionBarSize = (int) getResources().getDimension(R.dimen.barSize);
			//barSzRatio
			bottombar.getLayoutParams().height=barSzBot;
			
			toolbar.getLayoutParams().height=actionBarSize;
			UIData.appbar.getLayoutParams().height=actionBarSize;
			UIData.appbar.requestLayout();
			refreshContentBow(opt.isContentBow(), actionBarSize);

			if(d!=null) {
				if(d instanceof WindowChangeHandler)
					((WindowChangeHandler)d).OnWindowChange(dm);
				else {
					Window win = d.getWindow();
					if(win!=null && win.getDecorView().getWidth()>dm.widthPixels) {
						win.getAttributes().width = (int) (dm.widthPixels-2.5*getResources().getDimension(R.dimen.diagMarginHor));
						win.setAttributes(win.getAttributes());
					}
				}
			}

			if(bottombar_project!=null&&bottombar_project.bNeedCheckOrientation){
				RebuildBottombarIcons(this, bottombar_project, newConfig);
			}
			if(dictPicker !=null) {
				if(dictPicker.isVisible()) {
//					dialogHolder.setTag(null);//111
				} else {
					//ResizeDictPicker();
				}
			}
			if(GlobalOptions.isLarge) {
				drawerFragment.mDrawerListLayout.getLayoutParams().width = -1;
			}
		}
		mConfiguration.setTo(newConfig);
		if(Build.VERSION.SDK_INT>=29 && false){
			GlobalOptions.isDark = (mConfiguration.uiMode & Configuration.UI_MODE_NIGHT_MASK)==Configuration.UI_MODE_NIGHT_YES;
		} else {
			GlobalOptions.isDark = false;
		}
		opt.getInDarkMode();
		//CMN.Log("GlobalOptionsGlobalOptions", GlobalOptions.isDark, isDarkStamp);
		if(GlobalOptions.isDark!=isDarkStamp)
			changeToDarkMode();
		isDarkStamp=GlobalOptions.isDark;
		GlobalOptions.density = dm.density;
		if(settingsPanel!=null) {
			root.postDelayed(postOnConfigurationChanged, 200);
		}
	}
	
	Runnable postOnConfigurationChanged = new Runnable() {
		@Override
		public void run() {
			if(settingsPopup!=null) {
				//settingsPopup.dismiss();
				embedPopInCoordinatorLayout(settingsPanel, settingsPopup, settingsPanel==null?0:settingsPanel.bottomPadding, root);
			} else if(settingsPanel!=null ){
				int pad = UIData.bottombar.getHeight();
				if (UIData.appbar.getTop()>=0) {
					pad+=UIData.appbar.getHeight();
				}
				settingsPanel.setInnerBottomPadding(pad);
			}
			MenuGrid menuGrid = (MenuGrid) getReferencedObject(WeakReferenceHelper.menu_grid);
			if(menuGrid!=null) {
				menuGrid.refreshMenuGridSize(false);
			}
		}
	};
	
	
	private boolean isLocalesEqual(Configuration oldConfig, Configuration newConfig) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			LocaleList localeA = oldConfig.getLocales();
			LocaleList localeB = newConfig.getLocales();
			if(localeA.size()==0 || localeB.size()==0) return false;
			return localeA.get(0).equals(localeB.get(0));
		}
		return oldConfig.locale.equals(newConfig.locale);
	}

	/** 退出高级搜索 */
	public void exitFFSearch() {
		taskd=null;
		if(mAsyncTask!=null)
			mAsyncTask.stop(true);
		dvSeekbar = null;
		dvProgressFrac = null;
		dvResultN = null;
		currentSearchLayer.IsInterrupted=true;
	}

	public void OnEnterFullSearchTask(AsyncTaskWrapper task) {
		taskCounter=loadManager.md_size;
		AdvancedSearchInterface _currentSearchLayer = currentSearchLayer = fullSearchLayer;
		_currentSearchLayer.dirtyProgressCounter=
		_currentSearchLayer.dirtyResultCounter=0;
		_currentSearchLayer.IsInterrupted=false;
		ShowProgressDialog().findViewById(R.id.cancel).setOnClickListener(v13 -> {
			if(!_currentSearchLayer.IsInterrupted){
				_currentSearchLayer.IsInterrupted=true;
				task.stop(false);
			}else{
				task.stop(true);
				((FullSearchTask)task).harvest(true);
				mAsyncTask=null;
				if(taskd!=null){
					taskd.dismiss();
					taskd=null;
				}
				showT("强制关闭");
			}
		});
		for(int i=0;i<loadManager.md_size;i++) {//遍历所有词典
			BookPresenter presenter = loadManager.md_getAt(i);
			if(presenter!=null) {
				presenter.purgeSearch(SEARCHTYPE_SEARCHINTEXTS);
			}
		}
		CMN.stst = System.currentTimeMillis();
	}
	
	public void OnEnterBuildIndexTask(BuildIndexTask task) {
		taskCounter=IndexingBooks.size();
		AdvancedSearchInterface _currentSearchLayer = currentSearchLayer = fullSearchLayer;
		taskRecv = _currentSearchLayer;
		_currentSearchLayer.dirtyProgressCounter=
		_currentSearchLayer.dirtyResultCounter=0;
		_currentSearchLayer.IsInterrupted=false;
		ShowProgressDialog().findViewById(R.id.cancel).setOnClickListener(v13 -> {
			if(!_currentSearchLayer.IsInterrupted){
				_currentSearchLayer.IsInterrupted=true;
				task.stop(false);
			}else{
				task.stop(true);
				task.harvest(true);
				mAsyncTask=null;
				if(taskd!=null){
					taskd.dismiss();
					taskd=null;
				}
				showT("强制关闭");
			}
		});
		CMN.stst = System.currentTimeMillis();
	}

	public void OnEnterFuzzySearchTask(AsyncTaskWrapper task) {
		taskCounter=loadManager.md_size;
		currentSearchLayer=fuzzySearchLayer;
		fuzzySearchLayer.dirtyProgressCounter=
		fuzzySearchLayer.dirtyResultCounter=0;
		fuzzySearchLayer.IsInterrupted=false;
		ShowProgressDialog().findViewById(R.id.cancel).setOnClickListener(v13 -> {
			if(!fuzzySearchLayer.IsInterrupted){
				task.stop(false);
				fuzzySearchLayer.IsInterrupted=true;
			}else{
				task.stop(true);
				((FuzzySearchTask)task).harvest();
				CMN.Log("强制关闭");
			}
		});
		for(int i=0;i<loadManager.md_size;i++) {//遍历所有词典
			BookPresenter presenter = loadManager.md_getAt(i);
			if(presenter!=null) {
				presenter.purgeSearch(SEARCHTYPE_SEARCHINNAMES);
			}
		}
		CMN.stst = System.currentTimeMillis();
	}

	private View ShowProgressDialog() {
		View a_dv = getLayoutInflater().inflate(R.layout.dialog_progress, findViewById(R.id.dialog), false);
		AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
		builder2.setView(a_dv);
		AlertDialog dTmp = builder2.create();
		dTmp.setCanceledOnTouchOutside(false);
		dTmp.setOnDismissListener(dialog -> exitFFSearch());
		dTmp.show();
		Window win = dTmp.getWindow();
		if (win != null) {
			win.getAttributes().height = (int) (250*dm.density);
			win.setAttributes(win.getAttributes());
		}
		taskd = dTmp;
		dvTitle = a_dv.findViewById(R.id.title);
		dvSeekbar = a_dv.findViewById(R.id.seekbar);
		dvProgressFrac = a_dv.findViewById(R.id.progressFrac);
		dvResultN = a_dv.findViewById(R.id.resultN);
		dvDictFrac = a_dv.findViewById(R.id.tv);
		dvTitle.setText(currentDictionary.bookImpl.getDictionaryName());
		/* 跳过 */
		a_dv.findViewById(R.id.skip).setOnClickListener(v14 -> {
			if(currentSearchingDictIdx<loadManager.md_size){
				BookPresenter presenter = loadManager.md_getAt(currentSearchingDictIdx);
				if(presenter!=null && presenter.bookImpl instanceof mdict) {
					mdict mdTmp = (mdict) presenter.bookImpl;
					mdTmp.searchCancled=true; //todo
				}
			}
		});

		timer=new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if(0!=(foreground&(1<<thisActType.ordinal()))) hdl.sendEmptyMessage(1008601);
			}
		},0,180);
		return a_dv;
	}

	public void updateFFSearch(BookPresenter book, int index) {
		try {
			currentSearchingDictIdx = index;
			dvSeekbar.setMax((int) book.bookImpl.getNumberEntries());
			dvTitle.setText(book.bookImpl.getDictionaryName());
			dvDictFrac.setText(currentSearchingDictIdx+"/"+PDICMainActivity.taskCounter);
		} catch (Exception ignored) { }
	}
	
	public void updateIndexBuilding(Integer dPos, Integer tIdx) {
		try {
			PlaceHolder placeHolder = getPlaceHolderAt(dPos);
			currentSearchingDictIdx=-1;
			dvSeekbar.setMax((int) placeHolder.getPath(opt).length());
			dvTitle.setText(placeHolder.getName());
			dvDictFrac.setText(tIdx+"/"+PDICMainActivity.taskCounter);
		} catch (Exception ignored) { }
	}

	/** Jump to old or new UI with new text.
	 * @param content New text
	 * @param source specifies the source of our text.
	 * 0=intent share; 1=focused paste; 2=unfocused paste; -1=redirected from float search(text proessing)
	 * */
	public void JumpToWord(String content, int source) {
		CMN.Log("JumpToWord", focused, source, opt.getPasteTarget(), opt.getPasteToPeruseModeWhenFocued(), PeruseViewAttached());
		if((source>=1)&&opt.getPasteTarget()==PLAIN_TARGET_FLOAT_SEARCH && !(source==1&&PDICMainAppOptions.getPasteToPeruseModeWhenFocued())){
			Intent popup = new Intent().setClassName("com.knziha.plod.plaindict", "com.knziha.plod.plaindict.FloatActivitySearch").putExtra("EXTRA_QUERY", content);
			//this, FloatActivitySearch.class
			popup.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			if(PDICMainAppOptions.isFullScreen()){
				popup.putExtra(EXTRA_FULLSCREEN, true)
				.putExtra(EXTRA_HIDE_NAVIGATION, PDICMainAppOptions.isFullscreenHideNavigationbar());
			}
			//popup.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			//CMN.Log("pop this way!");
			getApplicationContext().startActivity(popup);
			return;
		}
		// todo 分享目标优化
		int PasteTarget=opt.getPasteTarget();
		int ShareTarget=opt.getShareToTarget();
		boolean isPeruseView=PeruseViewAttached();
		boolean toPeruseView =  (source>=1)&&(PasteTarget==2||PasteTarget==0&&isPeruseView) ||
				source == 1 && PDICMainAppOptions.getPasteToPeruseModeWhenFocued() ||
				source == 0 &&(ShareTarget==2||ShareTarget==0&&isPeruseView)
				;
		if(toPeruseView){
			JumpToPeruseModeWithWord(content);
		} else {
			来一发=true;
			etSearch.setText(content);
			//来一发=false;
			//todo opt
			if(peruseView !=null)
				peruseView.dismiss();
		}
	}

	public void forceFullscreen(boolean val) {
		drawerFragment.setCheckedForce(drawerFragment.sw1, val);
		drawerFragment.setCheckedForce(drawerFragment.sw2, val);
	}
	
	public void pendingModPath(String nextPath) {
		SU.UniversalObject = nextPath;
		UIData.drawerLayout.closeDrawer(GravityCompat.START);
		showExitDialog(true);
	}
	
	private static class MyHandler extends BaseHandler{
		private final WeakReference<Toastable_Activity> activity;
		MyHandler(Toastable_Activity a) {
			this.activity = new WeakReference<>(a);
		}
		@Override
		public void clearActivity() {
			activity.clear();
		}
		@Override
		public void handleMessage(@NonNull Message msg) {
			if(activity.get()==null) return;
			PDICMainActivity a = ((PDICMainActivity)activity.get());
			switch (msg.what) {
				case 2020:
					if(msg.obj instanceof String)
					a.showTopSnack((String)msg.obj);
				break;
				case 1024:
					a.handleFloatMessage(msg);
				break;
				case 1008601:
					//((TextView)dv.findViewById(R.id.tv)).setText("0/"+System.currentTimeMillis());
					removeMessages(1008601);
					int handlerIdx = a.currentSearchingDictIdx;
					AdvancedSearchInterface handlerRecv = a.currentSearchLayer;
					//SU.Log("handlerIdx", handlerIdx, handlerRecv.dirtyProgressCounter);
					if(a.dvSeekbar!=null) {
						try {
							if(handlerIdx==-1) {;
								if(handlerRecv.dirtyTotalProgress>0 && a.dvSeekbar.getMax()!=handlerRecv.dirtyTotalProgress)
									a.dvSeekbar.setMax(handlerRecv.dirtyTotalProgress);
								a.dvResultN.setText("");
							} else {
								if(handlerIdx<0 || handlerIdx>=a.md.size())
									return;
								a.dvResultN.setText("已搜索到: "+handlerRecv.dirtyResultCounter+" 项条目!");
							}
							a.dvSeekbar.setProgress(handlerRecv.dirtyProgressCounter);
							a.dvProgressFrac.setText(handlerRecv.dirtyProgressCounter+"/"+a.dvSeekbar.getMax());
						} catch (Exception ignored) { }
					}
					break;
				case 10086:
				break;
				case 112233:
					a.mDrawerToggle.onDrawerSlide(a.UIData.drawerLayout, animator);
					if(!a.triggered)
						animator+=animatorD;
					else
						animator-=animatorD;
					if(animator>=1) {
						a.triggered=true;
						a.mDrawerToggle.onDrawerOpened(a.UIData.drawerLayout);
					}
					if(animator>0) {
						a.hdl.sendEmptyMessage(112233);
					}else
						a.mDrawerToggle.onDrawerClosed(a.UIData.drawerLayout);
				break;
				case 3322123:
					a.performReadEntry();
				break;
				case 3344:
					((PageSlide)msg.obj).handleMsg(msg);
				break;
				case 3322124:
					a.enqueueNextAutoReadProcess();
				break;
				case 332211123:
				case 332211:
					removeMessages(332211);
					a.performAutoReadProcess();
				break;
				case 331122:
					animator+=animatorD;
					if(animator>=1) {
						a.refreshUIColors();
					}
					else {
						int filteredColor = a.AppWhite==Color.WHITE?ColorUtils.blendARGB(Color.BLACK,a.MainBackground, animator):ColorUtils.blendARGB(a.MainBackground, Color.BLACK, animator);
						int filteredWhite = ColorUtils.blendARGB(a.AppBlack, a.AppWhite, animator);

						a.bottombar.setBackgroundColor(filteredColor);
						a.toolbar.setBackgroundColor(filteredColor);
						a.UIData.viewpager.setBackgroundColor(filteredWhite);
						a.lv2.setBackgroundColor(filteredWhite);
						a.hdl.sendEmptyMessage(331122);
					}
				break;
				case 778898:
					a.NaugtyWeb.requestLayout();
				break;
				case 778899:
					//a.NaugtyWeb.setLayoutParams(a.NaugtyWeb.getLayoutParams());
					a.NaugtyWeb.requestLayout();
					//a.NaugtyWeb.zoomBy(a.NaugtyWeb.expectedZoom/a.NaugtyWeb.webScale);
					a.NaugtyWeb.scrollTo(a.NaugtyWeb.expectedPosX, a.NaugtyWeb.expectedPos);
					//CMN.Log("handler scroll scale recalibrating ...");
				break;
				case 7658941:
					CustomViewHideTime=0;
				break;
				case 7658942:
					a.fixVideoFullScreen();
				break;
			}
		}
	}
	
	protected boolean PerFormBackPrevention(boolean bBackBtn) {
//		if(dialogHolder.getVisibility()==View.VISIBLE) {
//			dialogHolder.setVisibility(View.GONE);
//			if(pickDictDialog!=null && pickDictDialog.isDirty)
//			{
//				opt.putFirstFlag();pickDictDialog.isDirty=false;
//			}
//			return true;
//		}//111
		if (super.PerFormBackPrevention(bBackBtn)) {
			return true;
		}
		if(removeBlack())
			return true;
		if(ActivedAdapter!=null && isContentViewAttached()) {
			contentUIData.mainProgressBar.setVisibility(View.GONE);
			
			applyMainMenu();
			
			//iItem_InPageSearch.setVisible(!opt.getInPageSearchVisible()&&!opt.isContentBow());
			
//			adaptermy2.currentKeyText=null;
//			adaptermy.currentKeyText=null;

//			Utils.removeAllViews(webholder);
//			Utils.removeAllViews(webSingleholder);
			
			WebViewmy backing_webview = contentUIData.PageSlider.getWebContext();
			if(backing_webview!=null) {
				backing_webview.expectedPos=0;
			}
			((ListViewAdapter2)adaptermy2).expectedPos=0;
			if(drawerFragment.d!=null) {
				drawerFragment.d.show();
			}
			contentUIData.PageSlider.setTranslationX(0);
			contentUIData.PageSlider.setTranslationY(0);
			int lastPos = ActivedAdapter.lastClickedPos;
			DetachContentView(true);
			PostDCV_TweakTBIC();
			ListView lva = ActivedAdapter.lava;
			if(lva!=null && (lastPos<lva.getFirstVisiblePosition() || lastPos>lva.getLastVisiblePosition()))
				lva.setSelection(lastPos);
			ActivedAdapter=null;
			return true;
		}
		if(isContentViewAttached()){/* avoid stuck */
			DetachContentView(true);
			return true;
		}
		if(mainF.getChildCount()==0 && !isContentViewAttached()){
			if(UIData.drawerLayout.isDrawerOpen(GravityCompat.START)) {
				UIData.drawerLayout.closeDrawer(GravityCompat.START);
				return true;
			}
			boolean b1=PDICMainAppOptions.exitToBackground();
			if(!b1||PDICMainAppOptions.getBackToHomePagePreventBack()) {
				int BackPrevention = PDICMainAppOptions.getBackPrevention();
				switch (BackPrevention) {
					default: break;
					case 1:
						if(topsnack==null || topsnack.msg!=R.string.warn_exit) {
							showTopSnack(null, R.string.warn_exit, 0.8f, -1, -1, 0);
							return true;
						}
					break;
					case 2:
						if ((System.currentTimeMillis() - exitTime) > 2000) {
							showX(R.string.warn_exit, 0);
							exitTime = System.currentTimeMillis();
							return true;
						}
						break;
					case 3:
						showExitDialog(false);
						return true;
				}
			}
			if(b1) {
				moveTaskToBack(true);
				return true;
			}
		}
		return false;
	}
	
	private void PostDCV_TweakTBIC() {
		etSearch_ToToolbarMode(0);
		bWantsSelection=false;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_VOLUME_DOWN: {
				if (opt.getMakeWayForVolumeAjustmentsWhenAudioPlayed() && opt.isAudioPlaying) {
					if (!opt.isAudioActuallyPlaying)
						transitAAdjustment();
					break;
				}
				if (opt.getUseVolumeBtn()) {
					if(opt.getAutoReadEntry()){
						forbidVolumeAjustmentsForTextRead =true;
					}
					boolean toHighlight=weblistHandler.pageSchBar !=null && PDICMainAppOptions.schPageNavAudioKey() && weblistHandler.pageSchBar.getParent()!=null;
					if (DBrowser != null && main.getChildCount() == 1) {//==1: 内容未加渲染
						if (opt.getUseVolumeBtn()) {
							if (DBrowser.inSearch)
								DBrowser.onClick(DBrowser.UIData.browserWidget13);
							else {
								View v = new View(this);
								v.setId(R.id.nxt_plain);
								DBrowser.onClick(v);
							}
							return true;
						}
					}
					else if (contentview.getParent() != null) {
						if(toHighlight) onIdClick(null, R.id.recess);
						else contentUIData.browserWidget11.performClick();
						return true;
					}
					else if (!UIData.drawerLayout.isDrawerOpen(GravityCompat.START)) {
						ListView lvlv = lv;
						if (CurrentViewPage == 0) lvlv = mlv1;
						else if (CurrentViewPage == 2) lvlv = mlv2;
						if (lvlv.getChildCount() > 0) {
							int fvp = lvlv.getFirstVisiblePosition();
							lvlv.setSelection(fvp + 1);
							return true;
						}
					}
				}
			} break;
			case KeyEvent.KEYCODE_VOLUME_UP: {
				if (opt.getMakeWayForVolumeAjustmentsWhenAudioPlayed() && opt.isAudioPlaying) {
					if (!opt.isAudioActuallyPlaying)
						transitAAdjustment();
					break;
				}
				if (opt.getUseVolumeBtn()) {
					if(opt.getAutoReadEntry()){
						forbidVolumeAjustmentsForTextRead =true;
					}
					boolean toHighlight=weblistHandler.pageSchBar !=null && PDICMainAppOptions.schPageNavAudioKey() && weblistHandler.pageSchBar.getParent()!=null;
					if (DBrowser != null && main.getChildCount() == 1) {
						if (DBrowser.inSearch)
							DBrowser.onClick(DBrowser.UIData.browserWidget14);
						else {
							View v = new View(this);
							v.setId(R.id.lst_plain);
							DBrowser.onClick(v);
						}
						return true;
					} else if (PeruseViewAttached()) {
						peruseView.contentUIData.browserWidget10.performClick();
						return true;
					} else if (contentview.getParent() != null) {
						if(toHighlight) onIdClick(null, R.id.forward);
						else contentUIData.browserWidget10.performClick();
						return true;
					} else if (!UIData.drawerLayout.isDrawerOpen(GravityCompat.START)) {
						ListView lvlv = lv;
						if (CurrentViewPage == 0) lvlv = mlv1;
						else if (CurrentViewPage == 2) lvlv = mlv2;
						if (lvlv.getChildCount() > 0) {
							int fvp = lvlv.getFirstVisiblePosition();
							if (lvlv.getChildAt(0).getTop() > -10)
								fvp--;
							lvlv.setSelection(fvp);
							return true;
						}
					}
				}
			} break;
			case KeyEvent.KEYCODE_VOLUME_MUTE:
				if(DBrowser !=null)
					return true;
				break;
//			case KeyEvent.KEYCODE_BACK:
//
//				return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	BooleanSingleton TintWildResult = new BooleanSingleton(true);
	BooleanSingleton TintFullResult = new BooleanSingleton(true);
	public String Current0SearchText;

	protected boolean bNeedSaveViewStates;

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		processIntent(intent, false);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		cbar_key=0;
		bIsFirstLaunch=false;
		focused=true;
		thisActType = ActType.PlainDict;
		CMN.Log("LauncherInstanceCount", LauncherInstanceCount);
		if(LauncherInstanceCount>=1) {
			Intent thisIntent = getIntent();
			startActivity((thisIntent==null?new Intent():new Intent(getIntent()))
					.setClass(this, PDICMainActivity.class)
					.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP));//, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
			shunt=true;
		}
		super.onCreate(null);
		if(shunt) {
			finish();
			return;
		}
		
		LauncherInstanceCount=1;
		Window win = getWindow();
		
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//if(Utils.littleCake) {
		if(Build.VERSION.SDK_INT<=22) {
			requestWindowFeature(Window.FEATURE_ACTION_MODE_OVERLAY);
			supportRequestWindowFeature(Window.FEATURE_ACTION_MODE_OVERLAY);
		}
		
		setSoftInputMode(softModeNothing);
		new HeightProvider(this).init().setHeightListener(new HeightProvider.HeightListener() {
			@Override
			public void onHeightChanged(int height) {
				//showT(""+height+settingsPanel);
				if(settingsPanel!=null) {
					GlobalOptions.softInputHeight = height;
					settingsPanel.refreshSoftMode(height);
				}
			}
		});
		
		boolean transit = PDICMainAppOptions.getTransitSplashScreen();
		if(!transit) setTheme(R.style.PlainAppTheme);
		
		UIData = DataBindingUtil.setContentView(this, R.layout.activity_main);
		dictPicker = new DictPicker(this, UIData.viewpagerPH, UIData.lnrSplitHdls, 0);
		
		root = UIData.root;
		
		if(transit) {
			root.setAlpha(0);
			ObjectAnimator fadeInContents = ObjectAnimator.ofFloat(root, "alpha", 0, 1);
			fadeInContents.setInterpolator(new AccelerateDecelerateInterpolator());
			fadeInContents.setDuration(350);
			fadeInContents.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					win.getDecorView().setBackground(Build.VERSION.SDK_INT>=23
							?null:new ColorDrawable(0));
				}
			});
			root.post(fadeInContents::start);
		}
		
		toolbar = UIData.toolbar;
		appbar = UIData.appbar;
		
		bottombar = UIData.bottombar;
		
		contentUIData = UIData.contentview;
		
		snack_holder = UIData.snackHolder;
		mainF = UIData.mainF;
		mlv = (ViewGroup) root.getChildAt(4);
		lv = UIData.mainList;
		lv2 = UIData.subList;
		mlv1 = UIData.subList1;
		mlv2 = UIData.subList2;
		
		main = root;
		
		mlv.removeViews(2, 2);
		
		toolbar.setId(R.id.action_context_bar);
		mDrawerToggle = new ActionBarDrawerToggle(this, UIData.drawerLayout, toolbar, R.string.open, R.string.close);
		mDrawerToggle.syncState();// 添加按钮
		
		toolbar.addNavigationOnClickListener((v,e) -> {
			if(etTools.isVisible()) {
				etTools.dismiss();
				etTools.hideIM();
				return false;
			}
			if(wordPopup.isVisible()) {
				wordPopup.dismiss();
				return false;
			}
			if(isContentViewAttached()) {
				DetachContentView(true);
				return false;
			}
			if(!UIData.drawerLayout.isDrawerVisible(GravityCompat.START)) {
				onDrawerOpened();
			}
			drawerFragment.adjustBottomPadding();
			return true;
		});
		toolbar.mNavButtonView.setOnLongClickListener(this);
		
//		ResizeNavigationIcon(toolbar);

		hdl = mHandle = new MyHandler(this);
		
		toolbar.inflateMenu(R.xml.menu);
		AllMenus = (MenuBuilder) toolbar.getMenu();
		AllMenusStamp = Arrays.asList(AllMenus.getItems().toArray(new MenuItemImpl[AllMenus.size()]));
		MenuCompat.setGroupDividerEnabled(AllMenus, true);
		
		Drawable drawable = getResources().getDrawable(R.drawable.ic_yes_blue);
		int sz = (int) (GlobalOptions.density*24);
		drawable.setBounds(0,0, sz, sz);
		AllMenus.checkDrawable = drawable;
		AllMenus.mOverlapAnchor = PDICMainAppOptions.menuOverlapAnchor();
		
	// 															23/*随机词条*/
		SingleContentMenu = ViewUtils.MapNumberToMenu(AllMenus, 4, 13, 14/*翻译*/, 2, 16, 3/*记忆位置*/, 9, 11, 12);
		Multi_ContentMenu = ViewUtils.MapNumberToMenu(AllMenus, 4, 13, 14, 1, 2, 15, 21/*记忆位置*/, 9, 10, 12);
		MainMenu = ViewUtils.MapNumberToMenu(AllMenus, 4, 0, 22, 7/*翻阅模式*/, 8/*分字搜索*/, 20/*搜索工具栏*/, 17, 18, 19);
		LEFTMenu = ViewUtils.MapNumberToMenu(AllMenus, 4, 0, 22, 7, 20, 5, 6);
		
		
		boolean showVal = PDICMainAppOptions.getShowSearchTools();
		if(showVal) {
			ViewUtils.findInMenu(MainMenu, R.id.schtools).setChecked(showVal);
			ViewUtils.setVisible(UIData.schtools, showVal);
		}
		
		if(opt.getRemPos())ViewUtils.findInMenu(SingleContentMenu, R.id.remPagePos).setChecked(true);
		if(opt.getRemPos2())ViewUtils.findInMenu(Multi_ContentMenu, R.id.remPagePos2).setChecked(true);
		if(opt.tapSch())ViewUtils.findInMenu(Multi_ContentMenu, R.id.tapSch).setChecked(true);
		if(TintWildResult.first = opt.getTintWildRes())ViewUtils.findInMenu(LEFTMenu, R.id.tintList).setChecked(true);
		PeruseListModeMenu = ViewUtils.findInMenu(MainMenu, R.id.peruseList);
		
		checkLog(savedInstanceState);
		
		if (PDICMainAppOptions.getNotificationEnabled()) {
			startService(new Intent(this, ServiceEnhancer.class));
//			locationReceiver = new EnchanterReceiver();
//			IntentFilter filter = new IntentFilter();
//			filter.addAction("plodlock");
//			registerReceiver(locationReceiver, filter);
		}
		
		if(false) {
			screenListener = new ScreenListener( PDICMainActivity.this ) ;
			screenListener.begin(new ScreenListener.ScreenStateListener() {
				@Override
				public void onScreenOn() {
					if(!focused) fortize();
					//Toast.makeText( PDICMainActivity.this , "屏幕打开了" , Toast.LENGTH_SHORT ).show();
				}
				
				@Override
				public void onScreenOff() {
					//Toast.makeText( PDICMainActivity.this , "屏幕关闭了" , Toast.LENGTH_SHORT ).show();
					if(!focused) fortize();
				}
				
				@Override
				public void onUserPresent() {
					if(!focused) fortize();
					//Toast.makeText( PDICMainActivity.this , "解锁了" , Toast.LENGTH_SHORT ).show();
				}
			});
		}
		
		CrashHandler.getInstance(this, opt).TurnOn();
	}
	
	private void fortize() {
		startActivity(new Intent(PDICMainActivity.this, PDICMainActivity.class)
				//.setFlags(MainShareActivity.SingleTaskFlags|Intent.FLAG_FROM_BACKGROUND)
				//.setAction("null")
		);
	}
	
	private ScreenListener screenListener ;
	
	public static class EnchanterReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String intentAction = intent.getAction();
			if ("plodlock".equals(intentAction)) {
				CMN.Log("plodlock!!!");
			}
		}
	}
	
	@Override
	protected void findFurtherViews() {
		// todo
		ivDeleteText = ivBack = UIData.multiline;
		ivDeleteText.setOnClickListener(this);
		etSearch = UIData.etSearch;
		// https://stackoverflow.com/questions/46004928/edittext-how-to-set-cliptopadding-to-false
		if(GlobalOptions.isLarge) {
			UIData.etPad.setVisibility(View.GONE);
			UIData.etPad1.setVisibility(View.GONE);
		} else {
			etSearch.setLayerType(View.LAYER_TYPE_SOFTWARE, null); // 不开可能崩溃、滑动recyclerview会卡顿
			etSearch.setShadowLayer(etSearch.getPaddingRight(), 0f, 0f, Color.TRANSPARENT);
		}
		schuiMain = SearchUI.MainApp.MAIN;
		schuiMainSchs = SearchUI.MainApp.ENTRYTEXT|SearchUI.MainApp.FULLTEXT;
		schuiMainPeruse = SearchUI.MainApp.MAIN|SearchUI.Fye.MAIN;
		schuiList = SearchUI.MainApp.表;
		super.findFurtherViews();
	}
	
	private int softMode;
	private boolean bottombarHidden;
	public final static int softModeHold = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN;
	public final static int softModeResize = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
	public final static int softModeNothing = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING;
	
	public void setSoftInputMode(int mode) {
		if(softMode!=mode) {
			softMode=mode;
			getWindow().setSoftInputMode(mode);
			if (systemIntialized) {
				if(mode==softModeResize) {
					bottombar.getLayoutParams().height=0;
					contentUIData.bottombar2.getLayoutParams().height=0;
				} else {
					bottombar.getLayoutParams().height=barSzBot;
					contentUIData.bottombar2.getLayoutParams().height=barSzBot;
				}
			}
		}
	}
	
	void onDrawerOpened() {
		drawerOpen = true;
		if(isContentViewAttached()) {
			DetachContentView(false);
		}
		imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
		fadeSnack();
	}
	
	private void
	
	processIntent(Intent intent, boolean initialzie) {
		int jump_source = 0;
		if(intent != null){
			String action = intent.getAction();
			if("lock".equals(action)) {
				//CMN.Log("锁住！！！");
				if(!focused) moveTaskToBack(false);
			}
			else {
				Uri url = intent.getData();
				if(url!=null) {
					HandleOpenUrl(url);
				}
				
				String extraText = null;
				
				if(intent.hasExtra(Intent.EXTRA_TEXT))
					extraText = intent.getStringExtra(Intent.EXTRA_TEXT);
				
				if(extraText ==null && intent.hasExtra(Intent.EXTRA_PROCESS_TEXT))
					extraText = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT);
				
				if(extraText ==null && intent.hasExtra("EXTRA_QUERY")) extraText = intent.getStringExtra("EXTRA_QUERY");
				
				CMN.Log("主程序-1", extraText, CMN.id(extraText), Intent.ACTION_MAIN.equals(action));
				
				//if(!bWantsSelection) bWantsSelection = intent.hasExtra(Intent.EXTRA_SHORTCUT_ID);
				if (intent.hasExtra(Intent.EXTRA_SHORTCUT_ID)) {
					int forceTarget = intent.getIntExtra(Intent.EXTRA_SHORTCUT_ID, 0);
					if (extraText !=null) {
						if (forceTarget==PLAIN_TARGET_INPAGE_SEARCH) {
							HandleLocateTextInPage(extraText);
							extraText = null;
						}
						jump_source = -1;
					}
					if(adaptermy2!=null){
						adaptermy2.avoyager.remove(0);
						//todo opt
						if(contentview.getParent()!=null)
							DetachContentView(true);
						bIsFirstLaunch=true;
					}
				}
				this.extraText = extraText;
				if(extraText!=null) {
					extraInvoker = PDICMainAppOptions.storeAppId()?ViewUtils.topThirdParty(this, initialzie?3:1):null;
					JumpToWord(extraText, jump_source);
				}
			}
		}
	}
	
	private void HandleOpenUrl(Uri url) {
		if(url==null) {
			return;
		}
		CMN.Log("接收到!!!", url, url.getPath());
		if(false)
		root.postDelayed(new Runnable() {
			@Override
			public void run() {
				try {
					// MLSN
					Uri url = Uri.parse("content://com.android.externalstorage.documents/document/primary%3ADownload%2F%E8%8B%B1%E8%AF%AD%E6%9E%84%E8%AF%8D%E6%B3%95.mdx");
					
					InputStream fin = getContentResolver().openInputStream(url);
					
					byte[] buffer = new byte[512];
					fin.read(buffer);
					CMN.Log("接收到!!!", new String(buffer, 0, 512, StandardCharsets.UTF_16LE));
					
				} catch (Exception e) {
					CMN.Log(e);
				}
			}
		}, 5000);
	}
	
	@SuppressLint("ResourceType")
	protected void further_loading(final Bundle savedInstanceState) {
		barSzBot=(int) mResource.getDimension(R.dimen.barSzBot);//opt.getBottombarSize();
		
		ViewUtils.removeView(mlv);
		View[] viewList = new View[]{mlv1, mlv, mlv2};
		for (int i = 0; i < 3; i++) {
			LinearLayout view = new LinearLayout(this);
			view.setOrientation(LinearLayout.VERTICAL);
			view.addView(viewList[i]);
			viewList[i] = view;
		}
		super.further_loading(savedInstanceState);
		
		CheckGlideJournal();

		//showT(root.getParent().getClass());
		DefaultTSView = mainframe;
		contentUIData.webcontentlister.scrollbar2guard=contentUIData.dragScrollBar;
		DetachContentView(true);

		if(!opt.getBottombarOnBottom())
			contentUIData.webcontentlister.SwitchingSides();
		//SplitViewGuarder_ svGuard = (SplitViewGuarder_) contentview.findViewById(R.id.svGuard);
		//svGuard.SplitViewsToGuard.add(webcontentlister);

		//PageSlider.SCViewToMute = (ScrollViewmy) webholder.getParent();
		
		final NoScrollViewPager viewPager = UIData.viewpager;
		viewPager.addOnPageChangeListener(new OnPageChangeListener() {
			@Override public void onPageScrollStateChanged(int arg0) { }
			@Override public void onPageScrolled(int arg0, float arg1, int arg2) { }
			@Override
			public void onPageSelected(int pos) {
				fadeSnack();
				CurrentViewPage=pos;
				boolean b1=pos==0;
				if((b1||pos==2) && PDICMainAppOptions.getHintSearchMode()) {
					boolean bUseRegex = b1?PDICMainAppOptions.getUseRegex1():PDICMainAppOptions.getUseRegex2();
					int msg=bUseRegex?R.string.regret:(b1?R.string.fuzzyret:R.string.fullret);
					viewPager.post(() -> showTopSnack(null, msg, 0.5f, -1, Gravity.CENTER, 0));
				}
				decorateBottombarFFSearchIcons(pos);
				applyMainMenu();
			}});
		
		//tofo
		if(Build.VERSION.SDK_INT >= 24) {
			ViewUtils.listViewStrictScroll(true, mlv1, mlv2, lv);
		}
		
		setNestedScrollingEnabled(PDICMainAppOptions.getEnableSuperImmersiveScrollMode());
		//Utils.listViewStrictScroll(lv2,true);
		
		
		PagerAdapter pagerAdapter = new PagerAdapter() {
			@Override public boolean isViewFromObject(@NonNull View arg0, @NonNull Object arg1) {
				return arg0 == arg1;
			}
			@Override public int getCount() { return 3; }
			@Override public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
				container.removeView(viewList[position]);
			}
			@NonNull @Override
			public Object instantiateItem(ViewGroup container, int position) {
				View child = viewList[position];
				container.addView(child);
				return child;
			}
		};
		
//333
//		UIData.browserWidget0.setOnClickListener(this);
//		UIData.browserWidget0.setOnLongClickListener(this);
		//widget0.getBackground().setTint(MainBackground);
		//widget0.getBackground().setColorFilter(MainBackground, PorterDuff.Mode.SRC_IN);
		//ViewCompat.setBackgroundTintList(widget0, ColorStateList.valueOf(MainBackground));
		boolean tint = PDICMainAppOptions.getTintIconForeground();
		if(tint&&ForegroundFilter==null)
			ForegroundFilter = new PorterDuffColorFilter(ForegroundTint, PorterDuff.Mode.SRC_IN);
		
		BottombarBtns[0] = UIData.browserWidget1;
		browser_widget1 = UIData.browserWidget1;
		UIData.browserWidget1.setOnClickListener(this);
		UIData.browserWidget1.setId(R.drawable.book_list);
		String appproject = opt.getAppBottomBarProject();
		if(appproject==null) {
			appproject = "0|1|2|3|4|5|6";
		}
		bottombar_project = new AppUIProject("btmprj", BottombarBtnIcons, appproject, bottombar, BottombarBtns);
		RebuildBottombarIcons(this, bottombar_project, mConfiguration);
		
		viewPager.setAdapter(pagerAdapter);
		viewPager.setCurrentItem(CurrentViewPage = 1);
		
		//mDrawerLayout.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		UIData.drawerLayout.addDrawerListener(mDrawerToggle);// 按钮动画特效
		UIData.drawerLayout.addDrawerListener(new DrawerListener() {
			@Override
			public void onDrawerOpened(@NonNull View arg0) {
				imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
				etSearch_ToToolbarMode(1);
				drawerOpen = true;
			}

			@Override public void onDrawerClosed(@NonNull View arg0) {
				drawerOpen = false;
			}
			@Override public void onDrawerSlide(@NonNull View arg0, float arg1) {
			}
			@Override public void onDrawerStateChanged(int newState) {
				if(!UIData.drawerLayout.isDrawerVisible(GravityCompat.START)) {
					etSearch_ToToolbarMode(0);
				}
				//if (newState==UIData.drawerLayout.STATE_DRAGGING)
				{
					drawerFragment.adjustBottomPadding();
				}
				checkFlags();
			}});
		//mDrawerLayout.setScrimColor(0x00ffffff);
		adaptermy = new ListViewAdapter(this, AllMenus, SingleContentMenu);
		adaptermy.setPresenter(currentDictionary);
		lv.setAdapter(adaptermy);
		lv2.setAdapter(adaptermy2 = new ListViewAdapter2(this, weblistHandler, AllMenus, Multi_ContentMenu, R.layout.listview_item1, 2));
		mlv1.setAdapter(adaptermy3 = new ListViewAdapter2(this, webSingleholder, AllMenus, LEFTMenu, 3));
		mlv2.setAdapter(adaptermy4 = new ListViewAdapter2(this, webSingleholder, AllMenus, LEFTMenu, 4));

		fuzzySearchLayer=new AdvancedSearchInterface(opt, md, SEARCHTYPE_SEARCHINNAMES);
		fullSearchLayer=new AdvancedSearchInterface(opt, md, SEARCHTYPE_SEARCHINTEXTS);

		adaptermy3.results = new resultRecorderScattered(this,md,TintWildResult,fuzzySearchLayer);
		adaptermy4.results = new resultRecorderScattered(this,md,TintWildResult,fullSearchLayer);
		//tc
		execSearchRunnable = () -> {
			if(CurrentViewPage==1) {
				String text = etSearch.getText().toString().trim();
				if(text.length()==0) return;
				if (drawerOpen) {
					try {
						UIData.drawerLayout.close();
					} catch (Exception e) {
						//todo modify to throw nothing.
					}
				}
				if(text.startsWith("<")) {
					String perWSTag = mResource.getString(R.string.perWSTag);
					String firstTag = firstTag(text);
					if(firstTag!=null) {
						String fTCpror = firstTag.replace("~", "");
						if(perWSTag.equals(fTCpror)||"分字".equals(fTCpror)) {
							String input = text.substring(text.indexOf(">")+1).trim();
							if(input.length()!=0)
								launchVerbatimSearch(input,!firstTag.contains("~"));
							return;
						}
					}
				}
				if(checkDicts()) {
					if(isCombinedSearching){
						//todo
						execBatchSearch(search_cs);
					} else {
						if (来一发 || currentDictionary.getType()!=PLAIN_TYPE_WEB)
							execSingleSearch(search_cs, search_count);
					}
				}
				if(来一发) {
					来一发 = false;
				}
			}
		};
		etSearch.addTextChangedListener(tw1);
		//ea
		etSearch.setOnEditorActionListener((v, actionId, event) -> {
			if (actionId==EditorInfo.IME_ACTION_SEARCH||actionId==EditorInfo.IME_ACTION_UNSPECIFIED){
				if(taskd!=null){
					return true;
				}
				String key = etSearch.getText().toString().trim();
				if(key.length()>0) Current0SearchText=key;
				int p = viewPager.getCurrentItem();
				if(etTools.isVisible()) {
					etTools.dismiss();
				}
				if(p==0 || p==2) {
					if(!PDICMainAppOptions.storeNothing() || PDICMainAppOptions.storeNothingButSch())
						addHistory(key, p==0?SearchUI.MainApp.ENTRYTEXT:SearchUI.MainApp.FULLTEXT, null, null);
					if(!checkDicts()) return true;
					//模糊搜索 & 全文搜索
					if(mAsyncTask!=null)
						mAsyncTask.stop(true);
					imm.hideSoftInputFromWindow(main.getWindowToken(),0);
					(mAsyncTask=p==0?new FuzzySearchTask(PDICMainActivity.this)
							:new FullSearchTask(PDICMainActivity.this)).execute(key);
				} else {
					if(!PDICMainAppOptions.storeNothing() || PDICMainAppOptions.storeNothingButSch())
						addHistory(key, schuiMain, null, null);
					if(key.length()>0)
					{
						if(!isCombinedSearching && currentDictionary.getType()==PLAIN_TYPE_WEB)
						{
							currentDictionary.SetSearchKey(key);
							adaptermy.onItemClick(0);
						}
						else
						{
							bIsFirstLaunch=true;
							tw1.onTextChanged(key, -1, -1, 0);
						}
					}
				}
				etTools.addHistory(key);
			}
			return true;
		});
		

		//switch_To_Dict_Idx(adapter_idx);
		if(currentDictionary!=EmptyBook) {
			lv.post(() -> {
				lv.setSelectionFromTop(currentDictionary.lvPos, currentDictionary.lvPosOff);
				setLv1ScrollChanged();
			});
		} else {
			setLv1ScrollChanged();
		}

		systemIntialized=true;
		
		if(opt.getInPeruseMode()) {
			PeruseListModeMenu.setChecked(true);
			showTopSnack("翻阅模式");
		}
		
		if(PDICMainAppOptions.getSimpleMode() && PDICMainAppOptions.getHintSearchMode())
			showTopSnack(null, "极简模式"
					, 0.5f, -1, Gravity.CENTER, 0);


		if(savedInstanceState!=null) {
			// 状态恢复
			//for(int i=0;i<md.size();i++){//遍历所有词典
			//	BookPresenter mdtmp = md.get(i);
			//	if(mdtmp!=null) {
			//		String full_Dictionary_fName = mdtmp.bookImpl.getDictionaryName();
			//		if (savedInstanceState.containsKey("sizeOf" + full_Dictionary_fName)) {
			//			int size = savedInstanceState.getInt("sizeOf" + full_Dictionary_fName);
			//			mdtmp.combining_search_tree2 = new ArrayList[size];
			//			for (int ti = 0; ti < size; ti++) {//遍历搜索结果
			//				if (savedInstanceState.containsKey(full_Dictionary_fName + "@" + ti)) {
			//					mdtmp.combining_search_tree2[ti] = savedInstanceState.getIntegerArrayList(full_Dictionary_fName + "@" + ti);
			//				}
			//			}
			//		}
			//		if (savedInstanceState.containsKey("sizeOf_4" + full_Dictionary_fName)) {
			//			int size = savedInstanceState.getInt("sizeOf_4" + full_Dictionary_fName);
			//			mdtmp.combining_search_tree_4 = new ArrayList[size];
			//			for (int ti = 0; ti < size; ti++) {//遍历搜索结果
			//				if (savedInstanceState.containsKey(full_Dictionary_fName + "@_4" + ti)) {
			//					mdtmp.combining_search_tree_4[ti] = savedInstanceState.getIntegerArrayList(full_Dictionary_fName + "@_4" + ti);
			//				}
			//			}
			//		}
			//	}
			//}
			adaptermy3.results.invalidate();
			adaptermy3.notifyDataSetChanged();

			adaptermy4.results.invalidate();
			adaptermy4.notifyDataSetChanged();


			CurrentViewPage = savedInstanceState.getInt("CVP", 1);
			if(CurrentViewPage==0 || CurrentViewPage==2){//亮A
				viewPager.setCurrentItem(CurrentViewPage,true);
			}


			int[] arr2 = savedInstanceState.getIntArray("P_L2");
			if(arr2!=null)
				pendingLv2Pos=arr2;

			int[] arr3 = savedInstanceState.getIntArray("P_M1");
			mlv1.post(() -> {
				if(arr3!=null) mlv1.setSelectionFromTop(arr3[0], arr3[1]);
			});
			int[] arr4 = savedInstanceState.getIntArray("P_M2");
			mlv2.post(() -> {
				if(arr4!=null) mlv2.setSelectionFromTop(arr4[0], arr4[1]);
			});

			boolean canAddPeruseView=true;
			int dbrowser = savedInstanceState.getInt("DB", -1);
			if(dbrowser!=-1){
				(dbrowser==1?findViewById(R.drawable.favoriteg) // get5 get6
						:findViewById(R.drawable.historyg)).performClick();
				dbrowser = savedInstanceState.getInt("DBPos", -1);
				if(dbrowser!=-1) {
					DBrowser.pendingDBClickPos = dbrowser;
					canAddPeruseView=opt.getDBMode()!=SelectionMode_peruseview;
				}
			}
			else if(contentview.getParent()!=null){
				int val = savedInstanceState.getInt("lv_pos",-1);
				if(val>=0) {
					switch (savedInstanceState.getInt("lv_id",-1)){
						case 1:
							ActivedAdapter=adaptermy;
						break;
						case 2:
							pendingLv2ClickPos=val;
						break;
					}
					if(ActivedAdapter!=null)
						ActivedAdapter.onItemClick(val);
				}
			}

//			if(canAddPeruseView){
//				ArrayList<Integer> data = savedInstanceState.getIntegerArrayList("p_data");
//				if(data!=null){
//					getPeruseView().prepareJump(this, savedInstanceState.getString("p_key"), data, savedInstanceState.getInt("p_adaidx"));
//					int val = savedInstanceState.getInt("lvp_pos",-1);
//					if(val!=-1){
//						getPeruseView().prepareClick(val);
//					}
//					AttachPeruseView(true);
//				}
//			}
		}

		File additional_config = new File(opt.pathToMainFolder().append("appsettings.txt").toString());
		if(additional_config.exists()) {
			try {
				BufferedReader in = new BufferedReader(new FileReader(additional_config));
				String line;
				while((line=in.readLine())!=null) {
					String[] arr = line.split(":", 2);
					if(arr.length==2) {
						if(arr[0].equals("window margin")||arr[0].equals("窗体边框")) {
							arr = arr[1].split(" ");
							if(arr.length==4) {
								try {
									MarginLayoutParams lp = (MarginLayoutParams) root.getLayoutParams();
									DockerMarginL = lp.leftMargin=Integer.parseInt(arr[2]);
									DockerMarginR = lp.rightMargin=Integer.parseInt(arr[3]);
									DockerMarginT = lp.topMargin=Integer.parseInt(arr[0]);
									DockerMarginB = lp.bottomMargin=Integer.parseInt(arr[1]);
									root.setLayoutParams(lp);
								} catch (Exception ignored) {}
							}
						}else if(arr[0].equals("edit all")) {
							CMN.editAll=arr[1].length()==3;
						}else if(arr[0].equals("test float search")) {
							CMN.testFLoatSearch=arr[1].length()==3;
						}else if(arr[0].equals("debug string")) {
							extraText =arr[1];
						}
					}
				}
			} catch (Exception ignored) {}

		}

		checkMargin(this);

		//if(opt.getInPageSearchVisible())
		//	toggleInPageSearch(false);

		if(false) {//按
			AdvancedNestScrollWebView wv = new AdvancedNestScrollWebView(getBaseContext());
			snack_holder.addView(wv, 0);
			wv.setVisibility(View.INVISIBLE);
			wv.loadUrl("file:///android_asset/load.html"); //wv.loadUrl("http://192.168.1.102:48626");
			wv.postDelayed(() -> {
				((ViewGroup)wv.getParent()).removeView(wv);
				wv.removeAllViews();
				wv.destroy();
			}, 2500);
		}
		
		
		//tg
		LayoutParams barBotLP = UIData.bottombar.getLayoutParams();
		//if(false)
//		root.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
//			int lastW, lastH;
//			@Override
//			public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
//				CMN.Log("onLayoutChange::", root.getHeight(), dm.heightPixels);
//				CMN.Log("onLayoutChange::", bottom, oldBottom);
//				CMN.Log("onLayoutChange::", getWindow().getDecorView().getHeight());
//				if(softMode!=softModeHold) {
//					boolean mKeyboardUp = isKeyboardShown(root);
//					if (bottombarHidden != mKeyboardUp) {
//						View bottombar2 = UIData.bottombar;
//						if (mKeyboardUp) {
//							//showT("键盘弹出...");
//							bottombarHidden = true;
//							barBotLP.height=0;
////							UIData.bottombar.setVisibility(View.INVISIBLE);
////							contentUIData.bottombar2.setVisibility(View.INVISIBLE);
//						} else {
//							//showT("键盘收起...");
//							bottombarHidden = false;
//							barBotLP.height=100;
////							UIData.bottombar.setVisibility(View.VISIBLE);
////							contentUIData.bottombar2.setVisibility(View.VISIBLE);
//						}
//					}
//				}
//			}
//		});
		
		
		try {
//			SSLContext sslcontext = SSLContext.getInstance("TLSv1");
//			sslcontext.init(null, new TrustManager[]{new PlainWeb.MyX509TrustManager()}, new java.security.SecureRandom());
//			HttpsURLConnection.setDefaultSSLSocketFactory(PlainWeb.NoSSLv3Factory = sslcontext.getSocketFactory());
//
			
			SSLContext sslcontext = SSLContext.getInstance("TLSv1");
			sslcontext.init(null, null, null);
			PlainWeb.NoSSLv3Factory = new NoSSLv3SocketFactory(sslcontext.getSocketFactory());

			HttpsURLConnection.setDefaultSSLSocketFactory(PlainWeb.NoSSLv3Factory);

//			l_connection = (HttpsURLConnection) l_url.openConnection();
//			l_connection.connect();
		
		
		
		} catch (Exception e) {
			CMN.Log(e);
		}
		
		new SearchToolsMenu(this, UIData.schtools);
		
		if(dictPicker.pinShow()) {
			showChooseDictDialog(0);
		}
		
		restLastSch = opt.restoreLastSch();
		if(restLastSch) {
			etTools.LoadHistory(null);
		}
		
		if(true) {
//			showRandomShuffles();
		}
//		showExitDialog(false);
		
//		Runnable runn = new Runnable() {
//			@Override
//			public void run() {
//				showTopSnack(null, R.string.peruse_mode
//						, 1f, LONG_DURATION_MS, Gravity.CENTER, 0);
//				root.postDelayed(this, 1200);
//			}
//		};
//		root.postDelayed(runn, 1200);
		
		
		//showT(""+currentDictionary.QueryByKey("woodie", SearchType.Normal, false, 0));
		TestHelper.wakeUpAndUnlock(this);


//		etSearch.setText("beat");
//		etSearch.setText("vignette");
//		etSearch.setText("class=\"main\"");
//		etSearch.setText("Label");
		
//		launchSettings(0, 0);
//		do_test_project_Test_Background_Loop();
		//CMN.Log(FU.listFiles(this, Uri.fromFile(new File("/sdcard"))));
		
		
//		try {
//			String path = CMN.AssetTag + "liba.mdx";
//			//BookPresenter mdx = new BookPresenter(new File(path), this, 0, null);
//			BookPresenter mdx = md.get(0);
//			TestHelper.insertMegaInAnnotDb(prepareHistoryCon().getDB(), (mdict) mdx.bookImpl);
//		} catch (Exception e) {
//			CMN.Log(e);
//		}
		
		//String[] array = getResources().getStringArray(R.array.drawer_hints);
		//CMN.Log("==??", array[2], array[5], array[2]==array[5], array[2].equals(array[5]), System.identityHashCode(array[2]), System.identityHashCode(array[5]));
		
		// MLSN
		/* CMN.Log("SDK_INT", Build.VERSION.SDK_INT);
		Uri url = Uri.parse("content://com.android.externalstorage.documents/document/primary%3ADownload%2F%E8%8B%B1%E8%AF%AD%E6%9E%84%E8%AF%8D%E6%B3%95.mdx");
		getLazyCC().add(new PlaceHolder("/storage/emulated/0/Download/英语构词法.mdx"));
		md.add(null);*/
		
		//showIconCustomizator();
//		try {
//			md.add(new mdict_dsl("/sdcard/En-En-Longman_Activator.dsl", this));
//			switch_To_Dict_Idx(md.size()-1, false, false);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
//		try {
//			md.add(new BookPresenter(new File("/ASSET/vocabulary.web"), this, 0, opt));
//			switch_To_Dict_Idx(md.size()-1, false, false, null);
//		} catch (Exception e) {
//			CMN.Log(e);
//		}

		//startActivity(new Intent().putExtra("realm",8).setClass(this, SettingsActivity.class));
		//popupWord("History of love", 0, 0);

/*		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_TEXT, "hello"); //Search Query
		intent.putExtra(Intent.EXTRA_HTML_TEXT, "<div style=\"color:red\">hello</div>"); //Search Query
		intent.setPackage("com.ichi2.anki");
		intent.setType("text/plain");
		startActivity(intent);*/

		//bottombar.findViewById(R.id.browser_widget2).performLongClick();
		//bottombar.findViewById(R.id.browser_widget5).performLongClick();
		if(opt.schPage())
			weblistHandler.togSchPage();
//		if(MainPageSearchbar!=null) MainPageSearchetSearch.setText("译");
		//if(false)
		root.postDelayed(() -> {
			//lv.getChildAt(0).performClick();
//			View cover=((ViewGroup) currentDictionary.mWebView.getParent()).getChildAt(0).findViewById(R.id.cover);
//			cover.setTag(0); cover.performClick();
//			ucc.setInvoker(currentDictionary, currentDictionary.mWebView);

			//ReadText("I've worked with TTS a couple of years ago and remember, that there were not so much configuration possibilities.，中国网科技1月6日讯 针对近期比特大陆大规模裁员消息。比特大陆创始人、第一大股东詹克团今日下午再发公开信，称坚决反对这样裁员，自己必须要站出来。他认为，比特大陆有足够的金支持现有员工成本，在AI市场可以像矿机一样从零做到世界第一。");
			//showTTS();
			//showSoundTweaker();
			//JumpToPeruseModeWithWord("doctrine");
			//myWebCClient.onShowCustomView(v, null);

			//widget12.performLongClick();

			//toolbar.setPopupTheme(R.style.toolbarBaseTheme_dark);
		}, 350);

		//showAppTweaker();
		if(CMN.testFLoatSearch)
			startActivity(new Intent(this,FloatSearchActivity.class).putExtra("EXTRA_QUERY", "happy"));

		//JumpToWord("crayon", 1);

		//Intent i = new Intent(this,dict_manager_activity.class); startActivity(i);
		processIntent(getIntent(), true);

		refreshUIColors();

		//lv.setFastScrollEnabled(false);
		//lv.setFastScrollStyle(R.style.AppBright);

		//lv.setVerticalScrollBarEnabled(false);
		//lv.setScrollBarStyle(R.style.AppTheme);
		//lv.setVerticalScrollBarEnabled(true);
		//lv.invalidate();
//xxx
//		if(GlobalOptions.isDark)
//			try {
//				Object FastScroller = FastScrollField.get(lv);
//				ImageView ThumbImage = (ImageView) ThumbImageViewField.get(FastScroller);
//				ThumbImage.setColorFilter(0x8a666666);
//
//
//				FastScroller = FastScrollField.get(mlv1);
//				ThumbImage = (ImageView) ThumbImageViewField.get(FastScroller);
//				ThumbImage.setColorFilter(0x8a666666);
//
//				FastScroller = FastScrollField.get(mlv2);
//				ThumbImage = (ImageView) ThumbImageViewField.get(FastScroller);
//				ThumbImage.setColorFilter(0x8a666666);
//
//				if(lv2.isFastScrollEnabled()) {//why why why
//					FastScroller = FastScrollField.get(lv2);
//					ThumbImage = (ImageView) ThumbImageViewField.get(FastScroller);
//					ThumbImage.setColorFilter(0x8a666666);
//				}
//				//if(pickDictDialog!=null) {
//				//	Object Scrollbar = ScrollCacheFiel
//				//yt	d.get(pickDictDialog.mRecyclerView);
//				//    Drawable ScrollbarDrawable = (Drawable) ScrollBarDrawableField.get(Scrollbar);
//				//    ScrollbarDrawable.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
//				//}
//			} catch (Exception e) {
//				CMN.Log(e);
//			}
	}
	
	private void setLv1ScrollChanged() {
		lv.setOnScrollChangedListener(new OnScrollChangedListener() {
			int lastVisible=-1;
			int lastOff=-1;
			@Override
			public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
				if(lv.dimmed && UIData.viewpager.isListHold) {
					ViewUtils.dimScrollbar(lv, true);
				}
				if(lastVisible!=-1)
					if(lv.getChildAt(0)!=null) {
						if(lv.getFirstVisiblePosition()!=lastVisible || lv.getChildAt(0).getTop()!=lastOff) {
							bNeedSaveViewStates =true;
						}
						lastOff=lv.getChildAt(0).getTop();
					}
				lastVisible=lv.getFirstVisiblePosition();
//				if(getCurrentFocus() instanceof EditText){
//					//tofo 滑动收键盘
//					getCurrentFocus().clearFocus();
//				}
				//CMN.Log("onScrollChange");
		}});
	}

	public AdvancedSearchInterface fuzzySearchLayer;
	public AdvancedSearchInterface fullSearchLayer;
	public AdvancedSearchInterface currentSearchLayer;
	public static class AdvancedSearchInterface extends com.knziha.plod.dictionary.mdict.AbsAdvancedSearchLogicLayer {
		public final ArrayList<BookPresenter> md;
		final PDICMainAppOptions opt;
		String currentSearchPhrase;
		private String currentSearchText;
		public Pattern currentPattern;
		public String currentPageText;
		/** 0=wild card match; 1=regular expression search; 2=plain search. */
		int mSearchEngineType;

		public AdvancedSearchInterface(PDICMainAppOptions opt, ArrayList<BookPresenter> md, int type) {
			this.opt = opt;
			this.md = md;
			this.type = type;

		}

		@Override
		public ArrayList<SearchResultBean>[] getTreeBuilding(Object book, int splitNumber) {
			BookPresenter presenter = (BookPresenter) book;
			if (presenter!=null) {
				if (type==SEARCHTYPE_SEARCHINNAMES) {
					if (presenter.combining_search_tree2==null || presenter.combining_search_tree2.length!=splitNumber) {
						presenter.combining_search_tree2=new ArrayList[splitNumber];
					}
					return presenter.combining_search_tree2;
				} else {
					if (presenter.combining_search_tree_4==null || presenter.combining_search_tree_4.length!=splitNumber) {
						presenter.combining_search_tree_4=new ArrayList[splitNumber];
					}
					return presenter.combining_search_tree_4;
				}
			}
			return null;
		}

		@Override
		public ArrayList<SearchResultBean>[] getTreeBuilt(Object book){
			BookPresenter presenter = (BookPresenter) book;
			if (presenter!=null) {
				return type==SEARCHTYPE_SEARCHINNAMES?presenter.combining_search_tree2:presenter.combining_search_tree_4;
			}
			return null;
		}

		@Override
		public boolean getEnableFanjnConversion() {
			return PDICMainAppOptions.getEnableFanjnConversion();
		}
		
		@Override
		public void setCurrentPhrase(String _currentSearchPhrase) {
			if (type==SEARCHTYPE_SEARCHINNAMES?PDICMainAppOptions.getUseRegex1():PDICMainAppOptions.getUseRegex2()) {
				mSearchEngineType = SEARCHENGINETYPE_REGEX;
			} else {
				mSearchEngineType = PDICMainAppOptions.getAdvSearchUseWildcard()?SEARCHENGINETYPE_WILDCARD:SEARCHENGINETYPE_PLAIN;
			}
			currentSearchPhrase = _currentSearchPhrase;
			currentPageText = null;
			currentPattern = null;
		}
		
		//tofo daxiaoxie
		@Override
		public Pattern getBakedPattern() {
			if(currentPattern==null){
				String val = currentSearchPhrase;
				if(val==null) val="";
				if(mSearchEngineType != SEARCHENGINETYPE_PLAIN)
				try {
					
					currentPattern = Pattern.compile(val, Pattern.CASE_INSENSITIVE);
				} catch (PatternSyntaxException ignored) {  }
				if(currentPattern==null)
					currentPattern = Pattern.compile(val,Pattern.CASE_INSENSITIVE|Pattern.LITERAL);
			}
			return currentPattern;
		}
		
		@Override
		public String getPagePattern() {
			if(currentPageText==null){
				String val = currentSearchPhrase;
				if(val==null) val="";
				String ret=val;
				/*0=wild card; 1=regex search; 2=plain search; */
				int InPageSearchType = PDICMainAppOptions.pageSchUseRegex()?1:PDICMainAppOptions.pageSchWild()?0:2;
				if(InPageSearchType==SEARCHENGINETYPE_WILDCARD){//wild card
					if(mSearchEngineType != SEARCHENGINETYPE_WILDCARD){//直接散开呗
						ret=VerbatimSearchTask.Pattern_VerbatimDelimiter.matcher(val).replaceAll(" ");
					} else { //有得救
						ret = ReplaceMWtoMMWOrRegex(ret, false);
						if(PDICMainAppOptions.pageSchSplitKeys())
							ret = ret.replaceAll("[|&^$]", " ");
					}
				} else if(InPageSearchType==SEARCHENGINETYPE_REGEX){//regex
					if(mSearchEngineType == SEARCHENGINETYPE_WILDCARD){
						ret = ReplaceMWtoMMWOrRegex(ret, true);
						ret = ret.replaceAll("(?<!\\\\)&&?", "|");
					}
				}
				currentPageText = ret;
			}
			return currentPageText;
		}
		
		Pattern startAndDots = Pattern.compile("([.*])(\\([0-9]+\\))?");
		/** Replace wildcard with quantifiers to normal wildcard or regular expression */
		private String ReplaceMWtoMMWOrRegex(String ret, boolean reg) {
			Pattern p = startAndDots;
			Matcher m = p.matcher(ret);
			StringBuffer sb = null;
			StringBuilder dots = null;
			while(m.find()){
				if(sb==null) {
					sb = new StringBuffer(ret.length());
					dots = new StringBuilder();
					if(reg) dots.append(".{");
				}
				String g1 = m.group(1);
				boolean isDots = g1.equals(".");
				String g2 = m.group(2);
				int length = g2==null?1:IU.parsint(g2.substring(1, g2.length()-1), 1);
				if(reg){
					if(g2!=null) {
						dots.setLength(2);
						m.appendReplacement(sb,
							(isDots?dots.append(length).append("}")
									:dots.append("0,").append(length).append("}")).toString()
						);
					} else if(!isDots){
						m.appendReplacement(sb, ".+?");
					}
				} else {
					if(g2!=null){
						if(isDots){
								int dlen = dots.length();
								if (dlen >= length) dots.setLength(length);
								else for (int i = dlen; i < length; i++) dots.append(".");
								m.appendReplacement(sb, dots.toString());
						} else {
							m.appendReplacement(sb, "*");
						}
					}
				}
			}
			if(sb!=null){
				ret = m.appendTail(sb).toString();
			}
			return ret;
		}
		
		@Override
		public int getSearchEngineType() {
			return mSearchEngineType;
		}
	}

	/** 如有必要，重建日志文件 */
	private void CheckGlideJournal() {
		File thumbs_dir=new File(opt.pathToGlide(getApplicationContext()));
		thumbs_dir.mkdirs();
		File journal_file = new File(thumbs_dir, "journal");
		if(opt.getUseLruDiskCache()){
			if(!journal_file.exists()){
				Pattern p = Pattern.compile("[a-z0-9_-]+\\.[0-9]{1,3}");
				File[] arr = thumbs_dir.listFiles(name -> !name.isDirectory() && p.matcher(name.getName()).matches());
				if(arr!=null){
					ArrayList<File> as = new ArrayList<>(Arrays.asList(arr));
					Collections.sort(as, (f1, f2) ->
					{long ret=f1.lastModified()-f2.lastModified();if(ret<0)return -1;if(ret>0)return 1;return 0;});
					long size_count=0; int trim_start=-1;
					for (int i = 0; i < as.size(); i++) {
						size_count+=as.get(as.size()-i-1).length();
						if(size_count>= DiskCache.Factory.DEFAULT_DISK_CACHE_SIZE){
							trim_start=i;
							break;
						}
					}
					if(trim_start!=-1)
						as.subList(0, as.size() - trim_start + 1).clear();
					//size_count=0;
					//for (int i = 0; i < as.size(); i++)   size_count+=as.get(i).length();
					//CMN.Log("oh oh",size_count-DiskCache.Factory.DEFAULT_DISK_CACHE_SIZE);
					try {
						BufferedOutputStream bo = new BufferedOutputStream(new FileOutputStream(journal_file));
						bo.write("libcore.io.DiskLruCache\n1\n1\n1\n\n".getBytes());
						for (File fn:as) {String name=fn.getName();name=name.substring(0,name.lastIndexOf(".")); bo.write(("DIRTY "+name+  ("\nCLEAN "+name+" "+fn.length())  +"\n").getBytes(StandardCharsets.US_ASCII));}
						bo.flush();bo.close();
					} catch (Exception ignored) {}
				}
			}
		} else {
			journal_file.delete();
		}
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
		// Always call the superclass so it can save the view hierarchy state
		//CMN.Log("----->onSaveInstanceState");
		super.onSaveInstanceState(savedInstanceState);

		if(systemIntialized){
			if(DBrowser!=null){
				savedInstanceState.putInt("DB",DBrowser.getFragmentType());
				savedInstanceState.putInt("DBPos",DBrowser.currentPos);
			}

			View VZero = lv2==null?null:lv2.getChildAt(0);
			if(VZero!=null)
				savedInstanceState.putIntArray("P_L2",new int[] {lv2.getFirstVisiblePosition(),VZero.getTop()});

			if(ActivedAdapter!=null) {
				savedInstanceState.putInt("lv_id", ActivedAdapter.getId());
				savedInstanceState.putInt("lv_pos", ActivedAdapter.lastClickedPos);
			}

//			if(PeruseViewAttached()) {
//				savedInstanceState.putIntegerArrayList("p_data", PeruseView.bookIds);
//				savedInstanceState.putString("p_key", PeruseView.etSearch.getText().toString());
//				savedInstanceState.putInt("p_adaidx", PeruseView.bookId);
//				savedInstanceState.putInt("lvp_id", PeruseView.ActivedAdapter.getId());
//				savedInstanceState.putInt("lvp_pos", PeruseView.ActivedAdapter.lastClickedPos);
//			}
		}
	}

	protected void launchVerbatimSearch(String input,final boolean isStrict) {
		if(!checkDicts()) return;
		if(lianHeTask!=null)
			lianHeTask.stop(false);
		lianHeTask = (AsyncTaskWrapper) new VerbatimSearchTask(this, isStrict).execute(input);
	}

	public void switchToSearchModeDelta(int i) {
		int new_curr = CurrentViewPage-i;
		new_curr = new_curr>2?2:new_curr;
		new_curr = new_curr<0?0:new_curr;
		if(new_curr==CurrentViewPage)
			return;
		UIData.viewpager.setCurrentItem(new_curr);
		int msg;
		if(new_curr==1){
			//都灭掉
			if(i>0){//tofo
				lastFullKeyword=etSearch.getText().toString();
			}else{
				lastFuzzyKeyword=etSearch.getText().toString();
			}
			decorateBottombarFFSearchIcons(1);
			//etSearch.setText(lastKeyword);
			CurrentViewPage = new_curr;
			//etSearch.addTextChangedListener(tw1);
			return;
		}
		//etSearch.removeTextChangedListener(tw1);
		lastKeyword = etSearch.getText().toString();
		if(i>0){//亮A
			//etSearch.setText(lastFuzzyKeyword);
			msg=R.string.fuzzyret;
		}else{//亮B
			//etSearch.setText(lastFullKeyword);
			msg=R.string.fullret;
		}
		decorateBottombarFFSearchIcons(new_curr);
		CurrentViewPage = new_curr;
		showTopSnack(null, msg
				, 0.5f, -1, Gravity.CENTER, 0);
	}
	
	private void decorateBottombarFFSearchIcons(int pos) {
		if(BottombarBtns[2]!=null)BottombarBtns[2].setActivated(pos==0);
		if(BottombarBtns[3]!=null)BottombarBtns[3].setActivated(pos==2);
	}
	
	@Override
	protected void onDestroy(){
		//CMN.Log("main_onDestroy");
		if(!shunt) {
			LauncherInstanceCount--;
		}
		if(systemIntialized){
			dumpSettiings();
			if(true){
				new File(opt.pathToMainFolder().toString()).setLastModified(System.currentTimeMillis());
			}
			FilePickerDialog.clearMemory(getBaseContext());
			drawerFragment.onDestroy();
			if(server!=null) {
				server.stop(this);
			}
			cancelToast();
		}
		if(ServiceEnhancer.isRunning) {
			if(PDICMainAppOptions.getAutoClearNotificationOnExit() || !PDICMainAppOptions.getNotificationEnabled()) {
				AU.stopService(this, ServiceEnhancer.class);
			} else {
				Intent intent = new Intent(this, ServiceEnhancer.class);
				intent.putExtra("exit", true);
				startService(intent);
			}
		}
		super.onDestroy();
	}
	
	@Override
	protected void scanSettings(){
		super.scanSettings();
		MainBackground = MainAppBackground = opt.getMainBackground();
		CMN.AppColorChangedFlag &= ~0x1;
		//getWindow().setNavigationBarColor(MainBackground);
		//文件网络
		//SharedPreferences read = getSharedPreferences("lock", MODE_PRIVATE);
		isCombinedSearching = opt.isCombinedSearching();
		//opt.globalTextZoom = read.getInt("globalTextZoom",dm.widthPixels>900?50:80);

		setStatusBarColor(getWindow(), MainBackground);
	}

	private void dumpSettiings(){
		Editor putter = null;
		//CMN.Log(webcontentlist.isDirty,"dumpSettiings", webcontentlist.getPrimaryContentSize());
		if(contentUIData.webcontentlister.isDirty)
			putter = opt.edit().putInt("BBS",contentUIData.webcontentlister.getPrimaryContentSize());
		if(checkFlagsChanged())
			opt.setFlags((putter==null?putter=opt.edit():putter), 0);
		if(putter!=null)
			putter.apply();
	}

	@Override
	protected void onPause() {
		//CMN.Log("onPause");
		try {
			super.onPause();
		} catch (Exception ignored) { }
		//removeBlack();
		if(systemIntialized)
			checkDictionaryProject(true);

		//pg
		//  nimp
		//if(currentDictionary!=null && currentDictionary.file_cache_map!=null)
		//CMN.Log("size", currentDictionary.file_cache_map.size());

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (bNeedSaveViewStates && systemIntialized &&!PDICMainAppOptions.getSimpleMode()){
			bNeedSaveViewStates = false;
			currentDictionary.lvPos = lv.getFirstVisiblePosition();
		}
		if(PDICMainAppOptions.getEnableResumeDebug()){
			//currentDictionary.Reload();
		}
	}

	@Override
	protected void onStop() {
		try {
			super.onStop();
		} catch (Exception ignored) { }
	}

	private void checkDictionaryProject(boolean performSave) {
		if (currentDictionary.getType()== DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_WEB)
			((PlainWeb)currentDictionary.bookImpl).saveWebSearches(this, prepareHistoryCon());
		if(bNeedSaveViewStates) {
			int pos = lv.getFirstVisiblePosition();
			if(currentDictionary.lvPos != pos && !PDICMainAppOptions.getSimpleMode()){
				currentDictionary.lvPos = pos;
				if(lv.getChildAt(0)!=null) currentDictionary.lvPosOff=lv.getChildAt(0).getTop();
				currentDictionary.saveStates(this, prepareHistoryCon());
			}
			bNeedSaveViewStates =false;
		}
//		if(performSave && dirtyMap.size()>0){
//			CMN.rt();
//			if(dirtyMap.size()==1 && SolveOneUIProject(dirtyMap.iterator().next())){
//				CMN.pt(currentDictionary+" 一典配置保存耗时：");
//			} else {
//				dumpViewStates();
//				CMN.pt("dumpViewStates耗时：");
//			}
//			dirtyMap.clear();
//		}
	}

	private int oldTime=-1;
	private boolean removeBlack() {
		if(mView!=null) {
			WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
			wm.removeView(mView);
			mView=null;
			Settings.System.putInt(getContentResolver(), android.provider.Settings.System.SCREEN_OFF_TIMEOUT, oldTime);
			return true;
		}
		return false;
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		focused=hasFocus;
		super.onWindowFocusChanged(hasFocus);
		//CMN.Log("onWindowFocusChanged", hasFocus);
		if(systemIntialized && hasFocus){
			fix_full_screen(getWindow().getDecorView());
			if(textToSetOnFocus!=null){
				etSearch.setText(textToSetOnFocus);
				textToSetOnFocus=null;
			}
			if(PDICMainAppOptions.locale==null)
				recreate();
			checkColors();
			//file-based UI-less command tool
		}
	}

	@Override
	public void fix_full_screen(@Nullable View decorView) {
		if(decorView==null) decorView=getWindow().getDecorView();
		boolean fullScreen = PDICMainAppOptions.isFullScreen();
		fix_full_screen_global(decorView, fullScreen, fullScreen&&PDICMainAppOptions.isFullscreenHideNavigationbar());
	}

	private void checkColors() {
		if(systemIntialized) {
			if(PDICMainAppOptions.getShowPasteBin()!=PDICMainAppOptions.getShowPasteBin(SFStamp)){
				drawerFragment.SetupPasteBin();
			}
			if(PDICMainAppOptions.getKeepScreen()!=PDICMainAppOptions.getKeepScreen(SFStamp)){
				if(PDICMainAppOptions.getKeepScreen()){
					getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				}else{
					getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				}
			}
			if(PDICMainAppOptions.getSimpleMode()!=PDICMainAppOptions.getSimpleMode(QFStamp)){
				adaptermy.notifyDataSetChanged();
			}
			
			if((CMN.AppColorChangedFlag&0x1)!=0)
			{
				MainBackground = MainAppBackground = opt.getMainBackground();
				CMN.AppColorChangedFlag &= ~0x1;
				refreshUIColors();
			}
			if(drawerFragment.sw4!=null && drawerFragment.sw4.isChecked()!=GlobalOptions.isDark){
				switch_dark_mode(GlobalOptions.isDark);
			}
			if(DBrowser!=null) {
				DBrowser.checkColors();
			}
			checkFlags();
		}
	}
	
	public void resetPatterns() {
		if(fullSearchLayer!=null){
			fullSearchLayer.currentPattern = null;
			fullSearchLayer.getBakedPattern();
			fullSearchLayer.currentPageText = null;
			prepareInPageSearch(fullSearchLayer.getPagePattern(), false);
		}
		else if(fuzzySearchLayer!=null){
			fuzzySearchLayer.currentPattern = null;
			fuzzySearchLayer.getBakedPattern();
		}
	}
	
	void refreshUIColors() {
		boolean isHalo=!GlobalOptions.isDark;
		MainAppBackground = isHalo?MainBackground:ColorUtils.blendARGB(MainBackground, Color.BLACK, ColorMultiplier_Wiget);
		int filteredColor = MainAppBackground;//CU.MColor(MainBackground,ColorMultiplier);
		CMN.AppBackground = MainAppBackground;
		UIData.viewpager.setBackgroundColor(AppWhite);
		lv2.setBackgroundColor(AppWhite);
		bottombar.setBackgroundColor(filteredColor);
		UIData.multilineBG.setBackgroundColor(filteredColor & 0x99ffffff);
		//UIData.multiline.setBackgroundColor(filteredColor & 0x99ffffff);
		UIData.etPad.setBackgroundColor(filteredColor);
		UIData.etPad1.setBackgroundColor(filteredColor);
		UIData.schtools.setBackgroundColor(filteredColor);
		toolbar.setBackgroundColor(filteredColor);

//		if(getPinPicDictDialog()) {
//			if(!isHalo)UIData.dialog.getBackground().setColorFilter(GlobalOptions.NEGATIVE);
//			else UIData.dialog.setBackgroundResource(R.drawable.popup_background3_split);
//		}
//		else if(!isHalo) {
//			UIData.dialog.setBackgroundResource(R.drawable.popup_shadow_l);
//			UIData.dialog.getBackground().setColorFilter(GlobalOptions.NEGATIVE);
//		} else {
//			UIData.dialog.setBackgroundResource(R.drawable.popup_background3);
//		}

		setStatusBarColor(getWindow(), filteredColor);
//		if(MainPageSearchbar!=null)//111
//			MainPageSearchbar.setBackgroundColor(filteredColor);
		
//		UIData.browserWidget0.getBackground().setColorFilter(filteredColor, PorterDuff.Mode.SRC_IN);
		
		MainPageBackground = isHalo?GlobalPageBackground:ColorUtils.blendARGB(GlobalPageBackground, Color.BLACK, ColorMultiplier_Web);
		
		weblistHandler.checkUI();
		//showT(Integer.toHexString(filteredColor)+" "+Integer.toHexString(GlobalPageBackground));
		if(dictPicker.pinned()) {
			dictPicker.refresh();
			dictPicker.adapter().notifyDataSetChanged();
		}
	}
	
	private boolean getPinPicDictDialog() {
//		return UIData.dialog.getParent()==UIData.viewpagerPH;
		return false;
	}
	
	public void animateUIColorChanges() {
		mHandle.removeMessages(331122);
		animator = 0.1f;
		animatorD = 0.15f;
		mHandle.sendEmptyMessage(331122);
		boolean isChecked = AppWhite==Color.BLACK;
		fix_pw_color();
		fix_dm_color();
		if(dictPicker !=null) {
			ViewUtils.setListViewScrollbarColor(dictPicker.mRecyclerView, isChecked);
		}
		if(isChecked) {
			ViewUtils.setListViewFastColor(lv, mlv1, mlv2, lv2);
		}
	}
	
	/** use animation for the main view. */
	public void viewContent(WebViewListHandler wlh) {
		wlh.viewContent();
		if(wlh==weblistHandler) {
			delayedAttaching = AttachContentView(opt.getDelayContents());
			if(delayedAttaching) {
				AttachContentViewDelayed(800);
			}
		}
	}
	

	static class ViewHolder {
		int position;
		final View itemView;
		TextView title;
		FlowTextView subtitle;

		public ViewHolder(Context context, int resId, ViewGroup parent) {
			itemView = LayoutInflater.from(context).inflate(resId, parent, false);
			itemView.setId(R.id.lvitems);
			title = itemView.findViewById(R.id.text);
			subtitle = itemView.findViewById(R.id.subtext);
			itemView.setTag(this);
		}
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		if(click_handled_not) {
			onIdClick(v, v.getId());
		}
	}

	// click
	@SuppressLint("SourceLockedOrientationActivity")
	public void onIdClick(View v, int id){
		layoutScrollDisabled=false;
		fadeSnack();
		OUT:
		if(DBrowser!=null) {
			switch(id) {
//				case R.id.browser_widget7:
//				case R.id.browser_widget13:
//				case R.id.browser_widget14:
//				case R.id.browser_widget9:
				default:
					break OUT;
				case R.id.browser_widget8:
					DBrowser.toggleFavor();
					break;
				case R.id.browser_widget10:
					if(ActivedAdapter instanceof PeruseView.LeftViewAdapter) break OUT;
					DBrowser.NavList(-1);
					break;
				case R.id.browser_widget11:
					if(ActivedAdapter instanceof PeruseView.LeftViewAdapter) break OUT;
					DBrowser.NavList(1);
				break;
			}
			return;
		}

		CheckableImageView cb;
		switch(id) {
			//切换翻阅 //333
//			case R.id.browser_widget0:{
//				if(mainF.getChildCount()!=0) return;
//				int msg;
//				if(opt.setInPeruseMode(!opt.getInPeruseMode())) {
//					UIData.browserWidget0.setImageResource(R.drawable.peruse_ic_on);
//					showTopSnack(null, R.string.peruse_mode
//							, 0.5f, -1, Gravity.CENTER, 0);
//				}else {
//					UIData.browserWidget0.setImageResource(R.drawable.peruse_ic);
//					fadeSnack();
//				}
//			} break;
			//切换词典
			case R.drawable.book_list: { // get1:
				if(!checkFastPreview()) {
//					if(ViewUtils.removeIfParentBeOrNotBe(UIData.dialog, UIData.viewpagerPH, true)) {
//						ViewUtils.setVisible(UIData.lnrSplitHdls, false);
//						if(PDICMainAppOptions.getPinPicDictDialog()) {
//							PDICMainAppOptions.setShowPinPicBook(false);
//						}
//					} else { //111
						dismissPopup();
						showChooseDictDialog(0);
//					}
				}
			} break;
			case R.id.multiline:
				etTools.show();
			break;
			//切换搜索模式
			case R.id.toolbar_action1:{
				toggleBatchSearch();
			} break;
			//返回
			case R.id.ivBack:{
//				if((etSearch_toolbarMode&1)==0) {//search
//					if(CurrentViewPage==1) {//viewPager
//						if(etSearch.getText().toString().trim().length()>0) {
//							etSearch.onEditorAction(EditorInfo.IME_ACTION_SEARCH);
//						}
//					} else {
//						etSearch.onEditorAction(EditorInfo.IME_ACTION_SEARCH);
//					}
//					UIData.drawerLayout.closeDrawer(GravityCompat.START);
//				}else {//back
//					lastBackBtnAct = true;
//					onBackPressed();
//					lastBackBtnAct = false;
//					etSearch_ToToolbarMode(0);
//				}
				if(isContentViewAttached()) {
					lastBackBtnAct = true;
					onBackPressed();
					lastBackBtnAct = false;
				}
				etSearch_ToToolbarMode(4);
			} break;
			//切换分组
			case R.drawable.book_bundle:{ // get2:
				if(fastPreview) {
					fastPreview=false;
					contentUIData.bottombar2.setVisibility(View.VISIBLE);
					AttachContentView(false);
				} else {
					if (d != null) {
						d.dismiss();
						d = null;
					}
					showChooseSetDialog();
				}
			} break;
			case R.drawable.book_bundle2:{
				showDictionaryManager();
			} break;
			//两大搜索
			case R.drawable.fuzzy_search:{ // get3:
				checkFastPreview();
				if(CurrentViewPage==0){
					CurrentViewPage = 1;
					UIData.viewpager.setCurrentItem(1);
				}
				else{
					CurrentViewPage = 0;
					UIData.viewpager.setCurrentItem(0);
				}
				lastKeyword = etSearch.getText().toString();
			} break;
			case R.drawable.full_search:{ // get4:
				if(CurrentViewPage==2){
					CurrentViewPage = 1;
					UIData.viewpager.setCurrentItem(1);
				}
				else{
					CurrentViewPage = 2;
					UIData.viewpager.setCurrentItem(2);
				}
				lastFullKeyword=etSearch.getText().toString();
			} break;
			//搜索词典
			case R.id.rcntSch:{
				
//				if(ViewUtils.toggleFadeInFadeOut(UIData.rcntSchList)) {
//
//				}
				
			} break;
			case R.id.settings:{

			} break;
			case R.drawable.ic_menu_drawer_24dp:
			case R.id.drawer_layout:{
				if (UIData.drawerLayout.isDrawerVisible(GravityCompat.START)) {
					UIData.drawerLayout.closeDrawer(GravityCompat.START);
				} else {
					UIData.drawerLayout.openDrawer(GravityCompat.START);
					onDrawerOpened();
				}
			} break;
			case R.drawable.ic_swich_landscape_orientation:{
				int ori = mConfiguration.orientation;
				int DesiredDir;
				if (ori == Configuration.ORIENTATION_LANDSCAPE) {
					DesiredDir=2;
					//bScreenLocked = DesiredDir!=2;
					DesiredDir=DesiredDir==0? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:DesiredDir==1?ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT:ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
					//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
				} else if (ori == Configuration.ORIENTATION_PORTRAIT) {
					DesiredDir=2;
					//bScreenLocked = DesiredDir!=2;
					DesiredDir=DesiredDir==0?ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:DesiredDir==1?ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
				}
			} break;
		}
	}
	
	private boolean checkFastPreview() {
		if (fastPreview) {
			fastPreview=false;
			contentUIData.bottombar2.setVisibility(View.VISIBLE);
			DetachContentView(true);
			PostDCV_TweakTBIC();
			return true;
		}
		return false;
	}
	
	/** 切换主界面沉浸式 */
	public void setNestedScrollingEnabled(boolean bImmersive) {
		if(systemIntialized) {
			for (BookPresenter mdTmp:md) {
				if(mdTmp!=null)
					mdTmp.setNestedScrollingEnabled(bImmersive);
			}
		}
		((AdvancedNestScrollView)weblistHandler.WHP).setNestedScrollingEnabled(bImmersive);
		((AdvancedNestScrollListview)lv).setNestedScrollingEnabled(bImmersive);
		((AdvancedNestScrollListview)lv2).setNestedScrollingEnabled(bImmersive);
		((AdvancedNestScrollListview)mlv1).setNestedScrollingEnabled(bImmersive);
		((AdvancedNestScrollListview)mlv2).setNestedScrollingEnabled(bImmersive);
		if(!bImmersive) {
			if(weblist==null){
				weblist = new LinearLayout(this);
				weblist.setOrientation(LinearLayout.VERTICAL);
			}
			bottombar.setTranslationY(0);
			contentUIData.bottombar2.setTranslationY(0);
		}
		ViewGroup contentHolder = bImmersive ? UIData.webcoord : weblist;
		ViewGroup sp = (ViewGroup) UIData.drawerLayout.getParent();
		if(sp!=contentHolder) {
			if(sp!=null) {
				sp.removeView(UIData.drawerLayout);
				sp.removeView(UIData.appbar);
				root.removeView(sp);
			}
			if(contentHolder.getParent()==null)
				root.addView(contentHolder, 0);
			contentHolder.addView(UIData.appbar, 0);
			contentHolder.addView(UIData.drawerLayout, 1);
			if(bImmersive) {
				((CoordinatorLayout.LayoutParams)UIData.drawerLayout.getLayoutParams()).setBehavior(new AppBarLayout.ScrollingViewBehavior(getBaseContext(), null));
			}
		}

		contentHolder = bImmersive ? UIData.webcoord : UIData.main;
		sp = (ViewGroup) bottombar.getParent();
		if(sp!=contentHolder) {
			//333
//			FrameLayout w0p = (FrameLayout) UIData.browserWidget0.getParent();
//			MarginLayoutParams w0plp = ((MarginLayoutParams) w0p.getLayoutParams());
			if(sp!=null) sp.removeView(bottombar);
			if(bImmersive) {
				contentHolder.addView(bottombar, 2);
				CoordinatorLayout.LayoutParams lp = ((CoordinatorLayout.LayoutParams)bottombar.getLayoutParams());
				lp.gravity=Gravity.BOTTOM;
				((CoordinatorLayout.LayoutParams)bottombar.getLayoutParams()).setBehavior(new BottomNavigationBehavior(getBaseContext(), null));
//				w0plp.bottomMargin=(int) (80*GlobalOptions.density);
//				w0plp.height=w0plp.width;
			} else {
				contentHolder.addView(bottombar, contentHolder.indexOfChild(UIData.viewpagerPH)+1);
				LayoutParams lp = bottombar.getLayoutParams();
				lp.height = (int) getResources().getDimension(R.dimen.barSzBot);
//				w0plp.bottomMargin=(int) (50*GlobalOptions.density);
//				w0plp.height=w0plp.width/5*3; //333
			}
		}
		setContentBow(opt.isContentBow());
	}
	
	Runnable DelayedAttacher = () -> {
		if(delayedAttaching) {
			AttachContentView(false);
		}
	};
	
	public void AttachContentViewDelayed(long ms) {
		root.removeCallbacks(DelayedAttacher);
		root.postDelayed(DelayedAttacher, ms);
	}
	
	boolean AttachContentView(boolean mayDelay) {
		boolean bImmersive = PDICMainAppOptions.getEnableSuperImmersiveScrollMode();
		ViewGroup contentHolder = bImmersive ? UIData.webcoord : root;
		mDrawerToggle.onDrawerOpened(UIData.drawerLayout);
		boolean fastPreview = this.fastPreview;
		
		boolean b1=ViewUtils.removeIfParentBeOrNotBe(contentview, contentHolder,false);
		if(b1 || contentviewDetachType==0 && contentview.getVisibility()!=View.VISIBLE) {
			if(mayDelay) return mayDelay;
			if(contentviewDetachType==0) {
				contentview.setVisibility(View.VISIBLE);
			}
			if(opt.getAnimateContents()) {
				Animation animation = AnimationUtils.loadAnimation(this, R.anim.content_in);
				animation.setAnimationListener(new ViewUtils.BaseAnimationListener(){
					@Override
					public void onAnimationEnd(Animation animation) {
						if(isContentViewAttached()) {
							UIData.viewpagerPH.setVisibility(View.INVISIBLE);
							if(!fastPreview) {
								bottombar.setVisibility(View.INVISIBLE);
							}
						}
					}
				});
				contentview.setTranslationY(0);
				contentview.setAnimation(animation);
			}
			else {
				UIData.viewpagerPH.setVisibility(View.INVISIBLE);
				if(!fastPreview) {
					bottombar.setVisibility(View.INVISIBLE);
				}
			}
			if(b1) {
				if(bImmersive) {
					if (!(contentview.getLayoutParams() instanceof CoordinatorLayout.LayoutParams)) {
						contentview.setLayoutParams(new CoordinatorLayout.LayoutParams(-1, -1));
					}
					CoordinatorLayout.LayoutParams lp = ((CoordinatorLayout.LayoutParams)contentview.getLayoutParams());
					lp.gravity=Gravity.BOTTOM;
					lp.setBehavior(new AppBarLayout.ScrollingViewBehavior(getBaseContext(), null));
					contentHolder.addView(contentview, 2);
				}
				else {
					contentHolder.addView(contentview, PhotoPager!=null&&PhotoPager.getParent()!=null?2:1);
				}
			}
		}
		setContentBow(opt.isContentBow());
		
		if(fastPreview) {
			contentUIData.bottombar2.setVisibility(View.INVISIBLE);
		}
		if(!fastPreview || !bImmersive) {
			PlaceContentBottombar(bImmersive);
		}
		
		return delayedAttaching=false;
	}
	
	//todo adjust bottombar size and place
	private void PlaceContentBottombar(boolean bImmersive) {
		ViewGroup contentHolder = bImmersive ? UIData.webcoord : contentview;
		View bottombar2 = contentUIData.bottombar2;
		if(ViewUtils.removeIfParentBeOrNotBe(bottombar2, contentHolder, false)) {
			contentHolder.addView(bottombar2/*, bImmersive?3:1*/);
			if(bImmersive) {
				CoordinatorLayout.LayoutParams lp = ((CoordinatorLayout.LayoutParams) bottombar2.getLayoutParams());
				lp.gravity = Gravity.BOTTOM;
				lp.setBehavior(new BottomNavigationBehavior(getBaseContext(), null));
				bottombar2.setTranslationY(bottombar.getTranslationY());
			}
		}
	}
	
	@Override
	void DetachContentView(boolean leaving) {
		//CMN.Log("DetachContentView");
		PostDCV_TweakTBIC();
		if (ActivedAdapter!=null) {
			ActivedAdapter.SaveVOA();
		}
		weblistHandler.savePagePos();
		delayedAttaching=false;
		applyMainMenu();
		mDrawerToggle.onDrawerClosed(UIData.drawerLayout);
//		if(DBrowser!=null){
//			AttachContentView();
//		} else {
			boolean bImmersive = PDICMainAppOptions.getEnableSuperImmersiveScrollMode();
			UIData.viewpagerPH.setVisibility(View.VISIBLE);
			bottombar.setVisibility(View.VISIBLE);

			//xxroot.removeView(contentview);
			if(contentviewDetachType==0) {
				contentview.setVisibility(View.GONE);
			} else {
				ViewUtils.removeView(contentview);
			}
			if(bImmersive) {
				ViewUtils.removeView(contentUIData.bottombar2);
			}
			ViewUtils.removeView(PhotoPagerHolder);
			contentUIData.webcontentlister.canClickThrough=false;
//		}
		if(bImmersive) {
			ResetIMOffset();
		}
		if(leaving && opt.getLeaveContentBlank() && ! currentIsWeb()) {
			WebViewmy current_webview = contentUIData.PageSlider.getWebContext();
			if(current_webview !=null) {
//				CMN.debug("页面置空了……");
				current_webview.active = false;
//				current_webview.loadUrl("about:blank");
//				current_webview.clearView();
			}
		}
		checkFastPreview();
	}
	
	private boolean currentIsWeb() {
		return currentDictionary !=null && currentDictionary.getType()==PLAIN_TYPE_WEB;
	}
	
	private void ResetIMOffset() {
		AppBarLayout barappla = (AppBarLayout) UIData.appbar;
		if(barappla.getTop()<0) {
			//CMN.Log("重置了");
			barappla.resetAppBarLayoutOffset();
			barappla.requestLayout();
		}
	}
	
	//longclick
	@SuppressLint("NonConstantResourceId")
	@Override
	public boolean onLongClick(View v) {
		boolean ret = super.onLongClick(v);
		if(ret) return ret;
		switch(v.getId()) {
			case R.id.home:{
				//getPeruseView().TextToSearch = currentDictionary.getEntryAt(pos);
				showIconCustomizator();
			} break;
			case R.drawable.ic_prv_dict_chevron:
			case R.drawable.ic_nxt_dict_chevron: {
				if (isContentViewAttached()) DetachContentView(false);
				else if(PDICMainAppOptions.fastPreview()) {
					if(fastPreview && opt.fastPreviewFragile()) {
						checkFastPreview();
					} else {
						ListView activeLv = CurrentViewPage==0?mlv1:CurrentViewPage==2?mlv2:
								lv2.getVisibility()==View.VISIBLE?lv2:lv;
						View c0 = activeLv.getChildAt(0);
						if(c0!=null) {
							fastPreview=true;
							activeLv.performItemClick(c0, activeLv.getFirstVisiblePosition(), 0);
						}
					}
				}
			} return true;
//			case R.id.browser_widget0:{ //333
//				//getPeruseView().TextToSearch = currentDictionary.getEntryAt(pos);
//				AttachPeruseView(false);
//			} break;
			case R.drawable.book_bundle:{ // get2:
				showDictionaryManager();
			} return true;
			case R.drawable.favoriteg:{ // get5:
				showChooseFavorDialog(0);
			} return true;
			case R.id.lvitems:{
				callDrawerIconAnima();
//				if(currentDictionary instanceof bookPresenter_pdf) {
//					bookPresenter_pdf pdx = (bookPresenter_pdf) currentDictionary;
//					AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
//					builder2.setTitle("PDF 选项");
//
//					builder2.setSingleChoiceItems(new String[]{}, 0,
//							(dialog, pos) -> {
//								switch (pos) {
//									case 0: {//提取目录
//										if (pdx.mWebView == null) {
//											showT("目录尚未加载!");
//											return;
//										}
//										pdx.parseContent();
//									}
//									break;
//									case 1: {//保存目录
//										if(pdx.pdf_index!=null){
//											File path = getExternalFilesDir(".PDF_INDEX");
//											path.mkdirs();
//											path = new File(path, pdx.getDictionaryName());
//											BU.printFile(StringUtils.join(pdx.pdf_index, "\n").getBytes(), path);
//											if(path.exists()) {
//												showT("保存成功");
//											}
//										}
//									}
//									break;
//									case 2: {//关键词索引
//
//									}
//									break;
//								}
//								dialog.dismiss();
//							});
//
//					String[] Menus = getResources().getStringArray(
//							R.array.pdf_option);
//					List<String> arrMenu = Arrays.asList(Menus);
//					AlertDialog d = builder2.create();
//					d.show();
//
//					TextView titleView = d.getWindow().getDecorView().findViewById(R.id.alertTitle);
//					titleView.setSingleLine(false);
//					titleView.setMovementMethod(LinkMovementMethod.getInstance());
//					if (!GlobalOptions.isLarge) titleView.setMaxLines(5);
//
//					d.getListView().setAdapter(new ArrayAdapterHardCheckMark<>(this,
//							R.layout.singlechoice, android.R.id.text1, arrMenu));
//					//drawerFragment.etAdditional.setText(((TextView)v.findViewById(R.id.text)).getText());
//				}
				// nimp
			} return false;
			case R.id.drawer_layout:{
				showIconCustomizator();
			}  return true;
			case R.drawable.ic_fulltext_reader:{
				toggleTTS();
			}  return true;
		}
		return false;
	}
	
	public void showPickFavorFolder() {
		showChooseFavorDialog(0);
	}
	
	@Deprecated
	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	@Override
	public void showChooseTTSDialog() {
		VoicePicker pickTTSDialog = new VoicePicker(this);
		pickTTSDialog.width=(int) (root.getWidth()-getResources().getDimension(R.dimen.diagMarginHor));
		pickTTSDialog.mMaxH=(int) (root.getHeight()-2*getResources().getDimension(R.dimen.diagMarginVer));
		pickTTSDialog.height=-2;
		pickTTSDialog.show(getSupportFragmentManager(), "PickTTSDialog");
	}
	
	
//	/** 显示带搜索框的词典选择器。
//	 * @param reason 发起理由。0：选择当前词典。<br> 1：选择点译上游词典。*/
//	public void showChooseDictDialogx(int reason) {
//		dismissing_dh=false;
//		boolean needRefresh=pickTarget!=reason;
//		pickTarget=reason;
//		if(dialogHolder.getTag()==null) {
//			ResizeDictPicker();
//		}
//		if(dialogHolder.getVisibility()==View.VISIBLE) {
//			dialogHolder.setVisibility(View.GONE);
//			checkFlags();
//			return;
//		}
//		if(reason==1){
//			if(root.getChildAt(root.getChildCount()-1)!=dialogHolder){
//				root.removeView(dialogHolder);
//				root.addView(dialogHolder);
//			}
//		}
//		int bForcePin = 0;
//		if(reason==1) {
//			bForcePin = -1;
//		}
//		// ...
//		if(!isFragInitiated) {
//			ViewUtils.setOnClickListenersOneDepth(dialogHolder, this, 999, 0, null);
//			needRefresh=false;
//			FragmentManager fragmentManager = getSupportFragmentManager();
//			FragmentTransaction transaction = fragmentManager.beginTransaction();
//			pickDictDialog = new DictPicker(this);
//			pickDictDialog.bForcePin = bForcePin;
//			transaction.replace(R.id.dialog_dict_picker, pickDictDialog);
//			findViewById(R.id.dialog_).setOnClickListener(ViewUtils.DummyOnClick);
//			transaction.commit();
//			root.postDelayed(() -> pickDictDialog.PostEnabled=false, 1000);
//			//pickDictDialog.PostEnabled=false;
//
//			dialogHolder.setOnClickListener(new OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					//if(etSearchDict_getWindowToken!=null && Searchbar.hasFocus())
//					boolean pinDlg = UIData.dialog.getParent()==UIData.viewpagerPH;
//					if(ViewUtils.isVisibleV2(Searchbar) || pinDlg)
//					{
//						imm.hideSoftInputFromWindow(etSearchDict_getWindowToken, 0);
//						ViewUtils.setVisibleV2(Searchbar, false);
//						if(pinDlg) ViewUtils.setVisible(dialogHolder, false);
//						return;
//					}
//					dismissDictPicker(R.anim.dp_dialog_exit);
//				}
//			});
//
//			TextViewmy rcntSchList = UIData.rcntSchList;
//			rcntSchList.setMovementMethod(ScrollingMovementMethod.getInstance());
//			rcntSchList.setVerticalScrollBarEnabled(true);
//			opt.setAsLinkedTextView(rcntSchList, false);
//			rcntSchList.setTextSize(GlobalOptions.isLarge?20f:14f);
//			rcntSchList.setMaxLines(3);
//			rcntSchList.longClick=new OnLongClickListener() {
//				@Override
//				public boolean onLongClick(View v) {
//					if(rcntSchList.span!=null) {
//						showT(""+rcntSchList.span);
//						return true;
//					}
//					return false;
//				}
//			};
//			rcntSchList.setOnLongClickListener(opt.XYTouchRecorderInstance());
//
//			isFragInitiated=true;
//			//pickDictDialog.mRecyclerView.scrollToPosition(adapter_idx);
//		}
//		else {
//			pickDictDialog.refresh(false, bForcePin);
//		}
//		if(needRefresh) pickDictDialog.adapter().notifyDataSetChanged();
//
//
//		refreshRecentDictsSpan();
//	}
	
	@SuppressLint("ResourceType")
	@Override
	// menu
	public boolean onMenuItemClick(MenuItem item) {
		int id = item.getItemId();
		if(etTools.isVisible()) {
			etTools.dismiss();
			if(id!=R.id.toolbar_action2)
				return true;
		}
		MenuItemImpl mmi = item instanceof MenuItemImpl?(MenuItemImpl)item:null;
		MenuBuilder menu = mmi.mMenu;
		WebViewListHandler wlh = (WebViewListHandler) menu.tag;
		boolean isLongClicked= mmi!=null && mmi.isLongClicked;
		/* 长按事件默认不处理，因此长按时默认返回false，且不关闭menu。 */
		boolean ret = !isLongClicked;
		boolean closeMenu=ret;
		switch(id){
			default: return super.onMenuItemClick(item);
			case R.id.text_tools:{
				handleTextTools();
			} return true;
			case R.id.toolbar_action1:{
				if(isLongClicked) break;
				onIdClick(null, id);
			}  break;
			/* 折叠全部 */
			case R.id.toolbar_action0:{
				if(isLongClicked) break;
				wlh.toggleFoldAll();
			} break;
			/* 翻页前记忆位置 */
			case R.id.remPagePos:{
				if(isLongClicked){
					ActivedAdapter.avoyager.clear();
					showT("已重置页面位置");
					ret = true;
				} else {
					item.setChecked(opt.setRemPos(!opt.getRemPos()));
				}
			} break;
			case R.id.remPagePos2:{
				if(isLongClicked){
					ActivedAdapter.avoyager.clear();
					showT("已重置页面位置");
					ret = true;
				} else {
					item.setChecked(opt.setRemPos2(!opt.getRemPos2()));
				}
			} break;
			/* 页内查找 */
			case R.id.toolbar_action13:{
				wlh.togSchPage();
			} break;
			case R.id.toolbar_action7://切换词典
				if(isLongClicked) break;
				dismissPopup();
				showChooseDictDialog(0);
			break;
			case R.id.toolbar_action8://切换切换分组
				if(isLongClicked) break;
				findViewById(R.drawable.book_list).performClick();  // get2
			break;
			case R.id.toolbar_action9:{//存书签
				if(isLongClicked) break;
				WebViewmy _mWebView = getCurrentWebContext();
				if (_mWebView!=null) {
					_mWebView.presenter.toggleBookMark(_mWebView, null, false);
				}
			} break;
			case R.id.toolbar_action10:{//保存搜索
				showTopSnack("功能尚未成功");
			} break;
			case R.id.tintList:{//切换着色
				if(isLongClicked){ ret=false; break;}
				TintWildResult.first=opt.toggleTintWildRes();
				item.setChecked(TintWildResult.first);
				if(systemIntialized) {
					adaptermy3.notifyDataSetChanged();
				}
			} break;
			case R.id.toolbar_action2: {
				if(isLongClicked) {
					launchSettings(7, 0);
					ret=true;
				} else {
					String text = etSearch.getText().toString().trim();
					if (CurrentViewPage == 1) {//viewPager
						tw1.onTextChanged(text, 0, 0, 0);
						addHistory(text, schuiMain, null, null);
						etTools.addHistory(text);
					} else {
						etSearch.onEditorAction(EditorInfo.IME_ACTION_SEARCH);
					}
					UIData.drawerLayout.closeDrawer(GravityCompat.START);
				}
			} break;
			case R.id.perwordSch:{//切换分字搜索
				if(isLongClicked){ break;}
				CombinedSearchTask_lastKey=null;
				String text = etSearch.getText().toString().trim();
				String perWSTag = getResources().getString(R.string.perWSTag);
				if(text.startsWith("<")) {
					String firstTag = firstTag(text);
					if(perWSTag.equals(firstTag)||"分字".equals(firstTag)) {
						etSearch.setText(text.substring(text.indexOf(">")+1));
						break;
					}
				}
				etSearch.setText(ToTag(perWSTag)+text);
			} break;
			case R.id.schtools:{//切换搜索工具栏
				if(isLongClicked){ break;}
				boolean newVal = !PDICMainAppOptions.getShowSearchTools();
				PDICMainAppOptions.setShowSearchTools(newVal);
				item.setChecked(newVal);
				ViewUtils.setVisible(UIData.schtools, newVal);
			} break;
			case R.id.peruseList:{//切换主列表翻阅模式
				if(isLongClicked){
					AttachPeruseView(false);
					ret=true;
				} else {
					boolean newVal = !item.isChecked();
					item.setChecked(newVal);
					opt.setInPeruseMode(newVal);
					if (opt.getInPeruseMode()) {
						showTopSnack(null, "点击列表，即进入翻阅模式"
								, 1f, LONG_DURATION_MS, Gravity.CENTER, 0);
					}
				}
			} break;
		}
		if(closeMenu)
			closeIfNoActionView(mmi);
		return ret;
	}
	
	private String firstTag(String text) {
		if(!text.startsWith("<"))return null;
		int idx = text.indexOf(">");
		if(idx==-1)return null;
		return text.substring(1,idx).trim();
	}

	private String ToTag(String input) {
		return "<"+input+">";
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent duco) {
		super.onActivityResult(requestCode, resultCode, duco);
		//CMN.Log("onActivityResult");
		switch (requestCode) {
			case Constants.OpenBookRequset:{  // MLSN
				if(duco!=null) {
					HandleOpenUrl(duco.getData());
				}
			} break;  // MLSN
			case Constants.OpenBooksRequset:{
				if(duco!=null) {
					CMN.Log("已获取目录权限", duco.getData());
				}
			} break;
			case 110:{
				boolean changed = duco!=null && duco.getBooleanExtra("changed", false);
				if (changed){
					loadManager.buildUpDictionaryList(lazyLoadManager.lazyLoaded, mdict_cache);
					if (dictPicker.adapter_idx<0) {
						switch_Dict(0, false, false, null);
					}
					invalidAllLists();
					//CMN.Log("变化了", md.size(), currentFilter.size());
				}
				if(PDICMainAppOptions.ChangedMap !=null && PDICMainAppOptions.ChangedMap.size()>0){
					for(String path: PDICMainAppOptions.ChangedMap) {
						BookPresenter mdTmp = mdict_cache.get(new File(path).getName());
						CMN.Log("重新读取配置！！！", path);
						if(mdTmp!=null)
						try {
							mdTmp.readConfigs(this, prepareHistoryCon());
							//dirtyMap.add(mdTmp.f().getName());
						} catch (IOException e) { if(GlobalOptions.debug) CMN.Log(e); }
						//else dirtyMap.add(new File(path).getName());
					}
					PDICMainAppOptions.ChangedMap = null;
				}
				if (duco!=null && duco.getBooleanExtra("result2", false)) {
					opt.putFirstFlag();
					CMN.Log("保存页码");
				}
				//todo 延时清除
				//mdict_cache.clear();
			} break;
			case 123:{
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					int i = checkSelfPermission(permissions[0]);
					if (i != PackageManager.PERMISSION_GRANTED) {
						RequestAppSettingsPermission();
					} else {
						if (d != null && d.isShowing()) {
							d.dismiss();
							d = null;
						}
						Toast.makeText(this, "权限获取成功", Toast.LENGTH_SHORT).show();
						pre_further_loading(null);
					}
				}
			} break;
			case 700:{
				Uri uri = duco.getData();
				if(uri!=null) {
					getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
					//CMN.show(duco.getIntExtra("asd", -1)+"");
					//DocumentFile encryptMyFolderf = DocumentFile.fromTreeUri(this, uri);
					//encryptMyFolderf.createDirectory("asd");
					//CMN.show(encryptMyFolderf.exists()+"");
					//DocumentFile.fromFile(new File("/storage/0DE6-2108/123.txt")).createFile("", null);
					//CMN.Log(uri);
				}
			} break;
			case 111:
				if (duco != null) {
					if (duco.getBooleanExtra("DC", false))
						drawerFragment.myAdapter.notifyDataSetChangedX();
				}
			break;
			case 1297:
				checkColors();
			break;
		}
		//TODO seal it
		//CMN.a = null;
	}

	private void RequestAppSettingsPermission() {
		new AlertDialog.Builder(this)
			.setTitle("仍无存储权限")
			.setMessage("请前往应用设置-权限，手动打开存储权限")
			.setPositiveButton("前往开启", (dialog, which) -> {
				startActivityForResult(new Intent().setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
						.setData(Uri.fromParts("package", getPackageName(), null)),
						123);
			})
			.setNegativeButton("取消", (dialog, which) -> EnterTrialMode()).setCancelable(false).show();
	}

	//权限申请回调
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		if (requestCode == 321) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
					RequestAppSettingsPermission();
				} else {
					Toast.makeText(this, "权限获取成功", Toast.LENGTH_SHORT).show();
					pre_further_loading(null);
				}
			}
		}
	}

	@Override
	public void jump(int newLexiEntryPos, BookPresenter mdCurr) {

		if(currentDictionary==mdCurr) {
			lv.setSelection(newLexiEntryPos);
			adaptermy.lastClickedPos = newLexiEntryPos;
		}


	}



	float animator = 0.1f;
	float animatorD = 0.15f;
	boolean triggered=false;

	public void callDrawerIconAnima() {
		triggered=false;
		mDrawerToggle.onDrawerClosed(UIData.drawerLayout);
		hdl.sendEmptyMessage(112233);

	}

	@Override
	public void invalidAllLists() {
		//tofo
		if(ActivedAdapter!=null) ActivedAdapter.ClearVOA();
		adaptermy.notifyDataSetChanged();
		//adaptermy2.combining_search_result.invalidate();
		CombinedSearchTask_lastKey=null;
		adaptermy2.results.shutUp();
		adaptermy2.currentKeyText=null;
		adaptermy2.notifyDataSetChanged();

		tw1.onTextChanged(etSearch.getText(), 0, 0, 0);

		adaptermy3.shutUp();adaptermy3.notifyDataSetChanged();
		adaptermy3.results.invalidate();adaptermy3.notifyDataSetChanged();
		adaptermy4.shutUp();adaptermy4.notifyDataSetChanged();
		adaptermy4.results.invalidate();adaptermy4.notifyDataSetChanged();
		if(dictPicker !=null) dictPicker.adapter().notifyDataSetChanged();
	}

	View mView;

	@Override
	public void onActionModeStarted(ActionMode mode) {
		//Toast.makeText(this, mode.getTag()+"ONACTMS"+mode.hashCode(), 0).show();
		//showT(mode.getTag()+" "+mode.getSubtitle()+" "+mode.getTitle());
		//mode.setTag(110);
		//final Menu menu = mode.getMenu();
		//MenuItem MyMenu = menu.add(0, R.id.position, 0, "高亮");

		//Toast.makeText(menu.getItem(0).getTitle()).show();
		//Toast.makeText(this, "\"onActionModeStarted2\"", 0).show();
		//Menu menu = mode.getMenu();
		//menu.clear();
//		if(Build.VERSION.SDK_INT<23) {//defalut float context menu comes from android Marshmallow
//			int share_id=WebViewmy.getReflactField("com.android.internal.R$string", "share");
//			if(share_id!=-1)
//				Toast.makeText(this, ""+getResources().getString(share_id),Toast.LENGTH_SHORT).show();
//
//		}
		super.onActionModeStarted(mode);
	}

	@Override
	public boolean switch_Dict(int i, boolean invalidate, boolean putName, AcrossBoundaryContext prvNxtABC){
		if(invalidate) checkDictionaryProject(false);
		boolean ret = super.switch_Dict(i, invalidate, putName, prvNxtABC);
		if(invalidate) {
			if (!getPinPicDictDialog())
				wordPopup.dismiss();
		}
		return ret;
	}

	@Override
	public void onCreateContextMenu (ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
		//Context menu
		//tofo
		//CMN.Log("onCreateContextMenu", getCurrentFocus());
	}

	protected void switch_dark_mode(boolean val) {
		drawerFragment.sw4.setChecked(val);
	}

	@Override
	protected void notifyFlagChanged() {
		boolean val;
		super.notifyFlagChanged();
		//000!!!
//		if((val=opt.getClickSearchEnabled())!=opt.getClickSearchEnabled(TFStamp)){
//			String title = "即点即译";
//			if(val) title+=" √";
//			AllMenus.findItem(R.id.toolbar_action14).setTitle(title);
//		}
	}
	
	public void startServer(boolean start) {
		if(start) {
			if(getMdictServer()!=null) {
				try {
					getMdictServer().start(this);
					showDrawerSnack("服务器启动成功");
				} catch (IOException e) {
					CMN.Log(e);
				}
			}
		} else {
			if(server!=null) {
				server.stop(this);
				showDrawerSnack("服务已中止");
				//showT("服务已中止");
			}
		}
	}
	
	private void showDrawerSnack(String msg) {
		//土司合着滑块动画有时太卡了
		if(UIData.drawerLayout.isDrawerOpen(Gravity.LEFT)) {
			showTopSnack((ViewGroup) drawerFragment.mDrawerListLayout, msg, 0.8f, -1, -1, 1|0x2);
		} else {
			showT(msg);
		}
	}
	
	public void embedPopInCoordinatorLayout(PlainAppPanel panel, PopupWindow pop, int padbot, ViewGroup root) {
		settingsPopup = pop;
		pop.setWidth(dm.widthPixels);
		//pop.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
		pop.setBackgroundDrawable(null);
		int[] vLocation = new int[2];
		
		ViewGroup svp = (ViewGroup) UIData.drawerLayout.getParent();
		svp.getLocationInWindow(vLocation);
		int topY = vLocation[1];
		//showT(UIData.webcoord.getHeight() +" = "+ UIData.bottombar.getHeight());
		//CMN.Log("embedPopInCoordinatorLayout::padbot::", padbot, panel.bottomPadding);
		if(padbot!=0) {
			if(panel.bottombar!=null) {
				padbot = panel.bottombar.getHeight();
			} else {
				padbot = bottombar!=null?bottombar.getHeight():app_panel_bottombar_height;
			}
			panel.bottomPadding = padbot;
			//CMN.Log("padbot::", bottombar.getHeight(), app_panel_bottombar_height);
		}
		int h = svp.getHeight() - padbot;
		//if (UIData.appbar.getTop()==0)
		//{
		//	int h1 = etSearch.getHeight();
		//	topY += h1;
		//	h -= h1;
		//}
		pop.setWidth(-1);
		pop.setHeight(h);
		if (pop.isShowing()) {
			pop.update(0, topY, -1, h);
		} else {
			// PeruseViewAttached()? peruseView.root: this.root
			pop.showAtLocation(root, Gravity.TOP, 0, topY);
		}
	}
}