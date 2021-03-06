/**
 * 
 */
package com.foreveross.chameleon.pad.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.csair.impc.R;
import com.foreveross.chameleon.Application;
import com.foreveross.chameleon.CubeConstants;
import com.foreveross.chameleon.TmpConstants;
import com.foreveross.chameleon.activity.FacadeActivity;
import com.foreveross.chameleon.event.EventBus;
import com.foreveross.chameleon.phone.chat.search.SearchFriendAdapter;
import com.foreveross.chameleon.phone.muc.IChoisedEventListener;
import com.foreveross.chameleon.phone.muc.MucAddFriendAdapter;
import com.foreveross.chameleon.phone.muc.MucBroadCastEvent;
import com.foreveross.chameleon.push.client.XmppManager;
import com.foreveross.chameleon.push.mina.library.util.PropertiesUtil;
import com.foreveross.chameleon.store.core.StaticReference;
import com.foreveross.chameleon.store.model.ChatGroupModel;
import com.foreveross.chameleon.store.model.ConversationMessage;
import com.foreveross.chameleon.store.model.IMModelManager;
import com.foreveross.chameleon.store.model.SessionModel;
import com.foreveross.chameleon.store.model.UserModel;
import com.foreveross.chameleon.store.model.UserStatus;
import com.squareup.otto.Subscribe;
import com.squareup.otto.ThreadEnforcer;

/**
 * [一句话功能简述]<BR>
 * [功能详细描述]
 * 
 * @author 冯伟立
 * @version [CubeAndroid, 2013-9-17]
 */
public class MucAddFirendFragment extends Fragment implements IChoisedEventListener{
	private MucAddFriendAdapter adapter;
	private Button titlebar_left;
	private Button titlebar_right;
	private TextView titlebar_content;
	private ListView listView;
	private Map<String, UserModel> selectFriends = null;
	private List<UserModel> invitors = new ArrayList<UserModel>();
	private Application application;
	private ChatGroupModel chatGroupModel = null;
	
	// 搜索的内容
	private EditText app_search_edt;
	private ImageView app_search_close;
	private ListView searchListView;
	private SearchFriendAdapter searchFriendAdapter;

	private List<UserModel> searchFriendList;
	private Button searchBtn;
	
	private ProgressDialog progressDialog;  
	
	private LinearLayout mContentLayout;
	
	HashMap<UserModel, RelativeLayout> hashMap;
	
	private Button add_friend_btn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		application = Application.class.cast(this.getAssocActivity()
				.getApplication());

	}

	/**
	 * [一句话功能简述]<BR>
	 * [功能详细描述]
	 * 
	 * @param inflater
	 * @param container
	 * @param savedInstanceState
	 * @return 2013-9-17 下午2:21:04
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.chat_muc_addfriend, container,false);
	}

	/**
	 * [一句话功能简述]<BR>
	 * [功能详细描述]
	 * 
	 * @param view
	 * @param savedInstanceState
	 *            2013-9-17 下午2:21:33
	 */
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initValues(view);
		EventBus.getEventBus(TmpConstants.EVENTBUS_MUC_BROADCAST,
				ThreadEnforcer.MAIN).register(this);
	}

	public void initValues(View view) {
		String roomJid = getAssocActivity().getIntent().getStringExtra(
				"roomJid");
		if (roomJid == null) {
			chatGroupModel = new ChatGroupModel();
		} else {
			chatGroupModel = IMModelManager.instance().getChatRoomContainer()
					.getStuff(roomJid);
		}
		
		if (chatGroupModel == null) {
			throw new IllegalStateException("不可能为空,传入的roomJid有错吗?");
		}
		add_friend_btn = (Button) view.findViewById(R.id.add_friend_btn);
//		add_friend_btn.setBackgroundResource(R.drawable.mm_title_act_btn_disable);
//		add_friend_btn.setClickable(false);
		add_friend_btn.setOnClickListener(clickListener);
		hashMap = new HashMap<UserModel, RelativeLayout>();
		mContentLayout = (LinearLayout) view.findViewById(R.id.add_layout);
		titlebar_left = (Button) view.findViewById(R.id.title_barleft);
		titlebar_left.setOnClickListener(clickListener);
		titlebar_right = (Button) view.findViewById(R.id.title_barright);
/*		titlebar_right.setOnClickListener(clickListener);
		titlebar_right.setText("邀请");*/
		titlebar_right.setVisibility(View.GONE);
		selectFriends = new HashMap<String, UserModel>();
		titlebar_content = (TextView) view.findViewById(R.id.title_barcontent);
		titlebar_content.setText("邀请成员");
		listView = (ListView) view.findViewById(R.id.mucAddFriendList);

		Map<String, UserModel> map = IMModelManager.instance().getUserMap();
		if (map != null) {
			List<UserModel> allUsers = new ArrayList<UserModel>(map.values());
			allUsers.removeAll(chatGroupModel.getList());
			String myJid = XmppManager.getMeJid();
			if (myJid != null){
				for (UserModel userModel : allUsers) {
					if(!myJid.equals(userModel.getJid())){
						invitors.add(userModel);
					}
				}
			}
		}
		// 搜索框
		// 搜索按钮
		searchBtn = (Button) view.findViewById(R.id.app_search_btn);
		searchBtn.setOnClickListener(clickListener);
		// 搜索框交叉按钮
		app_search_close = (ImageView) view
				.findViewById(R.id.app_search_close_chat);
		app_search_close.setOnClickListener(clickListener);
		app_search_close.setVisibility(View.GONE);

		searchListView = (ListView) view.findViewById(R.id.muc_addfriend_searchList);
		app_search_edt = (EditText) view.findViewById(R.id.app_search_edt);
		app_search_edt.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@SuppressLint("NewApi")
			@Override
			public void afterTextChanged(Editable s) {
				String searchContent = s.toString().trim();
				if (searchContent.isEmpty()) {
					commonMode();
				} else {
					searchMode(searchContent);
				}
			}

		});

		searchFriendList = new ArrayList<UserModel>();
		searchFriendAdapter = new SearchFriendAdapter(getAssocActivity(),
				searchFriendList);
		searchListView.setAdapter(searchFriendAdapter);
		searchFriendAdapter.setmListener(this);
//		searchListView.setOnItemClickListener(new OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> parent, View v,
//					int position, long id) {
//				CheckBox checkBox = (CheckBox) parent.getChildAt(position).findViewById(R.id.invite_checkbox);
//				UserModel user = searchFriendList.get(position);
//				
//				invitors.remove(user);
//				invitors.add(0, user);
////				adapter.notifyDataSetChanged();
////				commonMode();
////				app_search_edt.setText("");
//				addUsrModelItem(user);
//				selectFriends.put(user.getJid(), user);
//			}
//		});
		
		
		
		adapter = new MucAddFriendAdapter(this.getAssocActivity(), invitors,
				selectFriends , new Filter() {
					@Override
					protected FilterResults performFiltering(CharSequence prefix) {
						// 持有过滤操作完成之后的数据。该数据包括过滤操作之后的数据的值以及数量。 count:数量
						// values包含过滤操作之后的数据的值

						FilterResults results = new FilterResults();
						List<UserModel> mOriginalValues = new ArrayList<UserModel>();
						// 做正式的筛选
						String prefixString = prefix.toString().toLowerCase(
								Locale.CHINESE);

							List<UserModel> models = search(prefixString);
							for(UserModel userModel : models){
								if (!mOriginalValues.contains(userModel)){
									mOriginalValues.add(userModel);
								}
							}
						// 然后将这个新的集合数据赋给FilterResults对象
						results.values = mOriginalValues;
						results.count = mOriginalValues.size();
						return results;
					}

					@SuppressWarnings("unchecked")
					@Override
					protected void publishResults(CharSequence constraint,
							FilterResults results) {
						// 重新将与适配器相关联的List重赋值一下
						searchFriendList.clear();
						searchFriendList
								.addAll((List<UserModel>) results.values);
						searchFriendList.removeAll(selectFriends.values());
						searchFriendAdapter.notifyDataSetChanged();
					};
				});
		listView.setAdapter(adapter);
		adapter.setmListener(this);
	}

	/**
	 * [搜索模式]<BR>
	 * [功能详细描述] 
	 */
	public void searchMode(String content) {
		searchListView.setVisibility(View.VISIBLE);
		app_search_close.setVisibility(View.VISIBLE);
		adapter.getFilter().filter(content);
		listView.setVisibility(View.GONE);
	}

	/**
	 * [普通模式]<BR>
	 * [功能详细描述]
	 */
	public void commonMode() {
		searchListView.setVisibility(View.GONE);
		listView.setVisibility(View.VISIBLE);
		app_search_close.setVisibility(View.GONE);
	}
	
	public List<UserModel> search(String prefix) {
		List<UserModel> filteredList = new ArrayList<UserModel>();
		for (UserModel user : invitors) {
			if ((user.getJid() != null && user.getJid().contains(prefix))
					|| (user.getName() != null && user.getName().contains(
							prefix))) {
				filteredList.add(user);
			}
		}
		return filteredList;
	}
	
	OnClickListener clickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {

			switch (v.getId()) {
			case R.id.title_barleft: {

				InputMethodManager imm = (InputMethodManager) getAssocActivity()
						.getSystemService(Context.INPUT_METHOD_SERVICE); 
			    imm.hideSoftInputFromWindow(app_search_edt.getWindowToken(), 0); 
				if (getAssocActivity() instanceof FacadeActivity) {
					FacadeActivity.class.cast(getAssocActivity()).popRight();
				} else {
					getAssocActivity().finish();
				}
				break;
			}
			
			case R.id.app_search_close_chat:
				app_search_edt.setText("");
				break;
			case R.id.add_friend_btn: {
				if (selectFriends.size() == 0){
					Toast.makeText(MucAddFirendFragment.this.getAssocActivity(),
							"请选择需要添加的成员", Toast.LENGTH_SHORT).show();
					return;
				}
				// 创建
				String roomName = "";
				PropertiesUtil propertiesUtil = PropertiesUtil.readProperties(
						MucAddFirendFragment.this.getAssocActivity(), CubeConstants.CUBE_CONFIG);
				
				final String roomJid = UUID.randomUUID().toString() + "@"
						+ propertiesUtil.getString("MucServiceName", "conference.snda-192-168-2-32");
				UserModel me = IMModelManager.instance().getMe();
				if (chatGroupModel.getRoomJid() == null) {
					View dialogView= LayoutInflater.from(getAssocActivity()).inflate(R.layout.dialog_muc_createroom,null);
					final EditText edt = (EditText) dialogView.findViewById(R.id.dialog_muc_edt);
					new AlertDialog.Builder(getAssocActivity())
					.setTitle("请为你的群组起个名字")
					.setView(dialogView)
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String roomName = edt.getText().toString();
							if(roomName.equals("")){
								return;
							}
							else {
								chatGroupModel.setGroupName(roomName);
								chatGroupModel.setGroupCode(roomJid);
								chatGroupModel.setRoomJid(roomJid);
								IMModelManager
										.instance()
										.getChatRoomContainer()
										.createChatRoom(MucAddFirendFragment.this.getAssocActivity(),chatGroupModel,
												selectFriends.values());
								progressDialog = ProgressDialog.show(getAssocActivity(), "创建群组", "请稍候", true, false);   
							}
						}	
					})
					.setNegativeButton("取消", null)
					.show();
				}
				// 更新
				else {
					progressDialog = ProgressDialog.show(getAssocActivity(), "添加成员", "请稍候", true, false);   
					UserModel[] us = selectFriends.values().toArray(
							new UserModel[selectFriends.values().size()]);
					chatGroupModel.addMembers(application, us);
				}
				InputMethodManager imm = (InputMethodManager) getAssocActivity()
						.getSystemService(Context.INPUT_METHOD_SERVICE); 
			    imm.hideSoftInputFromWindow(app_search_edt.getWindowToken(), 0); 
			}
				break;
			}
		}
	};

	@Subscribe
	public void onMucManagerAddEvent(String event) {
		if (MucBroadCastEvent.PUSH_MUC_CREATE_ROOM_SUCCESS.equals(event)){
			Toast.makeText(MucAddFirendFragment.this.getAssocActivity(), "新建群组成功", Toast.LENGTH_SHORT).show();
			UserModel  me = IMModelManager.instance().getMe();
			if(!IMModelManager.instance().containGroup(chatGroupModel.getGroupName())){
				IMModelManager.instance().addUserGroupModel(chatGroupModel);	
			}
			chatGroupModel.addStuff(me);
			for (UserModel userModel : selectFriends.values()) {
				chatGroupModel.addStuff(userModel);
			}
			progressDialog.dismiss();
			// 发送一条创建房间成功的消息
			// 加入历史记录
			String roomId = chatGroupModel.getGroupCode();
			SessionModel sessionModel = IMModelManager.instance().getSessionContainer().getSessionModel(roomId, true);
			sessionModel.setRoomName(chatGroupModel.getGroupName());
			sessionModel.setFromType(SessionModel.SESSION_ROOM);
			sessionModel.setFromWhich(XmppManager.getMeJid());
			sessionModel.setToWhich(roomId);
			sessionModel.setChatter(roomId);
			sessionModel.setSendTime(System.currentTimeMillis());
			String content = "创建群组" + chatGroupModel.getGroupName();
			sessionModel.setLastContent(content);
			StaticReference.userMf.createOrUpdate(sessionModel);
			
			ConversationMessage conversation = createConversation(
					content, XmppManager.getMeJid(), roomId, "text");
			StaticReference.userMf.createOrUpdate(conversation);
			// 跳转到历史记录界面
			EventBus.getEventBus(TmpConstants.EVENTBUS_MUC_BROADCAST)
			.post(MucBroadCastEvent.PUSH_MUC_ADDFRIEND_SHOWLIST);
			if (getAssocActivity() instanceof FacadeActivity) {
				FacadeActivity.class.cast(getAssocActivity()).popRight();
			} else {
				getAssocActivity().finish();
			}
		}
		if (MucBroadCastEvent.PUSH_MUC_CREATE_ROOM_FAIL.equals(event)){
			progressDialog.dismiss();
			Toast.makeText(MucAddFirendFragment.this.getAssocActivity(), "新建群组失败", Toast.LENGTH_SHORT).show();
		}
		if (MucBroadCastEvent.PUSH_MUC_ADDFRIEND_SUCCESS.equals(event)){
			
			UserModel[] us = selectFriends.values().toArray(
					new UserModel[selectFriends.values().size()]);
			HashMap<String , Object> hashMap = new HashMap<String, Object>();
			hashMap.put(MucBroadCastEvent.PUSH_MUC_ADDFRIEND, us);
			//发消息刷新界面
			EventBus.getEventBus(TmpConstants.EVENTBUS_MUC_BROADCAST).post(
					hashMap);
			Toast.makeText(MucAddFirendFragment.this.getAssocActivity(), "新建成员成功", Toast.LENGTH_SHORT).show();
			progressDialog.dismiss();
			if (getAssocActivity() instanceof FacadeActivity) {
				FacadeActivity.class.cast(getAssocActivity()).popRight();
			} else {
				getAssocActivity().finish();
			}
		}
		if (MucBroadCastEvent.PUSH_MUC_ADDFRIEND_FAIL.equals(event)){
			progressDialog.dismiss();
			Toast.makeText(MucAddFirendFragment.this.getAssocActivity(), "新建成员失败", Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		EventBus.getEventBus(TmpConstants.EVENTBUS_MUC_BROADCAST,
				ThreadEnforcer.MAIN).unregister(this);
	}
	
	
	/**
	 * 构建一个会话对象
	 **/
	private ConversationMessage createConversation(String content,
			String fromWho, String toWho, String type) {
		ConversationMessage conversation = new ConversationMessage();
		conversation.setContent(content);
		conversation.setFromWho(fromWho);
		conversation.setToWho(toWho);
		conversation.setUser(fromWho);
		conversation.setChater(toWho);
		conversation.setLocalTime(System.currentTimeMillis());
		conversation.setType(type);
		return conversation;
	}
	
	private void addUsrModelItem(final UserModel userModel ,boolean addmore) {
		
		if (selectFriends.containsValue(userModel)){
			return;
		}
		RelativeLayout layout = (RelativeLayout) LayoutInflater.from(getAssocActivity())
				.inflate(R.layout.new_friend_item, null);
		int id = getHeadIcon(userModel);
		ImageView new_friend_icon = (ImageView) layout.findViewById(R.id.new_friend_icon);
		new_friend_icon.setImageResource(id);
		mContentLayout.addView(layout, 0);
		hashMap.put(userModel, layout);
		selectFriends.put(userModel.getJid(), userModel);
		if (addmore){
			invitors.remove(userModel);
			invitors.add(0, userModel);
		}
		adapter.notifyDataSetChanged();
		if (selectFriends.size() > 0 ){
//			add_friend_btn.setBackgroundResource(R.drawable.mm_title_act_btn_normal);
//			add_friend_btn.setClickable(true);
			add_friend_btn.setText("确定(" + selectFriends.size() + ")");
		} else {
//			add_friend_btn.setBackgroundResource(R.drawable.mm_title_act_btn_disable);
//			add_friend_btn.setClickable(false);
			add_friend_btn.setText("确定(0)");
		}
	}
	
	public int getHeadIcon(UserModel userModel) {
		String sex = userModel.getSex();
		String status = userModel.getStatus();
		if (sex == null || status == null){
			return -1;
		}
		if ("female".equals(sex)) {
			if (UserStatus.USER_STATE_AWAY.equals(status)) {
				return R.drawable.chatroom_female_online;
			} else if (UserStatus.USER_STATE_BUSY.equals(status)) {
				return R.drawable.chatroom_female_online;
			} else if (UserStatus.USER_STATE_OFFLINE.equals(status)) {
				return R.drawable.chatroom_female_outline;
			} else if (UserStatus.USER_STATE_ONLINE.equals(status)) {
				return R.drawable.chatroom_female_online;
			}
		} else if ("male".equals(sex)) {
			if (UserStatus.USER_STATE_AWAY.equals(status)) {
				return R.drawable.chatroom_male_online;
			} else if (UserStatus.USER_STATE_BUSY.equals(status)) {
				return R.drawable.chatroom_male_online;
			} else if (UserStatus.USER_STATE_OFFLINE.equals(status)) {
				return R.drawable.chatroom_male_outline;
			} else if (UserStatus.USER_STATE_ONLINE.equals(status)) {
				return R.drawable.chatroom_male_online;
			}
		} else {
			if (UserStatus.USER_STATE_AWAY.equals(status)) {
				return R.drawable.chatroom_unknow_online;
			} else if (UserStatus.USER_STATE_BUSY.equals(status)) {
				return R.drawable.chatroom_unknow_online;
			} else if (UserStatus.USER_STATE_OFFLINE.equals(status)) {
				return R.drawable.chatroom_unknow_outline;
			} else if (UserStatus.USER_STATE_ONLINE.equals(status)) {
				return R.drawable.chatroom_unknow_online;
			}
		}
		return -1;
	}


	@Override
	public void onAddChoisedEvent(UserModel model , boolean addmore) {
		// TODO Auto-generated method stub
		addUsrModelItem(model , addmore);
	}

	@Override
	public void onRemoveChoisedEvent(UserModel model) {
		// TODO Auto-generated method stub
		selectFriends.remove(model.getJid());
		RelativeLayout layout = hashMap.get(model);
		mContentLayout.removeView(layout);
		adapter.notifyDataSetChanged();
		if (selectFriends.size() > 0 ){
//			add_friend_btn.setBackgroundResource(R.drawable.mm_title_act_btn_normal);
//			add_friend_btn.setClickable(true);
			add_friend_btn.setText("确定(" + selectFriends.size() + ")");
		} else {
//			add_friend_btn.setBackgroundResource(R.drawable.mm_title_act_btn_disable);
			add_friend_btn.setText("确定(0)");
//			add_friend_btn.setClickable(false);
		}
	}
	
}
