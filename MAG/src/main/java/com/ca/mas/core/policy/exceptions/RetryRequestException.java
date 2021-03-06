/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.policy.exceptions;

import com.ca.mas.core.error.MAGStateException;

/**
 * Exception thrown if a request should retried from the beginning.
 */
public class RetryRequestException extends MAGStateException {

    public RetryRequestException(String message) {
        super(message);
    }

    public RetryRequestException(Throwable throwable) {
        super(throwable);
    }
}
