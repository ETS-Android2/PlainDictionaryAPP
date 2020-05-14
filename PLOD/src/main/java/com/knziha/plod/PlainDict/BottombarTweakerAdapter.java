package com.knziha.plod.PlainDict;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;

import com.jaredrummler.colorpicker.ColorPickerDialog;
import com.knziha.ankislicer.customviews.ShelfLinearLayout;
import com.knziha.filepicker.widget.CircleCheckBox;
import com.knziha.plod.dictionary.Utils.IU;
import com.mobeta.android.dslv.DragSortListView;

import static com.knziha.plod.PlainDict.Toastable_Activity.FLASH_DURATION_MS;

class BottombarTweakerAdapter extends BaseAdapter implements View.OnClickListener, DragSortListView.DragSortListener, View.OnLongClickListener{
	private final AlertDialog dialog;
	private final MainActivityUIBase a;
	private final PDICMainAppOptions opt;
	public PDICMainActivity.AppUIProject projectContext;
	public static int StateCount=2;
	public boolean isDirty;
	public Drawable switch_landscape;
	PorterDuffColorFilter darkMask = new PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
	private String[] customize_ctn;
	
	public BottombarTweakerAdapter(AlertDialog d, MainActivityUIBase _a) {
		dialog = d;
		opt = _a.opt;
		a = _a;
		switch_landscape = _a.getResources().getDrawable(R.drawable.ic_screen_rotation_black_24dp);
	}

	public String MakeProject(){
		int size = projectContext.iconData.size()-1;
		StringBuilder sb = new StringBuilder(64);
		projectContext.bNeedCheckOrientation=false;
		for (int i = 0; i <= size; i++) {
			PDICMainActivity.IconData icI = projectContext.iconData.get(i);
			icI.addString(sb);
			if(icI.tmpIsFlag==2){
				projectContext.bNeedCheckOrientation=true;
			}
			if(i!=size) sb.append("|");
		}
		return sb.toString();
	}

	@Override
	public int getCount() {
		return projectContext==null?0:projectContext.iconData.size();
	}

	@Override
	public Object getItem(int position) {
		return projectContext.iconData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return projectContext.iconData.get(position).number;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder vh=convertView==null?new ViewHolder(parent, this):(ViewHolder)convertView.getTag();
		PDICMainActivity.IconData item = projectContext.iconData.get(position);
		int id = item.number;
		Resources res = parent.getResources();
		vh.tv.setText(projectContext.titles[id]);
		vh.ccb_icon.setDrawable(0, res.getDrawable(projectContext.icons[id]));
		vh.ccb_toggle.setChecked(item.tmpIsFlag,false);
		vh.position = position;
		vh.itemView.getBackground().setAlpha(GlobalOptions.isDark?15:255);
		return vh.itemView;
	}

	@Override
	public boolean onLongClick(View v) {
		int id = v.getId();
		switch (id){
			default:
				int currentType = projectContext.type;
				if(currentType>=0){
					int bottombar_copy_from=0;
					if(id == R.id.customise_peruse_bar){
						bottombar_copy_from=1;
					} else if(id == R.id.customise_float_bar){
						bottombar_copy_from=2;
					}
					if(bottombar_copy_from!=currentType){
						String copy_from = a.getResources().getStringArray(R.array.bottombar_types)[bottombar_copy_from+1];
						String copy_to = a.getResources().getStringArray(R.array.bottombar_types)[currentType+1];
						int final_Bottombar_copy_from = bottombar_copy_from;
						
						String[] DictOpt = a.getResources().getStringArray(R.array.appbar_conf);
						final String[] Coef = DictOpt[0].split("_");
						final SpannableStringBuilder ssb = new SpannableStringBuilder();
						
						TextView tv = a.buildStandardConfigDialog(a, false, v12 -> {
							opt.linkContentbarProject(currentType, final_Bottombar_copy_from);
							projectContext.currentValue = opt.getAppContentBarProject(currentType);
							isDirty = false;
							projectContext.instantiate(customize_ctn);
							notifyDataSetChanged();
							a.showX(opt.getLinkContentBarProj()?R.string.linkedft:R.string.copyedft, Toast.LENGTH_LONG, copy_to, copy_from);
						}, R.string.warn_copyconfrom, copy_from);
						
						a.init_clickspan_with_bits_at(tv, ssb, DictOpt, 1, Coef, 0, 0, 0x1, 41, 1, 4, -1, true);
						ssb.delete(ssb.length()-4,ssb.length());
						
						tv.setText(ssb, TextView.BufferType.SPANNABLE);
						((AlertDialog) tv.getTag()).show();
						tv.setTag(null);
					}
				}
			break;
			case DialogInterface.BUTTON_POSITIVE:{
			
			} break;
			case DialogInterface.BUTTON_NEGATIVE:{
				if(StateCount==2){
					StateCount=1;
				} else {
					StateCount=2;
				}
				ColorPickerDialog.showTopToast(v.getContext(), "StateCount#"+StateCount);
			} break;
		}
		return true;
	}
	
	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id){
			case R.id.customise_main_bar:{
				if(isDirty){
					checkCurrentProject(v);
					break;
				}
				if(a instanceof PDICMainActivity){
					PDICMainActivity aa = (PDICMainActivity) a;
					
					if(aa.bottombar_project==null)
						aa.bottombar_project = new MainActivityUIBase.AppUIProject("btmprj", aa.BottombarBtnIcons, aa.BottombarBtnIds, opt.getAppBottomBarProject(), aa.bottombar, aa.BottombarBtns);
					
					if(aa.bottombar_project.iconData==null)
						aa.bottombar_project.instantiate(aa.getResources().getStringArray(R.array.customize_btm));
					
					projectContext=aa.bottombar_project;
					((ShelfLinearLayout) v.getParent()).setRbyView(v);
					dialog.setTitle("定制底栏-主程序");
					notifyDataSetChanged();
				}
				else {
					//tofo 跳转至主界面？
				}
			} break;
			case R.id.customise_main_content_bar:{
				if(isDirty){
					checkCurrentProject(v);
					break;
				}
				
				int bottombar_from=0;
				if(id == R.id.customise_peruse_bar){
					bottombar_from=1;
				} else if(id == R.id.customise_float_bar){
					bottombar_from=2;
				}
				
				
				MainActivityUIBase.AppUIProject projectContext=null;
				
				boolean isProjHost = bottombar_from==0?a instanceof PDICMainActivity:a instanceof FloatSearchActivity;
				if(isProjHost){
					projectContext = a.contentbar_project;
				}
				if(projectContext==null){
					projectContext = new MainActivityUIBase.AppUIProject(0, a.opt, a.ContentbarBtnIcons, a.ContentbarBtnIds, isProjHost?a.bottombar2:null, isProjHost?a.ContentbarBtns:null);
					if(isProjHost){
						a.contentbar_project = projectContext;
					}
				}
				if(projectContext.iconData==null){
					if(customize_ctn==null)
						customize_ctn=a.getResources().getStringArray(R.array.customize_ctn);
					projectContext.instantiate(customize_ctn);
				}
				
				this.projectContext = projectContext;
				((ShelfLinearLayout) v.getParent()).setRbyView(v);
				dialog.setTitle("定制底栏-"+a.getResources().getStringArray(R.array.bottombar_types)[bottombar_from+1]);
				notifyDataSetChanged();
			} break;
			case R.id.customise_peruse_bar:{
				if(isDirty){
					checkCurrentProject(v);
					break;
				}
				MainActivityUIBase.AppUIProject projectContext = a.peruseview_project;
				if(projectContext==null){
					projectContext = new MainActivityUIBase.AppUIProject(1, a.opt, a.ContentbarBtnIcons, a.ContentbarBtnIds, null, null);
					a.peruseview_project = projectContext;
				}
				if(a.PeruseView != null && projectContext.btns==null){
					projectContext.bottombar = a.PeruseView.bottombar2;
					projectContext.btns = a.PeruseView.ContentbarBtns;
				}
				if(projectContext.iconData==null){
					if(customize_ctn==null)
						customize_ctn=a.getResources().getStringArray(R.array.customize_ctn);
					projectContext.instantiate(customize_ctn);
				}
				
				this.projectContext = projectContext;
				((ShelfLinearLayout) v.getParent()).setRbyView(v);
				dialog.setTitle("定制底栏-翻阅模式");
				notifyDataSetChanged();
			} break;
			case DialogInterface.BUTTON_POSITIVE:{
				if(projectContext==null) return;
				checkCurrentProjectInternal(null);
				dialog.dismiss();
			} break;
			case DialogInterface.BUTTON_NEGATIVE:{
				clearCurrentProject();
				dialog.dismiss();
			} break;
			case android.R.id.text1:
			case R.id.check1:{
				isDirty=true;
				ViewGroup vp = ((ViewGroup) v.getParent());
				ViewHolder vh = (ViewHolder) vp.getTag();
				CircleCheckBox ccb_toggle = (CircleCheckBox) vp.getChildAt(1);
				ccb_toggle.setProgress(0);
				if(StateCount==2){
					ccb_toggle.iterate();
				} else {
					ccb_toggle.toggle();
				}
				projectContext.iconData.get(vh.position).tmpIsFlag=ccb_toggle.getChecked();
			} break;
			case R.id.check2:{
				CircleCheckBox ccb_icon = (CircleCheckBox) v;
				ccb_icon.setProgress(0);
				ccb_icon.addAnim(true);
			} break;
		}
	}

	void checkCurrentProject(View v) {
		String val = MakeProject();
		if(v!=null && projectContext!=null && !val.equals(projectContext.currentValue)){
			DialogInterface.OnClickListener ocl = (dialog, which) -> {
				if (which == DialogInterface.BUTTON_POSITIVE) {
					checkCurrentProjectInternal(val);
				} else if (which == DialogInterface.BUTTON_NEGATIVE) {
					clearCurrentProject();
				}
				v.performClick();
				dialog.dismiss();
			};
			new AlertDialog.Builder(a)
				.setTitle("配置已更改，是否应用？")
				.setPositiveButton(R.string.confirm, ocl)
				.setNegativeButton(R.string.no, ocl)
				.setNeutralButton(R.string.cancel, ocl)
				.create().show();
		} else {
			checkCurrentProjectInternal(val);
		}
	}

	void clearCurrentProject() {
		if(isDirty && projectContext!=null){
			MainActivityUIBase.AppUIProject _projectContext = projectContext;
			projectContext=null;
			_projectContext.clear();
		}
		isDirty=false;
	}

	void checkCurrentProjectInternal(String newVal) {
		if(isDirty){
			if(newVal==null) newVal = MakeProject();
			boolean bNeedSave = !newVal.equals(projectContext.currentValue);
			if(bNeedSave){
				projectContext.currentValue=newVal;
				if(projectContext.bottombar!=null){
					a.RebuildBottombarIcons(projectContext, a.mConfiguration);
				}
				if(projectContext.type>=0){
					checkReferncedChange(a.contentbar_project, newVal);
					checkReferncedChange(a.peruseview_project, newVal);
				}
				opt.putAppProject(projectContext);
			}
			isDirty=false;
		}
	}
	
	private void checkReferncedChange(MainActivityUIBase.AppUIProject checkNow, String newVal) {
		if(checkNow!=null && checkNow!=projectContext && opt.isAppContentBarProjectReferTo(checkNow.key, projectContext.type)){
			checkNow.currentValue=newVal;
			checkNow.clear();
			a.RebuildBottombarIcons(checkNow, a.mConfiguration);
		}
	}
	
	@Override
	public void drag(int from, int to) {

	}

	@Override
	public void drop(int from, int to) {
		if (from != to) {
			isDirty=true;
			dialog.setCanceledOnTouchOutside(false);
			PDICMainActivity.IconData item = projectContext.iconData.remove(from);
			projectContext.iconData.add(to, item);
			notifyDataSetChanged();
		}
	}
	@Override
	public void remove(int which) {  }
	
	static class ViewHolder{
		ViewHolder(ViewGroup v, BottombarTweakerAdapter biantai){
			itemView=(ViewGroup)LayoutInflater.from(v.getContext()).inflate(R.layout.circle_checker_item, v, false);
			itemView.setTag(this);
			ccb_icon = (CircleCheckBox) itemView.getChildAt(0);
			ccb_icon.drawIconForEmptyState = true;
			ccb_icon.drawInnerForEmptyState=false;
			ccb_icon.noTint=true;
			ccb_icon.setProgress(1);
			ccb_icon.setOnClickListener(biantai);

			ccb_toggle = (CircleCheckBox) itemView.getChildAt(1);
			ccb_toggle.drawIconForEmptyState=false;
			ccb_toggle.circle_shrinkage=0.75f*biantai.opt.dm.density;
			ccb_toggle.addStateWithDrawable(biantai.switch_landscape);
			ccb_toggle.setOnClickListener(biantai);

			tv = (TextView) itemView.getChildAt(2);
			tv.setOnClickListener(biantai);
		}
		private final ViewGroup itemView;
		public int position;
		CircleCheckBox ccb_icon;
		CircleCheckBox ccb_toggle;
		TextView tv;
	}
}