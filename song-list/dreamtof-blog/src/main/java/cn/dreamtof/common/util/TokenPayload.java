package cn.dreamtof.common.util;

import lombok.Data;
import java.util.HashMap;
import java.util.Map;

@Data
public class TokenPayload {
    private String userId;
    private String username;
    private Long createTime;
    private String deviceId;

    public static TokenPayload fromMap(Map<String, Object> map) {
        if (map == null) return null;
        TokenPayload payload = new TokenPayload();
        payload.setUserId(String.valueOf(map.getOrDefault("userId", "")));
        payload.setUsername(String.valueOf(map.getOrDefault("username", "")));
        Object timeObj = map.get("createTime");
        payload.setCreateTime(timeObj != null ? Long.parseLong(timeObj.toString()) : System.currentTimeMillis());
        payload.setDeviceId(String.valueOf(map.getOrDefault("deviceId", "")));
        return payload;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("username", username != null ? username : "");
        map.put("createTime", createTime != null ? createTime : System.currentTimeMillis());
        map.put("deviceId", deviceId != null ? deviceId : "");
        return map;
    }
}
