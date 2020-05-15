/*
 * 版权：Copyright(c) 2017 Toonyoo Tech. Co. Ltd. All Rights Reserved.
 * 描述：[项目描述]
 * 修改人：[Xia Wenhao]
 * 修改时间：[17-6-1 上午9:44]
 * 修改内容：[修改内容]
 */
package com.leedoo.printerdemo;

import java.io.IOException;

/**
 * Created by Xia Wenhao on 2017/6/1.
 */

public class Response extends ResponseProtocol {
    public Response(byte[] byteArray) throws IOException {
        super(byteArray);
    }

}
