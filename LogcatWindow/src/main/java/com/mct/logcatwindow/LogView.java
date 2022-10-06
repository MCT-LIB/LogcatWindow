package com.mct.logcatwindow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.mct.logcatwindow.adapter.SimpleTextAdapter;
import com.mct.logcatwindow.control.LogCtrl;
import com.mct.logcatwindow.model.LogCat;
import com.mct.logcatwindow.model.LogMainThread;
import com.mct.logcatwindow.model.LogManager;
import com.mct.logcatwindow.model.TraceLevel;
import com.mct.logcatwindow.model.TraceObject;
import com.mct.logcatwindow.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"ConstantConditions", "FieldCanBeLocal"})
@SuppressLint("ViewConstructor")
public class LogView extends LinearLayout implements LogCtrl.LogInteract {
    private Button settingButton;
    private EditText filterEditText;
    private LinearLayout bottomLayout;
    private Spinner spinner;
    private ListView mListView;
    private SimpleTextAdapter simpleTextAdapter;
    private List<TraceObject> data;
    private LogCtrl logCtrl;
    private int lastScrollPosition;
    private LinearLayout extraSetLayout;
    private SeekBar widthSeekBar;
    private SeekBar heightSeekBar;
    private SeekBar touchAreaSeekBar;
    private final Map<String, Object> extraData;
    private ChangeWindowListener changeWindowListener;
    private LayoutParams listLayoutParams;
    private LogManager logManager;
    private LogConfig logConfig;

    public LogView(Context context, Map<String, Object> extraData) {
        super(context);
        this.extraData = extraData;
        this.initializeView();
        this.hookListener();
        this.initializePresenter();
    }

    public LogManager getLogManager() {
        return this.logManager;
    }

    public void setChangeWindowListener(ChangeWindowListener changeWindowListener) {
        this.changeWindowListener = changeWindowListener;
    }

    private void hookListener() {
        this.data = new ArrayList<>();
        this.simpleTextAdapter = new SimpleTextAdapter(this.data, this.getContext());
        this.mListView.setAdapter(this.simpleTextAdapter);
        this.mListView.setOnScrollListener(new OnScrollListener() {
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (LogView.this.lastScrollPosition - firstVisibleItem != 1) {
                    LogView.this.lastScrollPosition = firstVisibleItem;
                }

                int lastVisiblePositionInTheList = firstVisibleItem + visibleItemCount;
                if (LogView.this.logCtrl != null) {
                    LogView.this.logCtrl.onScrollToPosition(lastVisiblePositionInTheList);
                }
            }
        });
        ArrayAdapter<TraceLevel> adapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_list_item_1, TraceLevel.values());
        this.spinner.setAdapter(adapter);
        this.spinner.setSelection(0);
        this.spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) view).setText(TraceLevel.values()[position].getValue());
                if (LogView.this.logCtrl != null && position != 1) {
                    LogView.this.logCtrl.updateFilterTraceLevel((TraceLevel) parent.getItemAtPosition(position));
                }
                if (LogView.this.logCtrl != null && position == 1) {
                    LogView.this.logCtrl.updateFilterTraceLevel((TraceLevel) parent.getItemAtPosition(position));
                    LogView.this.logCtrl.updateFilter("console");
                    LogView.this.filterEditText.setText("console");
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        this.filterEditText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                LogView.this.logCtrl.updateFilter(s.toString().trim());
                LogView.this.updateView();
            }

            public void afterTextChanged(Editable s) {
            }
        });
        this.settingButton.setOnClickListener(v -> {
            if (LogView.this.extraSetLayout.getVisibility() == VISIBLE) {
                LogView.this.extraSetLayout.setVisibility(GONE);
            } else {
                LogView.this.extraSetLayout.setVisibility(VISIBLE);
            }

        });
        this.heightSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                if (LogView.this.changeWindowListener != null) {
                    LogView.this.changeWindowListener.changeWindowHeight(seekBar.getProgress());
                }
            }
        });
        this.widthSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                if (LogView.this.changeWindowListener != null) {
                    LogView.this.changeWindowListener.changeWindowsWidth(seekBar.getProgress());
                }
            }
        });
        this.touchAreaSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                LogView.this.listLayoutParams.setMargins(0, LogUtils.dpToPx(LogView.this.getContext(), (float) progress), 0, 0);
                LogView.this.updateView();
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void updateView() {
        this.invalidate();
    }

    private void initializePresenter() {
        this.logManager = new LogManager(new LogCat(), new LogMainThread());
        this.logConfig = new LogConfig();
        this.logCtrl = new LogCtrl(this.logManager, this, this.logConfig);
        this.logCtrl.resume();
        this.filterEditText.setText(this.logConfig.getFilter());
    }

    @SuppressLint("SetTextI18n")
    private void initializeView() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.TRANSPARENT);
        drawable.setCornerRadius(LogUtils.dpToPx(getContext(), 8));
        this.setBackground(drawable);
        this.setOrientation(VERTICAL);
        this.setClipToOutline(true);
        this.setFocusableInTouchMode(true);
        this.requestFocus();

        this.filterEditText = new EditText(this.getContext());
        this.bottomLayout = new LinearLayout(this.getContext());
        this.bottomLayout.setPadding(16, 8, 16, 8);
        this.bottomLayout.setBackgroundColor(Color.parseColor("#1A000000"));
        this.bottomLayout.setGravity(Gravity.CENTER);

        this.spinner = new Spinner(this.getContext());
        LayoutParams refreshLayoutParams = new LayoutParams(0, -1, 5.0F);
        this.filterEditText.setHint("setFilter");
        this.filterEditText.setTextColor(-16777216);
        this.filterEditText.setSingleLine();
        this.filterEditText.setGravity(16);
        this.bottomLayout.addView(this.filterEditText, refreshLayoutParams);
        LayoutParams spinnerLayoutParams = new LayoutParams(0, -1, 4.0F);
        spinnerLayoutParams.setMargins(LogUtils.dpToPx(this.getContext(), 6.0F), 0, 0, 0);
        this.spinner.setGravity(17);
        this.bottomLayout.addView(this.spinner, spinnerLayoutParams);
        LayoutParams refreshButtonLayoutParams = new LayoutParams(0, -1, 2.0F);
        refreshButtonLayoutParams.setMargins(LogUtils.dpToPx(this.getContext(), 6.0F), 0, 0, 0);
        this.settingButton = new Button(this.getContext());
        this.settingButton.setText("setting");
        this.settingButton.setMaxLines(1);
        this.settingButton.setEllipsize(TextUtils.TruncateAt.END);

        LayoutParams settingButtonLayoutParams = new LayoutParams(0, -2, 3.0F);
        settingButtonLayoutParams.setMargins(LogUtils.dpToPx(this.getContext(), 6.0F), 0, 0, 0);
        this.bottomLayout.addView(this.settingButton, settingButtonLayoutParams);
        this.mListView = new ListView(this.getContext());
        this.mListView.setDivider(new ColorDrawable(-1));
        this.mListView.setBackgroundColor(Color.parseColor("#aa000000"));
        this.mListView.setStackFromBottom(true);
        this.listLayoutParams = new LayoutParams(-1, 0, 1.0F);
        this.addView(this.mListView, this.listLayoutParams);
        LayoutParams bottomLayoutLayoutParams = new LayoutParams(-1, -2);
        this.addView(this.bottomLayout, bottomLayoutLayoutParams);

        this.extraSetLayout = new LinearLayout(this.getContext());
        this.extraSetLayout.setPadding(16, 16, 0, 16);
        this.extraSetLayout.setBackgroundColor(Color.parseColor("#1A000000"));
        this.extraSetLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout widthLayout = new LinearLayout(this.getContext());
        TextView widthlab = new TextView(this.getContext());
        widthlab.setText("Width:  ");
        LayoutParams widthParams = new LayoutParams(-2, -2);
        widthLayout.addView(widthlab, widthParams);
        this.widthSeekBar = new SeekBar(this.getContext());
        this.widthSeekBar.setMax(LogUtils.getDevDisplay(this.getContext())[0]);
        this.widthSeekBar.setProgress(LogUtils.getDevDisplay(this.getContext())[0]);
        LayoutParams widthProgressBarParams = new LayoutParams(0, -2, 1.0F);
        widthLayout.addView(this.widthSeekBar, widthProgressBarParams);
        this.extraSetLayout.addView(widthLayout);

        LinearLayout heightLayout = new LinearLayout(this.getContext());
        TextView heightLab = new TextView(this.getContext());
        heightLab.setText("Height:");
        LayoutParams heightParams = new LayoutParams(-2, -2);
        heightLayout.addView(heightLab, heightParams);
        this.heightSeekBar = new SeekBar(this.getContext());
        this.heightSeekBar.setMax(LogUtils.getDevDisplay(this.getContext())[1]);
        this.heightSeekBar.setProgress((Integer) this.extraData.get("width"));
        LayoutParams heightProgressBarParams = new LayoutParams(0, -2, 1.0F);
        heightLayout.addView(this.heightSeekBar, heightProgressBarParams);
        this.extraSetLayout.addView(heightLayout);

        LinearLayout touchAreaLayout = new LinearLayout(this.getContext());
        TextView touchLab = new TextView(this.getContext());
        touchLab.setText("Touch: ");
        LayoutParams touchParams = new LayoutParams(-2, -2);
        touchAreaLayout.addView(touchLab, touchParams);
        this.touchAreaSeekBar = new SeekBar(this.getContext());
        this.touchAreaSeekBar.setMax((Integer) this.extraData.get("touchAreaMax"));
        this.touchAreaSeekBar.setProgress((Integer) this.extraData.get("touchArea"));
        LayoutParams touchAreaSeekBarParams = new LayoutParams(0, -2, 1.0F);
        touchAreaLayout.addView(this.touchAreaSeekBar, touchAreaSeekBarParams);
        this.extraSetLayout.addView(touchAreaLayout);

        this.addView(this.extraSetLayout);
        this.extraSetLayout.setVisibility(View.GONE);
        this.mListView.setFocusable(false);
        this.mListView.setItemsCanFocus(false);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.isVisible()) {
            this.resumePresenter();
        }
    }

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

    public void hideKeyboardAndFocus() {
        hideSoftInput(filterEditText);
        requestFocus();
    }

    private void hideSoftInput(@NonNull final View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return;
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private boolean isVisible() {
        return this.getVisibility() == VISIBLE;
    }

    private void resumePresenter() {
        if (this.isPresenterReady()) {
            this.logCtrl.resume();
            int lastPosition = this.data.size() - 1;
            this.mListView.setSelection(lastPosition);
        }
    }

    private boolean isPresenterReady() {
        return this.logCtrl != null;
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.pausePresenter();
        Log.d(LogUtils.LOGCAT_WINDOW_TAG, "View destroys");
    }

    private void pausePresenter() {
        if (this.isPresenterReady()) {
            this.logCtrl.pause();
        }
    }

    @Override
    public void showTraces(List<TraceObject> traces, int removedTraces) {
        if (this.lastScrollPosition == 0) {
            this.lastScrollPosition = this.mListView.getFirstVisiblePosition();
        }

        if (this.data != null) {
            this.data.clear();
            this.data.addAll(traces);
        }

        this.simpleTextAdapter.notifyDataSetChanged();
        this.updateScrollPosition(removedTraces);
    }

    private void updateScrollPosition(int removedTraces) {
        boolean shouldUpdateScrollPosition = removedTraces > 0;
        if (shouldUpdateScrollPosition) {
            int newScrollPosition = this.lastScrollPosition - removedTraces;
            this.lastScrollPosition = newScrollPosition;
            this.mListView.setSelectionFromTop(newScrollPosition, 0);
        }
    }

    @Override
    public void clear() {
        this.data.clear();
        this.simpleTextAdapter.notifyDataSetChanged();
    }

    @Override
    public void disableAutoScroll() {
        this.mListView.setTranscriptMode(0);
    }

    @Override
    public void enableAutoScroll() {
        this.mListView.setTranscriptMode(2);
    }

    public interface ChangeWindowListener {
        void changeWindowHeight(int height);

        void changeWindowsWidth(int width);
    }
}
