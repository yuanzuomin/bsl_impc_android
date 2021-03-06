package com.foreveross.chameleon.phone.modules;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.foreveross.chameleon.Application;
import com.csair.impc.R;
import com.foreveross.chameleon.store.core.StaticReference;
import com.foreveross.chameleon.store.model.MessageModule;
import com.foreveross.chameleon.util.imageTool.CubeAsyncImage;
import com.j256.ormlite.stmt.DeleteBuilder;

public class SectionListElement implements ListElement {
	private String msgSort;
	private int msgNum;
	private String msgIcon;
	
	private boolean isExpanded = false;
	
	private MsgTitleItem msgTitleItem;
	private RelativeLayout msg_num_layout;

	private RelativeLayout delete_msg_layout;
	
	private boolean displayFlag = false;
	
	public void setText(String msgSort,int msgNum,String msgIcon) {
		this.msgSort=msgSort;
		this.msgNum=msgNum;
		this.msgIcon=msgIcon;
	}
	
	public String getText()
	{
		return this.msgSort;
	}

	@Override
	public int getLayoutId() {
		return R.layout.msg_title;
	}

	@Override
	public boolean isClickable() {
		return false;
	}

	@Override
	public View getViewForListElement(LayoutInflater layoutInflater,
			Context context, View view,ViewGroup parent,int type) {
		if(view==null){
			if(type==MessageListAdapter.TYPE_TITLE){
				msgTitleItem = new MsgTitleItem();
				view = layoutInflater.inflate(getLayoutId(), null);
				msgTitleItem.msgIcon=(ImageView) view.findViewById(R.id.msg_icon);
				msgTitleItem.msgSort= (TextView) view.findViewById(R.id.msg_sort);
				msgTitleItem.msgNum= (TextView) view.findViewById(R.id.msg_num);
				msg_num_layout = (RelativeLayout) view.findViewById(R.id.msg_num_layout);
				delete_msg_layout = (RelativeLayout) view.findViewById(R.id.delete_msg_layout);
				msgTitleItem.msg_num_layout = msg_num_layout;
				msgTitleItem.delete_msg_layout = delete_msg_layout;
				view.setTag(msgTitleItem);
			}
		}else{
			msgTitleItem = (MsgTitleItem) view.getTag();
			msg_num_layout = msgTitleItem.msg_num_layout;
			delete_msg_layout = msgTitleItem.delete_msg_layout;
		}
		if(!displayFlag)
		{
			delete_msg_layout.setVisibility(View.GONE);
			LayoutParams params = (LayoutParams) msg_num_layout.getLayoutParams();
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,RelativeLayout.TRUE);
			msg_num_layout.setLayoutParams(params);
		}
		else
		{
			delete_msg_layout.setVisibility(View.VISIBLE);
			final Context mContxt = context;
			delete_msg_layout.setOnClickListener(new OnClickListener()
			{
				
				@Override
				public void onClick(View v)
				{
					final View vm = v;
					new AlertDialog.Builder(mContxt)
					.setTitle("提示")
					.setMessage("确定删除？")
					.setNegativeButton("取消", null)
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,int which) {
									Set<CubeModule> moduleList = CubeModuleManager.getInstance().getAllSet();
									for (CubeModule cubeModule : moduleList)
									{
										if(cubeModule.getName().equals(getText()))
										{
											try {
												DeleteBuilder<MessageModule, Long> delBuilder=StaticReference.defMf.deleteBuilder(MessageModule.class);
												delBuilder.where().eq("msgSort", cubeModule.getIdentifier());
												delBuilder.delete();
											} catch (SQLException e) {
												e.printStackTrace();
											}
//											MessageModule.delete(db,app, MessageModule.class, "msgSort='"+cubeModule.getIdentifier()+"'");
										}
									}
									if("系统".equals(getText()))
									{	
										try {
											DeleteBuilder<MessageModule, Long> delBuilder=StaticReference.defMf.deleteBuilder(MessageModule.class);
											delBuilder.where().eq("msgSort", getText());
											delBuilder.delete();
										} catch (SQLException e) {
											e.printStackTrace();
										}
//										MessageModule.delete(db,app, MessageModule.class, "msgSort='"+getText()+"'");
									}
									Toast.makeText(mContxt, "删除成功", Toast.LENGTH_SHORT).show();
									ExpandableListView list = (ExpandableListView) vm.getParent().getParent();
									MsgExpanderListAdapter adapter = (MsgExpanderListAdapter) list.getExpandableListAdapter();
									Map<SectionListElement, List<ContentListElement>> scmap = adapter.getScMap();
									scmap.remove(SectionListElement.this);
									adapter.setScMap(scmap);
									adapter.notifyDataSetChanged();
									Intent intent = new Intent("com.foss.moduleContentChange");
									Application.class.cast(mContxt.getApplicationContext()).sendBroadcast(intent);
									
								}
							}).show();
					
				}
			});
			LayoutParams params = (LayoutParams) msg_num_layout.getLayoutParams();
			msg_num_layout.setLayoutParams(params);
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,0);
			msg_num_layout.setLayoutParams(params);
		}
		
			msgTitleItem.msgSort.setText(msgSort); 
			
			if(msgNum>10){
				msgTitleItem.msgNumBg.setImageResource(R.drawable.msg_num_bg_big);
			}else{
				msgTitleItem.msgNumBg.setImageResource(R.drawable.msg_num_bg);
			}
			msgTitleItem.msgNum.setText(msgNum+"");
			if(isExpanded)
			{
				msgTitleItem.msgIcon.setImageResource(R.drawable.arrow_dowm);
			}
			else
			{
				msgTitleItem.msgIcon.setImageResource(R.drawable.arrow);
			}
//			setImageIcon(context,msgIcon,msgTitleItem.msgIcon,parent);
			return view;
	}

	@Override
	public Object getTitle() {
		// TODO Auto-generated method stub
		return msgTitleItem;
	}
    public class MsgTitleItem{
    	public ImageView msgNumBg;
    	public TextView msgSort;
    	public TextView msgNum;
    	public ImageView msgIcon;
    	public RelativeLayout msg_num_layout;
    	public RelativeLayout delete_msg_layout;
    }
    public void setImageIcon(Context context,String url,ImageView icon,final View parent){
		CubeAsyncImage asyncImageLoader=new CubeAsyncImage((Activity) context);
		if(null==url){
			//没有从服务器中下载到头像，则至为默认头像
			icon.setImageResource(R.drawable.defauit);
			icon.setTag(null);
		}else if(url.startsWith("local:")){
			String urlName=url.substring(6);
			AssetManager asm=context.getAssets();
			java.io.InputStream inputStream=null;
			try {
				inputStream = asm.open("www/res/icon/android/"+urlName);
			} catch (IOException e) {
				e.printStackTrace();
			}
			Drawable drawable=Drawable.createFromStream(inputStream, null);
			icon.setImageDrawable(drawable);
			icon.setTag(url.toString());
		
		}else{
				icon.setTag(url.toString());
				// 异步加载图片，从缓存，内存卡获取图片。
				Bitmap bitmap = asyncImageLoader.loadImage(url.toString(), new CubeAsyncImage.ImageCallback() {// 如果无法从缓存，内存卡获取图片，则会调用该方法从网络上下载
					public void imageLoaded(Bitmap bitmap, String imageUrl) {
						ImageView imageViewByTag = (ImageView) parent.findViewWithTag(imageUrl);
						if (imageViewByTag == null) {
							return;
						}
						if (null != bitmap) {
							imageViewByTag.setImageBitmap(bitmap);
				
						} else {
							imageViewByTag.setImageResource(R.drawable.defauit);
						}
					}
				});
				
				if (bitmap != null) {
					// 设置图片显示
					icon.setImageBitmap(bitmap);
				} else {
					icon.setImageResource(R.drawable.defauit);
				}
			}
	}

	/**
	 * @return the msg_num_layout
	 */
	public RelativeLayout getMsg_num_layout()
	{
		return msg_num_layout;
	}

	/**
	 * @param msg_num_layout the msg_num_layout to set
	 */
	public void setMsg_num_layout(RelativeLayout msg_num_layout)
	{
		this.msg_num_layout = msg_num_layout;
	}

	/**
	 * @return the delete_msg_layout
	 */
	public RelativeLayout getDelete_msg_layout()
	{
		return delete_msg_layout;
	}

	/**
	 * @param delete_msg_layout the delete_msg_layout to set
	 */
	public void setDelete_msg_layout(RelativeLayout delete_msg_layout)
	{
		this.delete_msg_layout = delete_msg_layout;
	}

	/**
	 * @return the displayFlag
	 */
	public boolean isDisplayFlag()
	{
		return displayFlag;
	}

	/**
	 * @param displayFlag the displayFlag to set
	 */
	public void setDisplayFlag(boolean displayFlag)
	{
		this.displayFlag = displayFlag;
	}

	/**
	 * @return the msgTitleItem
	 */
	public MsgTitleItem getMsgTitleItem()
	{
		return msgTitleItem;
	}

	/**
	 * @param msgTitleItem the msgTitleItem to set
	 */
	public void setMsgTitleItem(MsgTitleItem msgTitleItem)
	{
		this.msgTitleItem = msgTitleItem;
	}

	/**
	 * @return the isExpanded
	 */
	public boolean isExpanded()
	{
		return isExpanded;
	}

	/**
	 * @param isExpanded the isExpanded to set
	 */
	public void setExpanded(boolean isExpanded)
	{
		this.isExpanded = isExpanded;
	}

	/**
	 * @return the msgNum
	 */
	public int getMsgNum()
	{
		return msgNum;
	}

	/**
	 * @param msgNum the msgNum to set
	 */
	public void setMsgNum(int msgNum)
	{
		this.msgNum = msgNum;
	}



}
