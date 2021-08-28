package com.khammami.ecos.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2
import kotlin.math.roundToLong

class Speedometer : View {
    private var backgroundPaint: Paint? = null
    private var backgroundPath: Path? = null
    private var linePaint: Paint? = null
    private var linePath: Path? = null
    private var needleScrewPaint: Paint? = null
    private var needleScrewPath: Path? = null
    private var mScalePaint: Paint? = null
    private var mScaleTextPaint: Paint? = null
    private var mSpeedTextPaint: Paint? = null
    private var mMatrix: Matrix? = null
    private val framePerSeconds = 100
    private val animationDuration: Long = 10000
    private val startTime: Long
    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f
    private var mMaxStepSpeed = 127.0f
    private val mMinSpeed = 0.0f
    private var mCurrentSpeed = 0f
    private var totalTicks = 0
    private var speedListener: SpeedChangeListener? = null

    interface SpeedChangeListener {
        fun onSpeedChanged(newSpeedValue: Float)
    }

    /**
     *
     * @param context
     */
    constructor(context: Context?) : super(context) {
        mMatrix = Matrix()
        startTime = System.currentTimeMillis()
        //this.postInvalidate();
        init()
    }

    /**
     *
     * @param context
     * @param attrs
     */
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        mMatrix = Matrix()
        startTime = System.currentTimeMillis()
        //this.postInvalidate();
        init()
    }

    /**
     *
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        mMatrix = Matrix()
        startTime = System.currentTimeMillis()
        //this.postInvalidate();
        init()
    }

    /**
     *
     */
    private fun init() {

        //Background
        backgroundPaint = Paint()
        backgroundPaint!!.color = Color.BLACK // Set the color
        backgroundPaint!!.style = Paint.Style.FILL // set the border and fills the inside of needle
        backgroundPaint!!.isAntiAlias = true
        backgroundPath = Path()

        //Speedometer
        linePaint = Paint()
        linePaint!!.color = Color.parseColor("#FFD305") // Set the color
        linePaint!!.style =
            Paint.Style.FILL_AND_STROKE // set the border and fills the inside of needle
        linePaint!!.isAntiAlias = true
        linePaint!!.strokeWidth = 1.0f // width of the border
        //linePaint.setShadowLayer(8.0f, 0.1f, 0.1f, Color.GRAY); // Shadow of the needle
        linePath = Path()

        //Speedometer Screw
        needleScrewPaint = Paint()
        needleScrewPaint!!.color = Color.GRAY
        needleScrewPaint!!.style = Paint.Style.FILL_AND_STROKE
        needleScrewPaint!!.isAntiAlias = true
        needleScrewPath = Path()

        //Scale
        mScalePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mScalePaint!!.color = Color.WHITE
        mScalePaint!!.style = Paint.Style.STROKE
        mScalePaint!!.textSize = 0.05f
        mScalePaint!!.typeface = Typeface.SANS_SERIF
        mScalePaint!!.textAlign = Paint.Align.CENTER
        mScalePaint!!.strokeWidth = 10.0f

        //Scale Text
        mScaleTextPaint = Paint(Paint.LINEAR_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG)
        mScaleTextPaint!!.color = Color.WHITE
        mScaleTextPaint!!.style = Paint.Style.FILL_AND_STROKE
        mScaleTextPaint!!.textSize = 0.05f
        mScaleTextPaint!!.typeface = Typeface.MONOSPACE
        mScaleTextPaint!!.textAlign = Paint.Align.CENTER
        mScaleTextPaint!!.textSize = 45.0f
        mScaleTextPaint!!.strokeWidth = 3.0f

        //Current speed text
        mSpeedTextPaint = Paint(Paint.LINEAR_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG)
        mSpeedTextPaint!!.color = Color.WHITE
        mSpeedTextPaint!!.style = Paint.Style.FILL_AND_STROKE
        mSpeedTextPaint!!.textSize = 0.05f
        mSpeedTextPaint!!.typeface = Typeface.MONOSPACE
        mSpeedTextPaint!!.textAlign = Paint.Align.RIGHT
        mSpeedTextPaint!!.textSize = 45.0f
        mSpeedTextPaint!!.strokeWidth = 3.0f
    }

    /**
     *
     * @param canvas
     */
    private fun drawNeedleScrew(canvas: Canvas) {
        needleScrewPath!!.reset()
        needleScrewPath!!.moveTo(centerX, centerY)
        //needleScrewPath.lineTo(230.0f, 50.0f);
        //needleScrewPath.lineTo(50.0f, 50.0f);
        needleScrewPath!!.addCircle(centerX, centerY, radius * 0.136f, Path.Direction.CW)
        needleScrewPath!!.close()
        canvas.drawPath(needleScrewPath!!, needleScrewPaint!!)
    }

    /**
     *
     * @param canvas
     */
    private fun drawNeedle(canvas: Canvas) {
        linePath!!.reset()
        linePath!!.moveTo(centerX + 5.0f, centerY + 2.0f)
        linePath!!.lineTo(centerX + radius * 0.4864f, centerY + 2.0f)
        linePath!!.lineTo(centerX + radius * 0.4864f, centerY - 2.0f)
        linePath!!.lineTo(centerX + 5.0f, centerY - 2.0f)
        linePath!!.lineTo(centerX, centerY - radius * 0.0497f)
        linePath!!.lineTo(centerX + radius * 0.4864f, centerY - radius * 0.047f)
        linePath!!.lineTo(centerX + radius * 0.6793f, centerY)
        linePath!!.lineTo(centerX + radius * 0.4864f, centerY + radius * 0.047f)
        linePath!!.lineTo(centerX, centerY + radius * 0.0497f)
        linePath!!.lineTo(centerX + 5.0f, centerY + 2.0f)
        //linePath.addCircle(50.0f, 50.0f, 30.0f, Path.Direction.CW);
        linePath!!.close()
        canvas.save()
        val mCurrentAngle = convertSpeedToAngle(mCurrentSpeed)
        canvas.rotate(mCurrentAngle, centerX, centerY)
        canvas.drawPath(linePath!!, linePaint!!)
        canvas.restore()
    }

    /**
     *
     * @param canvas
     */
    private fun drawScaleBackground(canvas: Canvas) {
        backgroundPath!!.reset()
        backgroundPath!!.addCircle(centerX, centerY, radius, Path.Direction.CW)
        val onBackgroundPaint = Paint()
        onBackgroundPaint.color = Color.GRAY // Set the color
        onBackgroundPaint.style =
            Paint.Style.STROKE // set the border and fills the inside of needle
        onBackgroundPaint.isAntiAlias = true
        val strokeWidth = radius * 0.1078f
        onBackgroundPaint.strokeWidth = strokeWidth
        val onBackgroundPath = Path()
        onBackgroundPath.addCircle(
            centerX,
            centerY,
            radius - strokeWidth / 2 - 3,
            Path.Direction.CW
        )
        canvas.drawPath(backgroundPath!!, backgroundPaint!!)
        canvas.drawPath(onBackgroundPath, onBackgroundPaint)
    }

    /**
     *
     * @param canvas
     */
    private fun drawScale(canvas: Canvas) {
        canvas.save()
        val textModulo: Int
        val divisionRotation: Float
        //TODO Max Speed as an input
        if (mMaxStepSpeed > 31) {
            totalTicks = 25
            divisionRotation = SCALE_RANGE / (totalTicks - 1)
            textModulo = 8
            //canvas.rotate(divisionRotation/3, centerX, centerY);
        } else {
            totalTicks = mMaxStepSpeed.toInt() + 1
            //while ((totalTicks-1) % 4 != 0) totalTicks = totalTicks+1;
            divisionRotation = SCALE_RANGE / (totalTicks - 1)
            textModulo = if (mMaxStepSpeed % 4 != 0f) mMaxStepSpeed.toInt() / 3 else mMaxStepSpeed.toInt() / 2
            //canvas.rotate(divisionRotation, centerX, centerY);
        }
        for (i in 0 until totalTicks) {
            val largeTickW = radius * 0.1078f
            val largeTickOffset = radius * 0.1349f
            val largeTickStartX = centerX + radius - largeTickOffset
            val largeTickStopX = centerX + radius - largeTickOffset - largeTickW
            val mediumTickW = radius * 0.077f
            val mediumTickOffset = radius * 0.1535f
            val mediumTickStartX = centerX + radius - mediumTickOffset
            val mediumTickStopX = centerX + radius - mediumTickOffset - mediumTickW
            val smallTickW = radius * 0.0539f
            val smallTickOffset = radius * 0.175f
            val smallTickStartX = centerX + radius - smallTickOffset
            val smallTickStopX = centerX + radius - smallTickOffset - smallTickW
            if (i % 4 == 0) {
                // Draw a division tick
                canvas.drawLine(largeTickStartX, centerY, largeTickStopX, centerY, mScalePaint!!)
                if (i % textModulo == 0) {
                    drawScaleText(canvas, i, divisionRotation, largeTickStopX, centerY)
                }
            } else if (i % 2 == 0) {
                canvas.drawLine(mediumTickStartX, centerY, mediumTickStopX, centerY, mScalePaint!!)
                if (i % textModulo == 0) {
                    drawScaleText(canvas, i, divisionRotation, largeTickStopX, centerY)
                }
            } else {
                canvas.drawLine(smallTickStartX, centerY, smallTickStopX, centerY, mScalePaint!!)
                if (i % textModulo == 0) {
                    drawScaleText(canvas, i, divisionRotation, largeTickStopX, centerY)
                }
            }
            canvas.rotate(divisionRotation, centerX, centerY)
        }
        canvas.restore()
        //        if (mMaxStepSpeed > 28) canvas.rotate(divisionRotation/3, centerX, centerY);
//        else canvas.rotate(divisionRotation, centerX, centerY);
    }

    /**
     *
     * @param canvas
     * @param position
     * @param divisionRotation
     * @param pX
     * @param pY
     */
    private fun drawScaleText(
        canvas: Canvas, position: Int, divisionRotation: Float,
        pX: Float, pY: Float
    ) {
        canvas.save()
        var positionX = pX - 70.0f
        var positionY = pY
        if (position == 0) {
            positionX = pX - 40.0f
            positionY = pY - 40.0f
        }
        if (position == totalTicks - 1) {
            positionX = pX - 50.0f
            positionY = pY + 50.0f
        }
        canvas.rotate(-divisionRotation * position - SCALE_START_ANGLE, positionX, positionY)
        canvas.drawText(
            String.format("%d", convertAngleToSpeed(divisionRotation * position).toInt()),
            positionX, positionY, mScaleTextPaint!!
        )
        canvas.restore()
    }

    /**
     *
     * @param canvas
     */
    private fun drawCurrentSpeedText(canvas: Canvas) {
        canvas.save()
        canvas.rotate(45.0f, centerX, centerY)
        canvas.rotate(180.0f, centerX, centerY - radius * 0.7f)
        canvas.drawText(
            String.format("%d", mCurrentSpeed.toInt()),
            centerX + radius * 0.136f, centerY - radius * 0.65f, mScaleTextPaint!!
        )
        canvas.restore()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        //val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        //val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val chosenDim = chooseDimension(widthMode, widthSize)
        //val chosenHeight = chooseDimension(heightMode, heightSize)
        centerX = (chosenDim / 2).toFloat()
        centerY = (chosenDim / 2).toFloat()
        setMeasuredDimension(chosenDim, chosenDim)
    }

    override fun onSizeChanged(width: Int, height: Int, oldw: Int, oldh: Int) {
        radius = if (width > height) {
            (height / 2).toFloat()
        } else {
            (width / 2).toFloat()
        }
    }

    /**
     *
     * @param mode
     * @param size
     * @return
     */
    private fun chooseDimension(mode: Int, size: Int): Int {
        return if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.EXACTLY) {
            size
        } else { // (mode == MeasureSpec.UNSPECIFIED)
            val parent = this.parent as View
            parent.width
        }
    }

    /**
     *
     * @param speed
     * @return
     */
    private fun convertSpeedToAngle(speed: Float): Float {
        return when {
            speed >= mMaxStepSpeed -> SCALE_RANGE
            speed <= mMinSpeed -> 0f
            else -> SCALE_RANGE / mMaxStepSpeed * speed
        }
    }

    /**
     *
     * @param angle
     * @return
     */
    private fun convertAngleToSpeed(angle: Float): Float {
        return when {
            angle >= SCALE_RANGE -> mMaxStepSpeed
            angle <= 0 -> 0f
            else -> (mMaxStepSpeed / SCALE_RANGE * angle).roundToLong().toFloat()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)


        //long elapsedTime = System.currentTimeMillis() - startTime;

        //matrix.postRotate(1.0f, 130.0f, 50.0f); // rotate 10 degree every second
        //canvas.concat(matrix);
        drawScaleBackground(canvas)
        canvas.rotate(SCALE_START_ANGLE, centerX, centerY)
        drawScale(canvas)
        drawCurrentSpeedText(canvas)
        drawNeedle(canvas)
        drawNeedleScrew(canvas)

        //canvas.drawCircle(130.0f, 50.0f, 16.0f, needleScrewPaint);

//        if(elapsedTime < animationDuration){
//            this.postInvalidateDelayed(10000 / framePerSeconds);
//        }

        //this.postInvalidateOnAnimation();
        //invalidate();
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked
        val isInCircle: Boolean = (event.x - radius) * (event.x - radius) + (event.y - radius) * (event.y - radius) <= radius * radius
        return when (action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                if (isInCircle) {
                    var angle = (Math.toDegrees(
                        atan2(
                            (event.x - centerX).toDouble(),
                            (centerY - event.y).toDouble()
                        )
                    ) + 360.0f).toFloat() % 360.0f - 225.0f
                    if (angle < 0) angle += 225.0f + 135.0f
                    if (angle > 315.0f) angle = -angle
                    setCurrentSpeed(convertAngleToSpeed(angle))
                }
                true
            }
            MotionEvent.ACTION_UP -> {
                performClick()
                true
            }
            MotionEvent.ACTION_CANCEL ->                 //TODO
                true
            MotionEvent.ACTION_OUTSIDE ->                 //TODO
                true
            else -> super.onTouchEvent(event)
        }
    }

    /**
     *
     * @param listener
     */
    fun setSpeedChangeListener(listener: SpeedChangeListener?) {
        this.speedListener = listener
    }

    /**
     *
     * @param currentSpeed
     */
    fun setCurrentSpeed(currentSpeed: Float) {
        this.mCurrentSpeed = currentSpeed
        if (this.speedListener != null) this.speedListener!!.onSpeedChanged(currentSpeed)
        invalidate()
    }

    /**
     *
     * @param maxSpeed
     */
    fun setMaxSpeed(maxSpeed: Float){
        this.mMaxStepSpeed = maxSpeed
    }

    companion object {
        private val TAG = Speedometer::class.java.simpleName
        private const val SCALE_RANGE = 270.0f
        private const val SCALE_START_ANGLE = 135.0f
    }
}