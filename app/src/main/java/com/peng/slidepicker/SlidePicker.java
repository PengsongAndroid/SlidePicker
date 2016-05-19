package com.peng.slidepicker;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by PS on 2016/5/17.
 */
public class SlidePicker extends View {

	private static final String TAG = SlidePicker.class.getSimpleName();

	/**
	 * text之间间距和minTextSize之比
	 */
	public static final float MARGIN_ALPHA = 2.8f;

	private float mMaxTextAlpha = 255;
	private float mMinTextAlpha = 120;

	/**
	 * 自动回滚到中间的速度
	 * */
	private static final float Speed = 2;

	/**
	 * 默认选中的位置一直是控件中间
	 * */
	private int mItemSelected = 0;

	/**
	 * 画笔
	 * */
	private Paint mPaint;

	/**
	 * 控件宽高
	 * */
	private int mViewHeight;
	private int mViewWidth;

	/**
	 * 滑动的距离
	 */
	private float mMoveLen = 0;

	/**
	 * 字体大小
	 * */
	private float mMaxTextSize = 40;

	private float mMinTextSize = 40;

	/**
	 * 文字颜色
	 * */
	private int mColorText = 0x333333;

	/**
	 * 是否初始化完成
	 * */
	private boolean isInit = false;

	private boolean isDrawLine = false;

	/**
	 * 显示的内容
	 * */
	private List<String> mData = null;

    private onSelectListener mSelectListener;

	private pickClickListener mClickListener;

    /**
     * 记录上次按下的Y坐标
     * */
    private float mLastDownY;

	private float mTextSize = 10f;

	/**
	 * 是否循环滚动
	 * */
	private boolean isRecycle =  false;

	/**
	 * 一行的高度
	 * */
	private int midLineHeight;

	/**
	 * 选择文字的大小为 size/mTextScale + size
	 * */
	private int mTextScale = 5;

	/**
	 * 选中行的背景色
	 * */
	private int mLineBgColor;

	private static final int SELECT_MSG = 0;
	private static final int CLICK_MSG = 1;

	public SlidePicker(Context context) {
		super(context);
		init();
	}

	public SlidePicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public SlidePicker(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	/**
	 * 初始化画笔
	 * */
	private void init() {
		mData = new ArrayList<String>();
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setTextAlign(Paint.Align.CENTER);
		mPaint.setColor(mColorText);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mViewHeight = getMeasuredHeight();
		mViewWidth = getMeasuredWidth();
		// 按照View的高度计算字体大小
		mMaxTextSize = mViewHeight / mTextSize;
		mMinTextSize = mMaxTextSize / 2f;
		isInit = true;
		mPaint.setTextSize(mMaxTextSize);
		mPaint.setAlpha(0);
		if (!isDrawLine){
			isDrawLine = true;
			Paint.FontMetricsInt fmi = mPaint.getFontMetricsInt();
			midLineHeight = fmi.bottom - fmi.top;
		}
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (isInit){
			drawLine(canvas);
			drawMainText(canvas);
			drawBg(canvas);
		}

	}

	/**
	 * 绘制选中行的背景色
	 * */
	private void drawLine(Canvas canvas){
		Paint paint = new Paint();
		paint.setAlpha(0);
		paint.setColor(mLineBgColor);
		canvas.drawRect(0, (mViewHeight - midLineHeight)/2 , mViewWidth, (mViewHeight + midLineHeight)/2, paint);
	}

	/**
	 * 绘制渐变的背景
	 * */
	private void drawBg(Canvas canvas){
	//这里的渐变半径设为3/4的宽度，因为RadialGradient渐变是按照圆心来的，如果是椭圆类的渐变只能舍弃一边的效果
		Shader shader = new RadialGradient(mViewWidth/2, mViewHeight/2,
				mViewWidth*3/4, getResources().getColor(R.color.transparentColor),
				getResources().getColor(R.color.white), Shader.TileMode.CLAMP);

		Paint paint = new Paint();
		paint.setShader(shader);
		canvas.drawRect(0, 0, mViewWidth, mViewHeight, paint);
	}

	/**
	 * 先绘制中间的文字 再从上往下绘制
	 * */
	private void drawMainText(Canvas canvas) {
		// text居中绘制，文字的绘制都是从Baseline处开始，注意Baseline的计算才能达到居中，y值是text中心坐标
		float scale = parabola(mViewHeight / mTextSize, mMoveLen);
		float size = (mMinTextSize/mTextScale) * scale + mMinTextSize;
        mPaint.setTextSize(size);
        mPaint.setAlpha((int) ((mMaxTextAlpha - mMinTextAlpha) * scale + mMinTextAlpha));

		float x = (float) (mViewWidth / 2.0);
		float y = (float) (mViewHeight / 2.0 + mMoveLen);
		Paint.FontMetricsInt fmi = mPaint.getFontMetricsInt();
		float baseline = (float) (y - (fmi.bottom / 2.0 + fmi.top / 2.0));
		canvas.drawText(mData.get(mItemSelected), x, baseline, mPaint);
		// 绘制上方data
		for (int i = 1; (mItemSelected - i) >= 0; i++) {
			drawOtherText(canvas, i, -1);
		}
		// 绘制下方data
		for (int i = 1; (mItemSelected + i) < mData.size(); i++) {
			drawOtherText(canvas, i, 1);
		}
	}

	/**
	 * 绘制其他部分文字
	 *
	 * @param position
	 *            距离mCurrentSelected的差值
	 * @param type
	 *            1表示向下绘制，-1表示向上绘制
	 * */
	private void drawOtherText(Canvas canvas, int position, int type) {
		float d = (float) (MARGIN_ALPHA * mMinTextSize * position + type * mMoveLen);
		float scale = parabola(mViewHeight / mTextSize, d);
		float size = (mMinTextSize/mTextScale) * scale + mMinTextSize;
		mPaint.setTextSize(size);
		mPaint.setAlpha((int) ((mMaxTextAlpha - mMinTextAlpha) * scale + mMinTextAlpha));
		float y = (float) (mViewHeight / 2.0 + type * d);
		Paint.FontMetricsInt fmi = mPaint.getFontMetricsInt();
		float baseline = (float) (y - (fmi.bottom / 2.0 + fmi.top / 2.0));
		canvas.drawText(mData.get(mItemSelected + type * position), (float) (mViewWidth / 2.0), baseline, mPaint);
	}

	/**
	 * 抛物线
	 *
	 * @param zero
	 *            零点坐标
	 * @param x
	 *            偏移量
	 * @return scale
	 */
	private float parabola(float zero, float x) {
		float f = (float) (1 - Math.pow(x / zero, 2));
		return f < 0 ? 0 : f;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			doDown(event);
			break;
		case MotionEvent.ACTION_MOVE:
			doMove(event);
			break;
		case MotionEvent.ACTION_UP:
			doUp(event);
			break;
		}
		return true;
	}

	/**
	 * 移动
	 * */
	private void doMove(MotionEvent event) {

        mMoveLen += (event.getY() - mLastDownY);

		if (!isRecycle){
			//滑动到顶部
			if (mItemSelected == 0 && mMoveLen > 0){
				return;
			}
			//滑动到底部
			if (mItemSelected == mData.size()-1 && mMoveLen < 0){
				return;
			}
		}

        if (mMoveLen > MARGIN_ALPHA * mMinTextSize / 2)
        {
            // 往下滑超过离开距离
            moveTailToHead();
            mMoveLen = mMoveLen - MARGIN_ALPHA * mMinTextSize;
        } else if (mMoveLen < -MARGIN_ALPHA * mMinTextSize / 2)
        {
            // 往上滑超过离开距离
            moveHeadToTail();
            mMoveLen = mMoveLen + MARGIN_ALPHA * mMinTextSize;
        }
		mLastDownY = event.getY();
        invalidate();
	}

	private void moveHeadToTail()
	{
		if (isRecycle){
			String head = mData.get(0);
			mData.remove(0);
			mData.add(head);
		} else if (mItemSelected < mData.size() - 1){
			mItemSelected += 1;
		}
	}

	private void moveTailToHead()
	{
		if (isRecycle){
			String tail = mData.get(mData.size() - 1);
			mData.remove(mData.size() - 1);
			mData.add(0, tail);
		} else if (mItemSelected > 0){
			mItemSelected -= 1;
		}
	}

	/**
	 * 送开
	 * */
	private void doUp(MotionEvent event) {
        // 抬起手后mCurrentSelected的位置由当前位置move到中间选中位置
        if (Math.abs(mMoveLen) < 1)
        {
			//这里可以判断为点击某条
            mMoveLen = 0;
			updateHandler.sendEmptyMessage(CLICK_MSG);
        } else {
			updateHandler.sendEmptyMessage(SELECT_MSG);
		}
	}

	/**
	 * 按下
	 * */
	private void doDown(MotionEvent event) {
        mLastDownY = event.getY();
	}


    Handler updateHandler = new Handler()
    {

        @Override
        public void handleMessage(Message msg)
        {
			if (msg.what == SELECT_MSG){
				performSelect();
				mMoveLen = 0;
				invalidate();
			} else if (msg.what == CLICK_MSG){
				performOnClick();
			}

        }

    };

	public void setData(List<String> data) {
		this.mData = data;
		mItemSelected = mData.size() / 2;
		invalidate();
	}

	private void performOnClick()
	{
		if (mClickListener != null)
			mClickListener.onClick(mData.get(mItemSelected));
	}


	private void performSelect()
    {
        if (mSelectListener != null)
            mSelectListener.onSelect(mData.get(mItemSelected));
    }

    public void setSelected(int selected)
    {
        mItemSelected = selected;
    }

	public interface pickClickListener
	{
		void onClick(String text);
	}

	public void setPickOnClickListener(pickClickListener listener)
	{
		mClickListener = listener;
	}

    public interface onSelectListener
    {
        void onSelect(String text);
    }

	public void setOnSelectListener(onSelectListener listener)
	{
		mSelectListener = listener;
	}

	public void setRecycleRoll(boolean recycleRoll){
		this.isRecycle = recycleRoll;
	}

	public void  setLineColor(int color){
		this.mLineBgColor = color;
	}
}
