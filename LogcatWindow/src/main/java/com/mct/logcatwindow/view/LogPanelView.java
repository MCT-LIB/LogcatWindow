package com.mct.logcatwindow.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.mct.logcatwindow.LogConfig;
import com.mct.logcatwindow.R;
import com.mct.logcatwindow.adapter.SimpleTextAdapter;
import com.mct.logcatwindow.control.LogCtrl;
import com.mct.logcatwindow.model.LogCat;
import com.mct.logcatwindow.model.LogMainThread;
import com.mct.logcatwindow.model.LogManager;
import com.mct.logcatwindow.model.TraceLevel;
import com.mct.logcatwindow.model.TraceObject;
import com.mct.logcatwindow.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class LogPanelView extends RelativeLayout implements LogCtrl.LogInteract {

    public static final String KEY_MAX_HEIGHT = "max_height";
    public static final String KEY_MAX_WIDTH = "max_width";
    public static final String KEY_MAX_TOUCH = "max_touch";
    public static final String KEY_HEIGHT = "height";
    public static final String KEY_WIDTH = "width";
    public static final String KEY_TOUCH = "touch";

    private ListView listView;
    private EditText filterEditText;
    private CustomSpinner spnLevel;
    private ImageButton btnSetting, btnClose;
    private View extraSetting;
    private SeekBar sbHeight;
    private SeekBar sbWidth;
    private SeekBar sbTouch;

    private SimpleTextAdapter simpleTextAdapter;
    private List<TraceObject> data;
    private LogCtrl logCtrl;
    private int lastScrollPosition;

    private OnWindowChangeListener onWindowChangeListener;
    private LogManager logManager;

    public LogPanelView(@NonNull Context context) {
        this(context, null);
    }

    public LogPanelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LogPanelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initializeView(context);
        this.initializeListener();
        this.initializePresenter();
    }

    public LogManager getLogManager() {
        return logManager;
    }

    public void setChangeWindowListener(OnWindowChangeListener onWindowChangeListener) {
        this.onWindowChangeListener = onWindowChangeListener;
    }

    public void setData(@NonNull Bundle extraData) {
        sbHeight.setMax(extraData.getInt(KEY_MAX_HEIGHT));
        sbWidth.setMax(extraData.getInt(KEY_MAX_WIDTH));
        sbTouch.setMax(extraData.getInt(KEY_MAX_TOUCH));
        sbHeight.setProgress(extraData.getInt(KEY_HEIGHT));
        sbWidth.setProgress(extraData.getInt(KEY_WIDTH));
        sbTouch.setProgress(extraData.getInt(KEY_TOUCH));
    }

    private void initializeView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.lw_layout_log_panel, this, true);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.parseColor("#aa000000"));
        drawable.setCornerRadius(Utils.dp2px(8));
        setBackground(drawable);
        setClipToOutline(true);
        setFocusableInTouchMode(true);
        requestFocus();

        listView = findViewById(R.id.lw_lv_data);
        filterEditText = findViewById(R.id.lw_tv_filter);
        spnLevel = findViewById(R.id.lw_spn_level);
        btnSetting = findViewById(R.id.lw_btn_setting);
        btnClose = findViewById(R.id.lw_btn_close);
        extraSetting = findViewById(R.id.lw_extra_setting);
        sbHeight = findViewById(R.id.lw_sb_height);
        sbWidth = findViewById(R.id.lw_sb_width);
        sbTouch = findViewById(R.id.lw_sb_touch);

        listView.setDividerHeight(0);
        listView.setStackFromBottom(true);
        listView.setFocusable(false);
        listView.setItemsCanFocus(false);
    }

    private void initializeListener() {
        data = new ArrayList<>();
        simpleTextAdapter = new SimpleTextAdapter(this.data, this.getContext());
        listView.setAdapter(this.simpleTextAdapter);
        listView.setOnScrollListener(new OnScrollListener() {
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (lastScrollPosition - firstVisibleItem != 1) {
                    lastScrollPosition = firstVisibleItem;
                }

                int lastVisiblePositionInTheList = firstVisibleItem + visibleItemCount;
                if (logCtrl != null) {
                    logCtrl.onScrollToPosition(lastVisiblePositionInTheList);
                }
            }
        });
        ArrayAdapter<TraceLevel> adapter = new ArrayAdapter<>(this.getContext(), R.layout.lw_layout_log_level_item, TraceLevel.values());
        spnLevel.setAdapter(adapter);
        spnLevel.setSpinnerEventsListener((spinner, isOpened) -> {
            requestFocus();
            if (onWindowChangeListener != null) {
                onWindowChangeListener.onDropDownChanged(isOpened);
            }
        });
        spnLevel.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TraceLevel level = TraceLevel.values()[position];
                ((TextView) view).setText(level.getValue());
                if (logCtrl != null) {
                    logCtrl.updateFilterTraceLevel(level);
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        filterEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                ((InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });
        filterEditText.setOnKeyListener((v, keyCode, e) -> {
            if (KeyEvent.KEYCODE_BACK == keyCode && e.getAction() == KeyEvent.ACTION_UP) {
                requestFocus();
                return true;
            }
            return false;
        });
        filterEditText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                logCtrl.updateFilter(s.toString().trim());
            }

            public void afterTextChanged(Editable s) {
            }
        });
        OnClickListener onClickListener = v -> {
            if (v == btnSetting) {
                requestFocus();
                if (extraSetting.getVisibility() == VISIBLE) {
                    extraSetting.setVisibility(GONE);
                } else {
                    extraSetting.setVisibility(VISIBLE);
                }
                return;
            }
            if (v == btnClose) {
                if (onWindowChangeListener != null) {
                    onWindowChangeListener.requestDetachWindow();
                }
            }
        };
        btnSetting.setOnClickListener(onClickListener);
        btnClose.setOnClickListener(onClickListener);

        OnSeekBarChangeListener seekBarChangeListener = new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (seekBar == sbTouch) {
                    ((ViewGroup.MarginLayoutParams) listView.getLayoutParams())
                            .setMargins(0, Utils.dp2px(seekBar.getProgress()), 0, 0);
                    listView.setLayoutParams(listView.getLayoutParams());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                requestFocus();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (onWindowChangeListener != null) {
                    if (seekBar == sbWidth) {
                        onWindowChangeListener.changeWindowWidth(seekBar.getProgress());
                        return;
                    }
                    if (seekBar == sbHeight) {
                        onWindowChangeListener.changeWindowHeight(seekBar.getProgress());
                    }
                }
            }
        };
        sbHeight.setOnSeekBarChangeListener(seekBarChangeListener);
        sbWidth.setOnSeekBarChangeListener(seekBarChangeListener);
        sbTouch.setOnSeekBarChangeListener(seekBarChangeListener);
    }

    private void initializePresenter() {
        this.logManager = new LogManager(new LogCat(), new LogMainThread());
        LogConfig logConfig = new LogConfig();
        this.logCtrl = new LogCtrl(this.logManager, this, logConfig);
        this.logCtrl.resume();
        this.filterEditText.setText(logConfig.getFilter());
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.isVisible()) {
            this.resumePresenter();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.pausePresenter();
        Log.d(Utils.LOGCAT_WINDOW_TAG, "View destroys");
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (changedView == this) {
            if (visibility == 0) {
                this.resumePresenter();
            } else {
                this.pausePresenter();
            }
        }
    }

    private boolean isVisible() {
        return this.getVisibility() == VISIBLE;
    }

    private boolean isPresenterReady() {
        return this.logCtrl != null;
    }

    private void resumePresenter() {
        if (this.isPresenterReady()) {
            this.logCtrl.resume();
            int lastPosition = this.data.size() - 1;
            this.listView.setSelection(lastPosition);
        }
    }

    private void pausePresenter() {
        if (this.isPresenterReady()) {
            this.logCtrl.pause();
        }
    }

    @Override
    public void showTraces(List<TraceObject> traces, int removedTraces) {
        if (this.lastScrollPosition == 0) {
            this.lastScrollPosition = this.listView.getFirstVisiblePosition();
        }

        if (this.data != null) {
            this.data.clear();
            for (TraceObject trace : traces) if (trace != null) this.data.add(trace);
        }

        this.simpleTextAdapter.notifyDataSetChanged();
        boolean shouldUpdateScrollPosition = removedTraces > 0;
        if (shouldUpdateScrollPosition) {
            int newScrollPosition = this.lastScrollPosition - removedTraces;
            this.lastScrollPosition = newScrollPosition;
            this.listView.setSelectionFromTop(newScrollPosition, 0);
        }
    }

    @Override
    public void clear() {
        this.data.clear();
        this.simpleTextAdapter.notifyDataSetChanged();
    }

    @Override
    public void disableAutoScroll() {
        this.listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
    }

    @Override
    public void enableAutoScroll() {
        this.listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
    }

    public interface OnWindowChangeListener {
        void changeWindowHeight(int height);

        void changeWindowWidth(int width);

        void onDropDownChanged(boolean isOpened);

        void requestDetachWindow();
    }
}
