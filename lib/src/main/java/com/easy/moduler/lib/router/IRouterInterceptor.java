package com.easy.moduler.lib.router;

import android.content.Context;
import android.content.Intent;


public interface IRouterInterceptor {
    boolean intercept(Context context, String url, Intent matchIntent);
}
