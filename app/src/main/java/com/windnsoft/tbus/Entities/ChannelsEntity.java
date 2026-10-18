package com.windnsoft.tbus.Entities;

import java.io.Serializable;

/**
 * Created by Windn(Windnsoft@gmail.com) on 2016.
 */

public class ChannelsEntity implements Serializable {

    public long id;
    public long channel_number;
    public String channel_title;
    public String channel_pw;
    public boolean channel_public;
    public long channel_creator;
}
