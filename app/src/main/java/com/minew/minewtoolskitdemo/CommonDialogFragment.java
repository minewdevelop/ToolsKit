package com.minew.minewtoolskitdemo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class CommonDialogFragment extends BaseDialogFragment {

    private static CommonDialogFragment instance;
    public static final String Negative_TEXT_KEY = "negative";
    public static final String Positive_TEXT_KEY = "positive";
    public static final String Message = "message";

    /*public CommonDialogFragment() throws UnsupportedOperationException{
        throw new UnsupportedOperationException("unsupported new instance for CommonDialogFragment!");
    }*/

    private String negativeText;
    private String positiveText;
    private String message;

    private OnSelectCallback mOnSelectCallback;

    public static CommonDialogFragment newInstance(String message) {
        Bundle bundle = new Bundle();
        bundle.putString(Message, message);
        bundle.putBoolean("isOnlyConfirm", false);
        CommonDialogFragment dialog = new CommonDialogFragment();
        dialog.setArguments(bundle);
        return dialog;
    }

    /**
     * 是否隐藏“取消”按钮
     * @param message 弹窗显示的信息
     * @param isOnlyConfirm 是否隐藏？，调用该方法就直接认为隐藏了
     * @return
     */
    public static CommonDialogFragment newInstance(String message,boolean isOnlyConfirm) {
        Bundle bundle = new Bundle();
        bundle.putString(Message, message);
        bundle.putBoolean("isOnlyConfirm", true);
        CommonDialogFragment dialog = new CommonDialogFragment();
        dialog.setArguments(bundle);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle == null) {
            throw new IllegalArgumentException("未使用newInstance方法创建实例！");
        }
        View view = inflater.inflate(R.layout.dialog_reset, container, false);
        TextView tvRefuse = view.findViewById(R.id.tv_reset_refuse);
        TextView tvOk = view.findViewById(R.id.tv_reset_ok);
        TextView tvMessage = view.findViewById(R.id.tv_message);
        View viewLine3 = view.findViewById(R.id.view_line_3);
        if (bundle.getString(Message) != null) {
            tvMessage.setText(bundle.getString(Message));
        }

        if (bundle.getBoolean("isOnlyConfirm")) {
            tvRefuse.setVisibility(View.GONE);
            viewLine3.setVisibility(View.GONE);
        }

        tvRefuse.setOnClickListener(new ClickProxy(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnSelectCallback != null) {
                    mOnSelectCallback.onNegative();
                }
            }
        }));

        tvOk.setOnClickListener(new ClickProxy(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnSelectCallback != null) {
                    mOnSelectCallback.onPositive("");
                }
            }
        }));
        return view;
    }

    public void setOnSelectCallback(OnSelectCallback onSelectCallback) {
        mOnSelectCallback = onSelectCallback;
    }
}
