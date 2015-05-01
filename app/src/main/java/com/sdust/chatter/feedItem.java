package com.sdust.chatter;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by Eric on 3/4/2015.
 */
@ParseClassName("feedItem")
public class feedItem extends ParseObject {
    public String description;
    public ParseUser user;

    public feedItem() {

    }

    public String getDescription() {
        getString("description");
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        put("description", description);
    }

    public ParseUser getUser() {
        getParseUser("user");
        return user;
    }

    public void setUser(ParseUser user) {
        this.user = user;
        put("user", user);
    }
}
