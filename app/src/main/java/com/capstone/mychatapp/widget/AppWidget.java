package com.capstone.mychatapp.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import com.capstone.mychatapp.R;
import com.capstone.mychatapp.activities.ChatActivity;
import com.squareup.picasso.Picasso;

import io.paperdb.Paper;

import static android.support.constraint.Constraints.TAG;

public class AppWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, String listUserId, String userName, String userThumb) {

        if (userThumb != null) {
            if (!userThumb.equals(context.getString(R.string.widget_default_image_check))){
            Uri imageUri = Uri.parse(userThumb);

            Intent chatIntent = new Intent(context, ChatActivity.class);
            chatIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

            chatIntent.putExtra(context.getString(R.string.user_id), listUserId);
            chatIntent.putExtra(context.getString(R.string.chatUserName), userName);

            PendingIntent pendingIntent = PendingIntent.getActivity (context,appWidgetId,chatIntent,0);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.my_chat_app_widget);
            views.setImageViewUri(R.id.widget_user_single_image, imageUri);
            views.setTextViewText(R.id.widget_user_single_name, userName);
            views.setOnClickPendingIntent(R.id.layout_wrapper, pendingIntent);

            Picasso.get()
                    .load(userThumb)
                    .into(views, R.id.widget_user_single_image, new int[]{appWidgetId});

            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId,R.id.layout_wrapper);

            appWidgetManager.updateAppWidget(appWidgetId, views);

            }else{

                Intent chatIntent = new Intent(context, ChatActivity.class);
                chatIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

                chatIntent.putExtra(context.getString(R.string.user_id), listUserId);
                chatIntent.putExtra(context.getString(R.string.chatUserName), userName);

                PendingIntent pendingIntent = PendingIntent.getActivity (context,appWidgetId,chatIntent,0);

                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.my_chat_app_widget);
                views.setImageViewResource(R.id.widget_user_single_image, R.drawable.profile_pic);
                views.setTextViewText(R.id.widget_user_single_name, userName);
                views.setOnClickPendingIntent(R.id.layout_wrapper, pendingIntent);

                Picasso.get()
                        .load(R.drawable.profile_pic)
                        .into(views, R.id.widget_user_single_image, new int[]{appWidgetId});

                Log.d(TAG, "updateAppWidget: appWidgetId: "+ appWidgetId + ", user_id: " + listUserId + ", userName: " + userName + ", image: " + userThumb);

                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId,R.id.layout_wrapper);

                appWidgetManager.updateAppWidget(appWidgetId, views);

            }

        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            String list_user_id = AppWidgetConfigureActivity.loadTitlePref(context, appWidgetId);

            Paper.init(context);
            String userName = Paper.book(String.valueOf(appWidgetId)).read("user_name");
            String userThumb = Paper.book(String.valueOf(appWidgetId)).read("image_thumb");

            Log.d(TAG, "onUpdate: appWidgetId: " + appWidgetId + ", listUserId: " + list_user_id + ", userName: " + userName + ", userThumb: " + userThumb);

            updateAppWidget(context, appWidgetManager, appWidgetId, list_user_id, userName, userThumb);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            AppWidgetConfigureActivity.deleteTitlePref(context, appWidgetId);

        }
    }

    @Override
    public void onEnabled(Context context) {
    }

    @Override
    public void onDisabled(Context context) {
    }
}

