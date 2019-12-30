package com.matrix.yukun.matrix.leancloud_module.activity;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.matrix.yukun.matrix.R;
import com.matrix.yukun.matrix.leancloud_module.entity.ContactInfo;
import com.matrix.yukun.matrix.main_module.entity.UserInfoBMob;
import com.matrix.yukun.matrix.selfview.CubeRecyclerView;

import butterknife.OnClick;

public class LeanChatActivity extends LeanBaseActivity {

    public static void start(Context context, ContactInfo contactInfo){
        Intent intent=new Intent(context,LeanChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("data",contactInfo);
        context.startActivity(intent);
    }

    public static void start(Context context, UserInfoBMob userInfoBMob){
        Intent intent=new Intent(context,LeanChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("user",userInfoBMob);
        context.startActivity(intent);
    }

    @Override
    public void initView() {
        super.initView();

    }

    @Override
    public void initListener() {
        super.initListener();
        rvChatview.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    rvChatview.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mLeanChatAdapter.getItemCount() > 0) {
                                rvChatview.smoothScrollToPosition(mLeanChatAdapter.getItemCount()-1);
                            }
                        }
                    });
                }
            }
        });
        //点击
        this.rvChatview.setEventListener(new CubeRecyclerView.OnEventListener() {
            @Override
            public void onStartTouch() {
                mInputPanelManager.dismissLayout();
            }
        });
    }

    @OnClick({R.id.iv_back})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
        }
    }
}
