package io.procker.codeshare.actions;

import com.google.gson.JsonObject;
import com.intellij.ide.BrowserUtil;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.Messages;
import com.sun.istack.NotNull;
import io.procker.codeshare.constants.Constants;
import io.procker.codeshare.constants.MessageConstants;
import io.procker.codeshare.utils.ShareUtils;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.Objects;

public class ShareAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
        // get selected text
        Editor editor = actionEvent.getData(PlatformDataKeys.EDITOR);
        assert editor != null;
        String selectedText = editor.getSelectionModel().getSelectedText();

        // check selected text
        if(selectedText == null || selectedText.isEmpty()) {
            // show error + return
            Messages.showErrorDialog(MessageConstants.MSG_ERROR_NO_SELECTION, MessageConstants.MSG_TITLE);
            return;
        }

        // build body
        JsonObject body = new JsonObject();
        body.addProperty("share", selectedText);
        body.addProperty("language", Objects.requireNonNull(actionEvent.getData(CommonDataKeys.PSI_FILE)).getLanguage().getDisplayName());

        // send request
        String shareID = ShareUtils.sendRequest(Constants.URL_BACKEND_CREATE_SHARE, body);

        // check creation was successful
        if(shareID == null || shareID.isEmpty()) {
            // show error
            Messages.showErrorDialog(MessageConstants.MSG_ERROR_CREATION_FAILED, MessageConstants.MSG_TITLE);
            return;
        }

        // create action: copy
        AnAction copyAction = new AnAction(MessageConstants.MSG_TITLE_ACTION_COPY) {
            @Override
            public void actionPerformed(@org.jetbrains.annotations.NotNull AnActionEvent event) {
                // copy to clipboard
                StringSelection stringSelection = new StringSelection(Constants.URL_USER_SHARE + shareID);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);
            }
        };

        // create action: open url
        AnAction openURLAction = new AnAction(MessageConstants.MSG_TITLE_ACTION_OPEN_URL) {
            @Override
            public void actionPerformed(@org.jetbrains.annotations.NotNull AnActionEvent event) {
                // open url in browser
                BrowserUtil.browse(Constants.URL_USER_SHARE + shareID);
            }
        };

        // create notification
        NotificationGroup notificationGroup = new NotificationGroup(MessageConstants.MSG_TITLE, NotificationDisplayType.STICKY_BALLOON, true);
        notificationGroup.createNotification(MessageConstants.MSG_TITLE, "", MessageConstants.MSG_SUCCESS_CREATION, NotificationType.INFORMATION)
                .setImportant(true)
                .addAction(copyAction)
                .addAction(openURLAction)
                .notify(actionEvent.getProject());
    }
}
