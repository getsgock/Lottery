package com.example.nthforever.lottery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by nthforever on 2017/5/10.
 */

public class Lottery extends SurfaceView implements SurfaceHolder.Callback,Runnable {


    public static final int IDLE = 1;
    public static final int RUNNING = 2;
    public static final int END = 3;

    private DisplayMetrics metrics;

    public int getStatus() {
        return mStatus;
    }

    private int mStatus = IDLE;
    /**
     *
     */
    private SurfaceHolder mHolder;
    /**
     * 画布
     */
    private Canvas mCanvas;
    /**
     *控件的宽度
     */
    private int width;
    /**
     * 控件的高度
     */
    private int height;
    /**
     * 圆的半径
     */
    private float mRadius;
    /**
     * 圆的直径
     */
    private float mDiameter;
    /**
     * 周围的空隙间距
     */
    private int padding;

    /**
     * 中心点X坐标
     */
    private int centerX;
    /**
     * 中心点Y坐标
     */
    private int centerY;
    private boolean isRunning = false;
    /**
     * 绘制的线程
     */
    private Thread t;
    /**
     * 奖品的种类
     */
    private static final int MAX_NUM = 8;
    /**
     * 抽奖的文字
     */
    private String[] mStrs = new String[] { "ipad5 32G", "话费100元", "京东E卡500元", "268元红包",
            "500积分", "iphone7 128G","198元红包","谢谢参与"};
    /**
     * 每个盘块的颜色
     */
    private String[] mColors = new String[] {"#ffbb00","#ffdd00"};
    /**
     * 与文字对应的图片
     */
    private int[] mImgs = new int[] { R.drawable.prize_icon_ipad, R.drawable.prize_icon_100,
            R.drawable.prize_icon_500e, R.drawable.prize_icon_298, R.drawable.prize_icon_500,
            R.drawable.prize_icon_iphone7,R.drawable.prize_icon_198,R.drawable.prize_icon_none };
    /**
     * 与文字对应图片的bitmap数组
     */
    private Bitmap[] mImgsBitmap;
    /**
     * 偏移角度
     */
    private volatile float mStartAngle = 0;
    /**
     * 圆形背景
     */
    private Paint mPaintBg ;
    private String bgColor = "#ff0000";
    /**
     * 主背景
     */
    private Paint mPaintMainBg ;
    /**
     * 绘制盘块的范围
     */
    private RectF mRange ;
    /**
     * 绘制盘块的范围
     */
    private RectF mRangel ;
    /**
     * 绘制中间指针的范围
     */
    private RectF mRangePoint ;
    /**
     * 指针的边长
     */
    private float pointLength;

    private Bitmap pointBitmap;
    private Bitmap bgBitmap;
    /**
     * 画扇形
     */
    private volatile float maxSpeed = -1;
    private volatile float currSpeed = -1;
    private Paint mPaintArc;
    private Paint mPaintLine;

    private TextPaint mPaintText;
    private boolean hasReachMax = false;
    private boolean hasOverLength = false;

    /**
     * 加速度
     */
    private float a = 0.5f;
    private float lastAngle = 0;
    private float middleAngle = 0;
    private float target;
    private float overLength = -1;
    private volatile boolean finishFlag = false;
    private int levelId;
    /**
     * 手指按下的位置
     */
    private float pressX,pressY;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    if(!finishFlag && listener != null && mStatus == END){
                        listener.onFinish(levelId);
                        finishFlag = true;
                    }
                    break;
            }
        }
    };

    public void setListener(LotteryOnClickListener listener) {
        this.listener = listener;
    }

    private LotteryOnClickListener listener;

    public Lottery(Context context) {
        super(context);
        initView();
    }

    public Lottery(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public Lottery(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void setMetrics(DisplayMetrics metrics) {
        this.metrics = metrics;
    }

    private void initView(){
        mHolder = getHolder();
        mHolder.addCallback(this);
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setKeepScreenOn(true);

//        setBackgroundColor(Color.WHITE);
        setZOrderOnTop(true);
        mHolder.setFormat(PixelFormat.TRANSLUCENT);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = Math.min(getMeasuredWidth(), getMeasuredHeight());
        height = width;
        padding = width / 20;
        centerX = width / 2;
        centerY = height / 2;
        mRadius = (centerX - padding) * 0.76f;
        mDiameter = mRadius * 2;
        pointLength = mRadius ;
        setMeasuredDimension(width, width);

    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isRunning = true;
        mPaintBg = new Paint();
        mPaintBg.setAntiAlias(true);
        mPaintBg.setColor(Color.parseColor(bgColor));

        mPaintMainBg = new Paint();
        mPaintMainBg.setColor(Color.TRANSPARENT);

        mRange = new RectF(padding,padding,width-padding,height-padding);

        mRangel = new RectF(centerX - mRadius,centerY - mRadius,centerX + mRadius,centerY + mRadius);

        mPaintArc = new Paint();

        mPaintText = new TextPaint();
        float textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, metrics);
        mPaintText.setTextSize(textSize);
        mPaintText.setFakeBoldText(true);
        mPaintText.setColor(Color.parseColor("#e43006"));

        mPaintLine = new Paint();
        mPaintLine.setColor(Color.BLUE);
        mPaintLine.setStyle(Paint.Style.STROKE);
        mPaintLine.setStrokeWidth(2);

        pointBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.start);
        bgBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.main_bg);
        int width = pointBitmap.getWidth();
        int height = pointBitmap.getHeight();
        int max = Math.max(width, height);
        float v = max / pointLength;
        mRangePoint = new RectF(centerX - width/2/v,centerY - height/2/v, centerX + width/2/v,centerY + height/2/v);

        mImgsBitmap = new Bitmap[MAX_NUM];
        for (int i = 0; i < MAX_NUM; i++)
        {
            mImgsBitmap[i] = BitmapFactory.decodeResource(getResources(),
                    mImgs[i%mImgs.length]);
        }

        t = new Thread(this);
        t.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // 通知关闭线程
        isRunning = false;
        mImgsBitmap = null;
        pointBitmap = null;
        bgBitmap = null;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if(event.getAction() == MotionEvent.ACTION_UP){
            if(Math.abs(pressX - x) <= 50 && Math.abs(pressY - y) <= 50){
                if( mRangePoint.contains(x,y) && listener != null){
                    listener.onclick(mStatus);
                }
                return true;
            }
        }else if(event.getAction() == MotionEvent.ACTION_DOWN) {
            pressX = x;
            pressY = y;
            if(mRangePoint.contains(x,y)){
                return true;
            }
        }
        return super.onTouchEvent(event);

    }

    @Override
    public void run() {
        while (isRunning){
            draw();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void draw(){
        try {
            mCanvas = mHolder.lockCanvas();
            drawBg();
            drawCircle();
            if(maxSpeed == -1 || currSpeed == -1){
                drawTable(0);
                mStatus = IDLE;
            }else {
                if(currSpeed <= maxSpeed && !hasReachMax){
                    currSpeed +=a;
                    middleAngle = currSpeed*currSpeed/2/a;
                    drawTable(currSpeed*currSpeed/2/a);
                    mStatus = RUNNING;
                }else {
                    hasReachMax = true;
                    currSpeed -= a;
                    if(currSpeed <=0){
                        currSpeed = 0;
                    }
                    if(currSpeed == 0 && !hasOverLength){
                        overLength = middleAngle + ((maxSpeed * maxSpeed) -(  currSpeed * currSpeed))/2/a;
                        hasOverLength = true;
                    }
                    if(overLength != -1){
                        if(overLength <= target){
                            overLength = target;
                            mStatus = END;
                            drawTable(overLength);
                            mHandler.sendEmptyMessage(1);
                        }else {
                            overLength -=1;
                            mStatus = RUNNING;
                            drawTable(overLength);
                        }
                    }else {
                        drawTable( middleAngle + ((maxSpeed * maxSpeed) -(  currSpeed * currSpeed))/2/a);
                        mStatus = RUNNING;
                    }

                }
            }
            drawPoint();
//            drawVerticalLine();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(null != mCanvas){
                mHolder.unlockCanvasAndPost(mCanvas);
            }
        }

    }

    private void drawVerticalLine() {
        mCanvas.drawLine(width/2,0,width/2,height,mPaintLine);
    }

    private void drawBg(){
//        mCanvas.drawRect(0,0,width,height,mPaintMainBg);
        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    }

    private void drawCircle(){
//        mCanvas.drawCircle(centerX,centerY,mRadius,mPaintBg);
        mCanvas.drawBitmap(bgBitmap, null, mRange, null);
    }

    private void drawPoint(){
        mCanvas.save();
//        float angle = (float) ((270) * (Math.PI / 180));
//        mCanvas.rotate(angle,centerX,centerY);
        mCanvas.drawBitmap(pointBitmap, null, mRangePoint, null);
        mCanvas.restore();
    }
    /**
     * 画转盘
     * @param angle  偏移角度
     */
    private void drawTable(float angle){
//        mPaintArc.setColor(Color.MAGENTA);
//        mCanvas.drawRect(mRange,mPaintArc);
//        Log.i("ppp","angle is == "+angle);
        lastAngle = angle;
//        angle = target;
        for (int i=0;i<MAX_NUM;i++){
            float single = 360 / MAX_NUM;
            int length = mColors.length;
            int index = i % length;
            mPaintArc.setColor(Color.parseColor(mColors[index]));
            mCanvas.drawArc(mRangel,angle+single*i,single,true,mPaintArc);
            drawText(angle+single*i,single,mStrs[i%mStrs.length]);
            drawBitmap(angle+single*i,single,i);
        }
    }

    /**
     * 画奖品文字
     * @param start  起始角度
     * @param sweep  扫过角度
     * @param text   文本
     */
    private void drawText(float start,float sweep,String text){
        Path path = new Path();
        float width = mPaintText.measureText(text);
        path.addArc(mRangel, start, sweep);
        // 利用水平偏移让文字居中
        float hOffset = (float) (mDiameter * Math.PI / MAX_NUM / 2 - width / 2);// 水平偏移
        float vOffset = mRadius / 5;// 垂直偏移
        mCanvas.drawTextOnPath(text, path, hOffset, vOffset, mPaintText);
    }

    /**
     * 画奖品图片
     * @param start
     * @param sweep
     * @param index
     */
    private void drawBitmap(float start,float sweep,int index){
        mCanvas.save();
        int width = (int) (mRadius / 4f);
        float angle = (float) ((sweep / 2 + start) * (Math.PI / 180));
        int x = (int) (centerX + mRadius / 1.8 * Math.cos(angle));
        int y = (int) (centerY + mRadius / 1.8 * Math.sin(angle));
        float v = start + sweep / 2;
        float v1 = v % 360;
        mCanvas.rotate((90 + v1),x,y);
        Rect rect = new Rect(x - width / 2, y - width / 2, x + width
                / 2, y + width / 2);
        mCanvas.drawBitmap(mImgsBitmap[index], null, rect, null);
        mCanvas.restore();
    }

    /**
     * 中奖，开始旋转
     * @param index  奖品种类
     */
    public void start(int index){
        // 每项角度大小
        float angle = (float) (360 / MAX_NUM);
        float from = index*angle + angle /2;
        float to = 270;
         target = 8*360 + to - from;
//        Log.i("ppp","-----  target == "+target);
        maxSpeed = (float) Math.sqrt(target * a);
        currSpeed = 0;
        hasReachMax = false;
        overLength = -1;
        hasOverLength = false;
        levelId = index;
        mStatus = IDLE;
        finishFlag = false;
    }

    /**
     * 重置轮盘
     */
    public void reset(){
        finishFlag = false;
        currSpeed = -1;
        hasReachMax = false;
        maxSpeed = -1;
        target = 0;
        overLength = -1;
        hasOverLength = false;
        mStatus = IDLE;
        levelId = 0;
    }

    public interface LotteryOnClickListener {
        void onclick(int status);
        void onFinish(int i);
    }
}
