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
package org.jumpmind.pos.core.flow;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class Scope {

    private Map<String, ScopeValue> deviceScope = new ConcurrentHashMap<String, ScopeValue>();
    private Map<String, ScopeValue> sessionScope = new ConcurrentHashMap<String, ScopeValue>();
    private Map<String, ScopeValue> conversationScope = new ConcurrentHashMap<String, ScopeValue>();

    public void clearConversationScope() {
        conversationScope.clear();
    }

    public void clearSessionScope() {
        clearConversationScope();
        sessionScope.clear();
    }
    
    public void clearDeviceScope() {
        deviceScope.clear();
    }

    public void removeDeviceScope(String key) {
    	deviceScope.remove(key);
    }
    
    public void removeConversationScope(String key) {
    	conversationScope.remove(key);
    }
    
    public void removeSessionScope(String key) {
    	sessionScope.remove(key);
    }
    public ScopeValue resolve(String name) {
        if (conversationScope.containsKey(name)) {
            return conversationScope.get(name);
        } else if (sessionScope.containsKey(name)) {
            return sessionScope.get(name);
        } else if (deviceScope.containsKey(name)) {
            return deviceScope.get(name);
        }
        return null;
    }

    public void setScope(String name, ScopeType scopeType, Object value) {
        switch (scopeType) {
            case Device:
                setDeviceScope(name, value);
                break;
            case Session:
                setSessionScope(name, value);
                break;
            case Conversation:
                setConversationScope(name, value);
                break;
            default:
                throw new FlowException("Invalid scope " + scopeType + " for name '" + name + "'");
        }
    }

    public void setDeviceScope(String name, Object value) {
        log.debug("setting device scope value '{}' value = '{}'", name, value);

        setScope(deviceScope, name, value);
    }

    public void setSessionScope(String name, Object value) {
        setScope(sessionScope, name, value);
    }

    public void setConversationScope(String name, Object value) {
        setScope(conversationScope, name, value);
    }

    protected void setScope(Map<String, ScopeValue> scope, String name, Object value) {
        if (value instanceof ScopeValue) {
            scope.put(name, (ScopeValue)value);
        } else {            
            ScopeValue scopeValue = new ScopeValue();
            scopeValue.setValue(value);
            scope.put(name, scopeValue);
        }
    }

    public ScopeType getScopeType(String name) {
        if (getScopeValue(ScopeType.Device, name) != null) {
            return ScopeType.Device;
        } else if (getScopeValue(ScopeType.Session, name) != null) {
            return ScopeType.Session;
        } else if (getScopeValue(ScopeType.Conversation, name) != null) {
            return ScopeType.Conversation;
        } else {
            return null;
        }
    }
    
    public void setScopeValue(ScopeType scopeType, String name, Object value) {
        switch (scopeType) {
            case Device:
                setDeviceScope(name, value);
                break;
            case Session:
                setSessionScope(name, value);
                break;
            case Conversation:
                setConversationScope(name, value);
                break;
            case State:
                break;                
            default:
                throw new FlowException("Invalid scope " + scopeType);
        }
    }
    
    public ScopeValue getScopeValue(ScopeType scopeType, String name) {
        switch (scopeType) {
            case Device:
                return getDeviceScope().get(name);
            case Session:
                return getSessionScope().get(name);
            case Conversation:
                return getConversationScope().get(name);
            case State:
                return null;                          
            default:
                throw new FlowException("Invalid scope " + scopeType);
        }
        
    }

    public Map<String, ScopeValue> getDeviceScope() {
        return deviceScope;
    }

    public void setNodeScope(Map<String, ScopeValue> nodeScope) {
        this.deviceScope = nodeScope;
    }

    public Map<String, ScopeValue> getSessionScope() {
        return sessionScope;
    }

    public void setSessionScope(Map<String, ScopeValue> sessionScope) {
        this.sessionScope = sessionScope;
    }

    public Map<String, ScopeValue> getConversationScope() {
        return conversationScope;
    }

    public void setConversationScope(Map<String, ScopeValue> conversationScope) {
        this.conversationScope = conversationScope;
    }

}
