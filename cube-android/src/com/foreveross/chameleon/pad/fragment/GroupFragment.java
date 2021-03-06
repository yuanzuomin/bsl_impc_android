package com.foreveross.chameleon.pad.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.os.Build;
import org.jivesoftware.smack.XMPPConnection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.csair.impc.R;
import com.foreveross.chameleon.Application;
import com.foreveross.chameleon.CubeConstants;
import com.foreveross.chameleon.TmpConstants;
import com.foreveross.chameleon.activity.FacadeActivity;
import com.foreveross.chameleon.event.ConnectStatusChangeEvent;
import com.foreveross.chameleon.event.ConversationChangedEvent;
import com.foreveross.chameleon.event.EventBus;
import com.foreveross.chameleon.event.ModelChangeEvent;
import com.foreveross.chameleon.phone.activity.ChatRoomActivity;
import com.foreveross.chameleon.phone.activity.PushSettingActivity;
import com.foreveross.chameleon.phone.chat.chatroom.MucRoomAdapter;
import com.foreveross.chameleon.phone.chat.collect.CollectedAdapter;
import com.foreveross.chameleon.phone.chat.group.GroupAdapter;
import com.foreveross.chameleon.phone.chat.recently.HistoryAdapter;
import com.foreveross.chameleon.phone.chat.search.SearchFriendAdapter;
import com.foreveross.chameleon.phone.chat.search.SearchRecentlyAdapter;
import com.foreveross.chameleon.phone.modules.CubeModule;
import com.foreveross.chameleon.phone.modules.CubeModuleManager;
import com.foreveross.chameleon.phone.muc.MucAddFirendActivity;
import com.foreveross.chameleon.phone.muc.MucBroadCastEvent;
import com.foreveross.chameleon.push.client.NotificationService;
import com.foreveross.chameleon.push.mina.library.util.PropertiesUtil;
import com.foreveross.chameleon.store.model.ChatGroupModel;
import com.foreveross.chameleon.store.model.ChatRoomContainer;
import com.foreveross.chameleon.store.model.ContainerContentChangeListener;
import com.foreveross.chameleon.store.model.FavorContainer;
import com.foreveross.chameleon.store.model.FriendContainer;
import com.foreveross.chameleon.store.model.FriendGroupModel;
import com.foreveross.chameleon.store.model.IMModelManager;
import com.foreveross.chameleon.store.model.SessionContainer;
import com.foreveross.chameleon.store.model.SessionModel;
import com.foreveross.chameleon.store.model.UserModel;
import com.foreveross.chameleon.util.PadUtils;
import com.squareup.otto.Subscribe;
import com.squareup.otto.ThreadEnforcer;

/**
 * [功能详细描述] 即时通信好友列表Activity迁移至fragment
 * 
 * @author Amberlo
 * @version [CubeAndroid , 2013-6-13]
 */
public class GroupFragment extends Fragment {
	private ExpandableListView group_exlistview;
	private ListView recently_listview;
	private ListView collect_listview;
	private ListView room_listview;
	private GroupAdapter groupAdapter;
	private HistoryAdapter recentlyAdapter;
	private CollectedAdapter collectAdapter;
	private MucRoomAdapter roomAdapter;
	private Button titlebar_left;
	private Button titlebar_right;
	private TextView titlebar_content;
	private FriendContainer friendContainer = null;
	private FavorContainer favorContainer = null;
	private SessionContainer sessionContainer = null;
	private ChatRoomContainer chatRoomContainer = null;

	// 搜索的内容
	private EditText app_search_edt;
	private ImageView app_search_close;
	private ListView searchListView;
	private ListView recently_searchListView;
	private SearchFriendAdapter searchFriendAdapter;
	private SearchRecentlyAdapter searchRecentlyAdapter;

	private List<UserModel> searchFriendList;
	private List<SessionModel> searchSessionList;
	private Button searchBtn;
	private RelativeLayout flowview;
	// private ProgressDialog dialog;
	private Application application;
	// 选择历史记录，分组好友，收藏好友，群组
	private LinearLayout linearLayoutTab;

	private LinearLayout friend_recently;
	private TextView friend_recently_text; 
	private ImageView countView;
	private TextView text;
	private ImageView friend_recently_line;
	private LinearLayout friend_all;
	private TextView friend_all_text; 
	private ImageView friend_all_line;
	private LinearLayout friend_collect;
	private TextView friend_collect_text; 
	private ImageView friend_collect_line;
	private int currentTab;
	
	private RelativeLayout searchbar;
	
	private PropertiesUtil propertiesUtil = null;
	
	public boolean showCollectDelete;
	
	public boolean showHistoryDelete;

//	private List<MucRoomModel> rooms = null;

//	private MucManager mucManager;

	/**
	 * [一句话功能简述]<BR>
	 * [功能详细描述]
	 * 
	 * @param savedInstanceState
	 *            2013-7-19 下午3:01:00
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		application = Application.class.cast(getAssocActivity()
				.getApplicationContext());
		friendContainer = IMModelManager.instance().getFriendContainer();
		favorContainer = IMModelManager.instance().getFavorContainer();
		sessionContainer = IMModelManager.instance().getSessionContainer();
		chatRoomContainer = IMModelManager.instance().getChatRoomContainer();
		propertiesUtil = PropertiesUtil.readProperties(
				GroupFragment.this.getAssocActivity(), CubeConstants.CUBE_CONFIG);
//		mucManager = MucManager.getInstanse(getAssocActivity());
//		initRoomContentData();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventBus.getEventBus(TmpConstants.EVENTBUS_COMMON, ThreadEnforcer.MAIN)
				.register(this);
		EventBus.getEventBus(TmpConstants.EVENTBUS_CHAT, ThreadEnforcer.MAIN)
				.register(this);
		EventBus.getEventBus(TmpConstants.EVENTBUS_MESSAGE_CONTENT,
				ThreadEnforcer.MAIN).register(this);
		EventBus.getEventBus(TmpConstants.EVENTBUS_PUSH, ThreadEnforcer.MAIN)
				.register(this);
		EventBus.getEventBus(TmpConstants.EVENTBUS_MUC_BROADCAST,
				ThreadEnforcer.MAIN).register(this);
		return inflater.inflate(R.layout.chat_group, container,false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initValues(view);
	}

	@Subscribe
	public void onConversationsChanged(
			ConversationChangedEvent conversationChangedEvent) {
		application.getUIHandler().post(new Runnable() {
			@Override
			public void run() {
				if (groupAdapter != null){
					groupAdapter.notifyDataSetChanged();
				}
				if (recentlyAdapter != null){
					recentlyAdapter.notifyDataSetChanged();
				}
				if (collectAdapter != null){
					collectAdapter.notifyDataSetChanged();
				}
				if (roomAdapter != null){
					roomAdapter.notifyDataSetChanged();
				}
			}
		});
	}

	public void initValues(View view) {
		initSearchValues(view);
		initCommonValues(view);
	}

	public void initCommonValues(View view) {
		showCollectDelete = false;
		showHistoryDelete = false;
		// dialog = ProgressDialog(getAssocActivity(), "", "数据查询中");
		// dialog.setCancelable(true);
		titlebar_left = (Button) view.findViewById(R.id.title_barleft);
		titlebar_left.setOnClickListener(clickListener);
		titlebar_right = (Button) view.findViewById(R.id.title_barright);
		titlebar_right.setOnClickListener(clickListener);
		titlebar_right.setVisibility(View.GONE);
		titlebar_content = (TextView) view.findViewById(R.id.title_barcontent);
		titlebar_content.setText("即时通讯");
		flowview = (RelativeLayout) view.findViewById(R.id.chat_group_flowview);
		flowview.setOnClickListener(clickListener);
		flowview.setVisibility(View.GONE);
		NotificationService notificationService = Application.class.cast(
				getAssocActivity().getApplication()).getNotificationService();
		if (notificationService != null && notificationService.isOnline(application.getChatManager())) {
			flowview.setVisibility(View.GONE);
		} else {
			flowview.setVisibility(View.VISIBLE);
		}
		
		searchbar = (RelativeLayout) view.findViewById(R.id.searchbar);
		linearLayoutTab = (LinearLayout) view.findViewById(R.id.choice_group);
		
		friend_recently = (LinearLayout) view.findViewById(R.id.friend_recently);
		friend_recently.setOnClickListener(clickListener);
		friend_recently_text = (TextView) view.findViewById(R.id.friend_recently_text);
		countView = (ImageView) view.findViewById(R.id.count_img);
		text = (TextView) view.findViewById(R.id.count_text);
		CubeModule module = CubeModuleManager.getInstance()
				.getCubeModuleByIdentifier(TmpConstants.CHAT_RECORD_IDENTIFIER);
		drawCountWithImg(module.getMsgCount());
		friend_recently_line = (ImageView) view.findViewById(R.id.friend_recently_line);
		
		friend_all = (LinearLayout) view.findViewById(R.id.friend_all);
		friend_all.setOnClickListener(clickListener);
		friend_all_text = (TextView) view.findViewById(R.id.friend_all_text);
		friend_all_line = (ImageView) view.findViewById(R.id.friend_all_line);
		
		friend_collect = (LinearLayout) view.findViewById(R.id.friend_collect);
		friend_collect.setOnClickListener(clickListener);
		friend_collect_text = (TextView) view.findViewById(R.id.friend_collect_text);
		friend_collect_line = (ImageView) view.findViewById(R.id.friend_collect_line);
		
		recently_listview = (ListView) view
				.findViewById(R.id.chat_recently_listview);
		recently_listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long id) {
				
				NotificationService notificationService = Application.class.cast(
						getAssocActivity().getApplication()).getNotificationService();
				if (notificationService != null && !notificationService.isOnline(application.getChatManager())) {
					flowview.setVisibility(View.VISIBLE);
				}
				List<SessionModel> modelList = sessionContainer.getList();
				SessionModel model = modelList.get(position);
				if (model != null && 
						SessionModel.SESSION_ROOM.equals(model.getFromType())){
					String jid = model.getChatter();
					ChatGroupModel chatModel = chatRoomContainer.getStuff(jid);
					CubeModule module = CubeModuleManager.getInstance()
							.getCubeModuleByIdentifier(TmpConstants.CHAT_RECORD_IDENTIFIER);
					if (module != null && chatModel != null) {
						module.decreaseMsgCountBy(chatModel.getUnreadMessageCount());
						drawCountWithImg(module.getMsgCount());
					}
					if (chatModel != null){
						chatModel.clearNewMessageCount();
					}
					String chatroom =  propertiesUtil.getString("chatroom", "");
					if (jid != null && !"".equals(jid)) {
						if (PadUtils.isPad(getAssocActivity())) {
							Intent intent = new Intent();
							intent.putExtra("chat", "room");
							intent.putExtra("jid", jid);
							intent.putExtra("direction", 2);
							intent.putExtra("type", "fragment");
							intent.putExtra("value", chatroom);
							intent.setClass(getAssocActivity(),
									FacadeActivity.class);
							getAssocActivity().startActivity(intent);
						} else {
							Intent intent = new Intent(getAssocActivity(),
									ChatRoomActivity.class);
							intent.putExtra("chat", "room");
							intent.putExtra("jid", jid);
							getAssocActivity().startActivity(intent);
						}
					}
				} else if (model != null && 
						SessionModel.SESSION_SINGLE.equals(model.getFromType())){
					UserModel user = IMModelManager.instance().getUserModel(
							model.getChatter());
					CubeModule module = CubeModuleManager.getInstance()
							.getCubeModuleByIdentifier(TmpConstants.CHAT_RECORD_IDENTIFIER);
					if (module != null && user != null) {
						module.decreaseMsgCountBy(user.getUnreadMessageCount());
						drawCountWithImg(module.getMsgCount());
					}
					String chatroom = propertiesUtil.getString("chatroom", "");
					if (user != null){
						user.clearNewMessageCount();
						if (PadUtils.isPad(getAssocActivity())) {
							Intent intent = new Intent();
							intent.putExtra("jid", user.getJid());
							intent.putExtra("direction", 2);
							intent.putExtra("type", "fragment");
							intent.putExtra("value", chatroom);
							intent.setClass(getAssocActivity(),
									FacadeActivity.class);
							getAssocActivity().startActivity(intent);
						} else {
							Intent intent = new Intent(getAssocActivity(),
									ChatRoomActivity.class);
							intent.putExtra("jid", user.getJid());
							getAssocActivity().startActivity(intent);
						}
					}

				}
			}
		});

		recentlyAdapter = new HistoryAdapter(getAssocActivity(),showHistoryDelete ,
				sessionContainer.getList(), new Filter() {

					@Override
					protected FilterResults performFiltering(CharSequence prefix) {
						// 持有过滤操作完成之后的数据。该数据包括过滤操作之后的数据的值以及数量。 count:数量
						// values包含过滤操作之后的数据的值

						FilterResults results = new FilterResults();
						List<SessionModel> mOriginalValues = new ArrayList<SessionModel>();
						// 做正式的筛选
						String prefixString = prefix.toString().toLowerCase(
								Locale.CHINESE);
						mOriginalValues.addAll(sessionContainer
								.search(prefixString));
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
						searchSessionList.clear();
						searchSessionList
								.addAll((List<SessionModel>) results.values);
						searchRecentlyAdapter.notifyDataSetChanged();
					};
				});

		recently_listview.setAdapter(recentlyAdapter);

		group_exlistview = (ExpandableListView) view
				.findViewById(R.id.chat_group_expand);
		group_exlistview.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				UserModel user = friendContainer.getObject(groupPosition)
						.getObject(childPosition);
				CubeModule module = CubeModuleManager.getInstance()
						.getCubeModuleByIdentifier(TmpConstants.CHAT_RECORD_IDENTIFIER);
				if (module != null && user != null) {
					module.decreaseMsgCountBy(user.getUnreadMessageCount());
					drawCountWithImg(module.getMsgCount());
				}
				
				String chatroom = propertiesUtil.getString("chatroom", "");
				if (user != null){
					user.clearNewMessageCount();
					if (PadUtils.isPad(getAssocActivity())) {
						Intent intent = new Intent();
						intent.putExtra("jid", user.getJid());
						intent.putExtra("direction", 2);
						intent.putExtra("type", "fragment");
						intent.putExtra("value", chatroom);
						intent.setClass(getAssocActivity(), FacadeActivity.class);
						getAssocActivity().startActivity(intent);
					} else {
						Intent intent = new Intent(getAssocActivity(),
								ChatRoomActivity.class);
						intent.putExtra("jid", user.getJid());
						getAssocActivity().startActivity(intent);
					}
				}
				return true;
			}

		});
		groupAdapter = new GroupAdapter(getAssocActivity(),
				friendContainer.getList(), new Filter() {

					@Override
					protected FilterResults performFiltering(CharSequence prefix) {
						// 持有过滤操作完成之后的数据。该数据包括过滤操作之后的数据的值以及数量。 count:数量
						// values包含过滤操作之后的数据的值

						FilterResults results = new FilterResults();
						List<UserModel> mOriginalValues = new ArrayList<UserModel>();
						// 做正式的筛选
						String prefixString = prefix.toString().toLowerCase(
								Locale.CHINESE);

						List<FriendGroupModel> groupData = friendContainer
								.getList();
						for (FriendGroupModel group : groupData) {
							// 如果姓名的前缀相符或者电话相符就添加到新的集合
							List<UserModel> models = group.search(prefixString);
							for(UserModel userModel : models){
								if (!mOriginalValues.contains(userModel)){
									mOriginalValues.add(userModel);
								}
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
						searchFriendAdapter.notifyDataSetChanged();
					};
				});

		group_exlistview.setAdapter(groupAdapter);

		friendContainer
				.setContainerContentChangeListener(new ContainerContentChangeListener() {

					@Override
					public void onContentChange() {
						
						application.getUIHandler().post(new Runnable() {

							@Override
							public void run() {
								groupAdapter.notifyDataSetChanged();
							}
							
						});
					}
				});

		collect_listview = (ListView) view
				.findViewById(R.id.chat_collect_listview);
		collect_listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long id) {
				NotificationService notificationService = Application.class.cast(
						getAssocActivity().getApplication()).getNotificationService();
				if (notificationService != null && !notificationService.isOnline(application.getChatManager())) {
					flowview.setVisibility(View.VISIBLE);
				}
				List<UserModel> ulist = favorContainer.getList();
				UserModel user = ulist.get(position);
				CubeModule module = CubeModuleManager.getInstance()
						.getCubeModuleByIdentifier(TmpConstants.CHAT_RECORD_IDENTIFIER);
				if (module != null && user != null) {
					module.decreaseMsgCountBy(user.getUnreadMessageCount());
					drawCountWithImg(module.getMsgCount());
				}
				String chatroom = propertiesUtil.getString("chatroom", "");
				if (user != null){
					user.clearNewMessageCount();
					if (PadUtils.isPad(getAssocActivity())) {
						Intent intent = new Intent();
						intent.putExtra("jid", user.getJid());
						intent.putExtra("direction", 2);
						intent.putExtra("type", "fragment");
						intent.putExtra("value", chatroom);
						intent.setClass(getAssocActivity(),
								FacadeActivity.class);
						getAssocActivity().startActivity(intent);
					} else {
						Intent intent = new Intent(getAssocActivity(),
								ChatRoomActivity.class);
						intent.putExtra("jid", user.getJid());
						getAssocActivity().startActivity(intent);
					}
				}
			}
		});

		collectAdapter = new CollectedAdapter(getAssocActivity(), showCollectDelete , 
				favorContainer.getList(), new Filter() {

					@Override
					protected FilterResults performFiltering(CharSequence prefix) {
						// 持有过滤操作完成之后的数据。该数据包括过滤操作之后的数据的值以及数量。 count:数量
						// values包含过滤操作之后的数据的值

						FilterResults results = new FilterResults();
						List<UserModel> mOriginalValues = new ArrayList<UserModel>();
						// 做正式的筛选
						String prefixString = prefix.toString().toLowerCase(
								Locale.CHINESE);
						mOriginalValues.addAll(favorContainer
								.search(prefixString));
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
						searchFriendAdapter.notifyDataSetChanged();
					};
				});

		collect_listview.setAdapter(collectAdapter);

		favorContainer
				.setContainerContentChangeListener(new ContainerContentChangeListener() {

					@Override
					public void onContentChange() {
						application.getUIHandler().post(new Runnable() {

							@Override
							public void run() {
								collectAdapter.notifyDataSetChanged();
							}
						});
					}
				});

		room_listview = (ListView) view
				.findViewById(R.id.chat_room_listview);
//		rooms = mucManager.getMucRoomList();
		roomAdapter = new MucRoomAdapter(getAssocActivity(), chatRoomContainer.getList(),
				new Filter() {

					@Override
					protected FilterResults performFiltering(CharSequence prefix) {
						// 持有过滤操作完成之后的数据。该数据包括过滤操作之后的数据的值以及数量。 count:数量
						// values包含过滤操作之后的数据的值

						FilterResults results = new FilterResults();
						List<UserModel> mOriginalValues = new ArrayList<UserModel>();
						// 做正式的筛选
						String prefixString = prefix.toString().toLowerCase(
								Locale.CHINESE);

						List<FriendGroupModel> groupData = friendContainer
								.getList();
						for (FriendGroupModel group : groupData) {
							// 如果姓名的前缀相符或者电话相符就添加到新的集合
							mOriginalValues.addAll(group.search(prefixString));
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
						searchFriendAdapter.notifyDataSetChanged();
					};
				});

		room_listview.setAdapter(roomAdapter);
		
		room_listview.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long id) {
				NotificationService notificationService = Application.class.cast(
						getAssocActivity().getApplication()).getNotificationService();
				if (notificationService != null && !notificationService.isOnline(application.getChatManager())) {
					flowview.setVisibility(View.VISIBLE);
				}
				List<ChatGroupModel> ulist = chatRoomContainer.getList();
				ChatGroupModel chatModel = ulist.get(position);
				CubeModule module = CubeModuleManager.getInstance()
						.getCubeModuleByIdentifier(TmpConstants.CHAT_RECORD_IDENTIFIER);
				if (module != null && chatModel != null) {
					module.decreaseMsgCountBy(chatModel.getUnreadMessageCount());
					drawCountWithImg(module.getMsgCount());
				}
				String chatroom = propertiesUtil.getString("chatroom", "");
				if (chatModel != null){
					chatModel.clearNewMessageCount();
					if (PadUtils.isPad(getAssocActivity())) {
						Intent intent = new Intent();
						intent.putExtra("chat", "room");
						intent.putExtra("jid", chatModel.getRoomJid());
						intent.putExtra("direction", 2);
						intent.putExtra("type", "fragment");
						intent.putExtra("value", chatroom);
						intent.setClass(getAssocActivity(),
								FacadeActivity.class);
						getAssocActivity().startActivity(intent);
					} else {
						Intent intent = new Intent(getAssocActivity(),
								ChatRoomActivity.class);
						intent.putExtra("chat", "room");
						intent.putExtra("jid", chatModel.getRoomJid());
						getAssocActivity().startActivity(intent);
					}
				}
			}
		});
		
		chatRoomContainer
		.setContainerContentChangeListener(new ContainerContentChangeListener() {

			@Override
			public void onContentChange() {
				application.getUIHandler().post(new Runnable() {
					
					@Override
					public void run() {
						roomAdapter.notifyDataSetChanged();
					}
				});
			}
		});
		
		// 显示历史会话
		currentTab = 1;
		titlebar_content.setText("即时通讯");
		recently_listview.setVisibility(View.VISIBLE);
		group_exlistview.setVisibility(View.GONE);
		collect_listview.setVisibility(View.GONE);
		room_listview.setVisibility(View.GONE);
		titlebar_right.setVisibility(View.VISIBLE);
		titlebar_right.setText("编辑");
	}

	public void initSearchValues(View view) {
		// 搜索框
		// 搜索按钮
		searchBtn = (Button) view.findViewById(R.id.app_search_btn);
		searchBtn.setOnClickListener(clickListener);
		// 搜索框交叉按钮
		app_search_close = (ImageView) view
				.findViewById(R.id.app_search_close_chat);
		app_search_close.setOnClickListener(clickListener);
		app_search_close.setVisibility(View.GONE);

		searchListView = (ListView) view.findViewById(R.id.searchList);
		recently_searchListView = (ListView) view
				.findViewById(R.id.recently_searchList);
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
		searchListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				commonMode();
				app_search_edt.setText("");
				NotificationService notificationService = Application.class.cast(
						getAssocActivity().getApplication()).getNotificationService();
				if (notificationService != null && !notificationService.isOnline(application.getChatManager())) {
					flowview.setVisibility(View.VISIBLE);
				}
				UserModel user = searchFriendList.get(position);
				CubeModule module = CubeModuleManager.getInstance()
						.getCubeModuleByIdentifier(TmpConstants.CHAT_RECORD_IDENTIFIER);
				if (module != null) {
					module.decreaseMsgCountBy(user.getUnreadMessageCount());
					drawCountWithImg(module.getMsgCount());
				}
				user.clearNewMessageCount();
				// TODO[TEST]多组会产生bug;

				String chatroom = propertiesUtil.getString("chatroom", "");
				if (PadUtils.isPad(getAssocActivity())) {
					Intent intent = new Intent();
					intent.putExtra("jid", user.getJid());
					intent.putExtra("direction", 2);
					intent.putExtra("type", "fragment");
					intent.putExtra("value", chatroom);
					intent.setClass(getAssocActivity(), FacadeActivity.class);
					getAssocActivity().startActivity(intent);
				} else {
					Intent intent = new Intent(getAssocActivity(),
							ChatRoomActivity.class);
					intent.putExtra("jid", user.getJid());
					getAssocActivity().startActivity(intent);
				}

			}

		});

		searchSessionList = new ArrayList<SessionModel>();
		searchRecentlyAdapter = new SearchRecentlyAdapter(getAssocActivity(),
				searchSessionList);
		recently_searchListView.setAdapter(searchRecentlyAdapter);
		recently_searchListView
				.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View view,
							int position, long id) {
						commonMode();
						app_search_edt.setText("");
						NotificationService notificationService = Application.class.cast(
								getAssocActivity().getApplication()).getNotificationService();
						if (notificationService != null && !notificationService.isOnline(application.getChatManager())) {
							flowview.setVisibility(View.VISIBLE);
						}
						SessionModel model = searchSessionList.get(position);
						if (model != null && 
								SessionModel.SESSION_ROOM.equals(model.getFromType())){
							String jid = model.getChatter();
							ChatGroupModel chatModel = chatRoomContainer.getStuff(jid);
							CubeModule module = CubeModuleManager.getInstance()
									.getCubeModuleByIdentifier(TmpConstants.CHAT_RECORD_IDENTIFIER);
							if (module != null && chatModel != null) {
								module.decreaseMsgCountBy(chatModel.getUnreadMessageCount());
								drawCountWithImg(module.getMsgCount());
							}
							if (chatModel != null){
								chatModel.clearNewMessageCount();
							}
							String chatroom =  propertiesUtil.getString("chatroom", "");
							if (jid != null && !"".equals(jid)) {
								if (PadUtils.isPad(getAssocActivity())) {
									Intent intent = new Intent();
									intent.putExtra("chat", "room");
									intent.putExtra("jid", jid);
									intent.putExtra("direction", 2);
									intent.putExtra("type", "fragment");
									intent.putExtra("value", chatroom);
									intent.setClass(getAssocActivity(),
											FacadeActivity.class);
									getAssocActivity().startActivity(intent);
								} else {
									Intent intent = new Intent(getAssocActivity(),
											ChatRoomActivity.class);
									intent.putExtra("chat", "room");
									intent.putExtra("jid", jid);
									getAssocActivity().startActivity(intent);
								}
							}
						} else if (model != null && 
								SessionModel.SESSION_SINGLE.equals(model.getFromType())){
							UserModel user = IMModelManager.instance().getUserModel(
									model.getChatter());
							CubeModule module = CubeModuleManager.getInstance()
									.getCubeModuleByIdentifier(TmpConstants.CHAT_RECORD_IDENTIFIER);
							if (module != null && user != null) {
								module.decreaseMsgCountBy(user.getUnreadMessageCount());
								drawCountWithImg(module.getMsgCount());
							}
							String chatroom = propertiesUtil.getString("chatroom", "");
							if (user != null){
								user.clearNewMessageCount();
								if (PadUtils.isPad(getAssocActivity())) {
									Intent intent = new Intent();
									intent.putExtra("jid", user.getJid());
									intent.putExtra("direction", 2);
									intent.putExtra("type", "fragment");
									intent.putExtra("value", chatroom);
									intent.setClass(getAssocActivity(),
											FacadeActivity.class);
									getAssocActivity().startActivity(intent);
								} else {
									Intent intent = new Intent(getAssocActivity(),
											ChatRoomActivity.class);
									intent.putExtra("jid", user.getJid());
									getAssocActivity().startActivity(intent);
								}
							}

						}
					}
				});

	}

	/**
	 * [搜索模式]<BR>
	 * [功能详细描述] 2013-8-28 上午9:56:36
	 */
	public void searchMode(String content) {
		if (currentTab == 1) {
			recently_searchListView.setVisibility(View.VISIBLE);
			searchListView.setVisibility(View.GONE);
		} else {
			recently_searchListView.setVisibility(View.GONE);
			searchListView.setVisibility(View.VISIBLE);
		}

		app_search_close.setVisibility(View.VISIBLE);
		switch (currentTab) {
		case 1:
			recently_listview.setVisibility(View.GONE);
			recentlyAdapter.getFilter().filter(content);
			break;
		case 2:
			group_exlistview.setVisibility(View.GONE);
			groupAdapter.getFilter().filter(content);
			break;
		case 3:
			collect_listview.setVisibility(View.GONE);
			collectAdapter.getFilter().filter(content);
			break;
		default:
			break;
		}
		// room_exlistview.setVisibility(View.GONE);
		// roomAdapter.getFilter().filter(content);
	}

	/**
	 * [普通模式]<BR>
	 * [功能详细描述] 2013-8-28 上午9:56:21
	 */
	public void commonMode() {
		searchListView.setVisibility(View.GONE);
		recently_searchListView.setVisibility(View.GONE);
		app_search_close.setVisibility(View.GONE);
		switch (currentTab) {
		case 1:
			recently_listview.setVisibility(View.VISIBLE);
			break;
		case 2:
			group_exlistview.setVisibility(View.VISIBLE);
			break;
		case 3:
			collect_listview.setVisibility(View.VISIBLE);
			break;
		default:
			break;
		}
		// room_exlistview.setVisibility(View.VISIBLE);
	}

	OnClickListener clickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {

			switch (v.getId()) {
			case R.id.title_barleft:
				if (getAssocActivity() instanceof FacadeActivity) {
					((FacadeActivity) getAssocActivity()).popRight();
				} else {
					getAssocActivity().finish();
				}

				break;
			case R.id.app_search_close_chat:
				app_search_edt.setText("");
				break;
			case R.id.chat_group_flowview:
				Intent i = new Intent();
				i.setClass(getAssocActivity(), PushSettingActivity.class);
				getAssocActivity().startActivity(i);
				break;
			case R.id.title_barright:
				if (currentTab == 2){
					XMPPConnection conn = application.getChatManager().getConnection();
					if (conn != null && conn.isConnected()) {
						if (PadUtils.isPad(getAssocActivity())) {
							String addfirend = propertiesUtil.getString("addfirend", "");
							Intent intent = new Intent();
							intent.putExtra("direction", 2);
							intent.putExtra("type", "fragment");
							intent.putExtra("inviteType", "create");
							intent.putExtra("value", addfirend);
							intent.setClass(getAssocActivity(), FacadeActivity.class);
							getAssocActivity().startActivity(intent);
						} else {
							Intent intent = new Intent();
							intent.putExtra("inviteType", "create");
							intent.setClass(getAssocActivity(), MucAddFirendActivity.class);
							getAssocActivity().startActivity(intent);
						}
					}
				}
				if (currentTab == 3){
					if (showCollectDelete){
						collectAdapter.setShowCollectDelete(false);
						showCollectDelete = false;
						titlebar_right.setText("编辑");
					} else {
						collectAdapter.setShowCollectDelete(true);
						showCollectDelete = true;
						titlebar_right.setText("取消");
					}
					collectAdapter.notifyDataSetChanged();
				}
				if (currentTab == 1){
					if (showHistoryDelete){
						recentlyAdapter.setShowHistoryDelete(false);
						showHistoryDelete = false;
						titlebar_right.setText("编辑");
					} else {
						recentlyAdapter.setShowHistoryDelete(true);
						showHistoryDelete = true;
						titlebar_right.setText("取消");
					}
					recentlyAdapter.notifyDataSetChanged();
				}
				break;
				
			case R.id.friend_recently:
				searchbar.setVisibility(View.VISIBLE);
				if (currentTab != 1){
					app_search_edt.setText("");
					closeKeyboard();
				}
				if (!app_search_edt.getText().toString().equals("") ){
					break;
				}
				friend_recently_text.setTextColor(getResources().getColor(R.color.black));
				friend_recently_line.setBackgroundResource(R.drawable.tab_line_light);
				friend_all_text.setTextColor(getResources().getColor(R.color.lightgrey));
                if(Build.VERSION.SDK_INT >= 16)
                {

                    friend_all_line.setBackground(null);
                }
                else
                {
                    friend_all_line.setBackgroundDrawable(null);
                }
				friend_collect_text.setTextColor(getResources().getColor(R.color.lightgrey));
                if(Build.VERSION.SDK_INT >= 16)
                {

                    friend_collect_line.setBackground(null);
                }
                else
                {
                    friend_collect_line.setBackgroundDrawable(null);
                }
				currentTab = 1;
				titlebar_content.setText("即时通讯");
				recently_listview.setVisibility(View.VISIBLE);
				group_exlistview.setVisibility(View.GONE);
				collect_listview.setVisibility(View.GONE);
				room_listview.setVisibility(View.GONE);
				titlebar_right.setVisibility(View.VISIBLE);
				titlebar_right.setText("编辑");
				collectAdapter.setShowCollectDelete(false);
				showCollectDelete = false;
				collectAdapter.notifyDataSetChanged();
				recentlyAdapter.setShowHistoryDelete(false);
				showHistoryDelete = false;
				recentlyAdapter.notifyDataSetChanged();
				break;
			case R.id.friend_all:
				searchbar.setVisibility(View.VISIBLE);
				if (currentTab != 2){
					app_search_edt.setText("");
					closeKeyboard();
				}
				if (!app_search_edt.getText().toString().equals("") ){
					break;
				}
				friend_recently_text.setTextColor(getResources().getColor(R.color.lightgrey));
                if(Build.VERSION.SDK_INT >= 16)
                {
                    friend_recently_line.setBackground(null);
                }
				else
                {
                    friend_recently_line.setBackgroundDrawable(null);
                }
				friend_all_text.setTextColor(getResources().getColor(R.color.black));
				friend_all_line.setBackgroundResource(R.drawable.tab_line_light);
				friend_collect_text.setTextColor(getResources().getColor(R.color.lightgrey));
                if(Build.VERSION.SDK_INT >= 16)
                {
                    friend_collect_line.setBackground(null);
                }
                else
                {
                    friend_collect_line.setBackgroundDrawable(null);
                }


				currentTab = 2;
				titlebar_content.setText("即时通讯");
				recently_listview.setVisibility(View.GONE);
				group_exlistview.setVisibility(View.VISIBLE);
				collect_listview.setVisibility(View.GONE);
				room_listview.setVisibility(View.GONE);
				titlebar_right.setVisibility(View.VISIBLE);
				titlebar_right.setText("群聊");
				collectAdapter.setShowCollectDelete(false);
				showCollectDelete = false;
				collectAdapter.notifyDataSetChanged();
				
				recentlyAdapter.setShowHistoryDelete(false);
				showHistoryDelete = false;
				recentlyAdapter.notifyDataSetChanged();
				break;
			case R.id.friend_collect:
				searchbar.setVisibility(View.GONE);
				if (currentTab != 3){
					app_search_edt.setText("");
					closeKeyboard();
				}
				if (!app_search_edt.getText().toString().equals("") ){
					break;
				}
				friend_recently_text.setTextColor(getResources().getColor(R.color.lightgrey));
                if(Build.VERSION.SDK_INT >= 16)
                {

                    friend_recently_line.setBackground(null);
                }
                else
                {
                    friend_recently_line.setBackgroundDrawable(null);
                }

				friend_all_text.setTextColor(getResources().getColor(R.color.lightgrey));
                if(Build.VERSION.SDK_INT >= 16)
                {

                    friend_all_line.setBackground(null);
                }
                else
                {
                    friend_all_line.setBackgroundDrawable(null);
                }
				friend_collect_text.setTextColor(getResources().getColor(R.color.black));
				friend_collect_line.setBackgroundResource(R.drawable.tab_line_light);

				currentTab = 3;
				titlebar_content.setText("即时通讯");
				recently_listview.setVisibility(View.GONE);
				group_exlistview.setVisibility(View.GONE);
				collect_listview.setVisibility(View.VISIBLE);
				room_listview.setVisibility(View.GONE);
				titlebar_right.setVisibility(View.VISIBLE);
				titlebar_right.setText("编辑");
				collectAdapter.setShowCollectDelete(false);
				showCollectDelete = false;
				collectAdapter.notifyDataSetChanged();
				recentlyAdapter.setShowHistoryDelete(false);
				showHistoryDelete = false;
				recentlyAdapter.notifyDataSetChanged();
				break;		
			}
		}
	};

	@Override
	public void onPause() {
		super.onPause();
		// if (dialog != null && dialog.isShowing()) {
		// dialog.cancel();
		// }
	};

	public void onDestroyView() {
		super.onDestroyView();
		EventBus.getEventBus(TmpConstants.EVENTBUS_COMMON, ThreadEnforcer.MAIN)
				.unregister(this);
		EventBus.getEventBus(TmpConstants.EVENTBUS_CHAT, ThreadEnforcer.MAIN)
				.unregister(this);

		EventBus.getEventBus(TmpConstants.EVENTBUS_MESSAGE_CONTENT,
				ThreadEnforcer.MAIN).unregister(this);
		EventBus.getEventBus(TmpConstants.EVENTBUS_PUSH, ThreadEnforcer.MAIN)
				.unregister(this);
		EventBus.getEventBus(TmpConstants.EVENTBUS_MUC_BROADCAST,
				ThreadEnforcer.MAIN).unregister(this);
	};

	/**
	 * [一句话功能简述]<BR>
	 * [功能详细描述] 2013-9-2 上午11:47:16
	 */
	@Override
	public void onStop() {
		super.onStop();
		application.setShouldSendChatNotification(false);

	}

	@Override
	public void onResume() {
		super.onResume();
		application.setShouldSendChatNotification(false);
		
		application.getUIHandler().post(new Runnable() {
			@Override
			public void run() {
				
				collectAdapter.setShowCollectDelete(false);
				showCollectDelete = false;
				recentlyAdapter.setShowHistoryDelete(false);
				showHistoryDelete = false;
				if (currentTab == 2){
					titlebar_right.setText("群聊");
				} else {
					titlebar_right.setText("编辑");
				}
				
				
				if (groupAdapter != null){
					groupAdapter.notifyDataSetChanged();
				}
				if (recentlyAdapter != null){
					recentlyAdapter.notifyDataSetChanged();
				}
				if (collectAdapter != null){
					collectAdapter.notifyDataSetChanged();
				}
				if (roomAdapter != null){
					roomAdapter.notifyDataSetChanged();
				}
				CubeModule module = CubeModuleManager.getInstance()
						.getCubeModuleByIdentifier(TmpConstants.CHAT_RECORD_IDENTIFIER);
				drawCountWithImg(module.getMsgCount());
			}
		});
	}

	@Override
	public String toString() {
		return this.getClass().getCanonicalName();
	}

	/******************************************************************************************
	 * 
	 * modified by fengweili
	 * 
	 ******************************************************************************************/
	/**
	 * [一句话功能简述]<BR>
	 * [功能详细描述] 2013-7-19 上午10:36:43
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Subscribe
	public void onModelChangeEvent(ModelChangeEvent modelChangeEvent) {
		// if (dialog != null && dialog.isShowing()) {
		// dialog.dismiss();
		// }
		getAssocActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (groupAdapter != null){
					groupAdapter.notifyDataSetChanged();
				}
				if (recentlyAdapter != null){
					recentlyAdapter.notifyDataSetChanged();
				}
				if (collectAdapter != null){
					collectAdapter.notifyDataSetChanged();
				}
				if (roomAdapter != null){
					roomAdapter.notifyDataSetChanged();
				}
			}
		});
	}

	@Subscribe
	public void onConnectStatusChnageEvent(
			ConnectStatusChangeEvent connectStatusChnageEvent) {
		String status = connectStatusChnageEvent.getStatus();
		if (ConnectStatusChangeEvent.CONN_STATUS_ONLINE.equals(status)) {
			flowview.setVisibility(View.GONE);
		} else {
			flowview.setVisibility(View.VISIBLE);
		}
	}

	@Subscribe
	public void onMucManagerEvent(String mucBroadCastEvent) {
		if (MucBroadCastEvent.PUSH_MUC_MANAGER_MEMBER.equals(mucBroadCastEvent)) {
			// 刷新界面
			getAssocActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if(roomAdapter != null){
					roomAdapter.notifyDataSetChanged();
				}}
			});
		}
		if (MucBroadCastEvent.PUSH_MUC_INITROOMS.equals(mucBroadCastEvent)) {
			// 刷新界面
			getAssocActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if(roomAdapter != null){
					roomAdapter.notifyDataSetChanged();
				}}
			});
		}
		
		if (MucBroadCastEvent.PUSH_MUC_ADDFRIEND_SHOWLIST.equals(mucBroadCastEvent)) {
			friend_recently_text.setTextColor(getResources().getColor(R.color.black));
			friend_recently_line.setBackgroundResource(R.drawable.tab_line_light);
			friend_all_text.setTextColor(getResources().getColor(R.color.lightgrey));
			friend_all_line.setBackground(null);
			friend_collect_text.setTextColor(getResources().getColor(R.color.lightgrey));
			friend_collect_line.setBackground(null);
			currentTab = 1;
			titlebar_content.setText("即时通讯");
			recently_listview.setVisibility(View.VISIBLE);
			group_exlistview.setVisibility(View.GONE);
			collect_listview.setVisibility(View.GONE);
			room_listview.setVisibility(View.GONE);
			titlebar_right.setVisibility(View.VISIBLE);
			titlebar_right.setText("编辑");
		}
	}
	
	@Subscribe
	public void onMucManagerKillEvent(HashMap<String, String> map) {
		String muckill = map.get("muckill");
		String roomJid = map.get("roomJid");
		if (MucBroadCastEvent.PUSH_MUC_KICKED.equals(muckill)){
		}
	}
	
	@Subscribe
	public void onCountRefreshEvent(String countRefreshEvent) {
		if (MucBroadCastEvent.PUSH_MUC_COUNT.equals(countRefreshEvent)) {
			getAssocActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					CubeModule module = CubeModuleManager.getInstance()
							.getCubeModuleByIdentifier(TmpConstants.CHAT_RECORD_IDENTIFIER);
					drawCountWithImg(module.getMsgCount());}
			});
		}
	}
	
	public void closeKeyboard(){
		InputMethodManager imm2 = (InputMethodManager) getAssocActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm2.hideSoftInputFromWindow(app_search_edt.getWindowToken(), 0);
	}
	
	public void drawCountWithImg(int count) {
		if (count == 0) {
			countView.setVisibility(View.GONE);
			text.setVisibility(View.GONE);
		} else {
			countView.setVisibility(View.VISIBLE);
			text.setVisibility(View.VISIBLE);
			text.setText(String.valueOf(count));
			int tmpCount = 1;
			while (count >= 10) {
				count = count / 10;
				tmpCount++;
			}
			switch (tmpCount) {
			case 1:
				countView.setBackgroundResource(R.drawable.push_count_1);
				break;
			case 2:
				countView.setBackgroundResource(R.drawable.push_count_10);
				break;
			case 3:
				countView.setBackgroundResource(R.drawable.push_count_100);
				break;
			default:
				break;
			}
		}
	}
}