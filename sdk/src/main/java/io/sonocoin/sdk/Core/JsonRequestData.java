package io.sonocoin.sdk.Core;

import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import io.sonocoin.sdk.Types.Faq.FaqItem;
import io.sonocoin.sdk.Types.Faqitem;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

public class JsonRequestData {
    public static CountDownLatch requestData() {
        final CountDownLatch latch = new CountDownLatch(1);
        String url = Config.MAIN_SERVER + "/data.json";
        Config.COURSE = new BigDecimal("2.5");
        Config.FEE_TYPE = Integer.parseInt("1");
        Config.FEE_VALUE = new BigDecimal("0.01");
        AndroidNetworking.get(url)
                .setPriority(Priority.LOW)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        try {
                            // Config.FAQ_VERSION = jsonObject.get("faq_version").toString();
                            Config.ANDROID_VERSION = jsonObject.get("android").toString();
                            Config.IOS_VERSION = jsonObject.get("ios").toString();
                            Config.COURSE = new BigDecimal(jsonObject.get("course").toString());
                            JSONObject Commission = (JSONObject) jsonObject.get("commission");
                            Config.FEE_TYPE = Integer.parseInt(Commission.get("type").toString());
                            Config.FEE_VALUE = new BigDecimal(Commission.get("value").toString());
                            JSONArray jArray = (JSONArray) jsonObject.get("faq");
                            ArrayList<Faqitem> listData = new ArrayList<Faqitem>();
                            if (jArray != null) {
                                for (int i = 0; i < jArray.length(); i++) {
                                    JSONObject fi = jArray.getJSONObject(i);
                                    JSONObject ru_src = fi.getJSONObject("ru");
                                    JSONObject en_src = fi.getJSONObject("en");
                                    FaqItem ru = new FaqItem(ru_src.getString("title"), ru_src.getString("url"));
                                    FaqItem en = new FaqItem(en_src.getString("title"), en_src.getString("url"));
                                    Faqitem faq_item = new Faqitem(ru, en);
                                    listData.add(faq_item);
                                }
                                Config.FaqList = listData;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        latch.countDown();
                    }

                    @Override
                    public void onError(ANError error) {
                        Config.COURSE = new BigDecimal("2.5");
                        Config.FEE_TYPE = Integer.parseInt("1");
                        Config.FEE_VALUE = new BigDecimal("0.01");
                        latch.countDown();
                        // Log.d("response", "there was an error: "+error.getMessage());
                        // handle error
                    }
                });

        return latch;
    }
}
