package com.company.iendo.ui.dialog;

import android.content.Context;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.company.iendo.R;
import com.company.iendo.aop.SingleClick;
import com.hjq.base.BaseDialog;
import com.hjq.widget.view.RegexEditText;

/**
 * author : Android 轮子哥
 * github : https://github.com/getActivity/AndroidProject
 * time   : 2019/02/27
 * desc   : 输入对话框  --两个输入框
 */
public final class Input2Dialog {

    public static final class Builder
            extends CommonDialog.Builder<Builder>
            implements BaseDialog.OnShowListener,
            TextView.OnEditorActionListener {

        @Nullable
        private OnListener mListener;
        private final RegexEditText mInputView;
        private final RegexEditText mInput2View;

        public Builder(Context context) {
            super(context);
            setCustomView(R.layout.input2_dialog);

            mInputView = findViewById(R.id.tv_input_message);
            mInput2View = findViewById(R.id.tv_input_message2);
            mInputView.setOnEditorActionListener(this);
            mInput2View.setOnEditorActionListener(this);

            addOnShowListener(this);
        }

        public Builder setHint(@StringRes int id) {
            return setHint(getString(id));
        }

        public Builder setHint(CharSequence text) {
            mInputView.setHint(text);
            return this;
        }

        public Builder set2Hint(CharSequence text) {
            mInput2View.setHint(text);
            return this;
        }

        public Builder setContent(@StringRes int id) {
            return setContent(getString(id));
        }

        public Builder setContent(CharSequence text) {
            mInputView.setText(text);
            Editable editable = mInputView.getText();
            if (editable == null) {
                return this;
            }
            int index = editable.length();
            if (index <= 0) {
                return this;
            }
            mInputView.requestFocus();
            mInputView.setSelection(index);
            return this;
        }

        public Builder set2Content(CharSequence text) {
            mInput2View.setText(text);
            Editable editable = mInput2View.getText();
            if (editable == null) {
                return this;
            }
            int index = editable.length();
            if (index <= 0) {
                return this;
            }
            mInput2View.requestFocus();
            mInput2View.setSelection(index);
            return this;
        }

        public Builder setInputRegex(String regex) {
            mInputView.setInputRegex(regex);
            return this;
        }

        public Builder setInput2Regex(String regex) {
            mInput2View.setInputRegex(regex);
            return this;
        }


        public Builder setListener(OnListener listener) {
            mListener = listener;
            return this;
        }

        /**
         * {@link BaseDialog.OnShowListener}
         */
        @Override
        public void onShow(BaseDialog dialog) {
            postDelayed(() -> showKeyboard(mInputView), 500);
        }

        @SingleClick
        @Override
        public void onClick(View view) {
            int viewId = view.getId();
            if (viewId == R.id.tv_ui_confirm) {
                autoDismiss();
                if (mListener == null) {
                    return;
                }
                Editable editable = mInputView.getText();
                Editable editable2 = mInput2View.getText();
                mListener.onConfirm(getDialog(), editable != null ? editable.toString() : "", editable2 != null ? editable2.toString() : "");
            } else if (viewId == R.id.tv_ui_cancel) {
                autoDismiss();
                if (mListener == null) {
                    return;
                }
                mListener.onCancel(getDialog());
            }
        }

        /**
         * {@link TextView.OnEditorActionListener}
         */
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // 模拟点击确认按钮
                onClick(findViewById(R.id.tv_ui_confirm));
                return true;
            }
            return false;
        }
    }

    public interface OnListener {

        /**
         * 点击确定时回调
         */
        void onConfirm(BaseDialog dialog, String content, String content2);

        /**
         * 点击取消时回调
         */
        default void onCancel(BaseDialog dialog) {
        }
    }
}