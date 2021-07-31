package com.khammami.ecos.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class Speedometer extends View {
    private static final String TAG = Speedometer.class.getSimpleName();

    private static final float SCALE_RANGE = 270.0f;
    private static final float SCALE_START_ANGLE = 135.0f;

    private Paint backgroundPaint;
    private Path backgroundPath;
    private Paint linePaint;
    private Path linePath;
    private Paint needleScrewPaint;
    private Path needleScrewPath;
    private Paint mScalePaint;
    private Paint mScaleTextPaint;
    private Paint mSpeedTextPaint;

    private final Matrix matrix;
    private final int framePerSeconds = 100;
    private final long animationDuration = 10000;
    private final long startTime;

    private float centerX;
    private float centerY;
    private float radius;
    private final float mMaxSpeed = 127.0f;
    private float mMinSpeed = 0.0f;
    private float mCurrentSpeed;

    int totalTicks;

    private SpeedChangeListener speedListener;

    public interface SpeedChangeListener {

        public void onSpeedChanged(float newSpeedValue);

    }

    public Speedometer(Context context) {
        super(context);
        matrix = new Matrix();
        this.startTime = System.currentTimeMillis();
        //this.postInvalidate();
        init();
    }

    public Speedometer(Context context, AttributeSet attrs) {
        super(context, attrs);
        matrix = new Matrix();
        this.startTime = System.currentTimeMillis();
        //this.postInvalidate();
        init();
    }

    public Speedometer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        matrix = new Matrix();
        this.startTime = System.currentTimeMillis();
        //this.postInvalidate();
        init();
    }

    private void init(){

        //Background
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.BLACK); // Set the color
        backgroundPaint.setStyle(Paint.Style.FILL); // set the border and fills the inside of needle
        backgroundPaint.setAntiAlias(true);
        backgroundPath = new Path();

        //Speedometer
        linePaint = new Paint();
        linePaint.setColor(Color.parseColor("#FFD305")); // Set the color
        linePaint.setStyle(Paint.Style.FILL_AND_STROKE); // set the border and fills the inside of needle
        linePaint.setAntiAlias(true);
        linePaint.setStrokeWidth(1.0f); // width of the border
        //linePaint.setShadowLayer(8.0f, 0.1f, 0.1f, Color.GRAY); // Shadow of the needle
        linePath = new Path();

        //Speedometer Screw
        needleScrewPaint = new Paint();
        needleScrewPaint.setColor(Color.GRAY);
        needleScrewPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        needleScrewPaint.setAntiAlias(true);
        needleScrewPath = new Path();

        //Scale
        mScalePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mScalePaint.setColor(Color.WHITE);
        mScalePaint.setStyle(Paint.Style.STROKE);
        mScalePaint.setTextSize(0.05f);
        mScalePaint.setTypeface(Typeface.SANS_SERIF);
        mScalePaint.setTextAlign(Paint.Align.CENTER);
        mScalePaint.setStrokeWidth(10.0f);

        //Scale Text
        mScaleTextPaint = new Paint(Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        mScaleTextPaint.setColor(Color.WHITE);
        mScaleTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mScaleTextPaint.setTextSize(0.05f);
        mScaleTextPaint.setTypeface(Typeface.MONOSPACE);
        mScaleTextPaint.setTextAlign(Paint.Align.CENTER);
        mScaleTextPaint.setTextSize(45.0f);
        mScaleTextPaint.setStrokeWidth(3.0f);

        //Current speed text
        mSpeedTextPaint = new Paint(Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        mSpeedTextPaint.setColor(Color.WHITE);
        mSpeedTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mSpeedTextPaint.setTextSize(0.05f);
        mSpeedTextPaint.setTypeface(Typeface.MONOSPACE);
        mSpeedTextPaint.setTextAlign(Paint.Align.CENTER);
        mSpeedTextPaint.setTextSize(45.0f);
        mSpeedTextPaint.setStrokeWidth(3.0f);
    }

    private void drawNeedleScrew(Canvas canvas){
        needleScrewPath.reset();
        needleScrewPath.moveTo(centerX, centerY);
        //needleScrewPath.lineTo(230.0f, 50.0f);
        //needleScrewPath.lineTo(50.0f, 50.0f);
        needleScrewPath.addCircle(centerX, centerY, radius*0.136f, Path.Direction.CW);
        needleScrewPath.close();

        canvas.drawPath(needleScrewPath, needleScrewPaint);
    }

    private void drawNeedle(Canvas canvas) {
        linePath.reset();
        linePath.moveTo(centerX+5.0f, centerY+2.0f);
        linePath.lineTo(centerX+(radius*0.4864f), centerY+2.0f);
        linePath.lineTo(centerX+(radius*0.4864f), centerY-2.0f);
        linePath.lineTo(centerX+5.0f, centerY-2.0f);
        linePath.lineTo(centerX, centerY-(radius*0.0497f));
        linePath.lineTo(centerX+(radius*0.4864f), centerY-(radius*0.047f));
        linePath.lineTo(centerX+(radius*0.6793f), centerY);
        linePath.lineTo(centerX+(radius*0.4864f), centerY+(radius*0.047f));
        linePath.lineTo(centerX, centerY+(radius*0.0497f));
        linePath.lineTo(centerX+5.0f, centerY+2.0f);
        //linePath.addCircle(50.0f, 50.0f, 30.0f, Path.Direction.CW);
        linePath.close();

        canvas.save();
        float mCurrentAngle = convertSpeedToAngle(mCurrentSpeed);
        canvas.rotate(mCurrentAngle, centerX,centerY);
        canvas.drawPath(linePath, linePaint);
        canvas.restore();
    }

    private void drawScaleBackground(Canvas canvas){
        backgroundPath.reset();
        backgroundPath.addCircle(centerX,centerY,radius,Path.Direction.CW);

        Paint onBackgroundPaint = new Paint();
        onBackgroundPaint.setColor(Color.GRAY); // Set the color
        onBackgroundPaint.setStyle(Paint.Style.STROKE); // set the border and fills the inside of needle
        onBackgroundPaint.setAntiAlias(true);
        float strokeWidth = radius*0.1078f;
        onBackgroundPaint.setStrokeWidth(strokeWidth);

        Path onBackgroundPath = new Path();
        onBackgroundPath.addCircle(centerX,centerY,radius-(strokeWidth/2)-3,Path.Direction.CW);

        canvas.drawPath(backgroundPath, backgroundPaint);
        canvas.drawPath(onBackgroundPath, onBackgroundPaint);
    }

    private void drawScale(Canvas canvas) {
        canvas.save();
        int textModulo;
        float divisionRotation;
        //TODO Max Speed as an input
        if (mMaxSpeed >31) {
            totalTicks = 25;
            divisionRotation = SCALE_RANGE/(totalTicks-1);
            textModulo = 8;
            //canvas.rotate(divisionRotation/3, centerX, centerY);
        } else {
            totalTicks = (int)mMaxSpeed+1;
            //while ((totalTicks-1) % 4 != 0) totalTicks = totalTicks+1;
            divisionRotation = SCALE_RANGE/(totalTicks-1);
            if (mMaxSpeed % 4 != 0) textModulo = (int)mMaxSpeed/3;
            else textModulo = (int)mMaxSpeed/2;
            //canvas.rotate(divisionRotation, centerX, centerY);
        }

        for (int i = 0; i < totalTicks; i++) {
            float largeTickW = radius*0.1078f;
            float largeTickOffset = radius*0.1349f;
            float largeTickStartX = centerX+radius-largeTickOffset;
            float largeTickStopX = centerX+radius-largeTickOffset-largeTickW;

            float mediumTickW = radius*0.077f;
            float mediumTickOffset = radius*0.1535f;
            float mediumTickStartX = centerX+radius-mediumTickOffset;
            float mediumTickStopX = centerX+radius-mediumTickOffset-mediumTickW;

            float smallTickW = radius*0.0539f;
            float smallTickOffset = radius*0.175f;
            float smallTickStartX = centerX+radius-smallTickOffset;
            float smallTickStopX = centerX+radius-smallTickOffset-smallTickW;

            if (i % 4 == 0) {
                // Draw a division tick
                canvas.drawLine(largeTickStartX, centerY, largeTickStopX, centerY, mScalePaint);
                if (i % textModulo == 0) {
                    drawScaleText(canvas,i,divisionRotation,largeTickStopX,centerY);
                }
            } else if (i % 2 == 0) {
                canvas.drawLine(mediumTickStartX, centerY, mediumTickStopX, centerY, mScalePaint);
                if (i % textModulo == 0) {
                    drawScaleText(canvas,i,divisionRotation,largeTickStopX,centerY);
                }
            } else {
                canvas.drawLine(smallTickStartX, centerY, smallTickStopX, centerY, mScalePaint);
                if (i % textModulo == 0) {
                    drawScaleText(canvas,i,divisionRotation,largeTickStopX,centerY);
                }
            }


            canvas.rotate(divisionRotation, centerX, centerY);
        }

        canvas.restore();
//        if (mMaxSpeed > 28) canvas.rotate(divisionRotation/3, centerX, centerY);
//        else canvas.rotate(divisionRotation, centerX, centerY);

    }

    private void drawScaleText(Canvas canvas, int position, float divisionRotation,
                               float pX, float pY) {
        canvas.save();
        float positionX = pX - 70.0f;
        float positionY = pY;
        if (position == 0 ){
            positionX = pX - 40.0f;
            positionY = pY - 40.0f;
        }
        if (position == totalTicks-1){
            positionX = pX - 50.0f;
            positionY = pY + 50.0f;
        }
        canvas.rotate(-divisionRotation*position-SCALE_START_ANGLE, positionX, positionY);
        canvas.drawText(String.format("%d", (int) convertAngleToSpeed(divisionRotation * position)),
                positionX, positionY, mScaleTextPaint);
        canvas.restore();
    }

    private void drawCurrentSpeedText(Canvas canvas){
        canvas.save();
        canvas.rotate(45.0f, centerX, centerY);
        canvas.rotate(180.0f, centerX, centerY-radius*0.7f);
        canvas.drawText(String.format("%d", (int) mCurrentSpeed),
                centerX, centerY-radius*0.7f, mScaleTextPaint);
        canvas.restore();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int chosenDim= chooseDimension(widthMode, widthSize);
        int chosenHeight = chooseDimension(heightMode, heightSize);

        centerX = chosenDim / 2;
        centerY = chosenDim / 2;
        setMeasuredDimension(chosenDim, chosenDim);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        if (width > height){
            radius = height/2;
        }else{
            radius = width/2;
        }
    }

    private int chooseDimension(int mode, int size) {
        if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.EXACTLY) {
            return size;
        } else { // (mode == MeasureSpec.UNSPECIFIED)
            View parent = (View)this.getParent();
            return parent.getWidth();
        }
    }

    private float convertSpeedToAngle(float speed){
        if (speed >= mMaxSpeed) return SCALE_RANGE;
        else if (speed <= mMinSpeed) return 0;
        else return SCALE_RANGE/mMaxSpeed*speed;
    }

    private float convertAngleToSpeed(float angle){
        if (angle >= SCALE_RANGE) return mMaxSpeed;
        else if (angle <= 0) return 0;
        else return Math.round(mMaxSpeed/SCALE_RANGE*angle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        //long elapsedTime = System.currentTimeMillis() - startTime;

        //matrix.postRotate(1.0f, 130.0f, 50.0f); // rotate 10 degree every second
        //canvas.concat(matrix);

        drawScaleBackground(canvas);
        canvas.rotate(SCALE_START_ANGLE, centerX,centerY);
        drawScale(canvas);
        drawCurrentSpeedText(canvas);
        drawNeedle(canvas);
        drawNeedleScrew(canvas);

        //canvas.drawCircle(130.0f, 50.0f, 16.0f, needleScrewPaint);

//        if(elapsedTime < animationDuration){
//            this.postInvalidateDelayed(10000 / framePerSeconds);
//        }

        //this.postInvalidateOnAnimation();
        //invalidate();
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        switch(action) {
            case (MotionEvent.ACTION_DOWN) :
                boolean isInCircle = (event.getX() - (radius)) * (event.getX() - (radius))
                        + (event.getY() - (radius)) * (event.getY() - (radius)) <= radius * radius;
                if (isInCircle) {
                    float angle = (float) (Math.toDegrees(
                            Math.atan2(event.getX() - centerX, centerY - event.getY())) + 360.0f)
                            % 360.0f - 225.0f;
                    if (angle < 0) angle = angle + 225.0f + 135.0f;
                    if (angle > 315.0f) angle = -angle;
                    setCurrentSpeed(convertAngleToSpeed(angle));
                }
                return true;
            case (MotionEvent.ACTION_MOVE) :
                //TODO
                return true;
            case (MotionEvent.ACTION_UP) :
                //TODO
                return true;
            case (MotionEvent.ACTION_CANCEL) :
                //TODO
                return true;
            case (MotionEvent.ACTION_OUTSIDE) :
                //TODO
                return true;
            default :
                return super.onTouchEvent(event);
        }
    }

    public void setSpeedChangeListener(SpeedChangeListener listener){
        this.speedListener = listener;
    }

    public void setCurrentSpeed(float currentSpeed) {
        this.mCurrentSpeed = currentSpeed;
        if (speedListener != null) speedListener.onSpeedChanged(currentSpeed);
        invalidate();
    }
}

