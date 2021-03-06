/*
 * Copyright (C) 2010 Moduad Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.foreveross.chameleon.push.client;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Intent;
import android.os.Environment;
import android.text.format.DateFormat;

import com.foreveross.chameleon.Application;
import com.foreveross.chameleon.CubeConstants;
import com.csair.impc.R;
import com.foreveross.chameleon.TmpConstants;
import com.foreveross.chameleon.URL;
import com.foreveross.chameleon.activity.FacadeActivity;
import com.foreveross.chameleon.event.ConversationChangedEvent;
import com.foreveross.chameleon.event.EventBus;
import com.foreveross.chameleon.phone.activity.ChatRoomActivity;
import com.foreveross.chameleon.phone.modules.CubeModule;
import com.foreveross.chameleon.phone.modules.CubeModuleManager;
import com.foreveross.chameleon.phone.muc.MucBroadCastEvent;
import com.foreveross.chameleon.push.mina.library.util.PropertiesUtil;
import com.foreveross.chameleon.store.core.StaticReference;
import com.foreveross.chameleon.store.model.ChatGroupModel;
import com.foreveross.chameleon.store.model.ConversationMessage;
import com.foreveross.chameleon.store.model.IMModelManager;
import com.foreveross.chameleon.store.model.SessionModel;
import com.foreveross.chameleon.store.model.UserModel;
import com.foreveross.chameleon.util.PadUtils;
import com.foreveross.chameleon.util.Preferences;
import com.foreveross.chameleon.util.TimeUnit;
import com.squareup.otto.ThreadEnforcer;

/**
 * [一句话功能简述]<BR>
 * [功能详细描述]
 * 
 * @author 冯伟立
 * @version [CubeAndroid, 2013-8-9]
 */
public class ChatMessageListener implements PacketListener {

	private final static Logger log = LoggerFactory
			.getLogger(ChatMessageListener.class);

	private final XmppManager xmppManager;
	private ExecutorService pool = Executors.newCachedThreadPool();
	private Application application;

	private PropertiesUtil propertiesUtil = null;
	
	public ChatMessageListener(final XmppManager xmppManager) {
		this.xmppManager = xmppManager;
		this.application = Application.class.cast(xmppManager
				.getNotificationService().getApplication());
		propertiesUtil = PropertiesUtil.readProperties(
				xmppManager.getNotificationService(), CubeConstants.CUBE_CONFIG);
		new Timer().scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				if (convs.isEmpty()) {
					return;
				}
				List<ConversationMessage> list = null;
				synchronized (this) {
					list = new ArrayList<ConversationMessage>(convs);
					convs.clear();
				}
				doNotify(list, doComplex(list));
			}
		}, 0, 2000);

	}

	private Map<String, AtomicInteger> doComplex(List<ConversationMessage> list) {
		Map<String, AtomicInteger> hashMap = new HashMap<String, AtomicInteger>();

		Map<String, ConversationMessage> lastConvMap = new HashMap<String, ConversationMessage>();

		for (ConversationMessage conversationMessage : list) {
			// 记录最后一条消息
			lastConvMap.put(conversationMessage.getChater(),
					conversationMessage);
			// 统计每个用户的消息条数
			AtomicInteger counter = null;
			if ((counter = hashMap.get(conversationMessage.getChater())) == null) {
				hashMap.put(conversationMessage.getChater(),
						counter = new AtomicInteger(0));
			}

			counter.incrementAndGet();

			log.info("received a voice message!");
			if (null != conversationMessage.getType()
					&& conversationMessage.getType().equals("voice")) {
				
				conversationMessage.setPicId(conversationMessage.getContent());
				conversationMessage.setContent(null);
			}else if(null != conversationMessage.getType()
					&& conversationMessage.getType().equals("image")) {
				conversationMessage.setPicId(conversationMessage.getContent());
				
				conversationMessage.setContent(null);
			}
			//如果是用户聊天
			if (IMModelManager.instance().containUserModel(
					conversationMessage.getChater())) {
				if (IMModelManager.instance()
						.getUserModel(conversationMessage.getFromWho()) != null){
					IMModelManager.instance()
					.getUserModel(conversationMessage.getFromWho())
					.addConversationMessage(conversationMessage);
				}
			} 
			//如果是用户组聊天
			else {
				ChatGroupModel chatGroupModel = IMModelManager.instance()
						.getChatRoomContainer().getStuff(conversationMessage.getChater());
				if (chatGroupModel != null){
					chatGroupModel.addConversationMessage(conversationMessage);
				}
			}
			StaticReference.userMf.createOrUpdate(conversationMessage);
		}
		// 构造SessionModel
		Set<Entry<String, ConversationMessage>> centries = lastConvMap
				.entrySet();
		for (Entry<String, ConversationMessage> centry : centries) {
			SessionModel sessionModel = IMModelManager.instance()
					.getSessionContainer()
					.getSessionModel(centry.getKey(), true);
			// TODO[fengweili]先留空
			
			ConversationMessage message = centry.getValue();
			
			if (SessionModel.SESSION_ROOM.equals(message.getFromType())){
				sessionModel.setFromType(SessionModel.SESSION_ROOM);
				ChatGroupModel chatGroupModel = IMModelManager.instance()
						.getChatRoomContainer().getStuff(centry.getKey());
				if (chatGroupModel != null){
					sessionModel.setRoomName(chatGroupModel.getGroupName());
				}
			} else {
				sessionModel.setFromType(SessionModel.SESSION_SINGLE);
			}
			
			sessionModel.setChatter(centry.getKey());
			sessionModel.setFromWhich(centry.getKey());
			if (centry.getValue().getType().equals("voice")){
				sessionModel.setLastContent("[声音]");
			} else if (centry.getValue().getType().equals("image")){
				sessionModel.setLastContent("[图片]");
			} else if (centry.getValue().getType().equals("text")){
				sessionModel.setLastContent(centry.getValue().getContent());
			}
			sessionModel.setMessageCount(hashMap.get(centry.getKey()).get());
			sessionModel.setToWhich(centry.getValue().getToWho());
			sessionModel.setStatus(IMModelManager.instance().getStatus(
					centry.getKey()));
			sessionModel.setSendTime(centry.getValue().getLocalTime());
			StaticReference.userMf.createOrUpdate(sessionModel);
		}
		return hashMap;
	}

	private void doNotify(List<ConversationMessage> list,
			Map<String, AtomicInteger> map) {

		CubeModule messageModule = CubeModuleManager.getInstance()
				.getCubeModuleByIdentifier(TmpConstants.CHAT_RECORD_IDENTIFIER);
		if (messageModule != null) {
			messageModule.increaseMsgCountBy(list.size());
		}

		Set<Map.Entry<String, AtomicInteger>> entries = map.entrySet();
		String chatJid = Preferences.getChatJid(Application.sharePref);
		int ignore = 0;
		for (Map.Entry<String, AtomicInteger> entry : entries) {
			UserModel userModel = null;
			String jid = entry.getKey();
			//如果是用户聊天
			if (IMModelManager.instance().containUserModel(jid)) {
				if ((userModel = IMModelManager.instance().getUserModel(jid)) != null) {
					if (userModel != null){
						if (application.getIsInChatRoomFragment()){
							if (!jid.equals(chatJid)){
								userModel.increaseCountBy(entry.getValue().get());
							} else {
								ignore++;
							}
						} else {
							userModel.increaseCountBy(entry.getValue().get());
						}
					}
				}
			} 
			//如果是用户组聊天
			else {
				ChatGroupModel chatGroupModel = IMModelManager.instance()
						.getChatRoomContainer().getStuff(jid);
				if (chatGroupModel != null){
					if (application.getIsInChatRoomFragment()){
						if (!jid.equals(chatJid)){
							chatGroupModel.increaseCountBy(entry.getValue().get());
						} else {
							ignore++;
						}
					} else {
						chatGroupModel.increaseCountBy(entry.getValue().get());
					}
				}
			}
		}
		
		if (messageModule != null) {
			messageModule.decreaseMsgCountBy(ignore);
		}

		if (application.shouldSendChatNotification()) {
			sendChatNotification(list.get(list.size() - 1));
		}
		// if
		// (UnkownUtil.isChatViewPresence(xmppManager.getNotificationService()))
		// {
		EventBus.getEventBus(TmpConstants.EVENTBUS_MESSAGE_CONTENT,
				ThreadEnforcer.MAIN).post(new ConversationChangedEvent());
		EventBus.getEventBus(TmpConstants.EVENTBUS_MUC_BROADCAST,
				ThreadEnforcer.MAIN).post(MucBroadCastEvent.PUSH_MUC_COUNT);
		// }

	}

	public void sendChatNotification(ConversationMessage conversationMessage) {
		String type = conversationMessage.getType();
		String ConversationContent = null;
		String ConversationTitle = null;
		if ("voice".equals(type)) {
			ConversationContent = "[声音]";
		} else if ("voice".equals(type)) {
			ConversationContent = "[图片]";
		} else if ("text".equals(type)) {
			ConversationContent = conversationMessage.getContent();
		}
		final String content = ConversationContent;
		final Intent intent = new Intent();
		intent.setClass(application, FacadeActivity.class);
		if (PadUtils.isPad(application)) {
			if (!application.isHasLogined()) {
				intent.putExtra("url", URL.PAD_LOGIN_URL);
				intent.putExtra("isPad", true);
			} else {
				String chatroom = propertiesUtil.getString("chatroom", "");
				intent.putExtra("jid", conversationMessage.getFromWho());
				intent.putExtra("direction", 2);
				intent.putExtra("type", "fragment");
				intent.putExtra("value", chatroom);
				//如果不是用户聊天
				if(!IMModelManager.instance().containUserModel
						(conversationMessage.getChater())){
					intent.putExtra("chat", "room");
					ChatGroupModel model = IMModelManager.instance().
							getChatRoomContainer().getStuff(conversationMessage.getChater());
					if (model != null){
						ConversationTitle = model.getGroupName();
					}
				} else {
					ConversationTitle = conversationMessage.getChater();
				}
			}

		} else {
			if (!application.isHasLogined()) {
				intent.putExtra("url", URL.PHONE_LOGIN_URL);
				intent.putExtra("isPad", false);
			} else {
				intent.setClass(application, ChatRoomActivity.class);
				intent.putExtra("jid", conversationMessage.getFromWho());
				//如果不是用户聊天
				if(!IMModelManager.instance().containUserModel
						(conversationMessage.getChater())){
					intent.putExtra("chat", "room");
					ChatGroupModel model = IMModelManager.instance().
							getChatRoomContainer().getStuff(conversationMessage.getChater());
					if (model != null){
						ConversationTitle = model.getGroupName();
					}
				} else {
					ConversationTitle = conversationMessage.getChater();
				}

			}
		}
		final String title = ConversationTitle;
		application.getUIHandler().post(new Runnable() {

			@Override
			public void run() {
				Notifier.notifyInfo(xmppManager.getNotificationService(),
						R.drawable.appicon, Constants.ID_CHAT_NOTIFICATION,
						title, content, intent);
			}
		});

	}

	private List<ConversationMessage> convs = new CopyOnWriteArrayList<ConversationMessage>();

	@Override
	public void processPacket(Packet packet) {
		log.debug("NotificationPacketListener.processPacket()...");
		log.debug("packet.toXML()=" + packet.toXML());

		if (packet instanceof Message) {
			Message message = Message.class.cast(packet);
			if (message.getType() == Type.chat || message.getType() == Type.groupchat) {
				log.info("received a chat message!");
				String localTime = (String) message.getProperty("sendDate");
				ConversationMessage conversation = new ConversationMessage();
				conversation.setToWho(message.getTo());
				conversation.setUser(message.getTo());
				conversation.setType(message.getSubject());
				conversation.setContent(message.getBody());
				if (message.getType() == Type.chat){
					conversation.setChater(StringUtils.parseBareAddress(message.getFrom()));
					conversation.setFromWho(StringUtils.parseBareAddress(message.getFrom()));
					//如果是陌生人
					if (!IMModelManager.instance().containUserModel
							(StringUtils.parseBareAddress(message.getFrom()))){
						return;
					}
					conversation.setFromType(SessionModel.SESSION_SINGLE);
				} 
				else if (message.getType() == Type.groupchat){
					String[] s = message.getFrom().split("/");
					String roomJid = s[0];
					String userJid = s[1];
					UserModel usemodel = IMModelManager.instance().getMe();
					//如果是自己发言
					if (usemodel != null && userJid.equals(usemodel.getJid())){
						return;
					}
					
					String subjectType = message.getSubject();
					//解散群组的通知
					if ("quitgroup".equals(subjectType)){
						IMModelManager.instance().getChatRoomContainer().
						leave(application ,roomJid);
						// 发送消息通知界面已退出群组
						List<ConversationMessage> list = ConversationMessage.findHistory(roomJid);
						if (localTime != null && !"".equals(localTime)){
							conversation.setLocalTime(TimeUnit.convert2long(localTime, TimeUnit.LONG_FORMAT));
						} else {
							conversation.setLocalTime(System.currentTimeMillis());
						}
						for(ConversationMessage conversationMessage : list){
							if(conversationMessage.getLocalTime() == conversation.getLocalTime()){
								return;
							}
						}
						conversation.setChater(roomJid);
						conversation.setFromWho(userJid);
						conversation.setFromType(SessionModel.SESSION_ROOM);
						String contentString = "用户群组被解散";
						conversation.setContent(contentString);
						StaticReference.userMf.createOrUpdate(conversation);
						return;
					}
					
					//T出群组的通知
					if ("killperson".equals(subjectType)){
						//被踢出群
						ChatGroupModel chatGroupModel = 
								IMModelManager.instance().getChatRoomContainer().getStuff(roomJid);
						List<ConversationMessage> list = ConversationMessage.findHistory(roomJid);
						if (localTime != null && !"".equals(localTime)){
							conversation.setLocalTime(TimeUnit.convert2long(localTime, TimeUnit.LONG_FORMAT));
						} else {
							conversation.setLocalTime(System.currentTimeMillis());
						}
						for(ConversationMessage conversationMessage : list){
							if(conversationMessage.getLocalTime() == conversation.getLocalTime()){
								return;
							}
						}
						conversation.setChater(roomJid);
						conversation.setFromWho(userJid);
						conversation.setFromType(SessionModel.SESSION_ROOM);
						String content = message.getBody();
						String contentString = null;
						if (content.equals(XmppManager.getMeJid())){
							IMModelManager.instance().getChatRoomContainer().
							leave(application ,roomJid);
							// 发送消息通知界面已退出群组
							contentString = "您已被" + chatGroupModel.getGroupName() + "群组踢出群组";
						} else {
							UserModel userModel = IMModelManager.instance().getUserModel(content);
							if (userModel != null){
								content = userModel.getName();
							} else {
								if (content.contains("@")){
									String s1[] = content.split("@");
									content = s1[0];
								}
							}
							contentString = content + "被" + chatGroupModel.getGroupName() + "群组踢出群组";
						}
						conversation.setContent(contentString);
						StaticReference.userMf.createOrUpdate(conversation);
						return;

					}
					
					//收到其他用户退出用户组的通知
					if ("quitperson".equals(subjectType)){
						String leaveUser = message.getBody();
						//刷新chatgroupModel数据
						UserModel userModel = null;
						if (IMModelManager.instance().containUserModel(leaveUser)){
							userModel = IMModelManager.instance().getUserModel(leaveUser);
							ChatGroupModel chatGroupModel = 
									IMModelManager.instance().getChatRoomContainer().getStuff(roomJid);
							if (chatGroupModel != null && userModel != null){
								chatGroupModel.getList().remove(userModel);
							}
						}
						List<ConversationMessage> list = ConversationMessage.findHistory(roomJid);
						if (localTime != null && !"".equals(localTime)){
							conversation.setLocalTime(TimeUnit.convert2long(localTime, TimeUnit.LONG_FORMAT));
						} else {
							conversation.setLocalTime(System.currentTimeMillis());
						}
						for(ConversationMessage conversationMessage : list){
							if(conversationMessage.getLocalTime() == conversation.getLocalTime()){
								return;
							}
						}
						conversation.setChater(roomJid);
						conversation.setFromWho(userJid);
						conversation.setFromType(SessionModel.SESSION_ROOM);
						conversation.setType("text");
						if (userModel != null){
							leaveUser = userModel.getName();
						} else {
							if (leaveUser.contains("@")){
								String s1[] = leaveUser.split("@");
								leaveUser = s1[0];
							}
						}
						String contentString = leaveUser + "离开用户组";
						conversation.setContent(contentString);
						StaticReference.userMf.createOrUpdate(conversation);
						return;
					}
//					//如果是陌生人
//					if (!IMModelManager.instance().containUserModel(userJid)){
//						return;
//					}
					conversation.setChater(roomJid);
					conversation.setFromWho(userJid);
					conversation.setFromType(SessionModel.SESSION_ROOM);
				}
				
				if (localTime != null && !"".equals(localTime)){
					conversation.setLocalTime(TimeUnit.convert2long(localTime, TimeUnit.LONG_FORMAT));
				} else {
					conversation.setLocalTime(System.currentTimeMillis());
				}
				if (null == message.getSubject()) {
					log.info("received a text message!");
					conversation.setType("text");
				}
				synchronized (this) {
					String jid = conversation.getChater();
					if (IMModelManager.instance().containUserModel(jid)) {
						UserModel userModel = IMModelManager.instance().getUserModel(jid);
						if (userModel != null){
							List<ConversationMessage>  conversationMessages = userModel.findLastHistory(1);
							if (conversationMessages.size() > 0){
								ConversationMessage cMessage = conversationMessages.get(0);
								if(cMessage != null && cMessage.getLocalTime() < conversation.getLocalTime()){
									convs.add(conversation);
								}
								if (cMessage == null){
									convs.add(conversation);
								}
							}
							else {
								convs.add(conversation);
							}
						}
						
					} 
					else {
						ChatGroupModel chatGroupModel = IMModelManager.instance()
								.getChatRoomContainer().getStuff(jid);
						if (chatGroupModel != null){
							List<ConversationMessage>  conversationMessages = chatGroupModel.findLastHistory(1);
							if (conversationMessages.size() > 0){
								ConversationMessage cMessage = conversationMessages.get(0);
								if(cMessage != null && cMessage.getLocalTime() < conversation.getLocalTime()){
									convs.add(conversation);
								}
								if (cMessage == null){
									convs.add(conversation);
								}
							} else {
								convs.add(conversation);
							}
						}
					}
					// 通知刷新界面
//					EventBus.getEventBus(TmpConstants.EVENTBUS_MESSAGE_CONTENT,
//							ThreadEnforcer.MAIN).post(new ConversationChangedEvent());
				}
			} else if (message.getType() == Type.normal) {
				log.info("received a normal message,ignore it!");
			}

		} else {
			log.info("unknow message typ ,ignore it!");
		}

	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		pool.shutdown();
	}

	/**
	 * @return 创建并音频文件路径
	 **/
	private String makeVoiceReceiveDir(String chater) {
		long time = System.currentTimeMillis();
		String datetime = DateFormat.format("yyyyMMddhhmmssfff", time)
				.toString();
		String dir = Environment.getExternalStorageDirectory().getPath() + "/"
				+ TmpConstants.RECORDER_RECEIVE_PATH;
		File recordDir = new File(dir);
		if (!recordDir.exists()) {
			recordDir.mkdirs();
		}
		String voicePath = dir + "/" + chater + " " + datetime + ".aac";
		return voicePath;
	}

}
