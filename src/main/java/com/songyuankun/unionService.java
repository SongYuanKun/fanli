package com.songyuankun;

import com.songyuankun.jd.repository.entity.JdUserPO;

public interface unionService {

    String getGoodInfo(String url, String fromUserId);

    default void createUser(String fromUserId) {

    }

    default JdUserPO getUser(String fromUserId) {
        return null;
    }
}
