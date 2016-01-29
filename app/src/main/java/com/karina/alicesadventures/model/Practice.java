package com.karina.alicesadventures.model;

import java.util.List;

/**
 * Created by Karina Nishimura on 15-09-30.
 */
public class Practice {
    private Integer _id;
    private User user;
    private Practice practice;

    public Practice(Integer id) {
        _id = id;

    }

    public Integer get_id() {
        return _id;
    }

    public void set_id(Integer _id) {
        this._id = _id;
    }


}
