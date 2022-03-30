/**
 * Licensed to JumpMind Inc under one or more contributor
 * license agreements.  See the NOTICE file distributed
 * with this work for additional information regarding
 * copyright ownership.  JumpMind Inc licenses this file
 * to you under the GNU General Public License, version 3.0 (GPLv3)
 * (the "License"); you may not use this file except in compliance
 * with the License.
 *
 * You should have received a copy of the GNU General Public License,
 * version 3.0 (GPLv3) along with this library; if not, see
 * http://www.gnu.org/licenses.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jumpmind.pos.core.ui;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ActionItem implements Serializable {

    private static final long serialVersionUID = 1L;
    
    protected String action;
    protected String title;

    /**
     * Optional default payload for action to be sent back to the server
     */
    protected String defaultPayload;

    protected String icon;
    @Builder.Default
    protected boolean enabled = true;
    protected ConfirmationDialog confirmationDialog;
    protected List<ActionItem> children;
    protected boolean sensitive;
    protected String buttonSize;
    protected String fontSize;
    protected String keybind;
    protected String keybindDisplayName;
    protected boolean queueIfBlocked;
    protected boolean doNotBlockForResponse;
    protected String additionalIcon;
    protected String additionalText;
    protected ActionTimer actionTimer;
    protected String additionalStyle;

    @JsonIgnore
    @Builder.Default
    protected boolean autoAssignEnabled = true;

    @JsonIgnore
    @Builder.Default
    protected boolean globalActionFlag = false;

    public final static String FONT_SIZE_XS = "text-xs";
    public final static String FONT_SIZE_SM = "text-sm";
    public final static String FONT_SIZE_MD = "text-md";
    public final static String FONT_SIZE_LG = "text-lg";
    public final static String FONT_SIZE_XL = "text-xl";

    public final static String BUTTON_SIZE_XS = "menuItem-xs";
    public final static String BUTTON_SIZE_SM = "menuItem-sm";
    public final static String BUTTON_SIZE_MD = "menuItem-md";
    public final static String BUTTON_SIZE_LG = "menuItem-lg";
    public final static String BUTTON_SIZE_XL = "menuItem-xl";

    public ActionItem(String action) {
        this();
        this.action = action;
    }

    public ActionItem(String action, String title) {
        this();
        this.action = action;
        this.title = title;
    }

    public ActionItem(String action, String title, String icon) {
        this(action, title);
        this.icon = icon;
    }

    public ActionItem(boolean autoAssignEnabled, String action, String title, String icon) {
        this(action, title, icon);
        this.autoAssignEnabled = autoAssignEnabled;
    }

    public ActionItem(String action, String title, String icon, String confirmationMessage) {
        this(action, title, icon);
        if (confirmationMessage != null) {
            this.confirmationDialog = new ConfirmationDialog();
            this.confirmationDialog.setTitle(title);
            this.confirmationDialog.setMessage(confirmationMessage);
        }
    }

    public ActionItem(String action, String title, String icon, ConfirmationDialog confirmationDialog) {
        this(action, title, icon);
        this.confirmationDialog = confirmationDialog;
    }

    public ActionItem(String title, String action, boolean enabled) {
        this();
        this.action = action;
        this.title = title;
        this.enabled = enabled;
    }

    public ActionItem(String title, String action, String icon, boolean enabled) {
        this();
        this.action = action;
        this.title = title;
        this.enabled = enabled;
        this.icon = icon;
    }

    public ActionItem(String title, String action, boolean enabled, boolean sensitive) {
        this();
        this.action = action;
        this.title = title;
        this.enabled = enabled;
        this.sensitive = sensitive;
    }

    public String getConfirmationMessage() {
        if (this.confirmationDialog != null) {
            return this.confirmationDialog.getMessage();
        }

        return null;
    }

    public void setConfirmationMessage(String confirmationMessage) {
        if (this.confirmationDialog == null) {
            this.confirmationDialog = new ConfirmationDialog();
        }

        this.confirmationDialog.setMessage(confirmationMessage);
    }
}
