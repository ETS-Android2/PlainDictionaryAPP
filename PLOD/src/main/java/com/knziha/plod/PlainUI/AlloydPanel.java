package com.knziha.plod.PlainUI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.annotation.GlideOption;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.databinding.ContentviewBinding;
import com.knziha.plod.widgets.DarkToggleButton;
import com.knziha.plod.widgets.SplitView;
import com.knziha.plod.widgets.SwipeBackLayout;

import java.util.ArrayList;
import java.util.List;

public class AlloydPanel extends PlainAppPanel {
	protected MainActivityUIBase a;
	public ContentviewBinding contentUIData;
	public Toolbar toolbar;
	public MenuBuilder AllMenus;
	public List<MenuItemImpl> RandomMenu;
	
	public AlloydPanel(MainActivityUIBase a, @NonNull ContentviewBinding contentUIData) {
		super(a);
		this.contentUIData=contentUIData;
		this.bottomPadding = 0;
		this.bPopIsFocusable = true;
		this.bFadeout = -2;
		setShowInDialog();
	}
	
	@SuppressLint("ResourceType")
	@Override
	public void init(Context context, ViewGroup root) {
		if(a==null) {
			a=(MainActivityUIBase) context;
			mBackgroundColor = 0;
			setShowInDialog();
		}
		if (settingsLayout==null && contentUIData!=null) {
			SplitView linearView = contentUIData.webcontentlister;
			Toolbar toolbar = this.toolbar = new Toolbar(context);
			linearView.addView(toolbar, 0);
			toolbar.getLayoutParams().height = (int) (GlobalOptions.density * 45);
			toolbar.setBackgroundColor(a.MainAppBackground);
			toolbar.setTitleTextColor(Color.WHITE);
			toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);
			toolbar.inflateMenu(R.xml.menu_popup_content);
			AllMenus = (MenuBuilder) toolbar.getMenu();
			RandomMenu = new ArrayList<>(AllMenus.mItems);
			toolbar.setNavigationOnClickListener(v -> dismiss());
			toolbar.setOnMenuItemClickListener(a);
			
			settingsLayout = linearView;
		}
	}
	
	@Override
	public void refresh() {
	
	}
	
	@SuppressLint("ResourceType")
	@Override
	public void onClick(View v) {
//		if (v.getId() == R.id.dayNightBtn) {
//			dayNightArt.toggle(true);
//			a.toggleDarkMode();
//		} else {
//			dismiss();
//			if (v.getId() != R.drawable.ic_menu_24dp) {
//				a.mInterceptorListenerHandled = true;
//			}
//		}
	}
	
	@Override
	protected void onDismiss() {
		CMN.Log("onDismiss::");
//		super.onDismiss();
//		if (mSettingsChanged!=0) {
//			//a.currentViewImpl.checkSettings(true, true);
//			mSettingsChanged=0;
//		}
//		mScrollY = settingsLayout.getScrollY();
	}
}