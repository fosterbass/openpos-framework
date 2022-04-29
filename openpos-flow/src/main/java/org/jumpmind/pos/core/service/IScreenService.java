package org.jumpmind.pos.core.service;

import org.jumpmind.pos.core.ui.CloseToast;
import org.jumpmind.pos.core.ui.Toast;
import org.jumpmind.pos.core.ui.UIMessage;
import org.jumpmind.pos.core.ui.data.UIDataMessageProvider;

import java.util.Map;

public interface IScreenService {

    void showScreen(String nodeId, UIMessage screen, Map<String, UIDataMessageProvider<?>> dataMessageProvider);

    void showScreen(String nodeId, UIMessage screen);

    void showToast(String nodeId, Toast toast);

    void closeToast(String nodeId, CloseToast toast);

    UIMessage getLastScreen(String nodeId);

    UIMessage getLastDialog(String nodeId);

    UIMessage getLastPreInterceptedScreen(String deviceId);

    UIMessage getLastPreInterceptedDialog(String deviceId);

}
