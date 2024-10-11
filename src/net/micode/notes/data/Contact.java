/*
 * Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// 定义包名
package net.micode.notes.data;
// 导入所需的类
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import java.util.HashMap;
// 定义Contact类
public class Contact {
        // 静态缓存，用于存储联系人信息
    private static HashMap<String, String> sContactCache;
    // 日志标记，用于调试
    private static final String TAG = "Contact";
   // 查询联系人时的选择条件
    private static final String CALLER_ID_SELECTION = "PHONE_NUMBERS_EQUAL(" + Phone.NUMBER
    + ",?) AND " + Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'"
    + " AND " + Data.RAW_CONTACT_ID + " IN "
            + "(SELECT raw_contact_id "
            + " FROM phone_lookup"
            + " WHERE min_match = '+')";

     // 根据电话号码获取联系人名称
    public static String getContact(Context context, String phoneNumber) {
         // 如果缓存未初始化，则进行初始化
        if(sContactCache == null) {
            sContactCache = new HashMap<String, String>();
        }
           // 如果缓存中已有该号码对应的联系人，直接返回

        if(sContactCache.containsKey(phoneNumber)) {
            return sContactCache.get(phoneNumber);
        }

        
         // 替换查询条件中的最小匹配字符
        String selection = CALLER_ID_SELECTION.replace("+",
                PhoneNumberUtils.toCallerIDMinMatch(phoneNumber));
          // 查询联系人数据
        Cursor cursor = context.getContentResolver().query(
                Data.CONTENT_URI,// 查询的数据来源
                new String [] { Phone.DISPLAY_NAME },// 查询的列
                selection,// 查询的选择条件
                new String[] { phoneNumber },// 查询参数
                null);// 排序条件
      // 如果查询结果不为空且移动到第一条记录
        if (cursor != null && cursor.moveToFirst()) {
            try {
                  // 获取联系人名称
                String name = cursor.getString(0);
                // 将联系人名称放入缓存
                sContactCache.put(phoneNumber, name);
                return name;// 返回联系人名称
            } catch (IndexOutOfBoundsException e) {
                // 处理索引越界异常并记录错误日志
                Log.e(TAG, " Cursor get string error " + e.toString());
                return null;// 返回空
            } finally {
                 // 确保游标在结束时被关闭
                cursor.close();
            }
        } else {
            // 如果没有找到对应的联系人，记录调试日志
            Log.d(TAG, "No contact matched with number:" + phoneNumber);
            return null; // 返回空
        }
    }
}
