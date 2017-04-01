package com.flock.atgb.handler;

import com.flock.atgb.dto.FlockEvent;
import com.flock.atgb.exception.FlockException;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by B0095829 on 4/1/17.
 */
public interface IUrlRequestHandler {

    public Object handleRequest(FlockEvent flockEvent, String requestPayload, HttpServletRequest request) throws FlockException;
}
