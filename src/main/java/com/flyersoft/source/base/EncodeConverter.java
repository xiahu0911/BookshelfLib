package com.flyersoft.source.base;

import android.text.TextUtils;

import com.flyersoft.source.utils.EncodingDetect;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

public class EncodeConverter extends Converter.Factory {
    private String encode;

    private EncodeConverter() {

    }

    private EncodeConverter(String encode) {
        this.encode = encode;
    }

    public static EncodeConverter create() {
        return new EncodeConverter();
    }

    public static EncodeConverter create(String en) {
        return new EncodeConverter(en);
    }

    @Override
    public Converter<ResponseBody, String> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        return new Converter<ResponseBody, String>() {
            @Override
            public String convert(ResponseBody value) throws IOException {
                byte[] responseBytes = UTF8BOMFighter.removeUTF8BOM(value.bytes());
                if (!TextUtils.isEmpty(encode)) {
                    try {
                        return new String((responseBytes), Charset.forName(encode));
                    } catch (Exception ignored) {
                    }
                }
                String charsetStr;
                MediaType mediaType = value.contentType();
                //根据http头判断
                if (mediaType != null) {
                    Charset charset = mediaType.charset();
                    if (charset != null) {
                        return new String((responseBytes), charset);
                    }
                }
                //根据内容判断
                charsetStr = EncodingDetect.getEncodeInHtml(responseBytes);
                return new String(responseBytes, Charset.forName(charsetStr));
            }
        };
    }

}
