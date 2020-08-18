package com.creditclub.pos.api

import com.creditclub.core.data.CreditClubMiddleWareAPI

inline val CreditClubMiddleWareAPI.posApiService: PosApiService
    get() = retrofit.create(PosApiService::class.java)