package com.flock.atgb.handler;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by B0095829 on 4/1/17.
 */
public interface IAuthenticatedUrlRequestHandler {

    public boolean authenticate(String flockEventToken);
}
